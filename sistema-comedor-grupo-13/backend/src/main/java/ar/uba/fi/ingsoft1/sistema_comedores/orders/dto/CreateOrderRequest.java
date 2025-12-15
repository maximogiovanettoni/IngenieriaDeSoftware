package ar.uba.fi.ingsoft1.sistema_comedores.orders.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

public record CreateOrderRequest(
    @NotEmpty(message = "El pedido debe tener al menos un ítem")
    List<OrderItemRequest> items,
    Long promotionId
) {}
