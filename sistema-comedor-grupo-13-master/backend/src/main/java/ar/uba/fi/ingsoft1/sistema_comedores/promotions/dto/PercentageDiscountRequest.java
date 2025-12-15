package ar.uba.fi.ingsoft1.sistema_comedores.promotions.dto;

import ar.uba.fi.ingsoft1.sistema_comedores.products.ProductCategory;
import ar.uba.fi.ingsoft1.sistema_comedores.promotions.DayOfWeek;
import ar.uba.fi.ingsoft1.sistema_comedores.promotions.PercentageDiscount;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

public record PercentageDiscountRequest(
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

        @NotNull(message = "El porcentaje de descuento es obligatorio")
        @DecimalMin(value = "0.0", message = "El porcentaje de descuento debe ser mayor o igual a 0")
        @DecimalMax(value = "100.0", message = "El porcentaje de descuento no puede superar el 100%")
        Integer discount
) implements PromotionRequest {
    public PercentageDiscountRequest {
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

    public PercentageDiscount toEntity() {
        return new PercentageDiscount(
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
                discount
        );
    }
}