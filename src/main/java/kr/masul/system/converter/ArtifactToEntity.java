package kr.masul.system.converter;

import kr.masul.artifact.Artifact;
import kr.masul.artifact.ArtifactDto;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ArtifactToEntity implements Converter<ArtifactDto, Artifact> {
   @Override
   public Artifact convert(ArtifactDto source) {
      Artifact artifact = new Artifact();
      artifact.setId(source.id());
      artifact.setName(source.name());
      artifact.setDescription(source.description());
      artifact.setCreateAt(source.createAt());
      artifact.setImageUrl(source.imageUrl());

      return artifact;
   }
}
