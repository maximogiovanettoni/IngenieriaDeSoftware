package ar.uba.fi.ingsoft1.sistema_comedores.user.exception;

import ar.uba.fi.ingsoft1.sistema_comedores.common.exception.base.BusinessRuleException;

public class TokenExpiredException extends BusinessRuleException {
    public TokenExpiredException(String message) {
        super(message);
    }
    
}
