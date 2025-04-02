package kr.masul.wizard;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.masul.artifact.Artifact;
import kr.masul.system.StatusCode;
import kr.masul.system.exception.ObjectNotFoundException;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("dev")
class WizardControllerTest {

   @MockitoBean
   WizardService wizardService;

   @Autowired
   MockMvc mockMvc;

   @Autowired
   ObjectMapper objectMapper;

   @Value("${api.base-url}")
   String url;

   List<Wizard> wizards;

   @BeforeEach
   void setUp() {
      wizards = new ArrayList<>();
      Artifact a1 = new Artifact();
      a1.setId("91");
      a1.setName("Deluminator");
      a1.setDescription("A Deluminator is a device invented by Albus Dumbledore that resembles a cigarette lighter....");
      a1.setImageUrl("ImageUrl");

      Artifact a2 = new Artifact();
      a2.setId("92");
      a2.setName("Invisibility Cloak");
      a2.setDescription("An invisibility cloak invisible.");
      a2.setImageUrl("ImageUrl");

      Artifact a3 = new Artifact();
      a3.setId("93");
      a3.setName("Elder Wand");
      a3.setDescription("The Elder Wand, known ...");
      a3.setImageUrl("ImageUrl");

      Artifact a4 = new Artifact();
      a4.setId("94");
      a4.setName("The Marauder's Map");
      a4.setDescription("A magical map of Hogwarts , ....");
      a4.setImageUrl("ImageUrl");

      Artifact a5 = new Artifact();
      a5.setId("95");
      a5.setName("The Sword Of Gryffindor");
      a5.setDescription("A goblin-made sword adorned ....");
      a5.setImageUrl("ImageUrl");

      Artifact a6 = new Artifact();
      a6.setId("96");
      a6.setName("Resurrection Stone");
      a6.setDescription("The Resurrection Stone, ....");
      a6.setImageUrl("ImageUrl");

      Wizard w1 = new Wizard();
      w1.setId(1);
      w1.setName("Albus Dumbledore");
      w1.addArtifact(a1);
      w1.addArtifact(a3);
      this.wizards.add(w1);

      Wizard w2 = new Wizard();
      w2.setId(2);
      w2.setName("Harry Potter");
      w2.addArtifact(a2);
      w2.addArtifact(a4);
      this.wizards.add(w2);

      Wizard w3 = new Wizard();
      w3.setId(3);
      w3.setName("Neville Longbottom");
      w3.addArtifact(a5);
      this.wizards.add(w3);
   }

