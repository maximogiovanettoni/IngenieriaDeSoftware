package ar.uba.fi.ingsoft1.sistema_comedores.orders.status;

import ar.uba.fi.ingsoft1.sistema_comedores.orders.Order;
import ar.uba.fi.ingsoft1.sistema_comedores.orders.exception.IllegalOrderStatusException;
import ar.uba.fi.ingsoft1.sistema_comedores.orders.exception.OrderCannotBeCancelledException;
import ar.uba.fi.ingsoft1.sistema_comedores.orders.exception.OrderCannotBeRejectedException;

public class PreparingOrderStatus implements OrderStatus {

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
        return new ReadyOrderStatus();
    }

    @Override
    public OrderStatus moveOrderBackward(Long orderNumber) {
        return new ConfirmedOrderStatus();
    }

    @Override
    public String getStatusCode() {
        return "PREPARING";
    }
    
}
