package kr.masul.artifact;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.masul.system.StatusCode;
import kr.masul.system.exception.ObjectNotFoundException;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Artifact Integration test")
class ArtifactControllerIntegrationTest {

   @Autowired
   MockMvc mockMvc;

   @Autowired
   ObjectMapper objectMapper;

   @Value("${api.base-url}")
   String url;

   @Test
   @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
   void testFindByIdSuccess() throws Exception {
      mockMvc.perform(get(url+"/artifacts/12303")
                      .accept(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath("$.flag").value(true))
              .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
              .andExpect(jsonPath("$.message").value("Find Success"))
              .andExpect(jsonPath("$.data.id").value("12303"));
   }

   @Test
   void testFindByIdNotFound() throws Exception {
      mockMvc.perform(get(url+"/artifacts/12309")
                      .accept(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath("$.flag").value(false))
              .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
              .andExpect(jsonPath("$.message").value("Could not find artifact with id 12309"))
              .andExpect(jsonPath("$.data").isEmpty());
   }

   @Test
   void testFindAll() throws Exception {
      mockMvc.perform(get(url + "/artifacts").accept(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath("$.flag").value(true))
              .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
              .andExpect(jsonPath("$.message").value("Find all Success"))
              .andExpect(jsonPath("$.data", Matchers.hasSize(8)));
   }

   @Test
   @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
   void testAddSuccess() throws Exception {
      // Given
      ArtifactDto newArtifactDto = new ArtifactDto(
              "12309", "new Art", "added Art desc.", "image",
              LocalDateTime.of(1234,2,2,2,2,2), null);

      String json = objectMapper.writeValueAsString(newArtifactDto);

      // When and Then
      mockMvc.perform(post(url + "/artifacts").accept(MediaType.APPLICATION_JSON)
              .contentType(MediaType.APPLICATION_JSON).content(json))
              .andExpect(jsonPath("$.flag").value(true))
              .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
              .andExpect(jsonPath("$.message").value("Add Success"))
              .andExpect(jsonPath("$.data.id").isNotEmpty())
              .andExpect(jsonPath("$.data.name").value("new Art"));
   }

   @Test
   void testUpdateSuccess() throws Exception {
      // Given
      ArtifactDto updateArtifactDto = new ArtifactDto(
              "12302", "update Art", "update Art desc.", "image",
              LocalDateTime.of(1234,2,2,2,2,2), null);
      String json = objectMapper.writeValueAsString(updateArtifactDto);

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
              "12309", "update Art", "update Art desc.", "image",
              LocalDateTime.of(1234,2,2,2,2,2),null);

      String json = objectMapper.writeValueAsString(updateArtifactDto);

      // When and Then
      mockMvc.perform(put(url + "/artifacts/12309").accept(MediaType.APPLICATION_JSON)
                      .contentType(MediaType.APPLICATION_JSON).content(json))
              .andExpect(jsonPath("$.flag").value(false))
              .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
              .andExpect(jsonPath("$.message").value("Could not find artifact with id 12309"))
              .andExpect(jsonPath("$.data").isEmpty());
   }

   @Test
   void testDeleteSuccess() throws Exception {
      mockMvc.perform(delete(url+"/artifacts/12302").accept(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath("$.flag").value(true))
              .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
              .andExpect(jsonPath("$.message").value("Delete Success"))
              .andExpect(jsonPath("$.data").isEmpty());

   }
   @Test
   void testDeleteNotFound() throws Exception {
      mockMvc.perform(delete(url+"/artifacts/12309").accept(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath("$.flag").value(false))
              .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
              .andExpect(jsonPath("$.message").value("Could not find artifact with id 12309"))
              .andExpect(jsonPath("$.data").isEmpty());

   }
}