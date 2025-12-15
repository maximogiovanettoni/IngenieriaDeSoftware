package ar.uba.fi.ingsoft1.sistema_comedores.orders.notifications;

import ar.uba.fi.ingsoft1.sistema_comedores.orders.Order;
import ar.uba.fi.ingsoft1.sistema_comedores.orders.status.ConfirmedOrderStatus;
import ar.uba.fi.ingsoft1.sistema_comedores.user.User;
import ar.uba.fi.ingsoft1.sistema_comedores.user.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderNotificationServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderNotificationService notificationService;

    private User testUser;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");

        testOrder = new Order();
        testOrder.setOrderNumber(123L);
        testOrder.setUserId(1L);

        testOrder.setStatus(new ConfirmedOrderStatus());
    }

    @Test
    void testSubscribeCreatesEmitter() {
        SseEmitter emitter = notificationService.subscribe("test@example.com");
        assertNotNull(emitter);
    }

    @Test
    void testNotifyStatusChangeSendsEvent() {

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        SseEmitter emitter = notificationService.subscribe("test@example.com");

        assertDoesNotThrow(() -> notificationService.notifyStatusChange(testOrder));

        assertNotNull(emitter);
    }

    @Test
    void testNoEmittersDoesNotThrow() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        assertDoesNotThrow(() -> notificationService.notifyStatusChange(testOrder));
    }

    @Test
    void testNotifyStatusChangeUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> notificationService.notifyStatusChange(testOrder));
    }
}
