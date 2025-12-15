package ar.uba.fi.ingsoft1.sistema_comedores.orders.notifications;

public record OrderStatusDTO(
        Long orderNumber,
        String newStatus,
        String email
) {}
