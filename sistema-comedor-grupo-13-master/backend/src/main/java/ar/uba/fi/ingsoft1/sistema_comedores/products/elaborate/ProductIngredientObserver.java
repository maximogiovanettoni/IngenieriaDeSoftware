package ar.uba.fi.ingsoft1.sistema_comedores.products.elaborate;

import ar.uba.fi.ingsoft1.sistema_comedores.ingredients.Ingredient;
import ar.uba.fi.ingsoft1.sistema_comedores.ingredients.IngredientObserver;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product_ingredient_observers")
@DiscriminatorValue("PRODUCT")
@Getter
@Setter
@NoArgsConstructor
public class ProductIngredientObserver extends IngredientObserver {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ElaborateProduct product;

    public ProductIngredientObserver(Ingredient ingredient, ElaborateProduct product) {
        super(ingredient);
        this.product = product;
        setObserverId(product.getId());
    }

    @Override
    public void onIngredientStockChange() {
        product.onIngredientStockChange();
    }

    @Override
    public void onIngredientStatusChange() {
        product.onIngredientStatusChange();
    }

    @Override
    public Long getObservedEntityId() {
        return product.getId();
    }

    public void setProduct(ElaborateProduct product) {
        this.product = product;
    }
}