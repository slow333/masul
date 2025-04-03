package kr.masul.wizard;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.masul.artifact.Artifact;
import kr.masul.system.StatusCode;
import kr.masul.system.exception.ObjectNotFoundException;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Wizard Integration test")
@Tag("Integration")
@ActiveProfiles(value = "dev")
class WizardControllerIntegrationTest {

   @Autowired
   MockMvc mockMvc;

   @Autowired
   ObjectMapper objectMapper;

   @Value("${api.base-url}")
   String url;

   String token;

   @BeforeEach
   void setUp() throws Exception {
      ResultActions admin = mockMvc.perform(post(url + "/users/login")
              .with(httpBasic("kim", "123")));
      MvcResult mvcResult = admin.andDo(print()).andReturn();
      String contentAsString = mvcResult.getResponse().getContentAsString();
      JSONObject jsonObject = new JSONObject(contentAsString);
      token = "Bearer " + jsonObject.getJSONObject("data").getString("token");
   }

   @Test
   void testFindByIdSuccess() throws Exception {
      mockMvc.perform(get(url+"/wizards/2").accept(MediaType.APPLICATION_JSON).header("Authorization", token))
              .andExpect(jsonPath("$.flag").value(true))
              .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
              .andExpect(jsonPath("$.message").value("Find Success"))
              .andExpect(jsonPath("$.data.id").value(2))
              .andExpect(jsonPath("$.data.name").value("WonderWoman"));
   }

   @Test
   void testFindByIdNotFound() throws Exception {
      mockMvc.perform(get(url+"/wizards/5").accept(MediaType.APPLICATION_JSON).header("Authorization", token))
              .andExpect(jsonPath("$.flag").value(false))
              .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
              .andExpect(jsonPath("$.message").value("Could not find wizard with id 5"))
              .andExpect(jsonPath("$.data").isEmpty());
   }

   @Test
   @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
   void testFindAllSuccess() throws Exception {
      mockMvc.perform(get(url + "/wizards").accept(MediaType.APPLICATION_JSON).header("Authorization", token))
              .andExpect(jsonPath("$.flag").value(true))
              .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
              .andExpect(jsonPath("$.message").value("Find all Success"))
              .andExpect(jsonPath("$.data", Matchers.hasSize(3)));
   }

   @Test
   void testAddSuccess() throws Exception {
      // Given
      WizardDto dto = new WizardDto(null, "SuperMan-new",
              LocalDateTime.of(1234, 2, 2, 2, 2, 2), 1);

      String json = objectMapper.writeValueAsString(dto);

      // When and Then
      mockMvc.perform(post(url+"/wizards").accept(MediaType.APPLICATION_JSON).header("Authorization", token)
                      .contentType(MediaType.APPLICATION_JSON).content(json))
              .andExpect(jsonPath("$.flag").value(true))
              .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
              .andExpect(jsonPath("$.message").value("Add Success"))
              .andExpect(jsonPath("$.data.id").isNotEmpty())
              .andExpect(jsonPath("$.data.name").value("SuperMan-new"))
              .andExpect(jsonPath("$.data.birthday").value("1234-02-02T02:02:02"));
   }

   @Test
   @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
   void testUpdateSuccess() throws Exception {
      // Given
      WizardDto dto = new WizardDto(2, "SuperMan-new",
              LocalDateTime.of(1999, 2, 2, 2, 2, 2), 1);

      String json = objectMapper.writeValueAsString(dto);

      // When and Then
      mockMvc.perform(put(url+"/wizards/2").accept(MediaType.APPLICATION_JSON).header("Authorization", token)
                      .contentType(MediaType.APPLICATION_JSON).content(json))
              .andExpect(jsonPath("$.flag").value(true))
              .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
              .andExpect(jsonPath("$.message").value("Update Success"))
              .andExpect(jsonPath("$.data.id").value(2))
              .andExpect(jsonPath("$.data.name").value("SuperMan-new"))
              .andExpect(jsonPath("$.data.birthday").value("1999-02-02T02:02:02"));
   }

   @Test
   void testUpdateNotFound() throws Exception {
      // Given
      WizardDto dto = new WizardDto(6, "SuperMan-new",
              LocalDateTime.of(1999, 2, 2, 2, 2, 2), 1);

      String json = objectMapper.writeValueAsString(dto);
      // When and Then
      mockMvc.perform(put(url+"/wizards/6").accept(MediaType.APPLICATION_JSON).header("Authorization", token)
                      .contentType(MediaType.APPLICATION_JSON).content(json))
              .andExpect(jsonPath("$.flag").value(false))
              .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
              .andExpect(jsonPath("$.message").value("Could not find wizard with id 6"))
              .andExpect(jsonPath("$.data").isEmpty());
   }

   @Test
   void testDeleteSuccess() throws Exception {
      mockMvc.perform(delete(url+"/wizards/2").accept(MediaType.APPLICATION_JSON).header("Authorization", token))
              .andExpect(jsonPath("$.flag").value(true))
              .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
              .andExpect(jsonPath("$.message").value("Delete Success"))
              .andExpect(jsonPath("$.data").isEmpty());
   }

   @Test
   void testDeleteNotFound() throws Exception {
      mockMvc.perform(delete(url+"/wizards/6").accept(MediaType.APPLICATION_JSON).header("Authorization", token))
              .andExpect(jsonPath("$.flag").value(false))
              .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
              .andExpect(jsonPath("$.message").value("Could not find wizard with id 6"))
              .andExpect(jsonPath("$.data").isEmpty());
   }

   @Test
   @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
   void testAssignArtifactSuccess() throws Exception {
      // When and Then
      mockMvc.perform(put(url+"/wizards/2/artifacts/12306").accept(MediaType.APPLICATION_JSON).header("Authorization", token))
              .andExpect(jsonPath("$.flag").value(true))
              .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
              .andExpect(jsonPath("$.message").value("Assign artifact Success"))
              .andExpect(jsonPath("$.data").isEmpty());
   }

   @Test
   void testAssignArtifactNotFoundWizard() throws Exception {
      mockMvc.perform(put(url+"/wizards/6/artifacts/12306").accept(MediaType.APPLICATION_JSON).header("Authorization", token))
              .andExpect(jsonPath("$.flag").value(false))
              .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
              .andExpect(jsonPath("$.message").value("Could not find wizard with id 6"))
              .andExpect(jsonPath("$.data").isEmpty());
   }
   @Test
   void testAssignArtifactNotFoundArtifact() throws Exception {
      mockMvc.perform(put(url+"/wizards/2/artifacts/12309").accept(MediaType.APPLICATION_JSON).header("Authorization", token))
              .andExpect(jsonPath("$.flag").value(false))
              .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
              .andExpect(jsonPath("$.message").value("Could not find artifact with id 12309"))
              .andExpect(jsonPath("$.data").isEmpty());
   }
}