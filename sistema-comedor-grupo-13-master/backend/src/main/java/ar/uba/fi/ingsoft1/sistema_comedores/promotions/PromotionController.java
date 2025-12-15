package ar.uba.fi.ingsoft1.sistema_comedores.promotions;

import ar.uba.fi.ingsoft1.sistema_comedores.promotions.dto.PromotionRequest;
import ar.uba.fi.ingsoft1.sistema_comedores.promotions.dto.UpdatePromotionRequest;
import ar.uba.fi.ingsoft1.sistema_comedores.promotions.dto.PromotionDetailsResponse;
import ar.uba.fi.ingsoft1.sistema_comedores.promotions.dto.UpdatePromotionRequest;
import ar.uba.fi.ingsoft1.sistema_comedores.promotions.exception.PromotionAlreadyExistsException;
import ar.uba.fi.ingsoft1.sistema_comedores.promotions.exception.InvalidPromotionException;
import ar.uba.fi.ingsoft1.sistema_comedores.promotions.exception.PromotionNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/promotions")
@RequiredArgsConstructor
@Tag(name = "Promotions", description = "API para gestión de promociones")
public class PromotionController {

    private final PromotionService promotionService;

    @GetMapping
    @Operation(summary = "Obtener todas las promociones")
    @ApiResponse(responseCode = "200", description = "Lista de promociones obtenida exitosamente")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<List<PromotionDetailsResponse>> getAllPromotions() {
        List<PromotionDetailsResponse> promotions = promotionService.getAllPromotions();
        return ResponseEntity.ok(promotions);
    }

    @GetMapping("/active")
    @Operation(summary = "Obtener promociones activas")
    @ApiResponse(responseCode = "200", description = "Lista de promociones activas obtenida exitosamente")
    public ResponseEntity<List<PromotionDetailsResponse>> getActivePromotions() {
        List<PromotionDetailsResponse> promotions = promotionService.getActivePromotions();
        return ResponseEntity.ok(promotions);
    }

    @GetMapping("/valid")
    @Operation(summary = "Obtener promociones actualmente válidas")
    @ApiResponse(responseCode = "200", description = "Lista de promociones válidas obtenida exitosamente")
    public ResponseEntity<List<PromotionDetailsResponse>> getCurrentlyValidPromotions() {
        List<PromotionDetailsResponse> promotions = promotionService.getCurrentlyValidPromotions();
        return ResponseEntity.ok(promotions);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener promoción por ID")
    @ApiResponse(responseCode = "200", description = "Promoción encontrada")
    @ApiResponse(responseCode = "404", description = "Promoción no encontrada", content = @Content)
    public ResponseEntity<?> getPromotionById(@PathVariable Long id) {
        try {
            PromotionDetailsResponse promotion = promotionService.getPromotionById(id);
            return ResponseEntity.ok(promotion);
        } catch (PromotionNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "error", "PROMOTION_NOT_FOUND",
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping
    @Operation(summary = "Crear nueva promoción")
    @ApiResponse(responseCode = "201", description = "Promoción creada exitosamente")
    @ApiResponse(responseCode = "400", description = "Datos de promoción inválidos", content = @Content)
    @ApiResponse(responseCode = "409", description = "Ya existe una promoción con ese nombre", content = @Content)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createPromotion(@Valid @RequestBody PromotionRequest request) {
        try {
            promotionService.createPromotion(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true,
                    "message", "Promoción creada exitosamente."
            ));
        } catch (PromotionAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "success", false,
                    "error", "DUPLICATE_PROMOTION",
                    "message", e.getMessage()
            ));
        } catch (InvalidPromotionException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "error", "INVALID_PROMOTION",
                    "message", e.getMessage()
            ));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar promoción existente")
    @ApiResponse(responseCode = "200", description = "Promoción actualizada exitosamente")
    @ApiResponse(responseCode = "404", description = "Promoción no encontrada", content = @Content)
    @ApiResponse(responseCode = "400", description = "Datos de promoción inválidos", content = @Content)
    @ApiResponse(responseCode = "409", description = "Ya existe una promoción con ese nombre", content = @Content)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updatePromotion(
            @PathVariable Long id,
            @NotNull @Valid @RequestBody UpdatePromotionRequest request) {
        try {
            promotionService.updatePromotion(id, request);
            return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                    "success", true,
                    "message", "Promoción actualizada exitosamente."
            ));
        } catch (PromotionNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "error", "PROMOTION_NOT_FOUND",
                    "message", e.getMessage()
            ));
        } catch (PromotionAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "success", false,
                    "error", "DUPLICATE_PROMOTION",
                    "message", e.getMessage()
            ));
        } catch (InvalidPromotionException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "error", "INVALID_PROMOTION",
                    "message", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar promoción")
    @ApiResponse(responseCode = "204", description = "Promoción eliminada exitosamente")
    @ApiResponse(responseCode = "404", description = "Promoción no encontrada", content = @Content)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletePromotion(@PathVariable Long id) {
        try {
            promotionService.deletePromotion(id);
            return ResponseEntity.noContent().build();
        } catch (PromotionNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "error", "PROMOTION_NOT_FOUND",
                    "message", e.getMessage()
            ));
        }
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activar promoción")
    @ApiResponse(responseCode = "200", description = "Promoción activada exitosamente")
    @ApiResponse(responseCode = "404", description = "Promoción no encontrada", content = @Content)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> activatePromotion(@PathVariable Long id) {
        try {
            PromotionDetailsResponse promotion = promotionService.activatePromotion(id);
            return ResponseEntity.ok(promotion);
        } catch (PromotionNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "error", "PROMOTION_NOT_FOUND",
                    "message", e.getMessage()
            ));
        }
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Desactivar promoción")
    @ApiResponse(responseCode = "200", description = "Promoción desactivada exitosamente")
    @ApiResponse(responseCode = "404", description = "Promoción no encontrada", content = @Content)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deactivatePromotion(@PathVariable Long id) {
        try {
            PromotionDetailsResponse promotion = promotionService.deactivatePromotion(id);
            return ResponseEntity.ok(promotion);
        } catch (PromotionNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "error", "PROMOTION_NOT_FOUND",
                    "message", e.getMessage()
            ));
        }
    }
}