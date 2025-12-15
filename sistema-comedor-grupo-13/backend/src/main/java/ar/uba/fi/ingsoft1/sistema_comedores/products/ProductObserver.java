package ar.uba.fi.ingsoft1.sistema_comedores.products;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product_observers",
        uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "observer_type", "observer_id"}))
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "observer_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@NoArgsConstructor
public abstract class ProductObserver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "observer_id", nullable = false)
    private Long observerId;

    public ProductObserver(Product product) {
        this.product = product;
    }

    public abstract void onProductStockChange();
    public abstract void onProductStatusChange();
    public abstract Long getObservedEntityId();
}
