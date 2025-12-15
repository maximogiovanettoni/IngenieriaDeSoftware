package ar.uba.fi.ingsoft1.sistema_comedores.user.profile;

import java.time.LocalDate;

public record UserProfileDTO(
    String firstName,
    String lastName,
    String email,
    boolean emailVerified,
    LocalDate birthDate,
    String gender,
    String address,
    String profileImage
) {}