   @Test
   void testFindByIdSuccess() throws Exception {
      // Given
      Wizard w = new Wizard();
      w.setId(2);
      w.setName("SuperMan");
      w.setBirthday(LocalDateTime.of(1234,2,2, 2,2, 2));

      given(wizardService.findById(2)).willReturn(w);
      // When and Then
      mockMvc.perform(get(url+"/wizards/2").accept(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath("$.flag").value(true))
              .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
              .andExpect(jsonPath("$.message").value("Find Success"))
              .andExpect(jsonPath("$.data.id").value(2))
              .andExpect(jsonPath("$.data.name").value("SuperMan"))
              .andExpect(jsonPath("$.data.birthday").value("1234-02-02T02:02:02"));
      verify(wizardService, times(1)).findById(2);
   }

   @Test
   void testFindByIdNotFound() throws Exception {
      // Given
      given(wizardService.findById(2)).willThrow(new ObjectNotFoundException("wizard", 2));
      // When and Then
      mockMvc.perform(get(url+"/wizards/2").accept(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath("$.flag").value(false))
              .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
              .andExpect(jsonPath("$.message").value("Could not find wizard with id 2"))
              .andExpect(jsonPath("$.data").isEmpty());
      verify(wizardService, times(1)).findById(2);
   }

   @Test
   void testFindAllSuccess() throws Exception {
      // Given
      given(wizardService.findAll()).willReturn(wizards);
      // When and Then
      mockMvc.perform(get(url + "/wizards").accept(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath("$.flag").value(true))
              .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
              .andExpect(jsonPath("$.message").value("Find all Success"))
              .andExpect(jsonPath("$.data", Matchers.hasSize(3)));
      verify(wizardService, times(1)).findAll();
   }

   @Test
   void testAddSuccess() throws Exception {
      // Given
      Wizard w = new Wizard();
      w.setId(6);
      w.setName("SuperMan-new");
      w.setBirthday(LocalDateTime.of(1234,2,2, 2,2, 2));

      WizardDto dto = new WizardDto(6, "SuperMan-new",
              LocalDateTime.of(1234, 2, 2, 2, 2, 2), 1);

      String json = objectMapper.writeValueAsString(dto);

      given(wizardService.add(Mockito.any(Wizard.class))).willReturn(w);

      // When and Then
      mockMvc.perform(post(url+"/wizards").accept(MediaType.APPLICATION_JSON)
                      .contentType(MediaType.APPLICATION_JSON).content(json))
              .andExpect(jsonPath("$.flag").value(true))
              .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
              .andExpect(jsonPath("$.message").value("Add Success"))
              .andExpect(jsonPath("$.data.id").value(6))
              .andExpect(jsonPath("$.data.name").value("SuperMan-new"))
              .andExpect(jsonPath("$.data.birthday").value("1234-02-02T02:02:02"));
      verify(wizardService, times(1)).add(Mockito.any(Wizard.class));
   }

   @Test
   void testUpdateSuccess() throws Exception {
      // Given
      Wizard update = new Wizard();
      update.setId(2);
      update.setName("SuperMan-new");
      update.setBirthday(LocalDateTime.of(1999,2,2, 2,2, 2));

      WizardDto dto = new WizardDto(2, "SuperMan-new",
              LocalDateTime.of(1999, 2, 2, 2, 2, 2), 1);

      String json = objectMapper.writeValueAsString(dto);

      given(wizardService.update(eq(2), Mockito.any(Wizard.class))).willReturn(update);

      // When and Then
      mockMvc.perform(put(url+"/wizards/2").accept(MediaType.APPLICATION_JSON)
                      .contentType(MediaType.APPLICATION_JSON).content(json))
              .andExpect(jsonPath("$.flag").value(true))
              .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
              .andExpect(jsonPath("$.message").value("Update Success"))
              .andExpect(jsonPath("$.data.id").value(2))
              .andExpect(jsonPath("$.data.name").value("SuperMan-new"))
              .andExpect(jsonPath("$.data.birthday").value("1999-02-02T02:02:02"));
      verify(wizardService, times(1)).update(eq(2),Mockito.any(Wizard.class));
   }

   @Test
   void testUpdateNotFound() throws Exception {
      // Given
      WizardDto dto = new WizardDto(6, "SuperMan-new",
              LocalDateTime.of(1999, 2, 2, 2, 2, 2), 1);

      String json = objectMapper.writeValueAsString(dto);

      given(wizardService.update(eq(6), Mockito.any(Wizard.class)))
              .willThrow(new ObjectNotFoundException("wizard", 6));

      // When and Then
      mockMvc.perform(put(url+"/wizards/6").accept(MediaType.APPLICATION_JSON)
                      .contentType(MediaType.APPLICATION_JSON).content(json))
              .andExpect(jsonPath("$.flag").value(false))
              .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
              .andExpect(jsonPath("$.message").value("Could not find wizard with id 6"))
              .andExpect(jsonPath("$.data").isEmpty());
      verify(wizardService, times(1)).update(eq(6),Mockito.any(Wizard.class));
   }

   @Test
   void testDeleteSuccess() throws Exception {
      // Given
      doNothing().when(wizardService).delete(2);

      // When and Then
      mockMvc.perform(delete(url+"/wizards/2").accept(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath("$.flag").value(true))
              .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
              .andExpect(jsonPath("$.message").value("Delete Success"))
              .andExpect(jsonPath("$.data").isEmpty());
      verify(wizardService, times(1)).delete(2);
   }

   @Test
   void testDeleteNotFound() throws Exception {
      // Given
       doThrow(new ObjectNotFoundException("wizard",6)).when(wizardService).delete(6);
      // When and Then
      mockMvc.perform(delete(url+"/wizards/6").accept(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath("$.flag").value(false))
              .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
              .andExpect(jsonPath("$.message").value("Could not find wizard with id 6"))
              .andExpect(jsonPath("$.data").isEmpty());
      verify(wizardService, times(1)).delete(6);
   }

   @Test
   void testAssignArtifactSuccess() throws Exception {
      // Given
       doNothing().when(wizardService).assignArtifact(2, "12306");

      // When and Then
      mockMvc.perform(put(url+"/wizards/2/artifacts/12306").accept(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath("$.flag").value(true))
              .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
              .andExpect(jsonPath("$.message").value("Assign artifact Success"))
              .andExpect(jsonPath("$.data").isEmpty());
      verify(wizardService, times(1)).assignArtifact(2,"12306");
   }

   @Test
   void testAssignArtifactNotFoundWizard() throws Exception {
      // Given
      doThrow(new ObjectNotFoundException("wizard", 6)).when(wizardService).assignArtifact(6, "12306");

      // When and Then
      mockMvc.perform(put(url+"/wizards/6/artifacts/12306").accept(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath("$.flag").value(false))
              .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
              .andExpect(jsonPath("$.message").value("Could not find wizard with id 6"))
              .andExpect(jsonPath("$.data").isEmpty());
      verify(wizardService, times(1)).assignArtifact(6,"12306");
   }
   @Test
   void testAssignArtifactNotFoundArtifact() throws Exception {
      // Given
      doThrow(new ObjectNotFoundException("artifact", "12309"))
              .when(wizardService).assignArtifact(2, "12309");

      // When and Then
      mockMvc.perform(put(url+"/wizards/2/artifacts/12309").accept(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath("$.flag").value(false))
              .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
              .andExpect(jsonPath("$.message").value("Could not find artifact with id 12309"))
              .andExpect(jsonPath("$.data").isEmpty());
      verify(wizardService, times(1)).assignArtifact(2,"12309");
   }
}