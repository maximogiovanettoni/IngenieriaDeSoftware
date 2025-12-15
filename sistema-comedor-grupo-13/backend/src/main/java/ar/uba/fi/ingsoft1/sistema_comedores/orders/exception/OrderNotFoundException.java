package ar.uba.fi.ingsoft1.sistema_comedores.orders.exception;

import ar.uba.fi.ingsoft1.sistema_comedores.common.exception.base.ResourceNotFoundException;

public class OrderNotFoundException extends ResourceNotFoundException {

    public OrderNotFoundException(Long orderId) {
        super(String.format("Orden %d no encontrada", orderId));
    }
    
}
