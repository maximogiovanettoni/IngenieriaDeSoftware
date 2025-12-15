package ar.uba.fi.ingsoft1.sistema_comedores.orders.notifications;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import ar.uba.fi.ingsoft1.sistema_comedores.orders.Order;
import ar.uba.fi.ingsoft1.sistema_comedores.user.User;
import ar.uba.fi.ingsoft1.sistema_comedores.user.UserRepository;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class OrderNotificationService {

    private final UserRepository userRepository;

    public OrderNotificationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    
    private final Map<String, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String email) {
        System.out.println("SSE subscription started");

        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        emitters.computeIfAbsent(email, e -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> {
            System.out.println("SSE emitter completed");
            removeEmitter(email, emitter);
        });

        emitter.onTimeout(() -> {
            System.out.println("SSE emitter timeout");
            removeEmitter(email, emitter);
        });

        emitter.onError((e) -> {
            System.out.println("SSE emitter error: " + e.getMessage());
            removeEmitter(email, emitter);
        });

        try {
            emitter.send(
                SseEmitter.event()
                    .name("connected")
                    .data("SSE connection established")
            );
        } catch (Exception ignored) {
            System.out.println("Failed to send initial SSE 'connected' event");
        }

        return emitter;
    }

    private void removeEmitter(String email, SseEmitter emitter) {
        List<SseEmitter> list = emitters.get(email);

        if (list != null) {
            list.remove(emitter);

            if (list.isEmpty()) {
                emitters.remove(email);
            }
        }

        System.out.println("SSE emitter removed");
    }

    public void sendOrderUpdate(String email, Object event) {
        List<SseEmitter> list = emitters.get(email);

        if (list == null || list.isEmpty()) {
            System.out.println("No active SSE connections");
            return;
        }

        for (SseEmitter emitter : list) {
            try {
                emitter.send(
                    SseEmitter.event()
                        .name("order-status-update")
                        .data(event)
                );
            } catch (IOException e) {
                System.out.println("Emitter send failed — removing emitter");
                removeEmitter(email, emitter);
            }
        }
    }

    public void notifyStatusChange(Order order) {
        if (order == null) return;

        User user = userRepository.findById(order.getUserId()).orElse(null);
        if (user == null) {
            System.out.println("User not found for order");
            return;
        }

        sendOrderUpdate(
            user.getEmail(),
            new OrderStatusDTO(
                order.getOrderNumber(),
                order.getStatus().getStatusCode(),
                user.getEmail()
            )
        );
    }
}
