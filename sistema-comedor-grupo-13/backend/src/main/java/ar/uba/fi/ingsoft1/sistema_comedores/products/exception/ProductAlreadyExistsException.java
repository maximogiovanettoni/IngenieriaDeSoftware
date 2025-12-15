package ar.uba.fi.ingsoft1.sistema_comedores.products.exception;

import ar.uba.fi.ingsoft1.sistema_comedores.common.exception.base.ResourceAlreadyExistsException;

public class ProductAlreadyExistsException extends ResourceAlreadyExistsException {

    private final String productName;

    public ProductAlreadyExistsException(String productName) { 
        super("Ya existe un producto con el nombre: " + productName);
        this.productName = productName;
    }
    
    public String getProductName() {
        return productName;
    }
}