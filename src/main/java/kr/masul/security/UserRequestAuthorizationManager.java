package kr.masul.security;

import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriTemplate;

import java.util.Map;
import java.util.function.Supplier;

@Component
public class UserRequestAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

   UriTemplate USER_URI_TEMPLATE = new UriTemplate("/users/{userId}");
   @Override
   public AuthorizationDecision check(Supplier<Authentication> authenticationSupplier, RequestAuthorizationContext context) {
      Map<String, String> pathVariable = USER_URI_TEMPLATE.match(context.getRequest().getRequestURI());
      // url에서 userId 가져 오기
      String userIdFromUrl = pathVariable.get("userId");

      Authentication authentication = authenticationSupplier.get();
      // JWT에서 userId 가져오기
      String userIdFromJwt = ((Jwt)authentication.getPrincipal()).getClaim("userId").toString();

      // admin인지 확인
      boolean hasAdminRole = authentication.getAuthorities().stream().anyMatch(
              grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_admin")
      );

      // user인지 확인
      boolean hasUserRole = authentication.getAuthorities().stream().anyMatch(
              grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_user")
      );
      // url id와 auth id를 비교
      boolean idsMatch = userIdFromUrl != null && userIdFromUrl.equals(userIdFromJwt);

      return new AuthorizationDecision(hasAdminRole || (hasUserRole && idsMatch));
   }

}
