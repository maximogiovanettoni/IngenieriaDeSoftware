package ar.uba.fi.ingsoft1.sistema_comedores.orders.status;

import ar.uba.fi.ingsoft1.sistema_comedores.orders.Order;
import ar.uba.fi.ingsoft1.sistema_comedores.orders.exception.IllegalOrderStatusException;
import ar.uba.fi.ingsoft1.sistema_comedores.orders.exception.OrderCannotBeCancelledException;
import ar.uba.fi.ingsoft1.sistema_comedores.orders.exception.OrderCannotBeRejectedException;

public class CancelledOrderStatus implements OrderStatus {

    @Override
    public OrderStatus cancelOrder(Long orderNumber) {
        throw new OrderCannotBeCancelledException(
            orderNumber, 
            this.getStatusCode()
        );
    }

    @Override
    public OrderStatus rejectOrder(Long orderNumber) {
        throw new OrderCannotBeRejectedException(
            orderNumber, 
            this.getStatusCode()
        );
    }

    @Override
    public OrderStatus moveOrderForward(Long orderNumber) {
        throw new IllegalOrderStatusException(
            orderNumber, 
            this.getStatusCode(),
            false
        );
    }

    @Override
    public OrderStatus moveOrderBackward(Long orderNumber) {
        throw new IllegalOrderStatusException(
            orderNumber, 
            this.getStatusCode(),
            true
        );
    }

    @Override
    public String getStatusCode() {
        return "CANCELLED";
    }
    
}
