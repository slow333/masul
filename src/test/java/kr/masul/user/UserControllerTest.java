package kr.masul.user;

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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

   @MockitoBean
   UserService userService;

   @Autowired
   ObjectMapper objectMapper;

   @Autowired
   MockMvc mockMvc;

   @Value("${api.base-url}")
   String url;

   List<MaUser> users;
   @BeforeEach
   void setUp() {
      users = new ArrayList<>();
      MaUser u = new MaUser();
      u.setId(1);
      u.setUsername("admin");
      u.setPassword("321");
      u.setEnabled(true);
      u.setRoles("admin user");

      MaUser u1 = new MaUser();
      u1.setId(2);
      u1.setUsername("kim");
      u1.setPassword("123");
      u1.setEnabled(true);
      u1.setRoles("user");

      MaUser u2 = new MaUser();
      u2.setId(3);
      u2.setUsername("woo");
      u2.setPassword("123");
      u2.setEnabled(true);
      u2.setRoles("user");
      users.add(u);
      users.add(u1);
      users.add(u2);
   }

   @Test
   void testFindByIdSuccess() throws Exception {
      // Given
      given(userService.findById(2)).willReturn(users.get(1));
      // When and Then
      mockMvc.perform(get(url + "/users/2").accept(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath("$.flag").value(true))
              .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
              .andExpect(jsonPath("$.message").value("Find Success"))
              .andExpect(jsonPath("$.data.id").value(2))
              .andExpect(jsonPath("$.data.username").value("kim"))
              .andExpect(jsonPath("$.data.roles").value("user"));
   }

   @Test
   void testFindByIdNotFound() throws Exception {
      // Given
      given(userService.findById(Mockito.anyInt()))
              .willThrow(new ObjectNotFoundException("user", 8));
      // When and Then
      mockMvc.perform(get(url + "/users/8").accept(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath("$.flag").value(false))
              .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
              .andExpect(jsonPath("$.message").value("Could not find user with id 8"))
              .andExpect(jsonPath("$.data").isEmpty());
   }

   @Test
   void testFindAllSuccess() throws Exception {
      // Given
      given(userService.findAll()).willReturn(users);
      // When and Then
      mockMvc.perform(get(url + "/users").accept(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath("$.flag").value(true))
              .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
              .andExpect(jsonPath("$.message").value("Find all Success"))
              .andExpect(jsonPath("$.data[0].id").value(1))
              .andExpect(jsonPath("$.data[0].username").value("admin"))
              .andExpect(jsonPath("$.data", Matchers.hasSize(3)));
   }

   @Test
   void testAddSuccess() throws Exception {
      // Given
      MaUser newUser = new MaUser();
      newUser.setId(9);
      newUser.setUsername("IronMan");
      newUser.setPassword("123");
      newUser.setEnabled(true);
      newUser.setRoles("user");

      String json = objectMapper.writeValueAsString(newUser);

      given(userService.add(Mockito.any(MaUser.class))).willReturn(newUser);
      // When and Then
      mockMvc.perform(post(url + "/users")
                      .accept(MediaType.APPLICATION_JSON)
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(json))
              .andExpect(jsonPath("$.flag").value(true))
              .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
              .andExpect(jsonPath("$.message").value("Add Success"))
              .andExpect(jsonPath("$.data.id").value(9))
              .andExpect(jsonPath("$.data.username").value("IronMan"))
              .andExpect(jsonPath("$.data.roles").value("user"))
              .andExpect(jsonPath("$.data.enabled").value(true));
   }

   @Test
   void testUpdateSuccess() throws Exception {
      // Given
      MaUser update = new MaUser();
      update.setId(2);
      update.setUsername("IronMan");
      update.setPassword("123");
      update.setEnabled(true);
      update.setRoles("user");

      String json = objectMapper.writeValueAsString(update);

      given(userService.update(eq(2),Mockito.any(MaUser.class))).willReturn(update);
      // When and Then
      mockMvc.perform(put(url + "/users/2")
                      .accept(MediaType.APPLICATION_JSON)
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

      given(userService.update(eq(7),Mockito.any(MaUser.class)))
              .willThrow(new ObjectNotFoundException("user", 7));
      // When and Then
      mockMvc.perform(put(url + "/users/7")
                      .accept(MediaType.APPLICATION_JSON)
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(json))
              .andExpect(jsonPath("$.flag").value(false))
              .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
              .andExpect(jsonPath("$.message").value("Could not find user with id 7"))
              .andExpect(jsonPath("$.data").isEmpty());
   }

   @Test
   void testDeleteSuccess() throws Exception {
      // Given
      doNothing().when(userService).delete(2);
      // When and Then
      mockMvc.perform(delete(url + "/users/2")
                      .accept(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath("$.flag").value(true))
              .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
              .andExpect(jsonPath("$.message").value("Delete Success"))
              .andExpect(jsonPath("$.data").isEmpty());
   }
   @Test
   void testDeleteFail() throws Exception {
      // Given
      doThrow(new ObjectNotFoundException("user", 8)).when(userService).delete(8);
      // When and Then
      mockMvc.perform(delete(url + "/users/8")
                      .accept(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath("$.flag").value(false))
              .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
              .andExpect(jsonPath("$.message").value("Could not find user with id 8"))
              .andExpect(jsonPath("$.data").isEmpty());
   }
}