package ar.uba.fi.ingsoft1.sistema_comedores.products.combos.exception;

import ar.uba.fi.ingsoft1.sistema_comedores.common.exception.base.ResourceAlreadyExistsException;

public class ComboAlreadyExistsException extends ResourceAlreadyExistsException {

    private final String comboName;

    public ComboAlreadyExistsException(String comboName) { 
        super("Ya existe un combo con el nombre: " + comboName);
        this.comboName = comboName;
    }
    
    public String getComboName() {
        return comboName;
    }
    
}
