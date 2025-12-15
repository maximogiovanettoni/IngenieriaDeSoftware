package ar.uba.fi.ingsoft1.sistema_comedores.common.exception;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
    int status,
    String message,
    Instant timestamp,
    Map<String, Object> details
) {
    public ErrorResponse(int status, String message, Instant timestamp) {
        this(status, message, timestamp, Map.of());
    }
}