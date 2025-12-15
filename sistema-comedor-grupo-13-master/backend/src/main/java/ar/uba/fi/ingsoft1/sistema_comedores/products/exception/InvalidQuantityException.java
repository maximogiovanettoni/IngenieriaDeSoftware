package ar.uba.fi.ingsoft1.sistema_comedores.products.exception;

import ar.uba.fi.ingsoft1.sistema_comedores.common.exception.ValidationException;

public class InvalidQuantityException extends ValidationException {
    public InvalidQuantityException(String message) {
        super(message);
    }
}