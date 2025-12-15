package ar.uba.fi.ingsoft1.sistema_comedores.products.elaborate;

import java.math.BigDecimal;

import ar.uba.fi.ingsoft1.sistema_comedores.ingredients.Ingredient;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "elaborate_product_ingredients")
@Getter
@Setter
public class ProductIngredient {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ElaborateProduct product;
    
    @ManyToOne(fetch = FetchType.EAGER) // EAGER to avoid lazy loading issues
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;
    
    @Column(nullable = false, precision = 10, scale = 3)
    @Positive
    private BigDecimal quantity;
    
    // Constructors
    protected ProductIngredient() {}
    
    public ProductIngredient(ElaborateProduct product, Ingredient ingredient, BigDecimal quantity) {
        this.product = product;
        this.ingredient = ingredient;
        this.quantity = quantity;
    }
}
