package ar.uba.fi.ingsoft1.sistema_comedores.user.exception;

import ar.uba.fi.ingsoft1.sistema_comedores.common.exception.ValidationException;

public class InvalidTokenException extends ValidationException {
    public InvalidTokenException(String message) {
        super(message);
    }
    
}
