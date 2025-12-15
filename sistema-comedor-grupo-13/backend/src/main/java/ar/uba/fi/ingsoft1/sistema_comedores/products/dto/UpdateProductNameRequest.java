package ar.uba.fi.ingsoft1.sistema_comedores.products.dto;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;

public record UpdateProductNameRequest(
        @NotBlank(message = "El nombre es requerido")
        @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
        String name
) {}