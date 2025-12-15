package ar.uba.fi.ingsoft1.sistema_comedores.promotions.exception;

import ar.uba.fi.ingsoft1.sistema_comedores.common.exception.base.ResourceAlreadyExistsException;

public class PromotionAlreadyExistsException extends ResourceAlreadyExistsException {
    public PromotionAlreadyExistsException(String message) { super(message); }
    public PromotionAlreadyExistsException(String name, String field) {
        super("Ya existe una promoción con " + field + ": " + name);
    }
}
