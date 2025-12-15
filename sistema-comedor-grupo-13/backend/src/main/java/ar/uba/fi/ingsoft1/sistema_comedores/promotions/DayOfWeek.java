
package ar.uba.fi.ingsoft1.sistema_comedores.promotions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DayOfWeek {
    MONDAY("Lunes"),
    TUESDAY("Martes"),
    WEDNESDAY("Miércoles"),
    THURSDAY("Jueves"),
    FRIDAY("Viernes"),
    SATURDAY("Sábado"),
    SUNDAY("Domingo");

    private final String value;

    DayOfWeek(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static DayOfWeek fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Invalid day of week value: null");
        }
        
        try {
            return DayOfWeek.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            String normalized = normalize(value);
            for (DayOfWeek day : DayOfWeek.values()) {
                if (normalize(day.value).equalsIgnoreCase(normalized)) {
                    return day;
                }
            }
        }
        throw new IllegalArgumentException("Invalid day of week value: " + value);
    }

    @Override
    public String toString() {
        return value;
    }

    private static String normalize(String s) {
        if (s == null) return "";
        return java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").trim().toLowerCase();
    }
}