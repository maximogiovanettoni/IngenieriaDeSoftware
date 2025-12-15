package ar.uba.fi.ingsoft1.sistema_comedores.products.combos;

import ar.uba.fi.ingsoft1.sistema_comedores.products.Product;
import ar.uba.fi.ingsoft1.sistema_comedores.products.ProductCategory;

import ar.uba.fi.ingsoft1.sistema_comedores.products.ProductType;
import ar.uba.fi.ingsoft1.sistema_comedores.products.exception.InsufficientStockException;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


@Entity
@DiscriminatorValue("COMBO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Combo extends Product {

    @OneToMany(mappedBy = "combo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ComboProduct> comboProducts = new ArrayList<>();

    public Combo(String name, String description, BigDecimal price, Boolean active) {
        super(name, description, price, ProductCategory.COMBO, active != null ? active : false);
        this.stock = calculateStock();
        this.available = this.stock > 0 && this.active;
        this.productType = ProductType.COMBO;
    }

    public Map<Product, Integer> getProductsMap() {
        return comboProducts.stream()
            .collect(Collectors.toMap(
                ComboProduct::getProduct,
                ComboProduct::getQuantity
            ));
    }

    public int calculateStock() {
        if (comboProducts.isEmpty()) {
            return 0;
        }
        int stock = Integer.MAX_VALUE;
        for (ComboProduct comboProduct : comboProducts) {
            Product product = comboProduct.getProduct();
            Integer quantity = comboProduct.getQuantity();
            if (!product.isAvailable() || !product.isActive()) {
                return 0;
            }
            
            int productStock = product.getStock() != null ? product.getStock() : 0;
            int possibleUnits = productStock / quantity;
            stock = Math.min(stock, possibleUnits);
        }
        return stock;
    }

    private List<Product> getProductsMissingStock(Integer requiredStock) {
        List<Product> productsMissingStock = new LinkedList<Product>();
        for (ComboProduct comboProduct : comboProducts) {
            Integer totalRequiredQuantity = comboProduct.getQuantity() * requiredStock;
            Product product = comboProduct.getProduct();
            if (product.getStock() < totalRequiredQuantity) {
                productsMissingStock.add(product);
            }
        }
        return productsMissingStock;
    }

    @Override
    public void consumeStock(Integer usedStock) throws IllegalArgumentException {
        if (usedStock == null) return;
        if (stock < usedStock) {
            throw InsufficientStockException.forMissingProducts(this.getName(), this.getStock(), usedStock, this.getProductsMissingStock(usedStock));
        }
        for (ComboProduct comboProduct : comboProducts) {
            Product product = comboProduct.getProduct();
            Integer quantity = comboProduct.getQuantity();
            Integer totalUsedQuantity = usedStock * quantity;
            product.consumeStock(totalUsedQuantity);
        }
        stock = calculateStock();
        available = stock > 0 && active;
    }

    @Override
    public void restoreStock(Integer quantity) {
        for (ComboProduct comboProduct : comboProducts) {
            Product product = comboProduct.getProduct();
            Integer quantityToRestore = comboProduct.getQuantity() * quantity;
            Integer newStock = product.getStock() + quantityToRestore;
            product.updateStockAndNotify(newStock);
        }
    }

    public BigDecimal getRegularPrice() {
        return comboProducts.stream()
                .map(comboProduct -> {
                    BigDecimal productPrice = comboProduct.getProduct().getPrice();
                    BigDecimal quantity = BigDecimal.valueOf(comboProduct.getQuantity());
                    return productPrice.multiply(quantity);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getDiscount() {
        BigDecimal regularPrice = getRegularPrice();
        if (this.getPrice() == null) {
            return BigDecimal.ZERO;
        }
        return regularPrice.subtract(this.getPrice());
    }

    public void onProductStockChange() {
        stock = calculateStock();
        available = stock > 0 && active;
    }

    public void onProductStatusChange() {
        stock = calculateStock();
        available = stock > 0 && active;
    }

    public void addProduct(Product product, Integer quantity) {
        Optional<ComboProduct> existing = comboProducts.stream()
                .filter(pi -> pi.getProduct().getId().equals(product.getId()))
                .findFirst();

        if (existing.isPresent()) {
            existing.get().setQuantity(quantity);
        } else {
            ComboProduct comboProduct = new ComboProduct(this, product, quantity);
            comboProducts.add(comboProduct);
            product.addObserver(new ComboProductObserver(product, this));
        }
        onProductStockChange();
    }

    public void removeProduct(Product product) {
        comboProducts.removeIf(pi ->
                pi.getProduct().getId().equals(product.getId())
        );
        product.removeObserver(this.getId());
        onProductStockChange();
    }

    @Transactional
    public void handleDeactivation() {
        for (ComboProduct comboProduct : comboProducts) {
            Product product = comboProduct.getProduct();
            product.removeObserver(this.getId());
        }
        active = available = false;
    }

    @Transactional
    public void handleRestoration() {
        for (ComboProduct comboProduct : comboProducts) {
            Product product = comboProduct.getProduct();
            product.addObserver(new ComboProductObserver(product, this));
        }
        stock = calculateStock();
        active = true;
        available = stock > 0;
    }
}
