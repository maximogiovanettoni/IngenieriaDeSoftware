package ar.uba.fi.ingsoft1.sistema_comedores.user.email;

import jakarta.validation.constraints.NotBlank;

public record EmailVerificationRequestDTO(
    @NotBlank(message = "Token is required")
    String token
) {}