package ar.uba.fi.ingsoft1.sistema_comedores.ingredients;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ingredient_observers",
        uniqueConstraints = @UniqueConstraint(columnNames = {"ingredient_id", "observer_type", "observer_id"}))
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "observer_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@NoArgsConstructor
public abstract class IngredientObserver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Column(name = "observer_id", nullable = false)
    private Long observerId;

    public IngredientObserver(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    public abstract void onIngredientStockChange();
    public abstract void onIngredientStatusChange();
    public abstract Long getObservedEntityId();
}
