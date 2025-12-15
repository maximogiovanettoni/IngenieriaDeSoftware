package ar.uba.fi.ingsoft1.sistema_comedores.user.token;

import jakarta.validation.constraints.NotBlank;

public record RefreshDTO(
        @NotBlank String refreshToken
) {}
