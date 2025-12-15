package ar.uba.fi.ingsoft1.sistema_comedores.user.admin;

import jakarta.validation.constraints.Size;

public record DeleteStaffDTO(
        @Size(max = 500, message = "La razón no puede exceder 500 caracteres")
        String reason
) {}
