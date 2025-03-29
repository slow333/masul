package kr.masul.system.converter;

import kr.masul.user.MaUser;
import kr.masul.user.UserDto;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class UserToDto implements Converter<MaUser, UserDto> {
   @Override
   public UserDto convert(MaUser source) {
      return new UserDto(
              source.getId(), source.getUsername(), source.isEnabled(), source.getRoles());
   }
}
