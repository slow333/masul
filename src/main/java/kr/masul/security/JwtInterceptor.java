package kr.masul.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.masul.client.redisCache.RedisCacheClient;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

// security에서 controller로 들고 나는 패킷을 캡처해서 처리
@Component
public class JwtInterceptor implements HandlerInterceptor {

   private final RedisCacheClient redisCacheClient;

   public JwtInterceptor(RedisCacheClient redisCacheClient) {
      this.redisCacheClient = redisCacheClient;
   }

   @Override
   public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
      // request해더에서 token를 얻음
      String authorizationHeader = request.getHeader("Authorization");

      // token이 null이 아니면, "Bearer "로 시작하면, redis에 맞는게 있는지 확인
      // header가 없으면 인증이 필요 없는 것으로 간주해서 pass(true)
      if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
         Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
         Jwt jwt = (Jwt)authentication.getPrincipal();

         // jwt에서 userId를 얻고, token이 redis에 있는지 확인
         String userId = jwt.getClaim("userId").toString();
         if(!redisCacheClient.isUserTokenInWhiteList(userId, jwt.getTokenValue())){
            throw new BadCredentialsException("Invalid token from interceptor");
         }
      }
      return true;
   }
}
