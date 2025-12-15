package ar.uba.fi.ingsoft1.sistema_comedores.ingredients;

import ar.uba.fi.ingsoft1.sistema_comedores.common.enums.AdminOperation;
import ar.uba.fi.ingsoft1.sistema_comedores.ingredients.dto.CreateIngredientRequest;
import ar.uba.fi.ingsoft1.sistema_comedores.ingredients.dto.IngredientDetailsResponse;
import ar.uba.fi.ingsoft1.sistema_comedores.ingredients.exception.IngredientAlreadyExistsException;
import ar.uba.fi.ingsoft1.sistema_comedores.ingredients.exception.IngredientInProductException;
import ar.uba.fi.ingsoft1.sistema_comedores.ingredients.exception.IngredientNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngredientServiceTest {

    @Mock
    private IngredientRepository repository;

    @Mock
    private IngredientAuditLogRepository auditRepo;

    @InjectMocks
    private IngredientService service;

    private Ingredient testIngredient;

    @BeforeEach
    void setUp() {
        testIngredient = new Ingredient();
        testIngredient.setId(1L);
        testIngredient.setName("Tomate");
        testIngredient.setUnitMeasure("kg");
        testIngredient.setStock(BigDecimal.TEN);
        testIngredient.setActive(true);
        testIngredient.setAvailable(true);
        testIngredient.setObservers(new HashSet<>());
    }

    // ==================== getAll Tests ====================

    @Test
    void getAll_returnsActiveIngredients() {
        Ingredient ing2 = new Ingredient();
        ing2.setId(2L);
        ing2.setName("Cebolla");
        ing2.setUnitMeasure("kg");
        ing2.setStock(BigDecimal.valueOf(5));
        ing2.setActive(true);
        ing2.setAvailable(true);

        when(repository.findByActiveTrue()).thenReturn(List.of(testIngredient, ing2));

        List<IngredientDetailsResponse> result = service.getAll();

        assertEquals(2, result.size());
        assertEquals("Tomate", result.get(0).name());
        assertEquals("Cebolla", result.get(1).name());
        verify(repository).findByActiveTrue();
    }

    @Test
    void getAll_returnsEmptyList_whenNoActiveIngredients() {
        when(repository.findByActiveTrue()).thenReturn(List.of());

        List<IngredientDetailsResponse> result = service.getAll();

        assertTrue(result.isEmpty());
    }

    // ==================== createIngredient Tests ====================

    @Test
    void createIngredient_success_newIngredient() {
        CreateIngredientRequest request = new CreateIngredientRequest("Lechuga", "unidad", BigDecimal.valueOf(20));

        when(repository.findByNameAndActiveTrue("Lechuga")).thenReturn(Optional.empty());
        when(repository.findByName("Lechuga")).thenReturn(Optional.empty());
        when(repository.save(any(Ingredient.class))).thenAnswer(inv -> {
            Ingredient i = inv.getArgument(0);
            i.setId(3L);
            return i;
        });
        when(auditRepo.save(any(IngredientAuditLog.class))).thenAnswer(inv -> inv.getArgument(0));

        Ingredient result = service.createIngredient(request);

        assertEquals("Lechuga", result.getName());
        assertEquals("unidad", result.getUnitMeasure());
        assertTrue(result.isAvailable());

        ArgumentCaptor<IngredientAuditLog> logCaptor = ArgumentCaptor.forClass(IngredientAuditLog.class);
        verify(auditRepo).save(logCaptor.capture());
        assertEquals(AdminOperation.CREATE, logCaptor.getValue().getOperation());
    }

    @Test
    void createIngredient_success_withZeroStock() {
        CreateIngredientRequest request = new CreateIngredientRequest("Lechuga", "unidad", BigDecimal.ZERO);

        when(repository.findByNameAndActiveTrue("Lechuga")).thenReturn(Optional.empty());
        when(repository.findByName("Lechuga")).thenReturn(Optional.empty());
        when(repository.save(any(Ingredient.class))).thenAnswer(inv -> {
            Ingredient i = inv.getArgument(0);
            i.setId(3L);
            return i;
        });
        when(auditRepo.save(any(IngredientAuditLog.class))).thenAnswer(inv -> inv.getArgument(0));

        Ingredient result = service.createIngredient(request);

        assertFalse(result.isAvailable());
    }

    @Test
    void createIngredient_success_withNullStock() {
        CreateIngredientRequest request = new CreateIngredientRequest("Lechuga", "unidad", null);

        when(repository.findByNameAndActiveTrue("Lechuga")).thenReturn(Optional.empty());
        when(repository.findByName("Lechuga")).thenReturn(Optional.empty());
        when(repository.save(any(Ingredient.class))).thenAnswer(inv -> {
            Ingredient i = inv.getArgument(0);
            i.setId(3L);
            return i;
        });
        when(auditRepo.save(any(IngredientAuditLog.class))).thenAnswer(inv -> inv.getArgument(0));

        Ingredient result = service.createIngredient(request);

        assertFalse(result.isAvailable());
    }

    @Test
    void createIngredient_reactivatesInactiveIngredient() {
        Ingredient inactive = new Ingredient();
        inactive.setId(5L);
        inactive.setName("Zanahoria");
        inactive.setUnitMeasure("kg");
        inactive.setStock(BigDecimal.ZERO);
        inactive.setActive(false);
        inactive.setAvailable(false);
        inactive.setObservers(new HashSet<>());

        CreateIngredientRequest request = new CreateIngredientRequest("Zanahoria", "kg", BigDecimal.valueOf(15));

        when(repository.findByNameAndActiveTrue("Zanahoria")).thenReturn(Optional.empty());
        when(repository.findByName("Zanahoria")).thenReturn(Optional.of(inactive));
        when(repository.save(any(Ingredient.class))).thenAnswer(inv -> inv.getArgument(0));
        when(auditRepo.save(any(IngredientAuditLog.class))).thenAnswer(inv -> inv.getArgument(0));

        Ingredient result = service.createIngredient(request);

        assertTrue(result.isActive());
        assertEquals(BigDecimal.valueOf(15), result.getStock());

        ArgumentCaptor<IngredientAuditLog> logCaptor = ArgumentCaptor.forClass(IngredientAuditLog.class);
        verify(auditRepo).save(logCaptor.capture());
        assertEquals(AdminOperation.REACTIVATE, logCaptor.getValue().getOperation());
    }

    @Test
    void createIngredient_reactivatesWithNullStock() {
        Ingredient inactive = new Ingredient();
        inactive.setId(5L);
        inactive.setName("Zanahoria");
        inactive.setUnitMeasure("kg");
        inactive.setStock(BigDecimal.ZERO);
        inactive.setActive(false);
        inactive.setAvailable(false);
        inactive.setObservers(new HashSet<>());

        CreateIngredientRequest request = new CreateIngredientRequest("Zanahoria", "kg", null);

        when(repository.findByNameAndActiveTrue("Zanahoria")).thenReturn(Optional.empty());
        when(repository.findByName("Zanahoria")).thenReturn(Optional.of(inactive));
        when(repository.save(any(Ingredient.class))).thenAnswer(inv -> inv.getArgument(0));
        when(auditRepo.save(any(IngredientAuditLog.class))).thenAnswer(inv -> inv.getArgument(0));

        Ingredient result = service.createIngredient(request);

        assertTrue(result.isActive());
        assertFalse(result.isAvailable());
    }

    @Test
    void createIngredient_throwsException_whenActiveIngredientExists() {
        CreateIngredientRequest request = new CreateIngredientRequest("Tomate", "kg", BigDecimal.TEN);

        when(repository.findByNameAndActiveTrue("Tomate")).thenReturn(Optional.of(testIngredient));

        assertThrows(IngredientAlreadyExistsException.class, () -> service.createIngredient(request));
        verify(repository, never()).save(any());
    }

    // ==================== changeName Tests ====================

    @Test
    void changeName_success() throws IngredientNotFoundException {
        when(repository.findById(1L)).thenReturn(Optional.of(testIngredient));
        when(repository.findByNameAndActiveTrue("Tomate Perita")).thenReturn(Optional.empty());
        when(repository.save(any(Ingredient.class))).thenAnswer(inv -> inv.getArgument(0));
        when(auditRepo.save(any(IngredientAuditLog.class))).thenAnswer(inv -> inv.getArgument(0));

        Ingredient result = service.changeName(1L, "Tomate Perita");

        assertEquals("Tomate Perita", result.getName());

        ArgumentCaptor<IngredientAuditLog> logCaptor = ArgumentCaptor.forClass(IngredientAuditLog.class);
        verify(auditRepo).save(logCaptor.capture());
        assertEquals(AdminOperation.CHANGE_NAME, logCaptor.getValue().getOperation());
        assertTrue(logCaptor.getValue().getReason().contains("Tomate"));
    }

    @Test
    void changeName_noChange_whenSameName() throws IngredientNotFoundException {
        when(repository.findById(1L)).thenReturn(Optional.of(testIngredient));

        Ingredient result = service.changeName(1L, "Tomate");

        assertEquals("Tomate", result.getName());
        verify(repository, never()).save(any());
        verify(auditRepo, never()).save(any());
    }

    @Test
    void changeName_noChange_whenSameNameDifferentCase() throws IngredientNotFoundException {
        when(repository.findById(1L)).thenReturn(Optional.of(testIngredient));

        Ingredient result = service.changeName(1L, "TOMATE");

        assertEquals("Tomate", result.getName());
        verify(repository, never()).save(any());
    }

    @Test
    void changeName_noChange_whenNullName() throws IngredientNotFoundException {
        when(repository.findById(1L)).thenReturn(Optional.of(testIngredient));

        Ingredient result = service.changeName(1L, null);

        assertEquals("Tomate", result.getName());
        verify(repository, never()).save(any());
    }

    @Test
    void changeName_throwsException_whenIngredientNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IngredientNotFoundException.class, () -> service.changeName(99L, "Nuevo"));
    }

    @Test
    void changeName_throwsException_whenNameAlreadyExists() {
        Ingredient other = new Ingredient();
        other.setId(2L);
        other.setName("Cebolla");
        other.setActive(true);

        when(repository.findById(1L)).thenReturn(Optional.of(testIngredient));
        when(repository.findByNameAndActiveTrue("Cebolla")).thenReturn(Optional.of(other));

        assertThrows(IngredientAlreadyExistsException.class, () -> service.changeName(1L, "Cebolla"));
    }

    @Test
    void changeName_allowsSameIngredientNameUpdate() throws IngredientNotFoundException {
        when(repository.findById(1L)).thenReturn(Optional.of(testIngredient));
        when(repository.findByNameAndActiveTrue("Tomate Rojo")).thenReturn(Optional.of(testIngredient));
        when(repository.save(any(Ingredient.class))).thenAnswer(inv -> inv.getArgument(0));
        when(auditRepo.save(any(IngredientAuditLog.class))).thenAnswer(inv -> inv.getArgument(0));

        Ingredient result = service.changeName(1L, "Tomate Rojo");

        assertEquals("Tomate Rojo", result.getName());
    }

    // ==================== deactivateIngredient Tests ====================

    @Test
    void deactivateIngredient_success() throws IngredientNotFoundException {
        when(repository.findById(1L)).thenReturn(Optional.of(testIngredient));
        when(repository.save(any(Ingredient.class))).thenAnswer(inv -> inv.getArgument(0));
        when(auditRepo.save(any(IngredientAuditLog.class))).thenAnswer(inv -> inv.getArgument(0));

        IngredientDetailsResponse result = service.deactivateIngredient(1L, "Stock vencido");

        assertFalse(result.active());
        assertFalse(result.available());

        ArgumentCaptor<IngredientAuditLog> logCaptor = ArgumentCaptor.forClass(IngredientAuditLog.class);
        verify(auditRepo).save(logCaptor.capture());
        assertEquals(AdminOperation.DEACTIVATE, logCaptor.getValue().getOperation());
        assertEquals("Stock vencido", logCaptor.getValue().getReason());
    }

    @Test
    void deactivateIngredient_success_withNullReason() throws IngredientNotFoundException {
        when(repository.findById(1L)).thenReturn(Optional.of(testIngredient));
        when(repository.save(any(Ingredient.class))).thenAnswer(inv -> inv.getArgument(0));
        when(auditRepo.save(any(IngredientAuditLog.class))).thenAnswer(inv -> inv.getArgument(0));

        IngredientDetailsResponse result = service.deactivateIngredient(1L, null);

        assertFalse(result.active());
    }

    @Test
    void deactivateIngredient_returnsWithoutChange_whenAlreadyInactive() throws IngredientNotFoundException {
        testIngredient.setActive(false);
        when(repository.findById(1L)).thenReturn(Optional.of(testIngredient));

        IngredientDetailsResponse result = service.deactivateIngredient(1L, "razón");

        assertFalse(result.active());
        verify(repository, never()).save(any());
        verify(auditRepo, never()).save(any());
    }

    @Test
    void deactivateIngredient_throwsException_whenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IngredientNotFoundException.class, () -> service.deactivateIngredient(99L, null));
    }

    @Test
    void deactivateIngredient_throwsException_whenHasObservers() {
        IngredientObserver mockObserver = mock(IngredientObserver.class);
        testIngredient.setObservers(Set.of(mockObserver));

        when(repository.findById(1L)).thenReturn(Optional.of(testIngredient));

        assertThrows(IngredientInProductException.class, () -> service.deactivateIngredient(1L, null));
        verify(repository, never()).save(any());
    }

    // ==================== updateStock Tests ====================

    @Test
    void updateStock_success() throws IngredientNotFoundException {
        when(repository.findById(1L)).thenReturn(Optional.of(testIngredient));
        when(repository.save(any(Ingredient.class))).thenAnswer(inv -> inv.getArgument(0));
        when(auditRepo.save(any(IngredientAuditLog.class))).thenAnswer(inv -> inv.getArgument(0));

        Ingredient result = service.updateStock(1L, BigDecimal.valueOf(25));

        assertEquals(BigDecimal.valueOf(25), result.getStock());
        assertTrue(result.isAvailable());

        ArgumentCaptor<IngredientAuditLog> logCaptor = ArgumentCaptor.forClass(IngredientAuditLog.class);
        verify(auditRepo).save(logCaptor.capture());
        assertEquals(AdminOperation.UPDATE_STOCK, logCaptor.getValue().getOperation());
        assertEquals(BigDecimal.valueOf(15), logCaptor.getValue().getAmountDelta());
    }

    @Test
    void updateStock_setsUnavailable_whenZeroStock() throws IngredientNotFoundException {
        when(repository.findById(1L)).thenReturn(Optional.of(testIngredient));
        when(repository.save(any(Ingredient.class))).thenAnswer(inv -> inv.getArgument(0));
        when(auditRepo.save(any(IngredientAuditLog.class))).thenAnswer(inv -> inv.getArgument(0));

        Ingredient result = service.updateStock(1L, BigDecimal.ZERO);

        assertEquals(BigDecimal.ZERO, result.getStock());
        assertFalse(result.isAvailable());
    }

    @Test
    void updateStock_handlesNullPreviousStock() throws IngredientNotFoundException {
        testIngredient.setStock(null);
        when(repository.findById(1L)).thenReturn(Optional.of(testIngredient));
        when(repository.save(any(Ingredient.class))).thenAnswer(inv -> inv.getArgument(0));
        when(auditRepo.save(any(IngredientAuditLog.class))).thenAnswer(inv -> inv.getArgument(0));

        Ingredient result = service.updateStock(1L, BigDecimal.valueOf(5));

        assertEquals(BigDecimal.valueOf(5), result.getStock());

        ArgumentCaptor<IngredientAuditLog> logCaptor = ArgumentCaptor.forClass(IngredientAuditLog.class);
        verify(auditRepo).save(logCaptor.capture());
        assertEquals(BigDecimal.valueOf(5), logCaptor.getValue().getAmountDelta());
    }

    @Test
    void updateStock_throwsException_whenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IngredientNotFoundException.class, () -> service.updateStock(99L, BigDecimal.TEN));
    }

    @Test
    void updateStock_notifiesObservers() throws IngredientNotFoundException {
        IngredientObserver mockObserver = mock(IngredientObserver.class);
        testIngredient.addObserver(mockObserver);

        when(repository.findById(1L)).thenReturn(Optional.of(testIngredient));
        when(repository.save(any(Ingredient.class))).thenAnswer(inv -> inv.getArgument(0));
        when(auditRepo.save(any(IngredientAuditLog.class))).thenAnswer(inv -> inv.getArgument(0));

        service.updateStock(1L, BigDecimal.valueOf(20));

        verify(mockObserver).onIngredientStockChange();
    }
}