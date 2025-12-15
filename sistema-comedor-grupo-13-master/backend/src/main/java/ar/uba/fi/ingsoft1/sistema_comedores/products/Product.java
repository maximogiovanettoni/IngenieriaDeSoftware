package ar.uba.fi.ingsoft1.sistema_comedores.products;

import java.time.Instant;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;

import java.util.Set;
import java.util.HashSet;

@Entity(name = "products") 
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "product_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public abstract class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Column(nullable = false, unique = true, length = 100)
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    @NotBlank(message = "El nombre del producto {this.type} es obligatorio")
    protected String name;

    @Size(max = 300, message = "La descripción no puede superar los 300 caracteres")
    @Column(length = 300)
    protected String description;
    
    @Positive(message = "El precio debe ser positivo")
    @Column(nullable = false, precision = 10, scale = 2)
    protected BigDecimal price;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    protected ProductCategory category;

    @Column(name = "product_type", insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    protected ProductType productType;

    @Column(nullable = false)
    @PositiveOrZero
    protected Integer stock;

    @Column(nullable = false)
    protected Boolean active = true;

    @Column(nullable = false)
    protected Boolean available = false;

    @Column(name = "image_url")
    protected String imageUrl;

    @Column(updatable = false, nullable = false)
    protected Instant createdAt;

    @Column(nullable = false)
    protected Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    @JsonIgnore
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<ProductObserver> observers = new HashSet<>();

    public Product(String name, String description, BigDecimal price, ProductCategory category, Boolean active) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.active = active;
    }

    public boolean isAvailable() {
        return available && stock != null;
    }

    public boolean isActive() {
        return active;
    }

    public boolean hasStock(Integer units) {
        return active && this.isAvailable() && stock >= units;
    }

    public void addObserver(ProductObserver observer) {
        if (observers == null) {
            observers = new HashSet<>();
        }
        observers.add(observer);
    }

    public void removeObserver(Long observerId) {
        if (observers != null) {
            observers.removeIf(observer -> observer.getObservedEntityId().equals(observerId));
        }
    }

    public void notifyObserversStockChange() {
        if (observers != null) {
            for (ProductObserver observer : observers) {
                observer.onProductStockChange();
            }
        }
    }

    public void notifyObserversStatusChange() {
        if (observers != null) {
            for (ProductObserver observer : observers) {
                observer.onProductStatusChange();
            }
        }
    }

    public void updateStockAndNotify(Integer newStock) {
        this.setStock(newStock);
        this.setAvailable(this.isActive() && this.getStock() > 0);
        notifyObserversStockChange();
    }

    @Transactional
    public abstract void restoreStock(Integer quantity);

    @Transactional
    public abstract void consumeStock(Integer usedStock) throws IllegalArgumentException;

    @Transactional
    public abstract void handleDeactivation();

    @Transactional
    public abstract void handleRestoration();

}