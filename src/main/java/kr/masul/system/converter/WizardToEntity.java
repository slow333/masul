package kr.masul.system.converter;

import kr.masul.wizard.Wizard;
import kr.masul.wizard.WizardDto;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class WizardToEntity implements Converter<WizardDto, Wizard> {
   @Override
   public Wizard convert(WizardDto source) {
      Wizard w = new Wizard();
      w.setId(source.id());
      w.setName(source.name());
      w.setBirthday(source.birthday());
      return w;
   }
}
