package ar.uba.fi.ingsoft1.sistema_comedores.promotions;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TimeRange {

    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime startTime;

    @NotNull(message = "La hora de fin es obligatoria")
    private LocalTime endTime;

    public boolean contains(LocalTime time) {
        if (time == null) {
            return false;
        }
        if (startTime.isBefore(endTime)) {
            return !time.isBefore(startTime) && !time.isAfter(endTime);
        }
        else if (startTime.isAfter(endTime)) {
            return !time.isBefore(startTime) || !time.isAfter(endTime);
        }
        else {
            return true;
        }
    }

    public void validate() {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Las horas de inicio y fin son obligatorias");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeRange timeRange = (TimeRange) o;
        return Objects.equals(startTime, timeRange.startTime) &&
                Objects.equals(endTime, timeRange.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startTime, endTime);
    }

    @Override
    public String toString() {
        return String.format("%s - %s", startTime, endTime);
    }
}