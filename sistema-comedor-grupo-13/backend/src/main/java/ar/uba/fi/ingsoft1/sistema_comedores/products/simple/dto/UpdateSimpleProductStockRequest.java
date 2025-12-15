package ar.uba.fi.ingsoft1.sistema_comedores.products.simple.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateSimpleProductStockRequest(
        @NotNull(message = "La cantidad no puede ser nula")
        @Min(value = 0, message = "La cantidad debe ser mayor o igual a 0")
        Integer stock
) {}


