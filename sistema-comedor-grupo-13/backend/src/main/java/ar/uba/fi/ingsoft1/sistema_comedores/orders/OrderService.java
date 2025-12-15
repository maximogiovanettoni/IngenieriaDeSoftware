package ar.uba.fi.ingsoft1.sistema_comedores.orders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.uba.fi.ingsoft1.sistema_comedores.user.User;
import ar.uba.fi.ingsoft1.sistema_comedores.orders.dto.CreateOrderRequest;
import ar.uba.fi.ingsoft1.sistema_comedores.orders.dto.OrderItemRequest;
import ar.uba.fi.ingsoft1.sistema_comedores.orders.dto.OrderDetailsResponse;
import ar.uba.fi.ingsoft1.sistema_comedores.orders.exception.OrderNotFoundException;
import ar.uba.fi.ingsoft1.sistema_comedores.orders.exception.StatusNeverReachedException;
import ar.uba.fi.ingsoft1.sistema_comedores.orders.exception.OrderCannotBeCancelledException;
import ar.uba.fi.ingsoft1.sistema_comedores.orders.status.CancelledOrderStatus;
import ar.uba.fi.ingsoft1.sistema_comedores.products.ProductCategory;
import ar.uba.fi.ingsoft1.sistema_comedores.orders.status.OrderStatus;
import ar.uba.fi.ingsoft1.sistema_comedores.orders.status.OrderStatusConverter;
import ar.uba.fi.ingsoft1.sistema_comedores.orders.status.PendingOrderStatus;
import ar.uba.fi.ingsoft1.sistema_comedores.orders.notifications.OrderNotificationService;
import ar.uba.fi.ingsoft1.sistema_comedores.orders.notifications.OrderStatusDTO;
import ar.uba.fi.ingsoft1.sistema_comedores.orders.status.history.OrderStatusUpdateEvent;
import ar.uba.fi.ingsoft1.sistema_comedores.orders.status.history.OrderStatusUpdateService;
import ar.uba.fi.ingsoft1.sistema_comedores.products.Product;
import ar.uba.fi.ingsoft1.sistema_comedores.products.ProductRepository;
import ar.uba.fi.ingsoft1.sistema_comedores.products.ProductService;
import ar.uba.fi.ingsoft1.sistema_comedores.products.ProductType;
import ar.uba.fi.ingsoft1.sistema_comedores.products.combos.Combo;
import ar.uba.fi.ingsoft1.sistema_comedores.products.combos.ComboService;
import ar.uba.fi.ingsoft1.sistema_comedores.products.combos.exception.ComboNotFoundException;
import ar.uba.fi.ingsoft1.sistema_comedores.products.exception.InsufficientStockException;
import ar.uba.fi.ingsoft1.sistema_comedores.products.exception.ProductNotAvailableException;
import ar.uba.fi.ingsoft1.sistema_comedores.products.exception.ProductNotFoundException;
import ar.uba.fi.ingsoft1.sistema_comedores.user.UserService;
import ar.uba.fi.ingsoft1.sistema_comedores.user.exception.UserNotFoundException;
import ar.uba.fi.ingsoft1.sistema_comedores.promotions.Promotion;
import ar.uba.fi.ingsoft1.sistema_comedores.promotions.PromotionService;
import ar.uba.fi.ingsoft1.sistema_comedores.promotions.DiscountOptimizer.PromotionCombination;
import ar.uba.fi.ingsoft1.sistema_comedores.promotions.dto.PromotionDetailsResponse;
import ar.uba.fi.ingsoft1.sistema_comedores.promotions.BuyXGetY;
import ar.uba.fi.ingsoft1.sistema_comedores.promotions.BuyXPayY;
import ar.uba.fi.ingsoft1.sistema_comedores.promotions.PercentageDiscount;
import ar.uba.fi.ingsoft1.sistema_comedores.promotions.FixedDiscount;
import ar.uba.fi.ingsoft1.sistema_comedores.promotions.DiscountOptimizer;
import ar.uba.fi.ingsoft1.sistema_comedores.promotions.DayOfWeek;
import ar.uba.fi.ingsoft1.sistema_comedores.orders.dto.promotion.AppliedPromotionResponse;
import ar.uba.fi.ingsoft1.sistema_comedores.orders.dto.promotion.CalculatePromotionRequest;
import ar.uba.fi.ingsoft1.sistema_comedores.orders.dto.promotion.CalculatePromotionResponse;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final UserService userService;
    private final PromotionService promotionService;
    private final OrderStatusUpdateService statusUpdateService;
    private final ProductRepository productRepository;
    private final OrderNotificationService notificationService;
    
    @Autowired
    public OrderService(OrderRepository orderRepository, ProductService productService, UserService userService, PromotionService promotionService, OrderStatusUpdateService statusUpdateService, ProductRepository productRepository, OrderNotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.productService = productService;
        this.userService = userService;
        this.promotionService = promotionService;
        this.statusUpdateService = statusUpdateService;
        this.productRepository = productRepository;
        this.notificationService = notificationService;
    }


    public Map<Product, Integer> getOrderRequestDetails(CreateOrderRequest request) throws IllegalArgumentException {
        Map<Product, Integer> orderRequestDetails = new HashMap<Product, Integer>();
        for (OrderItemRequest itemRequest : request.items()) {
            Product product = productService.getProductById(itemRequest.productId());
            orderRequestDetails.put(product, itemRequest.quantity());
        }
        return orderRequestDetails;
    }

    public List<OrderDetailsResponse> getAllOrders() {
        return orderRepository.findAll().stream()
            .map(OrderDetailsResponse::from)
            .toList();
    }
  
    public OrderDetailsResponse createOrder(CreateOrderRequest request, String email) throws RuntimeException {
        User user = userService.getUserByEmail(email);
        List<OrderItem> orderItems = this.getOrderItems(request.items());
        
        Order order = new Order();
        order.setUserId(user.getId());
        order.setStatus(new PendingOrderStatus());
        order.setItems(orderItems);
        
        BigDecimal subtotal = orderItems.stream()
            .map(OrderItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setSubtotal(subtotal);
        
        applyPromotions(order);
        
        Order savedOrder = orderRepository.save(order);
        statusUpdateService.recordStateChange(savedOrder, savedOrder.getStatus(), savedOrder.getStatus(), null);
        
        return OrderDetailsResponse.from(savedOrder);
    }

    private Map<Product, Integer> getProductQuantityMap(List<OrderItemRequest> items) {
        Map<Product, Integer> productQuantityMap = new HashMap<>();
        for (OrderItemRequest item : items) {
            Product p = productService.getProductById(item.productId());
            if (!p.isAvailable()) {
                throw new ProductNotAvailableException(p.getName());
            }
            productQuantityMap.put(p, item.quantity());
        }
        return productQuantityMap;
    }

    private Map<Product, Integer> getProductQuantityMapFromOrderItems(List<OrderItem> orderItems) {
        Map<Product, Integer> productQuantityMap = new HashMap<>();
        for (OrderItem item : orderItems) {
            Product p = productService.getProductById(item.getProductId());
            productQuantityMap.put(p, item.getQuantity());
        }
        return productQuantityMap;
    }

    private BigDecimal calculateSubtotal(Map<Product, Integer> productQuantityMap) {
        return productQuantityMap.entrySet().stream()
            .map(entry -> entry.getKey().getPrice().multiply(BigDecimal.valueOf(entry.getValue())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public CalculatePromotionResponse calculateAppliablePromotions(List<OrderItemRequest> items) {
        Map<Product, Integer> productQuantityMap = getProductQuantityMap(items);
        PromotionCombination appliablePromotions = promotionService.getAppliablePromotions(productQuantityMap);
        log.debug("👍👍👍👍👍👍👍👍DISCOUNT OPTIMIZER👍👍👍👍👍👍👍👍👍👍👍: {}", appliablePromotions);
        Map<Promotion, BigDecimal> promotions = appliablePromotions.promotions();
        
        BigDecimal subtotal = calculateSubtotal(productQuantityMap);
        BigDecimal discountAmount = appliablePromotions.discount();
        List<AppliedPromotion> appliedPromotions = getAppliedPromotions(promotions);
        
        return new CalculatePromotionResponse(
            subtotal, 
            discountAmount, 
            subtotal.subtract(discountAmount), 
            discountAmount.compareTo(BigDecimal.ZERO) > 0, 
            appliedPromotions.stream().map(AppliedPromotionResponse::from).toList()
        );
    }

    public List<AppliedPromotion> getAppliedPromotions(Map<Promotion, BigDecimal> promotions) {
        List<AppliedPromotion> appliedPromotions = new ArrayList<>();
        for (Map.Entry<Promotion, BigDecimal> appliablePromo : promotions.entrySet()) {
            AppliedPromotion appPromo = new AppliedPromotion();
            appPromo.setAppliedPromotionName(appliablePromo.getKey().getName());
            appPromo.setAppliedPromotionType(appliablePromo.getKey().getType());
            appPromo.setAppliedDiscount(appliablePromo.getValue());
            appPromo.setStartDate(appliablePromo.getKey().getStartDate());
            appPromo.setEndDate(appliablePromo.getKey().getEndDate());
            
            // Convert Set<DayOfWeek> to String
            java.util.Set<DayOfWeek> days = appliablePromo.getKey().getApplicableDays();
            if (days != null && !days.isEmpty()) {
                String daysString = days.stream()
                    .map(Enum::toString)
                    .collect(Collectors.joining(","));
                appPromo.setApplicableDays(daysString);
            }
            
            appliedPromotions.add(appPromo);
        }
        return appliedPromotions;
    }

    @Transactional  
    private void applyPromotions(Order order) {
        Map<Product, Integer> productQuantityMap = getProductQuantityMapFromOrderItems(order.getItems());
        PromotionCombination appliablePromotions = promotionService.getAppliablePromotions(productQuantityMap);
        
        order.setDiscountAmount(appliablePromotions.discount());
        order.setAppliedPromotions(getAppliedPromotions(appliablePromotions.promotions()));
    }

    @Transactional
    private List<OrderItem> getOrderItems(List<OrderItemRequest> orderRequestItems) {
        List<OrderItem> orderItems = new ArrayList<>();
        
        for (OrderItemRequest itemRequest : orderRequestItems) {
            Product product = productService.getProductById(itemRequest.productId());
            
            if (!product.isAvailable()) {
                throw new ProductNotAvailableException(product.getName());
            }
            
            product.consumeStock(itemRequest.quantity());
            
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setQuantity(itemRequest.quantity());
            orderItem.setUnitPrice(product.getPrice());
            orderItem.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.quantity())));
            
            orderItems.add(orderItem);
        }
        
        return orderItems;
    }

    private void restoreStockForItems(List<OrderItem> orderItems) {
        for (OrderItem item : orderItems) {
            Long productId = item.getProductId();
            Integer quantity = item.getQuantity();
            productService.restoreProductStock(productId, quantity);
        }
    }

    public OrderDetailsResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
        return OrderDetailsResponse.from(order);
    }
    
    
    public List<OrderDetailsResponse> getUserOrders(String email) {
        var user = userService.getUserByEmail(email);
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        return orders.stream()
            .map(OrderDetailsResponse::from)
            .collect(Collectors.toList());
    }

    public boolean userCanAccessOrder(Long orderId, String email) {
        User user = userService.getUserByEmail(email);
        String roleName = user.getRole().getValue();
        return orderRepository.existsByOrderNumberAndUserId(orderId, user.getId()) 
            || roleName.equals("STAFF") 
            || roleName.equals("ADMIN");
    }
    
    public List<OrderDetailsResponse> getPendingOrders() {
        List<Order> orders = orderRepository.findByStatus("PENDING");
        return orders.stream()
            .map(OrderDetailsResponse::from)
            .collect(Collectors.toList());
    }
    
    public OrderDetailsResponse cancelOrder(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
        OrderStatus previousStatus = order.getStatus();
        order.cancelOrder();
        this.restoreStockForItems(order.getItems());
        Order updated = orderRepository.save(order);
        log.info("Order {} cancelled and stock restored", orderId);
        statusUpdateService.recordStateChange(updated, previousStatus, updated.getStatus(), null);



        notificationService.notifyStatusChange(updated);
        return OrderDetailsResponse.from(updated);
    }

    public OrderDetailsResponse rejectOrder(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        OrderStatus previousStatus = order.getStatus();
        order.rejectOrder();
        this.restoreStockForItems(order.getItems());
        Order updated = orderRepository.save(order);
        log.info("Order {} rejected and stock restored", orderId);
        statusUpdateService.recordStateChange(updated, previousStatus, updated.getStatus(), reason);

        notificationService.notifyStatusChange(updated);
        return OrderDetailsResponse.from(updated);
    }
    
    public OrderDetailsResponse moveOrderForward(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
        OrderStatus previousStatus = order.getStatus();
        order.moveOrderForward();
        Order updated = orderRepository.save(order);
        log.info("Order {} moved forward to {}", orderId, updated.getStatus().getStatusCode());
        statusUpdateService.recordStateChange(updated, previousStatus, updated.getStatus(), null);

        
        notificationService.notifyStatusChange(updated);
        return OrderDetailsResponse.from(updated);
    }
    
    public OrderDetailsResponse moveOrderBackward(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
        OrderStatus previousStatus = order.getStatus();
        order.moveOrderBackward();
        Order updated = orderRepository.save(order);
        log.info("Order {} moved backward to {}", orderId, updated.getStatus().getStatusCode());
        statusUpdateService.recordStateChange(updated, previousStatus, updated.getStatus(), null);

        notificationService.notifyStatusChange(updated);
        return OrderDetailsResponse.from(updated);
    }
    
    public OrderDetailsResponse forceCancelOrder(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
        OrderStatus previousStatus = order.getStatus();
        restoreStockForItems(order.getItems());
        order.setStatus(new CancelledOrderStatus());
        Order updated = orderRepository.save(order);
        log.info("Order {} force cancelled by staff. Reason: {}", orderId, reason);
        statusUpdateService.recordStateChange(updated, previousStatus, updated.getStatus(), reason);

        notificationService.notifyStatusChange(updated);
        return OrderDetailsResponse.from(updated);
    }

    public Map<String, Object> getOrderTrackingStats(String email) {
        var user = userService.getUserByEmail(email);
        
        List<Order> allOrders = orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        
        Map<String, Object> stats = new HashMap<>();
        
        long pendingCount = allOrders.stream()
            .filter(o -> "PENDING".equals(o.getStatus().getStatusCode())).count();
        long confirmedCount = allOrders.stream()
            .filter(o -> "CONFIRMED".equals(o.getStatus().getStatusCode())).count();
        long deliveredCount = allOrders.stream()
            .filter(o -> "COMPLETED".equals(o.getStatus().getStatusCode())).count();
        long rejectedCount = allOrders.stream()
            .filter(o -> "REJECTED".equals(o.getStatus().getStatusCode())).count();
        
        BigDecimal totalSpent = allOrders.stream()
            .map(Order::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        stats.put("totalOrders", allOrders.size());
        stats.put("pendingCount", pendingCount);
        stats.put("confirmedCount", confirmedCount);
        stats.put("deliveredCount", deliveredCount);
        stats.put("rejectedCount", rejectedCount);
        stats.put("totalSpent", totalSpent);
        
        return stats;
    }
    
    public Map<String, Object> getStaffDashboardStats() {
        List<Order> allOrders = orderRepository.findAll();
        
        Map<String, Object> stats = new HashMap<>();
        
        long pendingCount = allOrders.stream()
            .filter(o -> "PENDING".equals(o.getStatus().getStatusCode())).count();
        long preparingCount = allOrders.stream()
            .filter(o -> "CONFIRMED".equals(o.getStatus().getStatusCode())).count();
        long completedCount = allOrders.stream()
            .filter(o -> "COMPLETED".equals(o.getStatus().getStatusCode())).count();
        
        stats.put("pendingCount", pendingCount);
        stats.put("preparingCount", preparingCount);
        stats.put("completedCount", completedCount);
        
        return stats;
    }

    public Map<String, Object> getAdminDashboardStats() {
        List<Order> allOrders = orderRepository.findAll();
        
        Map<String, Object> stats = new HashMap<>();
        
        long pendingCount = allOrders.stream()
            .filter(o -> "PENDING".equals(o.getStatus().getStatusCode())).count();
        long confirmedCount = allOrders.stream()
            .filter(o -> "CONFIRMED".equals(o.getStatus().getStatusCode())).count();
        long deliveredCount = allOrders.stream()
            .filter(o -> "COMPLETED".equals(o.getStatus().getStatusCode())).count();
        long rejectedCount = allOrders.stream()
            .filter(o -> "REJECTED".equals(o.getStatus().getStatusCode())).count();
        
        BigDecimal totalSpent = allOrders.stream()
            .filter(o -> "CONFIRMED".equals(o.getStatus().getStatusCode()) || 
                        "COMPLETED".equals(o.getStatus().getStatusCode()))
            .map(Order::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        stats.put("totalOrders", allOrders.size());
        stats.put("pendingCount", pendingCount);
        stats.put("confirmedCount", confirmedCount);
        stats.put("deliveredCount", deliveredCount);
        stats.put("rejectedCount", rejectedCount);
        stats.put("totalSpent", totalSpent);
        
        return stats;
    }

    public Instant getTimeWhenStatusReached(Long orderId, String status) {
        OrderStatus orderStatus = new OrderStatusConverter().convertToEntityAttribute(status);
        return statusUpdateService.getTimeWhenStatusReached(orderId, orderStatus).orElseThrow(() -> new StatusNeverReachedException(orderId, orderStatus));
    }
}