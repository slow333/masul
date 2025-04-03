package kr.masul.artifact;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles(value = "dev")
class ArtifactControllerTest {

   @MockitoBean
   private ArtifactService artifactService;

   @Autowired
   MockMvc mockMvc;

   @Autowired
   ObjectMapper objectMapper;

   @Value("${api.base-url}")
   String url;

   List<Artifact> artifactList;

   @BeforeEach
   void setUp() {
      artifactList = new ArrayList<>();
      Artifact a1 = new Artifact();
      a1.setId("12301");
      a1.setName("First Artifact");
      a1.setDescription("First Artifact hide");
      a1.setImageUrl("image");
      artifactList.add(a1);

      Artifact a2 = new Artifact();
      a2.setId("12302");
      a2.setName("Second Artifact");
      a2.setDescription("Second Artifact get small");
      a2.setImageUrl("image");
      artifactList.add(a2);

      Artifact a3 = new Artifact();
      a3.setId("12303");
      a3.setName("Third Artifact");
      a3.setDescription("Third Artifact get large");
      a3.setImageUrl("image");

      Artifact a4 = new Artifact();
      a4.setId("12304");
      a4.setName("Fourth Artifact");
      a4.setDescription("Fourth Artifact fly");
      a4.setImageUrl("image");

      Artifact a5 = new Artifact();
      a5.setId("12305");
      a5.setName("Fifth Artifact");
      a5.setDescription("Fifth Artifact money");
      a5.setImageUrl("image");

      Artifact a6 = new Artifact();
      a6.setId("12306");
      a6.setName("Sixth Artifact");
      a6.setDescription("Sixth Artifact brain");
      a6.setImageUrl("image");


      artifactList.add(a3);
      artifactList.add(a4);
      artifactList.add(a5);
      artifactList.add(a6);
   }

