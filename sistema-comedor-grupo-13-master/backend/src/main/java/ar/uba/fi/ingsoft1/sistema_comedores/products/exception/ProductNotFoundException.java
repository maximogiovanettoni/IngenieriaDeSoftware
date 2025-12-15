package ar.uba.fi.ingsoft1.sistema_comedores.products.exception;

import ar.uba.fi.ingsoft1.sistema_comedores.common.exception.base.ResourceNotFoundException;

public class ProductNotFoundException extends ResourceNotFoundException {

    public ProductNotFoundException(Long requestedId) { 
        super("Producto con id " + requestedId + " no encontrado"); 
    }

    public ProductNotFoundException(String requestedName) { 
        super("Producto con nombre " + requestedName + " no encontrado"); 
    }

}
