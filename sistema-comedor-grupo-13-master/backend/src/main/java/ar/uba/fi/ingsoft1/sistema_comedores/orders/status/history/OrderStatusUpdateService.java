package ar.uba.fi.ingsoft1.sistema_comedores.orders.status.history;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Comparator;

import ar.uba.fi.ingsoft1.sistema_comedores.orders.Order;
import ar.uba.fi.ingsoft1.sistema_comedores.orders.OrderRepository;
import ar.uba.fi.ingsoft1.sistema_comedores.orders.exception.OrderNotFoundException;
import ar.uba.fi.ingsoft1.sistema_comedores.orders.status.OrderStatus;

@Service
@RequiredArgsConstructor
public class OrderStatusUpdateService {
    
    private final OrderStatusUpdateEventRepository eventRepository;
    
    /**
     * Record a state transition and detect if it's backward
     * @throws OrderNotFoundException 
     */
    @Transactional
    public OrderStatusUpdateEvent recordStateChange(
            Order order,
            OrderStatus previousStatus,
            OrderStatus newStatus,
            String changeReason
    ) throws OrderNotFoundException {
        
        OrderStatusUpdateEvent event = new OrderStatusUpdateEvent();
        event.setOrder(order);
        event.setPreviousStatus(previousStatus);
        event.setNewStatus(newStatus);
        event.setChangeReason(changeReason);
        
        eventRepository.save(event);
                
        return event;
    }
    
    /**
     * Get the full state history for an order
     */
    public List<OrderStatusUpdateEvent> getOrderStateHistory(Long orderId) {
        return eventRepository.findByOrderOrderNumberOrderByChangedAtAsc(orderId);
    }
    
    /**
     * Get time when order reached a specific status (first occurrence)
     */
    public Optional<Instant> getTimeWhenStatusReached(Long orderId, OrderStatus status) {
        return eventRepository.findByOrderIdAndNewStatus(orderId, status)
                .stream()
                .min(Comparator.comparing(OrderStatusUpdateEvent::getChangedAt))
                .map(OrderStatusUpdateEvent::getChangedAt);
    }
    
    /**
     * Get all backward transitions for an order (potential issues)
     */
    public List<OrderStatusUpdateEvent> getBackwardTransitions(Long orderId) {
        return eventRepository.findBackwardTransitionsForOrder(orderId);
    }
    
    /**
     * Check if an order has ANY backward transitions
     */
    public boolean hasBackwardTransitions(Long orderId) {
        return !getBackwardTransitions(orderId).isEmpty();
    }
    
    /**
     * Get the duration an order stayed in a particular status
     */
    public long getDurationInStatus(Long orderId, OrderStatus status) {
        List<OrderStatusUpdateEvent> events = eventRepository.findByOrderOrderNumberOrderByChangedAtAsc(orderId);
        
        Instant enteredStatus = null;
        Instant exitedStatus = null;
        
        for (OrderStatusUpdateEvent event : events) {
            if (event.getNewStatus() == status) {
                enteredStatus = event.getChangedAt();
            }
            if (event.getPreviousStatus() == status) {
                exitedStatus = event.getChangedAt();
                break;
            }
        }
        
        if (enteredStatus != null && exitedStatus != null) {
            return exitedStatus.toEpochMilli() - enteredStatus.toEpochMilli();
        }
        
        return -1;
    }

}