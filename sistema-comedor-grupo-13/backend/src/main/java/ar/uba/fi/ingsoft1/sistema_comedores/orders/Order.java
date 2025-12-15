package ar.uba.fi.ingsoft1.sistema_comedores.orders;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import ar.uba.fi.ingsoft1.sistema_comedores.orders.status.OrderStatus;
import ar.uba.fi.ingsoft1.sistema_comedores.orders.status.OrderStatusConverter;
import ar.uba.fi.ingsoft1.sistema_comedores.orders.status.history.OrderStatusUpdateEvent;
import ar.uba.fi.ingsoft1.sistema_comedores.promotions.Promotion;

@Entity
@Table(name = "orders")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Order {
    
    @Id
    @Column(unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orders_seq")
    @SequenceGenerator(name = "orders_seq", sequenceName = "orders_order_number_seq", allocationSize = 1)
    private Long orderNumber;

    @Column(nullable = false)
    private Long userId; 
    
    @Convert(converter = OrderStatusConverter.class)
    @Column(nullable = false)
    private OrderStatus status;
    
    @Column(nullable = false)
    @PositiveOrZero
    private BigDecimal subtotal;

    @Column(nullable = false, name = "discount_amount")
    @PositiveOrZero
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @ElementCollection
    @CollectionTable(name = "applied_promotions", joinColumns = @JoinColumn(name = "order_id"))
    private List<AppliedPromotion> appliedPromotions = new ArrayList<>();
    
    @Column(nullable = false, name = "total_amount")
    @PositiveOrZero
    private BigDecimal totalAmount;
    
    @ElementCollection
    @CollectionTable(name = "order_items", joinColumns = @JoinColumn(name = "order_id"))
    @NotEmpty(message = "Una orden debe contener por lo menos 1 ítem")
    private List<OrderItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderStatusUpdateEvent> statusHistory = new ArrayList<>();
    
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;
    
    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public String getStatusCode() {
        return this.status.getStatusCode();
    }

    public void cancelOrder() {
        this.status = status.cancelOrder(this.orderNumber);
    }

    public void rejectOrder() {
        this.status = status.rejectOrder(this.orderNumber);
    }

    public void moveOrderForward() {
        this.status = status.moveOrderForward(this.orderNumber);
    }

    public void moveOrderBackward() {
        this.status = status.moveOrderBackward(this.orderNumber);
    }

    public void setDiscountAmount(BigDecimal amount) {
        this.discountAmount = amount;
        this.totalAmount = subtotal.subtract(amount);
    }
}