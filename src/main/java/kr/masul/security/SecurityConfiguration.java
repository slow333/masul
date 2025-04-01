package kr.masul.security;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import kr.masul.system.exception.CustomBasicAuthEntryPoint;
import kr.masul.system.exception.CustomBearerTokenAccessDeniedHandler;
import kr.masul.system.exception.CustomBearerTokenAuthEntryPoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
public class SecurityConfiguration {

   private final RSAPublicKey publicKey;
   private final RSAPrivateKey privateKey;

   private final CustomBasicAuthEntryPoint customBasicAuthEntryPoint;
   private final CustomBearerTokenAuthEntryPoint customBearerTokenAuthEntryPoint;
   private final CustomBearerTokenAccessDeniedHandler customBearerTokenAccessDeniedHandler;

   @Value("${api.base-url}")
   String url;

   public SecurityConfiguration(CustomBasicAuthEntryPoint customBasicAuthEntryPoint, CustomBearerTokenAuthEntryPoint customBearerTokenAuthEntryPoint, CustomBearerTokenAccessDeniedHandler customBearerTokenAccessDeniedHandler) throws NoSuchAlgorithmException {
      this.customBasicAuthEntryPoint = customBasicAuthEntryPoint;
      this.customBearerTokenAuthEntryPoint = customBearerTokenAuthEntryPoint;
      this.customBearerTokenAccessDeniedHandler = customBearerTokenAccessDeniedHandler;
      KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
      keyPairGenerator.initialize(2048);
      KeyPair keyPair = keyPairGenerator.generateKeyPair();
      this.publicKey = (RSAPublicKey) keyPair.getPublic();
      this.privateKey = (RSAPrivateKey) keyPair.getPrivate();
   }

   @Bean
   SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
      http.authorizeHttpRequests(request -> request
        .requestMatchers(HttpMethod.GET,url + "/artifacts/**").permitAll()
        .requestMatchers(HttpMethod.GET, url + "/users/**").hasAuthority("ROLE_admin")
        .requestMatchers(HttpMethod.POST, url + "/users").hasAuthority("ROLE_admin")
        .requestMatchers(HttpMethod.PUT, url + "/users/**").hasAuthority("ROLE_admin")
        .requestMatchers(HttpMethod.DELETE, url + "/users/**").hasAuthority("ROLE_admin")
        .requestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")).permitAll()
        .anyRequest().authenticated()
      )
        .httpBasic(httpBasic -> httpBasic
                .authenticationEntryPoint(customBasicAuthEntryPoint))
        .headers(headers -> headers
                .frameOptions(Customizer.withDefaults()).disable())
        .csrf(AbstractHttpConfigurer::disable)
        .cors(Customizer.withDefaults())
        .oauth2ResourceServer(resourceServer -> resourceServer
                .jwt(Customizer.withDefaults())
                .authenticationEntryPoint(customBearerTokenAuthEntryPoint)
                .accessDeniedHandler(customBearerTokenAccessDeniedHandler)
        )
              .sessionManagement(session -> session
                      .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      ;
      return http.build();
   }

   @Bean
   PasswordEncoder passwordEncoder() {
      return new BCryptPasswordEncoder(12);
   }

   @Bean
   public JwtEncoder jwtEncoder() {
      JWK jwk = new RSAKey.Builder(publicKey).privateKey(privateKey).build();
      JWKSource<SecurityContext> jwkSet = new ImmutableJWKSet<>(new JWKSet(jwk));
      return new NimbusJwtEncoder(jwkSet);
   }
   @Bean
   public JwtDecoder jwtDecoder() {
      return NimbusJwtDecoder.withPublicKey(publicKey).build();
   }
   @Bean
   public JwtAuthenticationConverter authenticationConverter() {
      JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
      grantedAuthoritiesConverter.setAuthorityPrefix("");
      grantedAuthoritiesConverter.setAuthoritiesClaimName("authorities");

      JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
      authenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);

      return authenticationConverter;
   }

}
