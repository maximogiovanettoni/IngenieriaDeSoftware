package ar.uba.fi.ingsoft1.sistema_comedores.products.dto;

import ar.uba.fi.ingsoft1.sistema_comedores.products.ProductCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record SearchProductRequest(
        ProductCategory category,
        
        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal minPrice,
        
        @Positive
        BigDecimal maxPrice,
        
        Boolean available,
        Boolean active
) {
    public SearchProductRequest {
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("minPrice cannot be greater than maxPrice");
        }
    }
}