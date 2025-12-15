package ar.uba.fi.ingsoft1.sistema_comedores.products.elaborate;

import ar.uba.fi.ingsoft1.sistema_comedores.products.Product;
import ar.uba.fi.ingsoft1.sistema_comedores.products.ProductCategory;
import ar.uba.fi.ingsoft1.sistema_comedores.products.ProductType;
import ar.uba.fi.ingsoft1.sistema_comedores.products.exception.InsufficientStockException;
import ar.uba.fi.ingsoft1.sistema_comedores.ingredients.Ingredient;
import ar.uba.fi.ingsoft1.sistema_comedores.ingredients.IngredientObserver;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.Column;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyJoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("ELABORATE")
@Getter
@Setter
@NoArgsConstructor
public class ElaborateProduct extends Product {

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductIngredient> productIngredients = new ArrayList<>();

    public ElaborateProduct(String name, String description, BigDecimal price, ProductCategory category, Boolean active) {
        super(name, description, price, category, active);
        stock = calculateStock();
        available = stock > 0 && this.active;
        productType = ProductType.ELABORATE;
    }

    public List<ProductIngredient> getProductIngredients() {
        return productIngredients;
    }
    
    public Map<Ingredient, BigDecimal> getIngredients() {
        return productIngredients.stream()
            .collect(Collectors.toMap(
                ProductIngredient::getIngredient,
                ProductIngredient::getQuantity
            ));
    }
    
    public void addIngredient(Ingredient ingredient, BigDecimal quantity) {
        Optional<ProductIngredient> existing = productIngredients.stream()
            .filter(pi -> pi.getIngredient().getId().equals(ingredient.getId()))
            .findFirst();
        
        if (existing.isPresent()) {
            existing.get().setQuantity(quantity);
        } else {
            ProductIngredient productIngredient = new ProductIngredient(this, ingredient, quantity);
            productIngredients.add(productIngredient);
            ingredient.addObserver(new ProductIngredientObserver(ingredient, this));
        }
        onIngredientStockChange();
    }
    
    public void removeIngredient(Ingredient ingredient) {
        productIngredients.removeIf(pi -> 
            pi.getIngredient().getId().equals(ingredient.getId())
        );
        ingredient.removeObserver(this.getId());
        onIngredientStockChange();
    }
    
    public boolean hasIngredient(Ingredient ingredient) {
        return productIngredients.stream()
            .anyMatch(pi -> pi.getIngredient().getId().equals(ingredient.getId()));
    }

    private List<Ingredient> getIngredientsMissingStock(Integer requiredStock) {
        List<Ingredient> ingredientsMissingStock = new LinkedList<Ingredient>();
        for (ProductIngredient productIngredient : productIngredients) {
            BigDecimal totalRequiredQuantity = productIngredient.getQuantity().multiply(BigDecimal.valueOf(requiredStock));
            Ingredient ingredient = productIngredient.getIngredient();
            if (ingredient.getStock().compareTo(totalRequiredQuantity) < 0) {
                ingredientsMissingStock.add(ingredient);
            }
        }
        return ingredientsMissingStock;
    }

    @Override
    public void consumeStock(Integer usedStock) throws IllegalArgumentException {
        if (usedStock == null) return;
        if (stock < usedStock) {
            throw InsufficientStockException.forMissingIngredients(this.getName(), this.getStock(), usedStock, this.getIngredientsMissingStock(usedStock));
        }
        for (ProductIngredient productIngredient : productIngredients) {
            BigDecimal totalUsedQuantity = productIngredient.getQuantity().multiply(BigDecimal.valueOf(usedStock));
            productIngredient.getIngredient().consumeStock(totalUsedQuantity);
        }
        stock = calculateStock();
        available = stock > 0 && active;
        this.notifyObserversStockChange();
    }

    @Override 
    public void handleDeactivation() {
        for (ProductIngredient productIngredient : productIngredients) {
            Ingredient ingredient = productIngredient.getIngredient();
            ingredient.removeObserver(getId());
        }
        active = available = false;
        this.notifyObserversStatusChange();
    }

    @Override
    public void handleRestoration() {
        for (ProductIngredient productIngredient : productIngredients) {
            Ingredient ingredient = productIngredient.getIngredient();
            ingredient.addObserver(new ProductIngredientObserver(ingredient, this));
        }
        active = true;
        this.stock = calculateStock();
        this.available = stock > 0 && active;
        this.notifyObserversStatusChange();
    }

    public void onIngredientStockChange() {
        this.stock = calculateStock();
        this.available = stock > 0 && active;
        this.notifyObserversStockChange();
    }

    public void onIngredientStatusChange() {
        this.stock = calculateStock();
        this.available = stock > 0 && active;
        this.notifyObserversStatusChange();
    }

    @Override
    public void restoreStock(Integer quantity) {
        BigDecimal bigQuantity = new BigDecimal(quantity);
        for (ProductIngredient pi : productIngredients) {
            Ingredient ingredient = pi.getIngredient();
            BigDecimal quantityToRestore = pi.getQuantity().multiply(bigQuantity);
            BigDecimal newStock = ingredient.getStock().add(quantityToRestore);
            ingredient.updateStockAndNotify(newStock);
        }
    }

    public int calculateStock() {
        if (productIngredients.isEmpty()) {
            return 0;
        }
        int availableStock = Integer.MAX_VALUE;
        for (ProductIngredient productIngredient : productIngredients) {
            Ingredient ingredient = productIngredient.getIngredient();
            BigDecimal quantityRequired = productIngredient.getQuantity();

            if (!ingredient.isAvailable()) {
                return 0;
            }

            BigDecimal ingredientStock = ingredient.getStock() != null ? ingredient.getStock() :
                    BigDecimal.ZERO;
            int possibleUnits = ingredientStock.divide(quantityRequired, RoundingMode.DOWN).intValue();
            availableStock = Math.min(availableStock, possibleUnits);
        }
        return availableStock;
    }
}
