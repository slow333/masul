package kr.masul.user;

import kr.masul.security.MaUserPrincipal;
import kr.masul.system.exception.ObjectNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles(value = "dev")
class UserServiceTest {

   @Mock
   UserRepository userRepository;
   @Mock
   PasswordEncoder passwordEncoder;

   @InjectMocks
   UserService userService;

   List<MaUser> users;
   @BeforeEach
   void setUp() {
      users = new ArrayList<>();
      MaUser u = new MaUser();
      u.setUsername("admin");
      u.setPassword("321");
      u.setEnabled(true);
      u.setRoles("admin user");

      MaUser u1 = new MaUser();
      u1.setUsername("kim");
      u1.setPassword("123");
      u1.setEnabled(true);
      u1.setRoles("user");

      MaUser u2 = new MaUser();
      u2.setUsername("woo");
      u2.setPassword("123");
      u2.setEnabled(true);
      u2.setRoles("user");
      users.add(u);
      users.add(u1);
      users.add(u2);
   }

   @Test
   void testFindByIdSuccess() {
      // Given
      MaUser u = new MaUser();
      u.setId(2);
      u.setUsername("kim");
      u.setPassword("123");
      u.setRoles("user");
      u.setEnabled(true);

      given(userRepository.findById(2)).willReturn(Optional.of(u));
      // When
      MaUser user = userService.findById(2);
      // Then
      assertThat(user.getId()).isEqualTo(2);
      assertThat(user.getUsername()).isEqualTo("kim");
      assertThat(user.getPassword()).isEqualTo("123");
      verify(userRepository, times(1)).findById(2);
   }

   @Test
   void testFindByIdFail() {
      // Given
       given(userRepository.findById(Mockito.anyInt())).willReturn(Optional.empty());
      // When
      Throwable thrown = catchThrowable(() -> {
         MaUser user = userService.findById(7);
      });
      // Then
      assertThat(thrown).isInstanceOf(ObjectNotFoundException.class).hasMessage("Could not find user with id 7");
   }

   @Test
   void testFindAllSuccess() {
      // Given
      given(userRepository.findAll()).willReturn(users);
      // When
      List<MaUser> all = userService.findAll();
      // Then
      assertThat(all.size()).isEqualTo(3);
      verify(userRepository, times(1)).findAll();
   }

   @Test
   void testAddSuccess(){
      // Given
      MaUser newUser = new MaUser();
      newUser.setId(7);
      newUser.setUsername("Tom");
      newUser.setPassword("123456");
      newUser.setRoles("user");
      newUser.setEnabled(false);

      given(passwordEncoder.encode(newUser.getPassword())).willReturn("123");
      given(userRepository.save(newUser)).willReturn(newUser);
      // When
      MaUser add = userService.add(newUser);
      // Then
      assertThat(add.getId()).isEqualTo(7);
      assertThat(add.getUsername()).isEqualTo("Tom");
      assertThat(add.getRoles()).isEqualTo("user");
      assertThat(add.isEnabled()).isEqualTo(false);
      verify(userRepository, times(1)).save(newUser);
   }

   @Test
   void testUpdateWithAdminSuccess() {
      // Given
      MaUser old = new MaUser();
      old.setId(2);
      old.setUsername("kim");
      old.setPassword("123");
      old.setRoles("user");
      old.setEnabled(true);

      MaUser updated = new MaUser();
      updated.setId(2);
      updated.setUsername("Tom-update");
      updated.setPassword("123");
      updated.setRoles("admin user");
      updated.setEnabled(true);

      given(userRepository.findById(2)).willReturn(Optional.of(old));
      given(userRepository.save(old)).willReturn(updated);

      // fake user for authentication
      MaUser fuser = new MaUser();
      fuser.setRoles("admin");
      MaUserPrincipal userPrincipal = new MaUserPrincipal(fuser);

      // Mock security context 를 생성해서 권한을 얻음
      SecurityContext fakeUserContext = SecurityContextHolder.createEmptyContext();
      fakeUserContext.setAuthentication(new UsernamePasswordAuthenticationToken(userPrincipal,
              null, userPrincipal.getAuthorities()));
      SecurityContextHolder.setContext(fakeUserContext);
      // When
      MaUser updatedUser = userService.update(2, updated);
      // Then
      assertThat(updatedUser.getId()).isEqualTo(2);
      assertThat(updatedUser.getUsername()).isEqualTo("Tom-update");
      assertThat(updatedUser.getRoles()).isEqualTo("admin user");
      assertThat(updatedUser.isEnabled()).isEqualTo(true);
      verify(userRepository, times(1)).findById(2);
      verify(userRepository, times(1)).save(old);
   }
   @Test
   void testUpdateByUserSuccess() {
      // Given
      MaUser old = new MaUser();
      old.setId(2);
      old.setUsername("kim");
      old.setPassword("123");
      old.setRoles("user");
      old.setEnabled(true);

      MaUser updated = new MaUser();
      updated.setId(2);
      updated.setUsername("Tom-update");
      updated.setPassword("123");
      updated.setRoles("admin user");
      updated.setEnabled(true);

      given(userRepository.findById(2)).willReturn(Optional.of(old));
      given(userRepository.save(old)).willReturn(updated);

      // fake user for authentication
      MaUser fuser = new MaUser();
      fuser.setRoles("user");
      MaUserPrincipal userPrincipal = new MaUserPrincipal(fuser);

      // Mock security context 를 생성해서 권한을 얻음
      SecurityContext fakeUserContext = SecurityContextHolder.createEmptyContext();
      fakeUserContext.setAuthentication(new UsernamePasswordAuthenticationToken(userPrincipal,
              null, userPrincipal.getAuthorities()));
      SecurityContextHolder.setContext(fakeUserContext);
      // When
      MaUser updatedUser = userService.update(2, updated);
      // Then
      assertThat(updatedUser.getId()).isEqualTo(2);
      assertThat(updatedUser.getUsername()).isEqualTo("Tom-update");
      assertThat(updatedUser.getRoles()).isEqualTo("user");
      assertThat(updatedUser.isEnabled()).isEqualTo(true);
      verify(userRepository, times(1)).findById(2);
      verify(userRepository, times(1)).save(old);
   }

   @Test
   void testUpdateFail() {
      // Given
      MaUser updated = new MaUser();
      updated.setId(2);
      updated.setUsername("Tom");
      updated.setPassword("123");
      updated.setRoles("admin");
      updated.setEnabled(true);
      given(userRepository.findById(6)).willReturn(Optional.empty());
      // When
      Throwable thrown = catchThrowable(() -> {
         MaUser updatedUser = userService.update(6, updated);
      });
      // Then
      assertThat(thrown).isInstanceOf(ObjectNotFoundException.class).hasMessage("Could not find user with id 6");
   }

   @Test
   void testDeleteSuccess(){
      // Given
      MaUser updated = new MaUser();
      updated.setId(2);
      updated.setUsername("Tom");
      updated.setPassword("123");
      updated.setRoles("admin");
      updated.setEnabled(true);

      given(userRepository.findById(2)).willReturn(Optional.of(updated));
      doNothing().when(userRepository).deleteById(2);
      // When
      userService.delete(2);
      // Then
      verify(userRepository).deleteById(2);
   }
   @Test
   void testDeleteFail(){
      // Given
      given(userRepository.findById(8)).willReturn(Optional.empty());
      // When
      Throwable thrown = catchThrowable(() -> {
         userService.delete(8);
      });
      // Then
      assertThat(thrown).isInstanceOf(ObjectNotFoundException.class).hasMessage("Could not find user with id 8");
   }
}