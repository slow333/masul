package kr.masul.user;

import jakarta.transaction.Transactional;
import kr.masul.security.MaUserPrincipal;
import kr.masul.system.exception.ObjectNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

   private final UserRepository userRepository;
   private final PasswordEncoder passwordEncoder;

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
}
