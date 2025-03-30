package kr.masul.security;

import kr.masul.user.MaUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collection;

public class MaUserPrincipal implements UserDetails {

   private MaUser maUser;

   public MaUserPrincipal(MaUser maUser) {
      this.maUser = maUser;
   }

   @Override
   public Collection<? extends GrantedAuthority> getAuthorities() {
      return Arrays.stream(StringUtils.tokenizeToStringArray(maUser.getRoles(), " "))
              .map(role -> new SimpleGrantedAuthority("ROLE_"+role))
              .toList();
   }

   @Override
   public String getPassword() {
      return maUser.getPassword();
   }

   @Override
   public String getUsername() {
      return maUser.getUsername();
   }

   @Override
   public boolean isAccountNonExpired() {
      return true;
   }

   @Override
   public boolean isAccountNonLocked() {
      return true;
   }

   @Override
   public boolean isCredentialsNonExpired() {
      return true;
   }

   @Override
   public boolean isEnabled() {
      return true;
   }

   public MaUser getMaUser() {
      return maUser;
   }
}
