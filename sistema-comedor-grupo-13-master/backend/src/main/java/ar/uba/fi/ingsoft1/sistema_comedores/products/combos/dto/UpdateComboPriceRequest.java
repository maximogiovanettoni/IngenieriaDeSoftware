package ar.uba.fi.ingsoft1.sistema_comedores.products.combos.dto;

import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record UpdateComboPriceRequest(
        @Positive(message = "El precio debe ser positivo")
        BigDecimal price
) {
}
