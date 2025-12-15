package ar.uba.fi.ingsoft1.sistema_comedores.orders.status;

import ar.uba.fi.ingsoft1.sistema_comedores.orders.Order;

public interface OrderStatus {
    OrderStatus cancelOrder(Long orderNumber);
    OrderStatus rejectOrder(Long orderNumber);
    OrderStatus moveOrderForward(Long orderNumber);
    OrderStatus moveOrderBackward(Long orderNumber);
    String getStatusCode();
}
