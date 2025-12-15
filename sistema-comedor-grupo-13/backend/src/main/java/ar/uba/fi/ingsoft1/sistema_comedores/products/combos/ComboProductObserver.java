package ar.uba.fi.ingsoft1.sistema_comedores.products.combos;

import ar.uba.fi.ingsoft1.sistema_comedores.products.Product;
import ar.uba.fi.ingsoft1.sistema_comedores.products.ProductObserver;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "combo_product_observers")
@DiscriminatorValue("COMBO")
@Getter
@Setter
@NoArgsConstructor
public class ComboProductObserver extends ProductObserver {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "combo_id", nullable = false)
    private Combo combo;

    public ComboProductObserver(Product product, Combo combo) {
        super(product);
        this.combo = combo;
        setObserverId(combo.getId());
    }

    @Override
    public void onProductStockChange() {
        combo.onProductStockChange();
    }

    @Override
    public void onProductStatusChange() {
        combo.onProductStatusChange();
    }

    @Override
    public Long getObservedEntityId() {
        return combo.getId();
    }

    public void setProduct(Combo combo) {
        this.combo = combo;
    }
}