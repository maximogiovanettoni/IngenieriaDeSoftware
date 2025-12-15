package ar.uba.fi.ingsoft1.sistema_comedores.promotions.dto;

import ar.uba.fi.ingsoft1.sistema_comedores.products.ProductCategory;
import ar.uba.fi.ingsoft1.sistema_comedores.promotions.BuyXGetY;
import ar.uba.fi.ingsoft1.sistema_comedores.promotions.DayOfWeek;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

public record BuyXGetYRequest(
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

        @NotNull(message = "La categoría del producto requerido es obligatoria")
        ProductCategory requiredProductCategory,

        @NotNull(message = "La categoría del producto gratis es obligatoria")
        ProductCategory freeProductCategory,

        @NotNull(message = "La cantidad requerida es obligatoria")
        @Positive(message = "La cantidad requerida debe ser positiva")
        Integer requiredQuantity,

        @NotNull(message = "La cantidad gratis es obligatoria")
        @Positive(message = "La cantidad gratis debe ser positiva")
        Integer freeQuantity
) implements PromotionRequest {

    public BuyXGetYRequest {
        if (active == null) {
            active = true;
        }
        if (applicableDays == null) {
            applicableDays = Set.of();
        }
        if (applicableHours == null) {
            applicableHours = Set.of();
        }
    }

    public BuyXGetY toEntity() {
        return new BuyXGetY(
                name,
                description,
                active != null ? active : true,
                startDate,
                endDate,
                applicableDays != null ? applicableDays : Set.of(),
                applicableHours != null ? applicableHours.stream()
                        .map(TimeRangeRequest::toEntity)
                        .collect(Collectors.toSet()) : Set.of(),
                requiredProductCategory,
                freeProductCategory,
                requiredQuantity,
                freeQuantity
        );
    }
}