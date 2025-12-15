package ar.uba.fi.ingsoft1.sistema_comedores.promotions.dto;

import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Set;
import ar.uba.fi.ingsoft1.sistema_comedores.promotions.DayOfWeek;

public record UpdatePromotionRequest(
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String name,

        @Size(max = 300, message = "La descripción no puede superar los 300 caracteres")
        String description,

        Boolean active,

        LocalDate startDate,

        LocalDate endDate,

        Set<DayOfWeek> applicableDays,

        Set<TimeRangeRequest> applicableHours
) {
}