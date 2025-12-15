package ar.uba.fi.ingsoft1.sistema_comedores.ingredients.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

import ar.uba.fi.ingsoft1.sistema_comedores.ingredients.Ingredient;

public record CreateIngredientRequest (

    @NotBlank(message = "Name is required and must be less than 100 characters")
    @Size(max = 100, message = "Name is required and must be less than 100 characters")
    String name,

    @NotBlank(message = "Unit measure is required")
    @Size(max = 50)
    String unitMeasure,

    @PositiveOrZero(message = "Initial stock cannot be negative")
    BigDecimal stock
) {
    public Ingredient toIngredient() {
        Ingredient ingredient = new Ingredient();
        ingredient.setName(name);
        ingredient.setUnitMeasure(unitMeasure);
        ingredient.setStock(stock);
        return ingredient;
    }
}
