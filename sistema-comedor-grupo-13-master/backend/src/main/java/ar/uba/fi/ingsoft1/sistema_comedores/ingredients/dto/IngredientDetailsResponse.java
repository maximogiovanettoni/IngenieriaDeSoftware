package ar.uba.fi.ingsoft1.sistema_comedores.ingredients.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import ar.uba.fi.ingsoft1.sistema_comedores.ingredients.Ingredient;

public record IngredientDetailsResponse (
    Long id,
    String name,
    String unitMeasure,
    @JsonSerialize(using = ToStringSerializer.class)
    BigDecimal stock,
    boolean available,
    boolean active
) {
    public static IngredientDetailsResponse from(Ingredient ingredient) {
        return new IngredientDetailsResponse(
            ingredient.getId(),
            ingredient.getName(),
            ingredient.getUnitMeasure(),
            ingredient.getStock(),
            ingredient.isAvailable(),
            ingredient.isActive()
        );
    }
}
