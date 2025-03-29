package kr.masul.system.converter;

import kr.masul.user.MaUser;
import kr.masul.user.UserDto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class UserToEntity implements Converter<UserDto, MaUser> {

   @Override
   public MaUser convert(UserDto source) {
      MaUser userEntity = new MaUser();
      userEntity.setId(source.id());
      userEntity.setUsername(source.username());

      userEntity.setEnabled(source.enabled());
      userEntity.setRoles(source.roles());

      return userEntity;
   }
}
