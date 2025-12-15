package ar.uba.fi.ingsoft1.sistema_comedores.ingredients;

import ar.uba.fi.ingsoft1.sistema_comedores.ingredients.dto.CreateIngredientRequest;
import ar.uba.fi.ingsoft1.sistema_comedores.ingredients.dto.IngredientDetailsResponse;
import ar.uba.fi.ingsoft1.sistema_comedores.ingredients.exception.IngredientAlreadyExistsException;
import ar.uba.fi.ingsoft1.sistema_comedores.ingredients.exception.IngredientNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class IngredientIntegrationTest {

    @Autowired
    private IngredientService service;

    @Autowired
    private IngredientRepository repository;

    @Autowired
    private IngredientAuditLogRepository auditRepo;

    // Helper para generar nombres únicos y evitar conflictos con datos existentes
    private String uniqueName(String base) {
        return base + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    // ==================== Full Flow Tests ====================

    @Test
    void createAndRetrieveIngredient() {
        String name = uniqueName("Tomate");
        CreateIngredientRequest request = new CreateIngredientRequest(
                name, "kg", BigDecimal.TEN
        );

        Ingredient created = service.createIngredient(request);

        assertNotNull(created.getId());
        assertEquals(name, created.getName());

        List<IngredientDetailsResponse> all = service.getAll();
        assertTrue(all.stream().anyMatch(i -> i.name().equals(name)));
    }

    @Test
    void createUpdateAndDeactivateIngredient() throws IngredientNotFoundException {
        String name = uniqueName("Cebolla");

        // Create
        CreateIngredientRequest request = new CreateIngredientRequest(
                name, "kg", BigDecimal.valueOf(5)
        );
        Ingredient created = service.createIngredient(request);
        Long id = created.getId();

        // Update stock
        Ingredient updated = service.updateStock(id, BigDecimal.valueOf(15));
        assertEquals(BigDecimal.valueOf(15), updated.getStock());

        // Change name
        String newName = uniqueName("CebollaMorada");
        Ingredient renamed = service.changeName(id, newName);
        assertEquals(newName, renamed.getName());

        // Deactivate
        IngredientDetailsResponse deactivated = service.deactivateIngredient(id, "Test");
        assertFalse(deactivated.active());

        // Verify not in active list
        List<IngredientDetailsResponse> all = service.getAll();
        assertTrue(all.stream().noneMatch(i -> i.id().equals(id)));
    }

    @Test
    void reactivateIngredient() throws IngredientNotFoundException {
        String name = uniqueName("Zanahoria");

        // Create and deactivate
        CreateIngredientRequest request = new CreateIngredientRequest(
                name, "unidad", BigDecimal.TEN
        );
        Ingredient created = service.createIngredient(request);
        service.deactivateIngredient(created.getId(), null);

        // Reactivate by creating with same name
        CreateIngredientRequest reactivateRequest = new CreateIngredientRequest(
                name, "unidad", BigDecimal.valueOf(20)
        );
        Ingredient reactivated = service.createIngredient(reactivateRequest);

        assertTrue(reactivated.isActive());
        assertEquals(BigDecimal.valueOf(20), reactivated.getStock());
        assertEquals(created.getId(), reactivated.getId());
    }

    @Test
    void duplicateNameThrowsException() {
        String name = uniqueName("Lechuga");

        CreateIngredientRequest request1 = new CreateIngredientRequest(
                name, "unidad", BigDecimal.ONE
        );
        service.createIngredient(request1);

        CreateIngredientRequest request2 = new CreateIngredientRequest(
                name, "kg", BigDecimal.TEN
        );

        assertThrows(IngredientAlreadyExistsException.class,
                () -> service.createIngredient(request2));
    }

    @Test
    void auditLogsAreCreated() {
        String name = uniqueName("Pimiento");
        long initialLogCount = auditRepo.count();

        // Create
        CreateIngredientRequest request = new CreateIngredientRequest(
                name, "kg", BigDecimal.valueOf(8)
        );
        service.createIngredient(request);

        // Verify audit log was created
        long finalLogCount = auditRepo.count();
        assertTrue(finalLogCount > initialLogCount);

        // Verify the log has the correct ingredient name
        List<IngredientAuditLog> logs = auditRepo.findAll();
        assertTrue(logs.stream().anyMatch(l -> l.getIngredientName().equals(name)));
    }

    @Test
    void updateStock_createsAuditWithCorrectDelta() throws IngredientNotFoundException {
        String name = uniqueName("Pepino");

        CreateIngredientRequest request = new CreateIngredientRequest(
                name, "unidad", BigDecimal.valueOf(10)
        );
        Ingredient created = service.createIngredient(request);

        service.updateStock(created.getId(), BigDecimal.valueOf(25));

        List<IngredientAuditLog> logs = auditRepo.findAll();

        // Find the UPDATE_STOCK log for this ingredient
        IngredientAuditLog updateLog = logs.stream()
                .filter(l -> l.getIngredientName().equals(name))
                .filter(l -> l.getOperation() == ar.uba.fi.ingsoft1.sistema_comedores.common.enums.AdminOperation.UPDATE_STOCK)
                .findFirst()
                .orElseThrow(() -> new AssertionError("No se encontró log de UPDATE_STOCK"));

        assertEquals(BigDecimal.valueOf(15), updateLog.getAmountDelta());
    }

    @Test
    void getAll_returnsOnlyActiveIngredients() throws IngredientNotFoundException {
        String activeName = uniqueName("IngredienteActivo");
        String inactiveName = uniqueName("IngredienteInactivo");

        // Create two ingredients
        Ingredient active = service.createIngredient(
                new CreateIngredientRequest(activeName, "kg", BigDecimal.ONE)
        );
        Ingredient inactive = service.createIngredient(
                new CreateIngredientRequest(inactiveName, "kg", BigDecimal.ONE)
        );

        // Deactivate one
        service.deactivateIngredient(inactive.getId(), "test");

        // Verify only active is returned
        List<IngredientDetailsResponse> all = service.getAll();
        assertTrue(all.stream().anyMatch(i -> i.name().equals(activeName)));
        assertTrue(all.stream().noneMatch(i -> i.name().equals(inactiveName)));
    }

    @Test
    void changeName_toExistingNameThrowsException() {
        String name1 = uniqueName("Ingrediente1");
        String name2 = uniqueName("Ingrediente2");

        Ingredient ing1 = service.createIngredient(
                new CreateIngredientRequest(name1, "kg", BigDecimal.ONE)
        );
        service.createIngredient(
                new CreateIngredientRequest(name2, "kg", BigDecimal.ONE)
        );

        // Try to rename ing1 to name2
        assertThrows(IngredientAlreadyExistsException.class,
                () -> service.changeName(ing1.getId(), name2));
    }
}