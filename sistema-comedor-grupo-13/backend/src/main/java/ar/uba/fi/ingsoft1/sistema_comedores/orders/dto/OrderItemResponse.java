package ar.uba.fi.ingsoft1.sistema_comedores.orders.dto;

import java.math.BigDecimal;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import ar.uba.fi.ingsoft1.sistema_comedores.orders.OrderItem;

public record OrderItemResponse(
    Long productId,
    String productName,
    
    @JsonSerialize(using = ToStringSerializer.class)
    BigDecimal unitPrice,
    
    Integer quantity,
    
    @JsonSerialize(using = ToStringSerializer.class)
    BigDecimal subtotal
) {
    public static OrderItemResponse from(OrderItem orderItem) {
        return new OrderItemResponse(
                orderItem.getProductId(),
                orderItem.getProductName(),
                orderItem.getUnitPrice(),
                orderItem.getQuantity(),
                orderItem.getSubtotal()
        );
    }
}