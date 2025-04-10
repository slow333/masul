package kr.masul.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redis.testcontainers.RedisContainer;
import kr.masul.system.StatusCode;
import kr.masul.system.exception.ObjectNotFoundException;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@DisplayName("User Integration test")
@ActiveProfiles(value = "dev")
class UserControllerIntegrationTest {

   @Autowired
   ObjectMapper objectMapper;

   @Autowired
   MockMvc mockMvc;

   @Value("${api.base-url}")
   String url;

   // Redis docker container 실행 없이 자체 시험을 위해 필요
   @Container
   @ServiceConnection // 원격 서비스에 접근하기 위해 필요
   // (redisCacheClient에 접속해서 독커를 DockerDetails>RedisConectionDetails를 가상화함)
   static RedisContainer redisContainer = new RedisContainer(DockerImageName.parse("redis:6.2.6"));

   String token;
   @BeforeEach
   void setUp() throws Exception {
      ResultActions admin = mockMvc.perform(post(url + "/users/login").with(httpBasic("admin", "123456")));
      MvcResult mvcResult = admin.andDo(print()).andReturn();
      String contentAsString = mvcResult.getResponse().getContentAsString();
      JSONObject jsonObject = new JSONObject(contentAsString);
      token = "Bearer " + jsonObject.getJSONObject("data").getString("token");
   }

   @Test
   @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
   void testFindByIdWithAdminAccessingAllSuccess() throws Exception {
      mockMvc.perform(get(url + "/users/2").accept(MediaType.APPLICATION_JSON)
                      .header("Authorization", token))
              .andExpect(jsonPath("$.flag").value(true))
              .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
              .andExpect(jsonPath("$.message").value("Find Success"))
              .andExpect(jsonPath("$.data.id").value(2))
              .andExpect(jsonPath("$.data.username").value("kim"))
              .andExpect(jsonPath("$.data.roles").value("user"));
   }

   @Test
   @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
   void testFindByIdWithUserAccessingOwnInfo() throws Exception {

      ResultActions admin = mockMvc.perform(post(url + "/users/login").with(httpBasic("kim", "123")));
      MvcResult mvcResult = admin.andDo(print()).andReturn();
      String contentAsString = mvcResult.getResponse().getContentAsString();
      JSONObject jsonObject = new JSONObject(contentAsString);
      String kimToken = "Bearer " + jsonObject.getJSONObject("data").getString("token");

      mockMvc.perform(get(url + "/users/2").accept(MediaType.APPLICATION_JSON)
                      .header("Authorization", kimToken))
              .andExpect(jsonPath("$.flag").value(true))
              .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
              .andExpect(jsonPath("$.message").value("Find Success"))
              .andExpect(jsonPath("$.data.id").value(2))
              .andExpect(jsonPath("$.data.username").value("kim"))
              .andExpect(jsonPath("$.data.roles").value("user"));
   }

   @Test
   @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
   void testFindByIdWithUserAccessingAnotherUserInfo() throws Exception {

      ResultActions admin = mockMvc.perform(post(url + "/users/login").with(httpBasic("kim", "123")));
      MvcResult mvcResult = admin.andDo(print()).andReturn();
      String contentAsString = mvcResult.getResponse().getContentAsString();
      JSONObject jsonObject = new JSONObject(contentAsString);
      String kimToken = "Bearer " + jsonObject.getJSONObject("data").getString("token");

      mockMvc.perform(get(url + "/users/3").accept(MediaType.APPLICATION_JSON)
                      .header("Authorization", kimToken))
              .andExpect(jsonPath("$.flag").value(false))
              .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
              .andExpect(jsonPath("$.message").value("No Permission"))
              .andExpect(jsonPath("$.data").value("Access Denied"));
   }

   @Test
   void testFindByIdNotFound() throws Exception {
      mockMvc.perform(get(url + "/users/8").accept(MediaType.APPLICATION_JSON)
                      .header("Authorization", token))
              .andExpect(jsonPath("$.flag").value(false))
              .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
              .andExpect(jsonPath("$.message").value("Could not find user with id 8"))
              .andExpect(jsonPath("$.data").isEmpty());
   }

