package kr.masul.wizard;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

import java.time.LocalDateTime;

public record WizardDto(
        Integer id,

        @NotEmpty(message = "NAME is required.")
        String name,

        LocalDateTime birthday,

        Integer numberOfArtifacts
) {
}
