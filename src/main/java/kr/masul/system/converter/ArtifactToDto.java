package kr.masul.system.converter;

import kr.masul.artifact.Artifact;
import kr.masul.artifact.ArtifactDto;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ArtifactToDto implements Converter<Artifact, ArtifactDto> {

   private final WizardToDto wizardToDto;

   public ArtifactToDto(WizardToDto wizardToDto) {
      this.wizardToDto = wizardToDto;
   }

   @Override
   public ArtifactDto convert(Artifact source) {
      ArtifactDto dto = new ArtifactDto(
              source.getId(),
              source.getName(),
              source.getDescription(),
              source.getImageUrl(),
              source.getCreateAt(),
              source.getOwner() != null ? wizardToDto.convert(source.getOwner()) : null
      );
      return dto;
   }
}
