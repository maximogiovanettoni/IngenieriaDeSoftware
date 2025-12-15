package ar.uba.fi.ingsoft1.sistema_comedores.image.exception;

import ar.uba.fi.ingsoft1.sistema_comedores.common.exception.ValidationException;

public class InvalidImageException extends ValidationException {
    public InvalidImageException(String message) {
        super(message);
    }
}