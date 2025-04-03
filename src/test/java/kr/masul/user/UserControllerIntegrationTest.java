package kr.masul.user;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
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
   void testFindByIdSuccess() throws Exception {
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
   void testUpdateSuccess() throws Exception {
      // Given
      MaUser update = new MaUser();
      update.setId(2);
      update.setUsername("IronMan");
      update.setPassword("123");
      update.setEnabled(true);
      update.setRoles("user");

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
              .andExpect(jsonPath("$.data.username").value("IronMan"))
              .andExpect(jsonPath("$.data.roles").value("user"))
              .andExpect(jsonPath("$.data.enabled").value(true));
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
}