package ar.uba.fi.ingsoft1.sistema_comedores.products.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import ar.uba.fi.ingsoft1.sistema_comedores.ingredients.Ingredient;
import ar.uba.fi.ingsoft1.sistema_comedores.products.Product;

public record ProductComponentResponse (
    Long id,
    String name,
    String type, // "INGREDIENT" or "PRODUCT"
    @JsonSerialize(using = ToStringSerializer.class)
    BigDecimal quantity
) {
    public static ProductComponentResponse fromProduct(Product product, BigDecimal quantity) {
        return new ProductComponentResponse(
            product.getId(),
            product.getName(),
            product.getProductType().getValue(),
            quantity
        );
    }

    public static ProductComponentResponse fromIngredient(Ingredient ingredient, BigDecimal quantity) {
        return new ProductComponentResponse(
            ingredient.getId(),
            ingredient.getName(),
            "INGREDIENT",
            quantity
        );
    }
}