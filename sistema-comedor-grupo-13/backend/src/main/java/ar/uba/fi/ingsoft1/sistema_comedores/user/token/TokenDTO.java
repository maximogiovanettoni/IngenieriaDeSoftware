package ar.uba.fi.ingsoft1.sistema_comedores.user.token;

import jakarta.validation.constraints.NotNull;

public record TokenDTO(
        @NotNull String accessToken,
        String refreshToken,
        String role
) {
}
