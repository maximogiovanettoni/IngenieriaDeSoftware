package ar.uba.fi.ingsoft1.sistema_comedores.user.password;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import ar.uba.fi.ingsoft1.sistema_comedores.config.validation.ValidationConsts;

public record PasswordResetDTO(
    @NotBlank(message = "${validation.user.password.messages.required}")
    @Size(min = ValidationConsts.PASSWORD_MIN_LENGTH, max = ValidationConsts.PASSWORD_MAX_LENGTH, 
        message = "${validation.user.password.messages.invalid-length}")
    @Pattern(regexp = ValidationConsts.PASSWORD_PATTERN, 
        message = "${validation.user.password.messages.invalid-format}")
    String newPassword,
    @NotBlank(message = "${validation.user.password.messages.reset-token}")
    String token)
    {}
