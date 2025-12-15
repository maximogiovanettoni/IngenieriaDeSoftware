package ar.uba.fi.ingsoft1.sistema_comedores.promotions.exception;

import ar.uba.fi.ingsoft1.sistema_comedores.common.exception.base.ResourceNotFoundException;

public class PromotionNotFoundException extends ResourceNotFoundException {
    public PromotionNotFoundException(String message) {
        super(message);
    }
    public PromotionNotFoundException(Long id) {
        super("Promoción con ID " + id + " no encontrada");
    }
}
