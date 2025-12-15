package ar.uba.fi.ingsoft1.sistema_comedores.user.exception;

import ar.uba.fi.ingsoft1.sistema_comedores.common.exception.ValidationException;

public class InvalidCredentialsException extends ValidationException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}