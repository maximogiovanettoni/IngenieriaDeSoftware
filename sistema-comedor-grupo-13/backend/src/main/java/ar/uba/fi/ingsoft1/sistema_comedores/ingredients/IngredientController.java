package ar.uba.fi.ingsoft1.sistema_comedores.ingredients;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import jakarta.validation.Valid;

import java.util.List;
import ar.uba.fi.ingsoft1.sistema_comedores.ingredients.dto.CreateIngredientRequest;
import ar.uba.fi.ingsoft1.sistema_comedores.ingredients.dto.UpdateIngredientStockRequest;
import ar.uba.fi.ingsoft1.sistema_comedores.ingredients.dto.UpdateIngredientNameDTO;
import ar.uba.fi.ingsoft1.sistema_comedores.ingredients.exception.IngredientNotFoundException;
import ar.uba.fi.ingsoft1.sistema_comedores.ingredients.dto.IngredientDetailsResponse;

@RestController
@RequestMapping("/ingredients")
@Tag(name = "Ingredients")
public class IngredientController {

    private final IngredientService service;

    @Autowired
    public IngredientController(IngredientService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List ingredients")
    public ResponseEntity<List<IngredientDetailsResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @PostMapping
    @Operation(summary = "Create ingredient")
    public ResponseEntity<IngredientDetailsResponse> create(@RequestBody @Valid CreateIngredientRequest dto) {
        Ingredient saved = service.createIngredient(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(IngredientDetailsResponse.from(saved));
    }

    @PutMapping("/{id}/stock")
    @Operation(summary = "Update stock to ingredient")
    public ResponseEntity<IngredientDetailsResponse> updateStock(@PathVariable Long id, @RequestBody @Valid UpdateIngredientStockRequest dto) throws IngredientNotFoundException {
        Ingredient saved = service.updateStock(id, dto.amount);
        return ResponseEntity.ok(IngredientDetailsResponse.from(saved));
    }

    @PutMapping("/{id}/name")
    @Operation(summary = "Change ingredient name (name must be unique)")
    public ResponseEntity<IngredientDetailsResponse> changeName(@PathVariable Long id, @RequestBody @Valid UpdateIngredientNameDTO dto) throws IngredientNotFoundException {
        Ingredient saved = service.changeName(id, dto.name);
        return ResponseEntity.ok(IngredientDetailsResponse.from(saved));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete (deactivate) ingredient")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestBody(required = false) @jakarta.validation.Valid ar.uba.fi.ingsoft1.sistema_comedores.ingredients.dto.DeleteIngredientRequest req) throws IngredientNotFoundException {
        String reason = req == null ? null : req.reason;
        service.deactivateIngredient(id, reason);
        return ResponseEntity.noContent().build();
    }

}
