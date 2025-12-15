package ar.uba.fi.ingsoft1.sistema_comedores.products.elaborate.dto;
import ar.uba.fi.ingsoft1.sistema_comedores.products.ProductCategory;
import ar.uba.fi.ingsoft1.sistema_comedores.products.elaborate.ElaborateProduct;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;

public record CreateElaborateProductRequest(
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

    @NotNull(message = "Los ingredientes son obligatorios")
    @Size(min = 2, message = "El producto debe tener al menos dos ingredientes")
    @Valid
    Map<Long, @Positive(message = "La cantidad debe ser mayor a 0") BigDecimal> ingredients

) {
    public ElaborateProduct toElaborateProduct() {
        return new ElaborateProduct(name, description, price, ProductCategory.fromValue(category), active);
    }
}
