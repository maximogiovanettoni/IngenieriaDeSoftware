package ar.uba.fi.ingsoft1.sistema_comedores.orders.exception;

import ar.uba.fi.ingsoft1.sistema_comedores.common.exception.base.ResourceNotFoundException;
import ar.uba.fi.ingsoft1.sistema_comedores.orders.status.OrderStatus;

public class StatusNeverReachedException extends ResourceNotFoundException {
    public StatusNeverReachedException(Long orderId, OrderStatus status) {
        super(String.format("Orden %d no alcanzó estado: ", orderId, status.getStatusCode()));
    }
}
