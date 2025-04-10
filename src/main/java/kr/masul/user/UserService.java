package kr.masul.user;

import jakarta.transaction.Transactional;
import kr.masul.client.redisCache.RedisCacheClient;
import kr.masul.security.MaUserPrincipal;
import kr.masul.system.exception.ObjectNotFoundException;
import kr.masul.system.exception.PasswordChangeIllegalArgumentException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

   private final UserRepository userRepository;
   private final PasswordEncoder passwordEncoder;
   private final RedisCacheClient redisCacheClient;

   public MaUser findById(Integer userId) {
      return userRepository.findById(userId)
              .orElseThrow(() -> new ObjectNotFoundException("user", userId));
   }

   public List<MaUser> findAll() {
      return userRepository.findAll();
   }

   public MaUser add(MaUser maUser) {
      maUser.setPassword(passwordEncoder.encode(maUser.getPassword()));
      return userRepository.save(maUser);
   }

   public MaUser update(Integer userId, MaUser update) {
      MaUser maUser = userRepository.findById(userId)
              .orElseThrow(() -> new ObjectNotFoundException("user", userId));
      // 권한에 따라 수정 사항을 지정: admin만 모두변경 가능(암호 제외)
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

      if(authentication.getAuthorities().stream().noneMatch(grantedAuthority ->
              grantedAuthority.getAuthority().equals("ROLE_admin"))){
         maUser.setUsername(update.getUsername());
      } else {
         maUser.setId(update.getId());
         maUser.setUsername(update.getUsername());
         maUser.setEnabled(update.isEnabled());
         maUser.setRoles(update.getRoles());
         // 사용자 정보를 admin이 변경하면 redis에서 정보를 삭제
         redisCacheClient.delete("whiteList:" + userId);
      }
      userRepository.save(maUser);
      return maUser;
   }

   public void delete(Integer userId) {
      userRepository.findById(userId).orElseThrow(() -> new ObjectNotFoundException("user", userId));
      userRepository.deleteById(userId);
   }

   @Override
   public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
     return  userRepository.findByUsername(username)
              .map(MaUserPrincipal::new)
              .orElseThrow(() -> new UsernameNotFoundException("Username "+ username+ " is not found"));

   }

   public void changePassword(
           Integer userId, String oldPassword, String newPassword, String confirmNewPassword) {
      MaUser maUser = userRepository.findById(userId)
              .orElseThrow(() -> new ObjectNotFoundException("user", userId));

      // 원래 암호가 맞지 않음
      if (!passwordEncoder.matches(oldPassword, maUser.getPassword())) {
         throw new BadCredentialsException("Old password is not correct.");
      }
      // 새암호와 확인 암호가 서로 다름
      if (!newPassword.equals(confirmNewPassword)) {
         throw new PasswordChangeIllegalArgumentException("Old password and new password dose not match.");
      }
      /**
       * ^                 # start-of-string
       * (?=.*[0-9])       # a digit must occur at least once
       * (?=.*[a-z])       # a lower case letter must occur at least once
       * (?=.*[A-Z])       # an upper case letter must occur at least once
       * (?=.*[@#$%^&+=])  # a special character must occur at least once
       * (?=\S+$)          # no whitespace allowed in the entire string
       * .{8,}             # anything, at least eight places though
       * $                 # end-of-string
       */
      // 새 암호가 암호 정책에 안맞음: 대문자1, 숫자1, 8자 이상
      String passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$";
      if(!newPassword.matches(passwordPattern)){
         throw new PasswordChangeIllegalArgumentException("New password does not conform password policy.");
      }
      maUser.setPassword(passwordEncoder.encode(newPassword));

      // 암포변경하면 redis에서 key를 삭제함
      // 이를 위해서는 interceptor를 생성해서 webMvc에 정의해야함
      redisCacheClient.delete("whiteList:" + userId);

      userRepository.save(maUser);
   }
}
