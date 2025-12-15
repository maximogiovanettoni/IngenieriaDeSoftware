package ar.uba.fi.ingsoft1.sistema_comedores.orders.status;

import ar.uba.fi.ingsoft1.sistema_comedores.orders.Order;

public class ConfirmedOrderStatus implements OrderStatus {

    @Override
    public OrderStatus cancelOrder(Long orderNumber) {
        return new CancelledOrderStatus();
    }

    @Override
    public OrderStatus rejectOrder(Long orderNumber) {
        return new RejectedOrderStatus();
    }

    @Override
    public OrderStatus moveOrderForward(Long orderNumber) {
        return new PreparingOrderStatus();
    }

    @Override
    public OrderStatus moveOrderBackward(Long orderNumber) {
        return new PendingOrderStatus();
    }

    @Override
    public String getStatusCode() {
        return "CONFIRMED";
    }
    
}
