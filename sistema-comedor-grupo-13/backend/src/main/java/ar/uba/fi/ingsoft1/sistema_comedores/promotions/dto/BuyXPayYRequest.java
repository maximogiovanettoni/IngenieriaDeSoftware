package ar.uba.fi.ingsoft1.sistema_comedores.promotions.dto;

import ar.uba.fi.ingsoft1.sistema_comedores.products.ProductCategory;
import ar.uba.fi.ingsoft1.sistema_comedores.promotions.BuyXPayY;
import ar.uba.fi.ingsoft1.sistema_comedores.promotions.DayOfWeek;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

public record BuyXPayYRequest(
        @NotBlank(message = "El nombre de la promoción es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String name,

        @Size(max = 300, message = "La descripción no puede superar los 300 caracteres")
        String description,

        Boolean active,

        LocalDate startDate,

        LocalDate endDate,

        Set<DayOfWeek> applicableDays,

        Set<TimeRangeRequest> applicableHours,

        @NotNull(message = "La categoría es obligatoria")
        ProductCategory category,

        @NotNull(message = "La cantidad requerida es obligatoria")
        @Positive(message = "La cantidad requerida debe ser positiva")
        Integer requiredQuantity,

        @NotNull(message = "La cantidad a pagar es obligatoria")
        @Positive(message = "La cantidad a pagar debe ser positiva")
        Integer chargedQuantity
) implements PromotionRequest {

    public BuyXPayYRequest {
        if (active == null) {
            active = true;
        }
        if (applicableDays == null) {
            applicableDays = Set.of();
        }
        if (applicableHours == null) {
            applicableHours = Set.of();
        }
        if (chargedQuantity >= requiredQuantity) {
            throw new IllegalArgumentException(
                    "La cantidad a pagar debe ser menor que la cantidad requerida"
            );
        }
    }

    public BuyXPayY toEntity() {
        return new BuyXPayY(
                name,
                description,
                active != null ? active : true,
                startDate,
                endDate,
                applicableDays != null ? applicableDays : Set.of(),
                applicableHours != null ? applicableHours.stream()
                        .map(TimeRangeRequest::toEntity)
                        .collect(Collectors.toSet()) : Set.of(),
                category,
                requiredQuantity,
                chargedQuantity
        );
    }
}