   @Test
   @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
   void testFindAllSuccess() throws Exception {
      mockMvc.perform(get(url + "/users").accept(MediaType.APPLICATION_JSON)
                      .header("Authorization", token))
              .andExpect(jsonPath("$.flag").value(true))
              .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
              .andExpect(jsonPath("$.message").value("Find all Success"))
              .andExpect(jsonPath("$.data[0].id").value(1))
              .andExpect(jsonPath("$.data[0].username").value("admin"))
              .andExpect(jsonPath("$.data", Matchers.hasSize(4)));
   }

   @Test
   void testAddSuccess() throws Exception {
      // Given
      MaUser newUser = new MaUser();
//      newUser.setId(9);
      newUser.setUsername("IronMan");
      newUser.setPassword("123");
      newUser.setEnabled(true);
      newUser.setRoles("user");

      String json = objectMapper.writeValueAsString(newUser);

      // When and Then
      mockMvc.perform(post(url + "/users")
                      .accept(MediaType.APPLICATION_JSON).header("Authorization", token)
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(json))
              .andExpect(jsonPath("$.flag").value(true))
              .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
              .andExpect(jsonPath("$.message").value("Add Success"))
              .andExpect(jsonPath("$.data.id").isNotEmpty())
              .andExpect(jsonPath("$.data.username").value("IronMan"))
              .andExpect(jsonPath("$.data.roles").value("user"))
              .andExpect(jsonPath("$.data.enabled").value(true));
   }

   @Test
   @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
   void testUpdateWithAdminUpdateAllUsers() throws Exception {
      // Given
      MaUser update = new MaUser();
      update.setId(2);
      update.setUsername("IronMan-update");
      update.setEnabled(false);
      update.setRoles("user admin");

      String json = objectMapper.writeValueAsString(update);

      // When and Then
      mockMvc.perform(put(url + "/users/2")
                      .accept(MediaType.APPLICATION_JSON).header("Authorization", token)
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(json))
              .andExpect(jsonPath("$.flag").value(true))
              .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
              .andExpect(jsonPath("$.message").value("Update Success"))
              .andExpect(jsonPath("$.data.id").value(2))
              .andExpect(jsonPath("$.data.username").value("IronMan-update"))
              .andExpect(jsonPath("$.data.roles").value("user admin"))
              .andExpect(jsonPath("$.data.enabled").value(false));
   }

   @Test
   @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
   void testUpdateWithUserUpdateOwnInfo() throws Exception {
      ResultActions admin = mockMvc.perform(post(url + "/users/login").with(httpBasic("kim", "123")));
      MvcResult mvcResult = admin.andDo(print()).andReturn();
      String contentAsString = mvcResult.getResponse().getContentAsString();
      JSONObject jsonObject = new JSONObject(contentAsString);
      String kimToken = "Bearer " + jsonObject.getJSONObject("data").getString("token");

      // Given
      MaUser update = new MaUser();
      update.setId(2);
      update.setUsername("IronMan-update");
      update.setEnabled(true);
      update.setRoles("user");

      String json = objectMapper.writeValueAsString(update);

      // When and Then
      mockMvc.perform(put(url + "/users/2")
                      .accept(MediaType.APPLICATION_JSON).header("Authorization", kimToken)
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(json))
              .andExpect(jsonPath("$.flag").value(true))
              .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
              .andExpect(jsonPath("$.message").value("Update Success"))
              .andExpect(jsonPath("$.data.id").value(2))
              .andExpect(jsonPath("$.data.username").value("IronMan-update"))
              .andExpect(jsonPath("$.data.roles").value("user"))
              .andExpect(jsonPath("$.data.enabled").value(true));
   }
   @Test
   @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
   void testUpdateWithUserUpdateAnotherUserInfo() throws Exception {
      ResultActions admin = mockMvc.perform(post(url + "/users/login").with(httpBasic("kim", "123")));
      MvcResult mvcResult = admin.andDo(print()).andReturn();
      String contentAsString = mvcResult.getResponse().getContentAsString();
      JSONObject jsonObject = new JSONObject(contentAsString);
      String kimToken = "Bearer " + jsonObject.getJSONObject("data").getString("token");

      // Given
      MaUser update = new MaUser();
      update.setUsername("IronMan-update");
      update.setEnabled(true);
      update.setRoles("user");

      String json = objectMapper.writeValueAsString(update);

      // When and Then
      mockMvc.perform(put(url + "/users/3")
                      .accept(MediaType.APPLICATION_JSON).header("Authorization", kimToken)
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(json))
              .andExpect(jsonPath("$.flag").value(false))
              .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
              .andExpect(jsonPath("$.message").value("No Permission"))
              .andExpect(jsonPath("$.data").value("Access Denied"));
   }


