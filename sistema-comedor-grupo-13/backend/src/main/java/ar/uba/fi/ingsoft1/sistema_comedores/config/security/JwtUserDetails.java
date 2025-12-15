package ar.uba.fi.ingsoft1.sistema_comedores.config.security;

public record JwtUserDetails (
        String username,
        String role
) {}