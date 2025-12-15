package ar.uba.fi.ingsoft1.sistema_comedores.orders.status;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class OrderStatusConverter implements AttributeConverter<OrderStatus, String> {

    @Override
    public String convertToDatabaseColumn(OrderStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getStatusCode();
    }

    @Override
    public OrderStatus convertToEntityAttribute(String dbStatus) {
        if (dbStatus == null) {
            return null;
        }
        
        switch (dbStatus) {
            case "PENDING":
                return new PendingOrderStatus();
            case "CONFIRMED":
                return new ConfirmedOrderStatus();
            case "PREPARING":
                return new PreparingOrderStatus();
            case "READY":
                return new ReadyOrderStatus();
            case "COMPLETED":
                return new CompletedOrderStatus();
            case "CANCELLED":
                return new CancelledOrderStatus();
            case "REJECTED":
                return new RejectedOrderStatus();
            default:
                throw new IllegalArgumentException("Unknown order status: " + dbStatus);
        }
    }
}
