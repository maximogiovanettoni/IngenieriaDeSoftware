package ar.uba.fi.ingsoft1.sistema_comedores.user.exception;

import ar.uba.fi.ingsoft1.sistema_comedores.common.exception.base.ResourceAlreadyExistsException;

public class UserAlreadyExistsException extends ResourceAlreadyExistsException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}