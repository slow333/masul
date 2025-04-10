package kr.masul.security;

import kr.masul.client.redisCache.RedisCacheClient;
import kr.masul.system.converter.UserToDto;
import kr.masul.user.MaUser;
import kr.masul.user.UserDto;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {

   private final UserToDto userToDto;
   private final JwtProvider jwtProvider;
   private final RedisCacheClient redisCacheClient;

   public AuthService(UserToDto userToDto,
                      JwtProvider jwtProvider,
                      RedisCacheClient redisCacheClient) {
      this.userToDto = userToDto;
      this.jwtProvider = jwtProvider;
      this.redisCacheClient = redisCacheClient;
   }

   public Map<String, Object> createLoginInfo(Authentication authentication) {
      MaUserPrincipal principal = (MaUserPrincipal) authentication.getPrincipal();
      MaUser maUser = principal.getMaUser();
      UserDto dto = userToDto.convert(maUser);
      String token = jwtProvider.createToken(authentication);

      redisCacheClient.set("whiteList:"+ maUser.getId(), token, 2, TimeUnit.HOURS);

      Map<String, Object> userMap = new HashMap<>();
      userMap.put("userInfo", dto);
      userMap.put("token", token);

      return userMap;
   }
}
