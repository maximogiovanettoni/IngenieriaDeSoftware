package ar.uba.fi.ingsoft1.sistema_comedores.promotions.dto;

import ar.uba.fi.ingsoft1.sistema_comedores.promotions.DayOfWeek;
import ar.uba.fi.ingsoft1.sistema_comedores.promotions.FixedDiscount;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

public record FixedDiscountRequest(
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

        @NotNull(message = "El monto mínimo de compra es obligatorio")
        @PositiveOrZero(message = "El monto mínimo debe ser positivo o cero")
        BigDecimal minimumPurchase,

        @NotNull(message = "El monto de descuento es obligatorio")
        @Positive(message = "El monto de descuento debe ser positivo")
        BigDecimal discountAmount


) implements PromotionRequest {

    public FixedDiscountRequest {
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

    public FixedDiscount toEntity() {
        return new FixedDiscount(
                name,
                description,
                active != null ? active : true,
                startDate,
                endDate,
                applicableDays != null ? applicableDays : Set.of(),
                applicableHours != null ? applicableHours.stream()
                        .map(TimeRangeRequest::toEntity)
                        .collect(Collectors.toSet()) : Set.of(),
                minimumPurchase,
                discountAmount
        );
    }
}