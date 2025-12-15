package ar.uba.fi.ingsoft1.sistema_comedores.products.exception;

import ar.uba.fi.ingsoft1.sistema_comedores.common.exception.base.BusinessRuleException;

public class ProductNotAvailableException extends BusinessRuleException {
    private final String productName;
    
    public ProductNotAvailableException(String productName) { 
        super("Producto no disponible: " + productName);
        this.productName = productName;
    }
    
    public String getProductName() {
        return productName;
    }
}
