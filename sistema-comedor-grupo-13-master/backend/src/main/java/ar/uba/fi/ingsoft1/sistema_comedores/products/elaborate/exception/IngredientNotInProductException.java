package ar.uba.fi.ingsoft1.sistema_comedores.products.elaborate.exception;

import ar.uba.fi.ingsoft1.sistema_comedores.common.exception.base.BusinessRuleException;

public class IngredientNotInProductException extends BusinessRuleException {
    private final Long productId;
    private final String productName;
    private final Long ingredientId;
    private final String ingredientName;
    
    public IngredientNotInProductException(Long productId, String productName, Long ingredientId, String ingredientName) {
        super(String.format("El producto '%s' no está compuesto por el ingrediente '%s'", 
            productName, ingredientName));
        this.productId = productId;
        this.productName = productName;
        this.ingredientId = ingredientId;
        this.ingredientName = ingredientName;
    }
    
    public IngredientNotInProductException(String productName, String ingredientName) {
        this(null, productName, null, ingredientName);
    }
    
    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public Long getIngredientId() { return ingredientId; }
    public String getIngredientName() { return ingredientName; }
}