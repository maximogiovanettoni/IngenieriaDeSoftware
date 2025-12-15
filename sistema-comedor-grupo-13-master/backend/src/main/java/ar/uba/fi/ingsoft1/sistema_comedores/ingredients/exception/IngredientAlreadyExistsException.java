package ar.uba.fi.ingsoft1.sistema_comedores.ingredients.exception;

import ar.uba.fi.ingsoft1.sistema_comedores.common.exception.base.ResourceAlreadyExistsException;

public class IngredientAlreadyExistsException extends ResourceAlreadyExistsException {
    public IngredientAlreadyExistsException(String name) {
        super("Ya existe un ingrediente con el nombre: " + name);
    }
}
