package ar.uba.fi.ingsoft1.sistema_comedores.orders.exception;

import ar.uba.fi.ingsoft1.sistema_comedores.common.exception.base.BusinessRuleException;

public class IllegalOrderStatusException extends BusinessRuleException {

    public IllegalOrderStatusException(Long orderId, String actualStatus, boolean isBackward) {
        super("La orden " + orderId + " no se puede mover hacia " + ((isBackward)? "atrás" : "adelante") + " desde el estado " + actualStatus);
    }
    
}
