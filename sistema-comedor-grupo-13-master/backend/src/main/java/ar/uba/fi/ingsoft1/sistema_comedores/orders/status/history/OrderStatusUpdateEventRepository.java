package ar.uba.fi.ingsoft1.sistema_comedores.orders.status.history;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import ar.uba.fi.ingsoft1.sistema_comedores.orders.status.OrderStatus;
import java.time.Instant;
import java.util.List;

@Repository
public interface OrderStatusUpdateEventRepository extends JpaRepository<OrderStatusUpdateEvent, Long> {
    
    /**
     * Get all state changes for a specific order, ordered by time
     */
    List<OrderStatusUpdateEvent> findByOrderOrderNumberOrderByChangedAtAsc(Long orderNumber);
    
    /**
     * Get all BACKWARD transitions for a specific order
     */
    @Query("SELECT e FROM OrderStatusUpdateEvent e WHERE e.order.orderNumber = ?1 ORDER BY e.changedAt DESC")
    List<OrderStatusUpdateEvent> findBackwardTransitionsForOrder(Long orderId);
    
    /**
     * Get state history for an order within a time range
     */
    @Query("SELECT e FROM OrderStatusUpdateEvent e WHERE e.order.orderNumber = ?1 AND e.changedAt BETWEEN ?2 AND ?3 ORDER BY e.changedAt ASC")
    List<OrderStatusUpdateEvent> findStateHistoryInRange(Long orderId, Instant from, Instant to);
    
    /**
     * Find all transitions TO a specific status
     */
    @Query("SELECT e FROM OrderStatusUpdateEvent e WHERE e.order.orderNumber = ?1 AND e.newStatus = ?2")
    List<OrderStatusUpdateEvent> findByOrderIdAndNewStatus(Long orderId, OrderStatus newStatus);
    
    /**
     * Find all transitions FROM a specific status
     */
    @Query("SELECT e FROM OrderStatusUpdateEvent e WHERE e.order.orderNumber = ?1 AND e.previousStatus = ?2")
    List<OrderStatusUpdateEvent> findByOrderIdAndPreviousStatus(Long orderId, OrderStatus previousStatus);
    
    /**
     * Page through all events for a specific order
     */
    Page<OrderStatusUpdateEvent> findByOrderOrderNumber(Long orderNumber, Pageable pageable);
}