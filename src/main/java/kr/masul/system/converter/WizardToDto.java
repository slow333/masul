package kr.masul.system.converter;

import kr.masul.wizard.Wizard;
import kr.masul.wizard.WizardDto;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class WizardToDto implements Converter<Wizard, WizardDto> {

   @Override
   public WizardDto convert(Wizard source) {
      WizardDto dto = new WizardDto(
              source.getId(),
              source.getName(),
              source.getBirthday(),
              source.getNumberOfArtifacts()
      );
      return dto;
   }
}