   @Test
   void testFindByIdSuccess() throws Exception {
      // Given
       given(artifactService.findById("12303")).willReturn(artifactList.get(2));
      // When and then
      mockMvc.perform(get(url+"/artifacts/12303")
                      .accept(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath("$.flag").value(true))
              .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
              .andExpect(jsonPath("$.message").value("Find Success"))
              .andExpect(jsonPath("$.data.id").value("12303"));
      verify(artifactService, times(1)).findById("12303");
   }

   @Test
   void testFindByIdNotFound() throws Exception {
      // Given
      given(artifactService.findById("12303")).willThrow(new ObjectNotFoundException("artifact", "12303"));
      // When and then
      mockMvc.perform(get(url+"/artifacts/12303")
                      .accept(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath("$.flag").value(false))
              .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
              .andExpect(jsonPath("$.message").value("Could not find artifact with id 12303"))
              .andExpect(jsonPath("$.data").isEmpty());
      verify(artifactService, times(1)).findById("12303");
   }

   @Test
   void testFindAll() throws Exception {
      // Given
      given(artifactService.findAll()).willReturn(artifactList);
      // When and Then
      mockMvc.perform(get(url + "/artifacts").accept(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath("$.flag").value(true))
              .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
              .andExpect(jsonPath("$.message").value("Find all Success"))
              .andExpect(jsonPath("$.data", Matchers.hasSize(6)));
      verify(artifactService, times(1)).findAll();
   }

   @Test
   void testAddSuccess() throws Exception {
      // Given
      Artifact newArtifact = new Artifact();
      newArtifact.setId("12309");
      newArtifact.setName("new Art");
      newArtifact.setDescription("added Art desc.");
      newArtifact.setImageUrl("image");
      newArtifact.setCreateAt(LocalDateTime.of(1234,2,2,2,2,2));
      newArtifact.setOwner(null);

      ArtifactDto newArtifactDto = new ArtifactDto(
              "12309", "new Art", "added Art desc.", "image",
              LocalDateTime.of(1234,2,2,2,2,2), null);

      String json = objectMapper.writeValueAsString(newArtifactDto);

      given(artifactService.add(Mockito.any(Artifact.class))).willReturn(newArtifact);
      // When and Then
      mockMvc.perform(post(url + "/artifacts").accept(MediaType.APPLICATION_JSON)
              .contentType(MediaType.APPLICATION_JSON).content(json))
              .andExpect(jsonPath("$.flag").value(true))
              .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
              .andExpect(jsonPath("$.message").value("Add Success"))
              .andExpect(jsonPath("$.data.id").value("12309"))
              .andExpect(jsonPath("$.data.name").value("new Art"));
      verify(artifactService, times(1)).add(Mockito.any(Artifact.class));
   }

   @Test
   void testUpdateSuccess() throws Exception {
      // Given
      ArtifactDto updateArtifactDto = new ArtifactDto(
              "12302", "update Art", "update Art desc.", "image",
              LocalDateTime.of(1234,2,2,2,2,2), null);

      Artifact update = new Artifact();
      update.setId("12302");
      update.setName("update Art");
      update.setDescription("update Art desc.");
      update.setImageUrl("image");
      update.setCreateAt(LocalDateTime.of(1234,2,2,2,2,2));

      update.setOwner(null);

      String json = objectMapper.writeValueAsString(updateArtifactDto);

      given(artifactService.update(eq("12302"), Mockito.any(Artifact.class))).willReturn(update);
      // When and Then
      mockMvc.perform(put(url + "/artifacts/12302").accept(MediaType.APPLICATION_JSON)
                      .contentType(MediaType.APPLICATION_JSON).content(json))
              .andExpect(jsonPath("$.flag").value(true))
              .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
              .andExpect(jsonPath("$.message").value("Update Success"))
              .andExpect(jsonPath("$.data.id").value("12302"))
              .andExpect(jsonPath("$.data.name").value("update Art"))
              .andExpect(jsonPath("$.data.description").value("update Art desc."));
   }

   @Test
   void testUpdateNotFound() throws Exception {
      // Given
      ArtifactDto updateArtifactDto = new ArtifactDto(
              "12302", "update Art", "update Art desc.", "image",
              LocalDateTime.of(1234,2,2,2,2,2),null);

      Artifact update = new Artifact();
      update.setId("12302");
      update.setName("update Art");
      update.setDescription("update Art desc.");
      update.setImageUrl("image");
      update.setOwner(null);

      String json = objectMapper.writeValueAsString(updateArtifactDto);

      given(artifactService.update(eq("12302"), Mockito.any(Artifact.class)))
              .willThrow(new ObjectNotFoundException("artifact", "12302"));
      // When and Then
      mockMvc.perform(put(url + "/artifacts/12302").accept(MediaType.APPLICATION_JSON)
                      .contentType(MediaType.APPLICATION_JSON).content(json))
              .andExpect(jsonPath("$.flag").value(false))
              .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
              .andExpect(jsonPath("$.message").value("Could not find artifact with id 12302"))
              .andExpect(jsonPath("$.data").isEmpty());
   }

   @Test
   void testDeleteSuccess() throws Exception {
      // Given
      doNothing().when(artifactService).delete("12302");
      // When and Then
      mockMvc.perform(delete(url+"/artifacts/12302").accept(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath("$.flag").value(true))
              .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
              .andExpect(jsonPath("$.message").value("Delete Success"))
              .andExpect(jsonPath("$.data").isEmpty());

   }
   @Test
   void testDeleteNotFound() throws Exception {
      // Given
      doThrow(new ObjectNotFoundException("artifact","12302")).when(artifactService).delete("12302");
      // When and Then
      mockMvc.perform(delete(url+"/artifacts/12302").accept(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath("$.flag").value(false))
              .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
              .andExpect(jsonPath("$.message").value("Could not find artifact with id 12302"))
              .andExpect(jsonPath("$.data").isEmpty());

   }
}