package ar.uba.fi.ingsoft1.sistema_comedores.user.exception;

import ar.uba.fi.ingsoft1.sistema_comedores.common.exception.base.ResourceNotFoundException;

public class TokenNotFoundException extends ResourceNotFoundException {
    public TokenNotFoundException(String message) {
        super(message);
    }
    
}
