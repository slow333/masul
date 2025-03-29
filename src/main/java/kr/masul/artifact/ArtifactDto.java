package kr.masul.artifact;

import jakarta.validation.constraints.NotEmpty;
import kr.masul.wizard.WizardDto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ArtifactDto(

        String id,

        @NotEmpty(message = "NAME is required.")
        String name,

        @NotEmpty(message = "DESCRIPTION is required.")
        String description,

        String imageUrl,

        LocalDateTime createAt,

        WizardDto owner
) {
}
