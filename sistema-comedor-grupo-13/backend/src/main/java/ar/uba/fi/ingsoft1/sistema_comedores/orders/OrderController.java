package ar.uba.fi.ingsoft1.sistema_comedores.orders;

import ar.uba.fi.ingsoft1.sistema_comedores.config.security.JwtUserDetails;
import ar.uba.fi.ingsoft1.sistema_comedores.orders.dto.CreateOrderRequest;
import ar.uba.fi.ingsoft1.sistema_comedores.orders.dto.OrderDetailsResponse;
import ar.uba.fi.ingsoft1.sistema_comedores.orders.dto.promotion.CalculatePromotionRequest;
import ar.uba.fi.ingsoft1.sistema_comedores.orders.dto.promotion.CalculatePromotionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
@Tag(name = "Orders", description = "Endpoints para gestión de pedidos")
public class OrderController {
    
    private final OrderService orderService;
    
    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('STUDENT')")
    @Operation(summary = "Crear nuevo pedido", description = "Crea un nuevo pedido. Solo STUDENT")
    public ResponseEntity<?> createOrder(
        @Valid @RequestBody CreateOrderRequest request,
        @AuthenticationPrincipal JwtUserDetails userDetails
    ) {
        OrderDetailsResponse response = orderService.createOrder(request, userDetails.username());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
            "success", true,
            "message", "Pedido creado exitosamente",
            "order", response
        ));
    }

    @PostMapping("/calculate-promotion")
    @PreAuthorize("hasAuthority('STUDENT')")
    @Operation(summary = "Calcular promoción aplicable", description = "Calcula qué promoción se aplicaría sin crear la orden")
    public ResponseEntity<?> calculatePromotion(
        @Valid @RequestBody CalculatePromotionRequest request
    ) {
        CalculatePromotionResponse response = orderService.calculateAppliablePromotions(request.items());
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", response
        ));
    }

    @GetMapping("/my-orders")
    @PreAuthorize("hasAuthority('STUDENT')")
    @Operation(summary = "Obtener mis pedidos", description = "Retorna los pedidos del usuario autenticado")
    public ResponseEntity<List<OrderDetailsResponse>> getMyOrders(
        @AuthenticationPrincipal JwtUserDetails userDetails
    ) {
        List<OrderDetailsResponse> orders = orderService.getUserOrders(userDetails.username());
        return ResponseEntity.ok(orders);
    }
    
    @PostMapping("/{orderId}/cancel")
    @PreAuthorize("hasAuthority('STUDENT')")
    @Operation(summary = "Cancelar pedido", description = "Cancela un pedido propio si está en estado cancelable")
    public ResponseEntity<?> cancelOrder(
        @PathVariable Long orderId,
        @RequestBody(required = false) Map<String, String> request,
        @AuthenticationPrincipal JwtUserDetails userDetails
    ) {
        String username = userDetails.username();
        if (!orderService.userCanAccessOrder(orderId, username)) {
            throw new AccessDeniedException("Usuario " + username + " no autorizado para cancelar este pedido");
        }

        String reason = request != null ? request.getOrDefault("reason", "") : "";

        OrderDetailsResponse order = orderService.cancelOrder(orderId, reason);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Pedido cancelado",
            "order", order
        ));
    }

    @GetMapping("/my-stats")
    @PreAuthorize("hasAuthority('STUDENT')")
    @Operation(summary = "Mis estadísticas", description = "Retorna estadísticas del usuario")
    public ResponseEntity<?> getMyStats(
        @AuthenticationPrincipal JwtUserDetails userDetails
    ) {        
        Map<String, Object> stats = orderService.getOrderTrackingStats(userDetails.username());
        return ResponseEntity.ok(Map.of("success", true, "stats", stats));
    }
    
    @GetMapping
    @PreAuthorize("hasAnyAuthority('STAFF','ADMIN')")
    @Operation(summary = "Obtener todos los pedidos", description = "STAFF/ADMIN: retorna todos los pedidos")
    public ResponseEntity<List<OrderDetailsResponse>> getAllOrders() {
        List<OrderDetailsResponse> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/pending")
    @PreAuthorize("hasAnyAuthority('STAFF','ADMIN')")
    @Operation(summary = "Pedidos pendientes", description = "PENDING")
    public ResponseEntity<List<OrderDetailsResponse>> getPendingOrders() {
        List<OrderDetailsResponse> orders = orderService.getPendingOrders();
        return ResponseEntity.ok(orders);
    }
    
    @PutMapping("/{orderId}/move-forward")
    @PreAuthorize("hasAnyAuthority('STAFF','ADMIN')")
    @Operation(summary = "Avanzar pedido", description = "Avanza el pedido al siguiente estado del flujo")
    public ResponseEntity<?> moveOrderForward(@PathVariable Long orderId) {
        OrderDetailsResponse order = orderService.moveOrderForward(orderId);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Pedido avanzado al siguiente estado",
            "order", order
        ));
    }
    
    @PutMapping("/{orderId}/move-backward")
    @PreAuthorize("hasAnyAuthority('STAFF','ADMIN')")
    @Operation(summary = "Retroceder pedido", description = "Retrocede el pedido al estado anterior")
    public ResponseEntity<?> moveOrderBackward(@PathVariable Long orderId) {
        OrderDetailsResponse order = orderService.moveOrderBackward(orderId);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Pedido retrocedido al estado anterior",
            "order", order
        ));
    }

    @PutMapping("/{orderId}/reject")
    @PreAuthorize("hasAnyAuthority('STAFF','ADMIN')")
    @Operation(summary = "Rechazar pedido", description = "Rechaza un pedido con motivo")
    public ResponseEntity<?> rejectOrder(
        @PathVariable Long orderId,
        @RequestBody Map<String, String> request
    ) {
        String reason = request.getOrDefault("reason", "Sin motivo especificado");
        OrderDetailsResponse order = orderService.rejectOrder(orderId, reason);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Pedido rechazado",
            "order", order
        ));
    }

    @PostMapping("/{orderId}/force-cancel")
    @PreAuthorize("hasAnyAuthority('STAFF','ADMIN')")
    @Operation(summary = "Forzar cancelación", description = "STAFF/ADMIN puede cancelar cualquier pedido")
    public ResponseEntity<?> forceCancelOrder(
        @PathVariable Long orderId,
        @RequestBody(required = false) Map<String, String> request
    ) {
        String reason = request != null ? request.getOrDefault("reason", "Cancelado por staff") : "Cancelado por staff";
        OrderDetailsResponse order = orderService.forceCancelOrder(orderId, reason);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Pedido cancelado por staff",
            "order", order
        ));
    }
    
    @GetMapping("/{orderId}")
    @PreAuthorize("authenticated()")
    @Operation(summary = "Obtener detalle del pedido")
    public ResponseEntity<?> getOrder(
        @PathVariable Long orderId,
        @AuthenticationPrincipal JwtUserDetails userDetails
    ) {
        String username = userDetails.username();
        if (!orderService.userCanAccessOrder(orderId, username)) {
            throw new AccessDeniedException("Usuario " + username + " no autorizado para ver este pedido");
        }

        OrderDetailsResponse order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/{orderId}/status/{status}")
    @PreAuthorize("authenticated()")
    @Operation(summary = "Obtener timestamp de estado del pedido")
    public ResponseEntity<?> getOrderStatusTimestamp(
        @PathVariable Long orderId,
        @PathVariable String status,
        @AuthenticationPrincipal JwtUserDetails userDetails
    ) {
        String username = userDetails.username();
        if (!orderService.userCanAccessOrder(orderId, username)) {
            throw new AccessDeniedException("Usuario " + username + " no autorizado para ver este pedido");
        }

        Instant timestamp = orderService.getTimeWhenStatusReached(orderId, status);
        return ResponseEntity.ok(timestamp);
    }
}