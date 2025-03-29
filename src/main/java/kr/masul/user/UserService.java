package kr.masul.user;

import jakarta.transaction.Transactional;
import kr.masul.system.exception.ObjectNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

   private final UserRepository userRepository;

   public MaUser findById(Integer userId) {
      return userRepository.findById(userId)
              .orElseThrow(() -> new ObjectNotFoundException("user", userId));
   }

   public List<MaUser> findAll() {
      return userRepository.findAll();
   }

   public MaUser add(MaUser maUser) {
      return userRepository.save(maUser);
   }

   public MaUser update(Integer userId, MaUser update) {
      return userRepository.findById(userId).map(user -> {
         user.setId(update.getId());
         user.setUsername(update.getUsername());
         user.setEnabled(update.isEnabled());
         user.setRoles(update.getRoles());
         return userRepository.save(user);
      }).orElseThrow(() -> new ObjectNotFoundException("user", userId));
   }

   public void delete(Integer userId) {
      userRepository.findById(userId).orElseThrow(() -> new ObjectNotFoundException("user", userId));
      userRepository.deleteById(userId);
   }
}
