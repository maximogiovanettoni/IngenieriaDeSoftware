package ar.uba.fi.ingsoft1.sistema_comedores.orders;

import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import java.math.BigDecimal;

@Embeddable
@NoArgsConstructor
@Getter
@Setter
public class OrderItem {
    
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(nullable = false)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    @PositiveOrZero(message = "La cantidad debe ser cero o un número positivo")
    private Integer quantity;
    
    @Column(nullable = false, name = "subtotal", precision = 10, scale = 3)
    private BigDecimal subtotal;
    
    public OrderItem(Long productId, String productName, BigDecimal unitPrice, Integer quantity) {
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.subtotal = BigDecimal.valueOf(quantity).multiply(unitPrice);
    }
}