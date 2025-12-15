package ar.uba.fi.ingsoft1.sistema_comedores.products.elaborate.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record AddIngredientRequest(
    @NotNull(message = "El producto no puede ser nulo")
    Long productId,
    @NotNull(message = "El ingrediente no puede ser nulo")
    Long ingredientId,
    @NotNull(message = "El ingrediente debe tener una cantidad")
    @Positive
    BigDecimal quantity
) {}
