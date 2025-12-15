package ar.uba.fi.ingsoft1.sistema_comedores.orders.dto;

public record OrderItemRequest(
    Long productId,
    Integer quantity
) {}
