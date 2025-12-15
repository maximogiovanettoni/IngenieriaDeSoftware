package ar.uba.fi.ingsoft1.sistema_comedores.promotions.dto;

import ar.uba.fi.ingsoft1.sistema_comedores.promotions.TimeRange;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record TimeRangeRequest(
        @NotNull(message = "La hora de inicio es obligatoria")
        @JsonFormat(pattern = "HH:mm")
        LocalTime startTime,

        @NotNull(message = "La hora de fin es obligatoria")
        @JsonFormat(pattern = "HH:mm")
        LocalTime endTime
) {
    public TimeRangeRequest {
        if (startTime != null && endTime != null && startTime.equals(endTime)) {
            throw new IllegalArgumentException(
                    "La hora de inicio no puede ser igual a la hora de fin"
            );
        }
    }

    public TimeRange toEntity() {
        return new TimeRange(startTime, endTime);
    }
}