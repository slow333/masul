package kr.masul.artifact;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redis.testcontainers.RedisContainer;
import kr.masul.system.StatusCode;
import kr.masul.system.exception.ObjectNotFoundException;
import netscape.javascript.JSObject;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@DisplayName("Artifact Integration test")
@ActiveProfiles(value = "dev")
class ArtifactControllerIntegrationTest {

   @Autowired
   MockMvc mockMvc;

   @Autowired
   ObjectMapper objectMapper;

   @Value("${api.base-url}")
   String url;

   // Redis docker container 실행 없이 자체 시험을 위해 필요
   @Container
   @ServiceConnection // 원격 서비스에 접근하기 위해 필요
   // (redisCacheClient에 접속해서 독커를 DockerDetails>RedisConectionDetails를 가상화함)
   static RedisContainer redisContainer = new RedisContainer(DockerImageName.parse("redis:6.2.6"));

   String token;
   @BeforeEach
   void setup() throws Exception {
      ResultActions adminResult = mockMvc.perform(post(url + "/users/login").with(httpBasic("admin","123456")));
      MvcResult mvcResult = adminResult.andDo(print()).andReturn();
      String string = mvcResult.getResponse().getContentAsString();
      JSONObject jsonObject =new JSONObject(string);
      token = "Bearer " + jsonObject.getJSONObject("data").getString("token");
   }

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
              .andExpect(jsonPath("$.data.content", Matchers.hasSize(8)));
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
             .header("Authorization", token)
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
                      .header("Authorization", token)
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
                      .header("Authorization", token)
                      .contentType(MediaType.APPLICATION_JSON).content(json))
              .andExpect(jsonPath("$.flag").value(false))
              .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
              .andExpect(jsonPath("$.message").value("Could not find artifact with id 12309"))
              .andExpect(jsonPath("$.data").isEmpty());
   }

   @Test
   void testDeleteSuccess() throws Exception {
      mockMvc.perform(delete(url+"/artifacts/12302").accept(MediaType.APPLICATION_JSON)
                      .header("Authorization", token))
              .andExpect(jsonPath("$.flag").value(true))
              .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
              .andExpect(jsonPath("$.message").value("Delete Success"))
              .andExpect(jsonPath("$.data").isEmpty());

   }
   @Test
   void testDeleteNotFound() throws Exception {
      mockMvc.perform(delete(url+"/artifacts/12309").accept(MediaType.APPLICATION_JSON)
                      .header("Authorization", token))
              .andExpect(jsonPath("$.flag").value(false))
              .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
              .andExpect(jsonPath("$.message").value("Could not find artifact with id 12309"))
              .andExpect(jsonPath("$.data").isEmpty());
   }

   @Test
   @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
   void testFindArtifactByDescription() throws Exception {
      // Given
      Map<String, String> searchCriteria = new HashMap<>();
      searchCriteria.put("description", "get");
      String json = objectMapper.writeValueAsString(searchCriteria);

      MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
      requestParams.add("page", "0");
      requestParams.add("size", "2");
      requestParams.add("sort","name,asc");

      // When and Then
      mockMvc.perform(post(url + "/artifacts/search")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(json)
                      .params(requestParams)
                      .accept(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath("$.flag").value(true))
              .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
              .andExpect(jsonPath("$.message").value("Search Success"))
              .andExpect(jsonPath("$.data.content", Matchers.hasSize(2)));
   }
   @Test
   void testFindArtifactByNameAndDescription() throws Exception {
      // Given
      Map<String, String> searchCriteria = new HashMap<>();
      searchCriteria.put("description", "get");
      searchCriteria.put("name","second");
      String json = objectMapper.writeValueAsString(searchCriteria);

      MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
      requestParams.add("page", "0");
      requestParams.add("size", "2");
      requestParams.add("sort","name,asc");

      // When and Then
      mockMvc.perform(post(url + "/artifacts/search")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(json)
                      .params(requestParams)
                      .accept(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath("$.flag").value(true))
              .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
              .andExpect(jsonPath("$.message").value("Search Success"))
              .andExpect(jsonPath("$.data.content", Matchers.hasSize(1)));
   }
   @Test
   void testFindArtifactByOwnerName() throws Exception {
      // Given
      Map<String, String> searchCriteria = new HashMap<>();
      // 실행시에 id가  변경되어서 고정된 값은 안됨
      searchCriteria.put("owner", "superMan");
      String json = objectMapper.writeValueAsString(searchCriteria);

      MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
      requestParams.add("page", "0");
      requestParams.add("size", "2");
      requestParams.add("sort","name,asc");

      // When and Then
      mockMvc.perform(post(url + "/artifacts/search")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(json)
                      .params(requestParams)
                      .accept(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath("$.flag").value(true))
              .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
              .andExpect(jsonPath("$.message").value("Search Success"))
              .andExpect(jsonPath("$.data.content", Matchers.hasSize(2)));
   }
}