package kr.masul.security;

import com.nimbusds.jwt.JWTClaimsSet;
import org.apache.logging.log4j.CloseableThreadContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

@Component
public class JwtProvider {

   private final JwtEncoder jwtEncoder;

   public JwtProvider(JwtEncoder jwtEncoder) {
      this.jwtEncoder = jwtEncoder;
   }

   public String createToken(Authentication authentication) {

      Instant issueAt = Instant.now();
      long duration = 2;

      String authorities = authentication.getAuthorities().stream()
              .map(GrantedAuthority::getAuthority)
              .collect(Collectors.joining(" "));

      JwtClaimsSet claims = JwtClaimsSet.builder()
              .issuer("self")
              .issuedAt(issueAt)
              .expiresAt(issueAt.plus(duration, ChronoUnit.HOURS))
              .subject(authentication.getName())
              .claim("userId", ((MaUserPrincipal)(authentication.getPrincipal())).getMaUser().getId())
              .claim("authorities", authorities)
              .build();
      return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
   }
}
