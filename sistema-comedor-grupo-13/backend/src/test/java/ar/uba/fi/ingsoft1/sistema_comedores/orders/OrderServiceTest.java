package ar.uba.fi.ingsoft1.sistema_comedores.orders;

import ar.uba.fi.ingsoft1.sistema_comedores.orders.dto.CreateOrderRequest;
import ar.uba.fi.ingsoft1.sistema_comedores.orders.dto.OrderItemRequest;
import ar.uba.fi.ingsoft1.sistema_comedores.orders.dto.OrderDetailsResponse;
import ar.uba.fi.ingsoft1.sistema_comedores.orders.notifications.OrderNotificationService;
import ar.uba.fi.ingsoft1.sistema_comedores.orders.status.history.OrderStatusUpdateService;
import ar.uba.fi.ingsoft1.sistema_comedores.products.ProductCategory;
import ar.uba.fi.ingsoft1.sistema_comedores.products.ProductRepository;
import ar.uba.fi.ingsoft1.sistema_comedores.products.ProductService;
import ar.uba.fi.ingsoft1.sistema_comedores.products.simple.SimpleProduct;
import ar.uba.fi.ingsoft1.sistema_comedores.user.User;
import ar.uba.fi.ingsoft1.sistema_comedores.user.UserService;
import ar.uba.fi.ingsoft1.sistema_comedores.promotions.PromotionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderServiceTest {
    
    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private ProductService productService;
    
    @Mock
    private UserService userService;
    
    @Mock
    private PromotionService promotionService;
    
    @Mock
    private OrderStatusUpdateService statusUpdateService;
    
    @Mock
    private OrderNotificationService notificationService;
    
    @InjectMocks
    private OrderService orderService;
    
    private User testUser;
    private SimpleProduct testProduct;
    private OrderItemRequest testItemRequest;
    
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        
        testProduct = new SimpleProduct("Pizza", "Deliciosa pizza", new BigDecimal("15.99"), ProductCategory.MAIN_COURSE, true, 100);
        testProduct.setId(1L);
        
        testItemRequest = new OrderItemRequest(1L, 2);
    }
    
    @Test
    @Disabled("Test requires complex mocking setup - needs refactoring")
    void testCreateOrderSuccessfully() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest(List.of(testItemRequest), null);
        
        when(userService.getUserByEmail("test@example.com")).thenReturn(testUser);
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        
        Order savedOrder = new Order();
        savedOrder.setOrderNumber(1L);
        savedOrder.setUserId(1L);
        savedOrder.setSubtotal(new BigDecimal("31.98"));
        
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        
        // Act
        OrderDetailsResponse response = orderService.createOrder(request, "test@example.com");
        
        // Assert
        assertNotNull(response);
        assertEquals(new BigDecimal("31.98"), response.subtotal());
        
        verify(orderRepository).save(any(Order.class));
        verify(userService).getUserByEmail("test@example.com");
    }
    
    @Test
    @Disabled("Test requires complex mocking setup - needs refactoring")
    void testCreateOrderWithEmptyItems() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest(List.of(), null);
        String email = "test@example.com";
        
        when(userService.getUserByEmail(email)).thenReturn(testUser);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            orderService.createOrder(request, email);
        });
    }
    
    @Test
    @Disabled("Test requires complex mocking setup - needs refactoring")
    void testGetUserOrders() {
        // Arrange
        Order order = new Order();
        order.setOrderNumber(1L);
        order.setUserId(1L);
        order.setSubtotal(new BigDecimal("50.00"));
        
        when(userService.getUserByEmail("test@example.com")).thenReturn(testUser);
        when(orderRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(order));
        
        // Act
        List<OrderDetailsResponse> response = orderService.getUserOrders("test@example.com");
        
        // Assert
        assertNotNull(response);
        assertEquals(1, response.size());
        verify(userService).getUserByEmail("test@example.com");
    }
    
    @Test
    @Disabled("Test requires complex mocking setup - needs refactoring")
    void testCancelOrderSuccessfully() {
        // Arrange
        Order order = new Order();
        order.setOrderNumber(1L);
        order.setUserId(1L);
        order.setSubtotal(new BigDecimal("50.00"));
        
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        
        // Act
        OrderDetailsResponse response = orderService.cancelOrder(1L, "cancel_order_test");
        
        // Assert
        assertNotNull(response);
        verify(orderRepository).save(any(Order.class));
    }
}

