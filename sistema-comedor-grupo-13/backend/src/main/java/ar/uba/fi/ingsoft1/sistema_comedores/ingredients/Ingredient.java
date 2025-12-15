package ar.uba.fi.ingsoft1.sistema_comedores.ingredients;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.util.Set;
import java.util.HashSet;

@Entity
@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor
public class Ingredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String unitMeasure;

    @PositiveOrZero
    private BigDecimal stock;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private boolean available = false;

    @JsonIgnore
    @OneToMany(mappedBy = "ingredient", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<IngredientObserver> observers = new HashSet<>();

    public void addObserver(IngredientObserver observer) {
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
            for (IngredientObserver observer : observers) {
                observer.onIngredientStockChange();
            }
        }
    }

    public void notifyObserversStatusChange() {
        if (observers != null) {
            for (IngredientObserver observer : observers) {
                observer.onIngredientStatusChange();
            }
        }
    }

    public void updateStockAndNotify(BigDecimal newStock) {
        this.setStock(newStock);
        this.setAvailable(this.isActive() && (this.getStock() != null && this.getStock().compareTo(BigDecimal.ZERO) > 0));
        notifyObserversStockChange();
    }

    public void consumeStock(BigDecimal quantity) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La cantidad a consumir debe ser un valor positivo");
        }
        BigDecimal currentStock = this.getStock() != null ? this.getStock() : BigDecimal.ZERO;
        if (quantity.compareTo(currentStock) > 0) {
            throw new IllegalArgumentException("No hay suficiente stock disponible");
        }
        BigDecimal newStock = currentStock.subtract(quantity);
        this.updateStockAndNotify(newStock);
    }

}
