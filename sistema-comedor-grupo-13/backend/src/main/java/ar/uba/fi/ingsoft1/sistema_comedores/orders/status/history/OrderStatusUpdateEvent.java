package ar.uba.fi.ingsoft1.sistema_comedores.orders.status.history;

import java.time.Instant;

import ar.uba.fi.ingsoft1.sistema_comedores.orders.Order;
import ar.uba.fi.ingsoft1.sistema_comedores.orders.status.OrderStatus;
import ar.uba.fi.ingsoft1.sistema_comedores.orders.status.OrderStatusConverter;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_state_update_events")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OrderStatusUpdateEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, foreignKey = @ForeignKey(name = "fk_order_status_event_order"))
    private Order order;
    
    @Convert(converter = OrderStatusConverter.class)
    @Column(nullable = true)
    private OrderStatus previousStatus;
    
    @Convert(converter = OrderStatusConverter.class)
    @Column(nullable = false)
    private OrderStatus newStatus;
    
    @Column(nullable = false)
    private Instant changedAt;
    
    @Column(nullable = true, columnDefinition = "TEXT")
    private String changeReason;
    
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        if (this.changedAt == null) {
            this.changedAt = Instant.now();
        }
    }
}