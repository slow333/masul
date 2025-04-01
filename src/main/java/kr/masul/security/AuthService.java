package kr.masul.security;

import kr.masul.system.converter.UserToDto;
import kr.masul.user.MaUser;
import kr.masul.user.UserDto;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

   private final UserToDto userToDto;
   private final JwtProvider jwtProvider;

   public AuthService(UserToDto userToDto, JwtProvider jwtProvider) {
      this.userToDto = userToDto;
      this.jwtProvider = jwtProvider;
   }

   public Map<String, Object> createLoginInfo(Authentication authentication) {
      MaUserPrincipal principal = (MaUserPrincipal) authentication.getPrincipal();
      MaUser maUser = principal.getMaUser();
      UserDto dto = userToDto.convert(maUser);
      String token = jwtProvider.createToken(authentication);
      Map<String, Object> userMap = new HashMap<>();
      userMap.put("userInfo", dto);
      userMap.put("token", token);

      return userMap;
   }
}
