package kr.masul.user;

import jakarta.validation.constraints.NotEmpty;

public record UserDto(

        Integer id,

        @NotEmpty(message = "username is required.")
        String username,

        boolean enabled,

        @NotEmpty(message = "Roles required.")
        String roles
) {
}
