package ar.uba.fi.ingsoft1.sistema_comedores.products.simple.dto;

import ar.uba.fi.ingsoft1.sistema_comedores.products.simple.SimpleProduct;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

import org.springframework.web.multipart.MultipartFile;

import ar.uba.fi.ingsoft1.sistema_comedores.products.ProductCategory;

public record CreateSimpleProductRequest(
    @NotBlank(message = "Nombre es obligatorio")
    @Size(max = 100, message = "Nombre no puede exceder 100 caracteres")
    String name,

    @Size(max = 300, message = "Descripción no puede exceder 300 caracteres")
    String description,

    @NotNull(message = "Precio es obligatorio")
    @Positive(message = "Precio debe ser positivo")
    BigDecimal price,
    
    @NotNull(message = "Se debe definir si el producto está activo o no")
    Boolean active,

    @NotNull(message = "Categoría es obligatoria")
    String category,

    @PositiveOrZero(message = "Stock no puede ser negativo")
    Integer stock
) {
    public SimpleProduct toSimpleProduct() {
        return new SimpleProduct(name, description, price, ProductCategory.fromValue(category), active, stock);
    }
}

