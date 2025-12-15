package ar.uba.fi.ingsoft1.sistema_comedores.orders.exception;

import ar.uba.fi.ingsoft1.sistema_comedores.common.exception.base.BusinessRuleException;

public class OrderCannotBeRejectedException extends BusinessRuleException {

    public OrderCannotBeRejectedException(Long orderId, String status) {
        super("La orden " + orderId + " no puede ser cancelada. Estado actual: " + status);
    }
    
}
