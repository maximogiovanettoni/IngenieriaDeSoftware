package ar.uba.fi.ingsoft1.sistema_comedores.products.dto;

import ar.uba.fi.ingsoft1.sistema_comedores.products.Product;

import java.math.BigDecimal;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public record ProductDetailsResponse(
    Long id,
    String name,
    String description,
    @JsonSerialize(using = ToStringSerializer.class)
    BigDecimal price,
    String type, // "SIMPLE", "ELABORATE", "COMBO"
    String category, // "SANDWICH", "BEBIDA", ETC
    Integer stock,
    Boolean active,
    Boolean available,
    String imageUrl    
) {
    public ProductDetailsResponse(Product product) {
        this(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getProductType() != null ? product.getProductType().name() : "UNKNOWN",
            product.getCategory() != null ? product.getCategory().name() : "UNKNOWN",
            product.getStock(),
            product.getActive(),
            product.getAvailable(),
            product.getImageUrl()
        );
    }
}