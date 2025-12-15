package ar.uba.fi.ingsoft1.sistema_comedores.ingredients.exception;

import ar.uba.fi.ingsoft1.sistema_comedores.common.exception.base.ResourceNotFoundException;

public class IngredientNotFoundException extends ResourceNotFoundException {
    public IngredientNotFoundException(Long id) {
        super("No se encontró un ingrediente con id " + id);
    }

    public IngredientNotFoundException(String name) {
        super("No se encontró un ingrediente con nombre " + name);
    }
}
