package ar.uba.fi.ingsoft1.sistema_comedores.ingredients.exception;

import ar.uba.fi.ingsoft1.sistema_comedores.common.exception.base.BusinessRuleException;

public class IngredientInProductException extends BusinessRuleException {

    private final Long ingredientId;
    private final String ingredientName;

    public IngredientInProductException(Long ingredientId, String ingredientName) {
        super("El ingrediente " + ingredientName + " conforma algún producto");
        this.ingredientId = ingredientId;
        this.ingredientName = ingredientName;
    }

    public Long getIngredientId() {
        return ingredientId;
    }

    public String getIngredientName() {
        return ingredientName;
    }
    
}
