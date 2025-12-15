package ar.uba.fi.ingsoft1.sistema_comedores.products.elaborate.dto;

import jakarta.validation.constraints.NotNull;

public record RemoveIngredientRequest (
        @NotNull(message = "El producto no puede ser nulo")
        Long productId,
        @NotNull(message = "El ingrediente no puede ser nulo")
        Long ingredientId
) {}
