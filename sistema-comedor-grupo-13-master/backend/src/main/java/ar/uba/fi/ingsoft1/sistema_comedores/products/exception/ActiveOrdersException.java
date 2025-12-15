package ar.uba.fi.ingsoft1.sistema_comedores.products.exception;

import ar.uba.fi.ingsoft1.sistema_comedores.common.exception.base.BusinessRuleException;

public class ActiveOrdersException extends BusinessRuleException {
    public ActiveOrdersException(String message) {
        super(message);
    }
}
