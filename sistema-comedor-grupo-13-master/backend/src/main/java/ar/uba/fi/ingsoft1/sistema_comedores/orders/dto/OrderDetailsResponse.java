package ar.uba.fi.ingsoft1.sistema_comedores.orders.dto;

import ar.uba.fi.ingsoft1.sistema_comedores.orders.Order;
import ar.uba.fi.ingsoft1.sistema_comedores.orders.dto.promotion.AppliedPromotionResponse;
import ar.uba.fi.ingsoft1.sistema_comedores.orders.AppliedPromotion;
import ar.uba.fi.ingsoft1.sistema_comedores.promotions.Promotion;
import ar.uba.fi.ingsoft1.sistema_comedores.user.UserInformation;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderDetailsResponse(
    Long id,
    Long orderNumber,
    Long userId,
    String status,
    
    @JsonSerialize(using = ToStringSerializer.class)
    BigDecimal subtotal,
    
    @JsonSerialize(using = ToStringSerializer.class)
    BigDecimal discountAmount,

    @JsonProperty("appliedPromotions")
    List<AppliedPromotionResponse> appliedPromotions,    
    
    @JsonSerialize(using = ToStringSerializer.class)
    BigDecimal totalAmount,
    
    List<OrderItemResponse> items,
    Instant createdAt,
    Instant updatedAt,
    UserInformation user
) {
    public static OrderDetailsResponse from(Order order) {
        final BigDecimal discount = order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO;
        final BigDecimal totalAmount = order.getTotalAmount() != null ? order.getTotalAmount() : order.getSubtotal();
        List<AppliedPromotion> appliedPromotions = order.getAppliedPromotions();
        
        return new OrderDetailsResponse(
            order.getOrderNumber(),
            order.getOrderNumber(),
            order.getUserId(),
            order.getStatus().getStatusCode(),
            order.getSubtotal(),
            discount,
            appliedPromotions.stream()
                .map(AppliedPromotionResponse::from)
                .toList(),
            totalAmount,
            order.getItems().stream()
                .map(OrderItemResponse::from)
                .toList(),
            order.getCreatedAt(),
            order.getUpdatedAt(),
            null
        );
    }
    
    public OrderDetailsResponse withUser(UserInformation user) {
        return new OrderDetailsResponse(
            id,
            orderNumber,
            userId,
            status,
            subtotal,
            discountAmount,
            appliedPromotions,
            totalAmount,
            items,
            createdAt,
            updatedAt,
            user
        );
    }
}