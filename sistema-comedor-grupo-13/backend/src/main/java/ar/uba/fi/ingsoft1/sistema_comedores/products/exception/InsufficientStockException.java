package ar.uba.fi.ingsoft1.sistema_comedores.products.exception;

import java.util.List;
import java.util.stream.Collectors;

import ar.uba.fi.ingsoft1.sistema_comedores.common.exception.base.BusinessRuleException;
import ar.uba.fi.ingsoft1.sistema_comedores.ingredients.Ingredient;
import ar.uba.fi.ingsoft1.sistema_comedores.products.Product;

public class InsufficientStockException extends BusinessRuleException {
    private final String productName;
    private final int availableStock;
    private final int requestedStock;
    private final String missingItems;
    
    private InsufficientStockException(String message, String productName, 
                                       int availableStock, int requestedStock, 
                                       String missingItems) {
        super(message);
        this.productName = productName;
        this.availableStock = availableStock;
        this.requestedStock = requestedStock;
        this.missingItems = missingItems;
    }
    
    // Getters
    public String getProductName() {
        return productName;
    }
    
    public int getAvailableStock() {
        return availableStock;
    }
    
    public int getRequestedStock() {
        return requestedStock;
    }
    
    public String getMissingItems() {
        return missingItems != null ? missingItems : "";
    }
    
    // Factory method for ingredients
    public static InsufficientStockException forMissingIngredients(
            String productName, int availableStock, 
            int requestedStock, List<Ingredient> missingIngredients) {
        String missing = missingIngredients.stream()
            .map(Ingredient::getName)
            .collect(Collectors.joining(", "));
        
        String message = String.format(
            "Stock insuficiente para el producto '%s'. Disponible: %d, Solicitado: %d. Ingredientes faltantes: %s",
            productName, availableStock, requestedStock, missing);
        
        return new InsufficientStockException(message, productName, availableStock, 
                                             requestedStock, missing);
    }
    
    // Factory method for products (combos)
    public static InsufficientStockException forMissingProducts(
            String productName, int availableStock, 
            int requestedStock, List<Product> missingProducts) {
        String missing = missingProducts.stream()
            .map(Product::getName)
            .collect(Collectors.joining(", "));
        
        String message = String.format(
            "Stock insuficiente para el combo '%s'. Disponible: %d, Solicitado: %d. Productos faltantes: %s",
            productName, availableStock, requestedStock, missing);
        
        return new InsufficientStockException(message, productName, availableStock, 
                                             requestedStock, missing);
    }
    
    // Factory method for simple products
    public static InsufficientStockException forSimpleProduct(
            String productName, int availableStock, int requestedStock) {
        String message = String.format(
            "Stock insuficiente para el producto '%s'. Disponible: %d, Solicitado: %d",
            productName, availableStock, requestedStock);
        
        return new InsufficientStockException(message, productName, availableStock, 
                                             requestedStock, null);
    }
}