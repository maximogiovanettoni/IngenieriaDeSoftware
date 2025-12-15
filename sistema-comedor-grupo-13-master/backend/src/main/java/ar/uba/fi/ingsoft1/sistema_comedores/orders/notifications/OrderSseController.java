package ar.uba.fi.ingsoft1.sistema_comedores.orders.notifications;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/orders/notifications")
public class OrderSseController {

    private final OrderNotificationService notificationService;

    public OrderSseController(OrderNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam String email) {
        System.out.println("SSE subscription initialized");
        return notificationService.subscribe(email);
    }
}
