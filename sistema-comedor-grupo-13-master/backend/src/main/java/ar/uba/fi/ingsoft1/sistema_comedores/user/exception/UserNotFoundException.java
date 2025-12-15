package ar.uba.fi.ingsoft1.sistema_comedores.user.exception;

import ar.uba.fi.ingsoft1.sistema_comedores.common.exception.base.ResourceNotFoundException;

public class UserNotFoundException extends ResourceNotFoundException {

    public UserNotFoundException(Long userId) {
        super(String.format("Usuario con id %d no encontrado.", userId));
    }

    public UserNotFoundException(String email) {
        super(String.format("Usuario con email %s no encontrado.", email));
    }
}
