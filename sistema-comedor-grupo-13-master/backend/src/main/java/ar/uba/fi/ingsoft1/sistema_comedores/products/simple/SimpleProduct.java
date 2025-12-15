package ar.uba.fi.ingsoft1.sistema_comedores.products.simple;

import ar.uba.fi.ingsoft1.sistema_comedores.products.Product;
import ar.uba.fi.ingsoft1.sistema_comedores.products.ProductCategory;
import ar.uba.fi.ingsoft1.sistema_comedores.products.ProductType;
import ar.uba.fi.ingsoft1.sistema_comedores.products.exception.InsufficientStockException;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.DiscriminatorValue;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("SIMPLE")
@Getter
@Setter
@NoArgsConstructor
public class SimpleProduct extends Product {

    public SimpleProduct(String name, String description, BigDecimal price, ProductCategory category, Boolean active, Integer stock) {
        super(name, description, price, category, active);
        this.stock = stock;
        available = this.stock != null && this.stock > 0 && this.active;
        productType = ProductType.SIMPLE;
    }

    @Override
    public void consumeStock(Integer usedStock) throws IllegalArgumentException {
        if (usedStock == null) return;
        if (usedStock > stock) {
            throw InsufficientStockException.forSimpleProduct(this.getName(), this.getStock(), usedStock);
        }
        stock = stock - usedStock;
        available = stock > 0 && active;
        this.notifyObserversStockChange();
    }

    @Override
    public void handleDeactivation() {
        available = active = false;
        this.notifyObserversStatusChange();
    }

    @Override
    public void handleRestoration() {
        active = true;
        available = stock > 0;
        this.notifyObserversStatusChange();
    }

    @Override
    public void restoreStock(Integer quantity) {
        Integer newStock = this.getStock() + quantity;
        this.updateStockAndNotify(newStock);
    }

}