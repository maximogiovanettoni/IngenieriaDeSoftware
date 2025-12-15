package ar.uba.fi.ingsoft1.sistema_comedores.products.combos.exception;

import ar.uba.fi.ingsoft1.sistema_comedores.common.exception.base.ResourceNotFoundException;

public class ComboNotFoundException extends ResourceNotFoundException {

    private final Long requestedId;

    public ComboNotFoundException(Long requestedId) { 
        super("Combo con id " + requestedId + " no encontrado"); 
        this.requestedId = requestedId;
    }
    
    public Long getRequestedId() {
        return requestedId;
    }
    
}
