package ar.uba.fi.ingsoft1.sistema_comedores.orders.notifications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderSseControllerTest {

    @Mock
    private OrderNotificationService notificationService;

    @InjectMocks
    private OrderSseController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testStreamReturnsEmitter() {
        SseEmitter fakeEmitter = new SseEmitter();

        when(notificationService.subscribe("test@example.com"))
                .thenReturn(fakeEmitter);

        SseEmitter result = controller.stream("test@example.com");

        assertNotNull(result);
        assertEquals(fakeEmitter, result);
        verify(notificationService).subscribe("test@example.com");
    }
}
