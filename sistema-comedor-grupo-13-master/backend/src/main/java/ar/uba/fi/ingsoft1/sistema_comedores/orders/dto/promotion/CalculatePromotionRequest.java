package ar.uba.fi.ingsoft1.sistema_comedores.orders.dto.promotion;

import java.util.List;

import ar.uba.fi.ingsoft1.sistema_comedores.orders.dto.OrderItemRequest;

public record CalculatePromotionRequest(
    List<OrderItemRequest> items
) {}