   @Test
   void testUpdateFail() throws Exception {
      // Given
      MaUser update = new MaUser();
      update.setId(7);
      update.setUsername("IronMan");
      update.setPassword("123");
      update.setEnabled(true);
      update.setRoles("user");

      String json = objectMapper.writeValueAsString(update);

      // When and Then
      mockMvc.perform(put(url + "/users/7")
                      .accept(MediaType.APPLICATION_JSON).header("Authorization", token)
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(json))
              .andExpect(jsonPath("$.flag").value(false))
              .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
              .andExpect(jsonPath("$.message").value("Could not find user with id 7"))
              .andExpect(jsonPath("$.data").isEmpty());
   }

   @Test
   @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
   void testDeleteSuccess() throws Exception {
      mockMvc.perform(delete(url + "/users/2").header("Authorization", token)
                      .accept(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath("$.flag").value(true))
              .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
              .andExpect(jsonPath("$.message").value("Delete Success"))
              .andExpect(jsonPath("$.data").isEmpty());
   }
   @Test
   void testDeleteFail() throws Exception {
      mockMvc.perform(delete(url + "/users/8").header("Authorization", token)
                      .accept(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath("$.flag").value(false))
              .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
              .andExpect(jsonPath("$.message").value("Could not find user with id 8"))
              .andExpect(jsonPath("$.data").isEmpty());
   }

   @Test
   @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
   void testPasswordChangeWithUserOwn() throws Exception {
      //given
      ResultActions admin = mockMvc.perform(post(url + "/users/login").with(httpBasic("kim", "123")));
      MvcResult mvcResult = admin.andDo(print()).andReturn();
      String contentAsString = mvcResult.getResponse().getContentAsString();
      JSONObject jsonObject = new JSONObject(contentAsString);
      String kimToken = "Bearer " + jsonObject.getJSONObject("data").getString("token");

      Map<String, String> passwordMap = new HashMap<>();
      passwordMap.put("oldPassword", "123");
      passwordMap.put("newPassword", "Abc12345");
      passwordMap.put("confirmNewPassword", "Abc12345");
      String json = objectMapper.writeValueAsString(passwordMap);

      mockMvc.perform(patch(url + "/users/2/password").contentType(MediaType.APPLICATION_JSON)
                      .content(json)
                      .header("Authorization", kimToken)
                      .accept(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath("$.flag").value(true))
              .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
              .andExpect(jsonPath("$.message").value("Change Password Success"));
   }

   @Test
   @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
   void testPasswordChangeWithAdminPrivilege() throws Exception {
      //given
      Map<String, String> passwordMap = new HashMap<>();
      passwordMap.put("oldPassword", "123");
      passwordMap.put("newPassword", "Abc12345");
      passwordMap.put("confirmNewPassword", "Abc12345");
      String json = objectMapper.writeValueAsString(passwordMap);

      mockMvc.perform(patch(url + "/users/2/password").contentType(MediaType.APPLICATION_JSON)
                      .content(json)
                      .header("Authorization", token)
                      .accept(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath("$.flag").value(true))
              .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
              .andExpect(jsonPath("$.message").value("Change Password Success"));
   }

   @Test
   @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
   void testPasswordChangeWithNotMatchNewAndConfirm() throws Exception {
      //given
      ResultActions admin = mockMvc.perform(post(url + "/users/login").with(httpBasic("kim", "123")));
      MvcResult mvcResult = admin.andDo(print()).andReturn();
      String contentAsString = mvcResult.getResponse().getContentAsString();
      JSONObject jsonObject = new JSONObject(contentAsString);
      String kimToken = "Bearer " + jsonObject.getJSONObject("data").getString("token");

      Map<String, String> passwordMap = new HashMap<>();
      passwordMap.put("oldPassword", "123");
      passwordMap.put("newPassword", "Abc12345");
      passwordMap.put("confirmNewPassword", "Abc123456");
      String json = objectMapper.writeValueAsString(passwordMap);

      mockMvc.perform(patch(url + "/users/2/password").contentType(MediaType.APPLICATION_JSON)
                      .content(json)
                      .header("Authorization", kimToken)
                      .accept(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath("$.flag").value(false))
              .andExpect(jsonPath("$.code").value(StatusCode.BAD_REQUEST))
              .andExpect(jsonPath("$.message").value("Old password and new password dose not match."));
   }



}