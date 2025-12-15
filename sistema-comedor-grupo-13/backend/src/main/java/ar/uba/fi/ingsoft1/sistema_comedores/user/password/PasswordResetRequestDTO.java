package ar.uba.fi.ingsoft1.sistema_comedores.user.password;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import ar.uba.fi.ingsoft1.sistema_comedores.config.validation.ValidationConsts;

public record PasswordResetRequestDTO(
    @NotBlank(message = "${validation.email.messages.required}")
    @Email(message = "${validation.email.messages.invalid-format}")
    @Pattern(regexp = ValidationConsts.EMAIL_DOMAIN_PATTERN, 
    message = "${validation.email.messages.invalid-domain}")
    String email) 
    {}
