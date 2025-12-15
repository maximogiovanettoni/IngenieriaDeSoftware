package ar.uba.fi.ingsoft1.sistema_comedores.orders.status;

import ar.uba.fi.ingsoft1.sistema_comedores.orders.Order;
import ar.uba.fi.ingsoft1.sistema_comedores.orders.exception.IllegalOrderStatusException;
import ar.uba.fi.ingsoft1.sistema_comedores.orders.exception.OrderCannotBeCancelledException;
import ar.uba.fi.ingsoft1.sistema_comedores.orders.exception.OrderCannotBeRejectedException;

public class ReadyOrderStatus implements OrderStatus {

    @Override
    public OrderStatus cancelOrder(Long orderNumber) {
        throw new OrderCannotBeCancelledException(
            orderNumber, 
            this.getStatusCode()
        );
    }

    @Override
    public OrderStatus rejectOrder(Long orderNumber) {
        return new RejectedOrderStatus();
    }

    @Override
    public OrderStatus moveOrderForward(Long orderNumber) {
        return new CompletedOrderStatus();
    }

    @Override
    public OrderStatus moveOrderBackward(Long orderNumber) {
        return new PreparingOrderStatus();
    }

    @Override
    public String getStatusCode() {
        return "READY";
    }
    
}
