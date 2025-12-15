package ar.uba.fi.ingsoft1.sistema_comedores.products.combos;

import ar.uba.fi.ingsoft1.sistema_comedores.products.Product;
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
@Table(name = "combo_products")
@Getter
@Setter
public class ComboProduct {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "combo_id", nullable = false)
    private Combo combo;
    
    @ManyToOne(fetch = FetchType.EAGER) // EAGER to avoid lazy loading when restoring stock
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Column(nullable = false)
    @Positive
    private Integer quantity;
    
    protected ComboProduct() {}
    
    public ComboProduct(Combo combo, Product product, Integer quantity) {
        this.combo = combo;
        this.product = product;
        this.quantity = quantity;
    }
    
}
