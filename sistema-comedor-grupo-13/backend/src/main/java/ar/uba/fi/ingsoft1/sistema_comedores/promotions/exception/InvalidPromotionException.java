package ar.uba.fi.ingsoft1.sistema_comedores.promotions.exception;

import ar.uba.fi.ingsoft1.sistema_comedores.common.exception.ValidationException;

public class InvalidPromotionException extends ValidationException {
    public InvalidPromotionException(String message) {
        super(message);
    }
}
