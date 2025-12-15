package ar.uba.fi.ingsoft1.sistema_comedores.products.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DeleteProductRequest(
    @Size(max = 300, message = "Motivo no puede exceder 300 caracteres")
    String reason
) {}
