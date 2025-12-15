package ar.uba.fi.ingsoft1.sistema_comedores.products.combos;

import ar.uba.fi.ingsoft1.sistema_comedores.products.combos.dto.ComboDetailsResponse;
import ar.uba.fi.ingsoft1.sistema_comedores.products.combos.dto.CreateComboRequest;
import ar.uba.fi.ingsoft1.sistema_comedores.products.combos.dto.UpdateComboNameRequest;
import ar.uba.fi.ingsoft1.sistema_comedores.products.combos.dto.UpdateComboPriceRequest;
import ar.uba.fi.ingsoft1.sistema_comedores.products.combos.exception.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.NonNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/combos")
@Tag(name = "Combos")
public class ComboController {

    private final ComboService comboService;

    @Autowired
    public ComboController(ComboService comboService) {
        this.comboService = comboService;
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener todos los combos")
    @ApiResponse(responseCode = "200", description = "Lista de todos los combos obtenida exitosamente")
    @ApiResponse(responseCode = "403", description = "Acceso denegado - requiere rol ADMIN", content = @Content)
    public ResponseEntity<?> getAllCombos() {
        List<ComboDetailsResponse> combos = comboService.getAllCombos();
        return ResponseEntity.ok(combos);
    }

    @GetMapping
    @Operation(summary = "Obtener combos activos")
    @ApiResponse(responseCode = "200", description = "Lista de combos activos obtenida exitosamente")
    public ResponseEntity<List<ComboDetailsResponse>> getActiveCombos() {
        List<ComboDetailsResponse> combos = comboService.getActiveCombos();
        return ResponseEntity.ok(combos);
    }

    @GetMapping("/available")
    @Operation(summary = "Obtener solo combos disponibles")
    @ApiResponse(responseCode = "200", description = "Lista de combos disponibles obtenida exitosamente")
    public ResponseEntity<List<ComboDetailsResponse>> getAvailableCombos() {
        List<ComboDetailsResponse> combos = comboService.getAvailableCombos();
        return ResponseEntity.ok(combos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener combo por ID")
    @ApiResponse(responseCode = "200", description = "Combo encontrado")
    @ApiResponse(responseCode = "404", description = "Combo no encontrado", content = @Content)
    public ResponseEntity<?> getComboById(@PathVariable Long id) {
        ComboDetailsResponse combo = comboService.getComboById(id);
        return ResponseEntity.ok(combo);
    }

    @GetMapping("/{id}/availability")
    @Operation(summary = "Verificar disponibilidad de un combo")
    @ApiResponse(responseCode = "200", description = "Disponibilidad verificada")
    @ApiResponse(responseCode = "404", description = "Combo no encontrado", content = @Content)
    public ResponseEntity<?> checkAvailability(@PathVariable Long id) {
        boolean isAvailable = comboService.checkAvailability(id);
        return ResponseEntity.ok(Map.of(
            "comboId", id,
            "isAvailable", isAvailable
        ));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear un nuevo combo")
    @ApiResponse(responseCode = "201", description = "Combo creado exitosamente")
    @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    @ApiResponse(responseCode = "403", description = "Acceso denegado - requiere rol ADMIN", content = @Content)
    public ResponseEntity<?> createCombo(@Valid @NonNull @RequestBody CreateComboRequest data) {
        ComboDetailsResponse result = comboService.createCombo(data);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Desactivar un combo")
    @ApiResponse(responseCode = "200", description = "Combo desactivado exitosamente")
    @ApiResponse(responseCode = "403", description = "Acceso denegado - requiere rol ADMIN", content = @Content)
    @ApiResponse(responseCode = "404", description = "Combo no encontrado", content = @Content)
    public ResponseEntity<?> deactivateCombo(@PathVariable Long id) {
        comboService.deactivateCombo(id);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Combo desactivado exitosamente"
        ));
    }

    @PutMapping("/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Restaurar un combo desactivado (ADMIN)")
    @ApiResponse(responseCode = "200", description = "Combo restaurado exitosamente")
    @ApiResponse(responseCode = "403", description = "Acceso denegado - requiere rol ADMIN", content = @Content)
    @ApiResponse(responseCode = "404", description = "Combo no encontrado", content = @Content)
    public ResponseEntity<?> restoreCombo(@PathVariable Long id) {
        comboService.restoreCombo(id);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Combo restaurado exitosamente"
        ));
    }

    @PutMapping("/{id}/name")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cambiar nombre del combo")
    @ApiResponse(responseCode = "200", description = "Nombre del combo actualizado exitosamente")
    @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    @ApiResponse(responseCode = "403", description = "Acceso denegado - requiere rol ADMIN", content = @Content)
    @ApiResponse(responseCode = "404", description = "Combo no encontrado", content = @Content)
    @ApiResponse(responseCode = "409", description = "El nombre ya existe", content = @Content)
    public ResponseEntity<?> changeComboName(
            @PathVariable Long id,
            @Valid @RequestBody UpdateComboNameRequest data
    ) {
        comboService.changeComboName(id, data.name());
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Nombre del combo actualizado exitosamente",
            "combo", data.name()
        ));
    }

    @PutMapping("/{id}/price")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cambiar precio del combo (ADMIN)")
    @ApiResponse(responseCode = "200", description = "Precio del combo actualizado exitosamente")
    @ApiResponse(responseCode = "400", description = "Datos inválidos o precio superior al precio regular", content = @Content)
    @ApiResponse(responseCode = "403", description = "Acceso denegado - requiere rol ADMIN", content = @Content)
    @ApiResponse(responseCode = "404", description = "Combo no encontrado", content = @Content)
    public ResponseEntity<?> changeComboPrice(
            @PathVariable Long id,
            @Valid @RequestBody UpdateComboPriceRequest data
    ) {
        comboService.changeComboPrice(id, data.price());
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Precio del combo actualizado exitosamente",
            "combo", data.price()
        ));
    }

    @PutMapping("/{id}/image")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cambiar imagen del combo (ADMIN)")
    @ApiResponse(responseCode = "200", description = "Imagen del combo actualizada exitosamente")
    @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    @ApiResponse(responseCode = "403", description = "Acceso denegado - requiere rol ADMIN", content = @Content)
    @ApiResponse(responseCode = "404", description = "Combo no encontrado", content = @Content)
    public ResponseEntity<?> changeComboImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile newImageFile
    ) {
        comboService.changeComboImage(id, newImageFile);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Imagen del combo actualizada exitosamente"
        ));
    }
}
