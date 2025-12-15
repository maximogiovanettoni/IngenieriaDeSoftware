package ar.uba.fi.ingsoft1.sistema_comedores.ingredients;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IngredientTest {

    private Ingredient ingredient;

    @BeforeEach
    void setUp() {
        ingredient = new Ingredient();
        ingredient.setId(1L);
        ingredient.setName("Tomate");
        ingredient.setUnitMeasure("kg");
        ingredient.setStock(BigDecimal.TEN);
        ingredient.setActive(true);
        ingredient.setAvailable(true);
        ingredient.setObservers(new HashSet<>());
    }

    // ==================== Observer Management Tests ====================

    @Test
    void addObserver_addsObserverToSet() {
        IngredientObserver observer = mock(IngredientObserver.class);

        ingredient.addObserver(observer);

        assertTrue(ingredient.getObservers().contains(observer));
        assertEquals(1, ingredient.getObservers().size());
    }

    @Test
    void addObserver_initializesSetIfNull() {
        ingredient.setObservers(null);
        IngredientObserver observer = mock(IngredientObserver.class);

        ingredient.addObserver(observer);

        assertNotNull(ingredient.getObservers());
        assertTrue(ingredient.getObservers().contains(observer));
    }

    @Test
    void removeObserver_removesObserverById() {
        IngredientObserver observer = mock(IngredientObserver.class);
        when(observer.getObservedEntityId()).thenReturn(100L);
        ingredient.addObserver(observer);

        ingredient.removeObserver(100L);

        assertTrue(ingredient.getObservers().isEmpty());
    }

    @Test
    void removeObserver_doesNothingIfNotFound() {
        IngredientObserver observer = mock(IngredientObserver.class);
        when(observer.getObservedEntityId()).thenReturn(100L);
        ingredient.addObserver(observer);

        ingredient.removeObserver(999L);

        assertEquals(1, ingredient.getObservers().size());
    }

    @Test
    void removeObserver_handlesNullObservers() {
        ingredient.setObservers(null);

        assertDoesNotThrow(() -> ingredient.removeObserver(100L));
    }

    // ==================== Notification Tests ====================

    @Test
    void notifyObserversStockChange_notifiesAllObservers() {
        IngredientObserver obs1 = mock(IngredientObserver.class);
        IngredientObserver obs2 = mock(IngredientObserver.class);
        ingredient.addObserver(obs1);
        ingredient.addObserver(obs2);

        ingredient.notifyObserversStockChange();

        verify(obs1).onIngredientStockChange();
        verify(obs2).onIngredientStockChange();
    }

    @Test
    void notifyObserversStockChange_handlesNullObservers() {
        ingredient.setObservers(null);

        assertDoesNotThrow(() -> ingredient.notifyObserversStockChange());
    }

    @Test
    void notifyObserversStatusChange_notifiesAllObservers() {
        IngredientObserver obs1 = mock(IngredientObserver.class);
        IngredientObserver obs2 = mock(IngredientObserver.class);
        ingredient.addObserver(obs1);
        ingredient.addObserver(obs2);

        ingredient.notifyObserversStatusChange();

        verify(obs1).onIngredientStatusChange();
        verify(obs2).onIngredientStatusChange();
    }

    @Test
    void notifyObserversStatusChange_handlesNullObservers() {
        ingredient.setObservers(null);

        assertDoesNotThrow(() -> ingredient.notifyObserversStatusChange());
    }

    // ==================== updateStockAndNotify Tests ====================

    @Test
    void updateStockAndNotify_updatesStockAndSetsAvailable() {
        IngredientObserver observer = mock(IngredientObserver.class);
        ingredient.addObserver(observer);

        ingredient.updateStockAndNotify(BigDecimal.valueOf(20));

        assertEquals(BigDecimal.valueOf(20), ingredient.getStock());
        assertTrue(ingredient.isAvailable());
        verify(observer).onIngredientStockChange();
    }

    @Test
    void updateStockAndNotify_setsUnavailableWhenZero() {
        ingredient.updateStockAndNotify(BigDecimal.ZERO);

        assertEquals(BigDecimal.ZERO, ingredient.getStock());
        assertFalse(ingredient.isAvailable());
    }

    @Test
    void updateStockAndNotify_setsUnavailableWhenNull() {
        ingredient.updateStockAndNotify(null);

        assertNull(ingredient.getStock());
        assertFalse(ingredient.isAvailable());
    }

    @Test
    void updateStockAndNotify_setsUnavailableWhenInactive() {
        ingredient.setActive(false);

        ingredient.updateStockAndNotify(BigDecimal.TEN);

        assertEquals(BigDecimal.TEN, ingredient.getStock());
        assertFalse(ingredient.isAvailable());
    }

    // ==================== consumeStock Tests ====================

    @Test
    void consumeStock_reducesStock() {
        ingredient.consumeStock(BigDecimal.valueOf(3));

        assertEquals(BigDecimal.valueOf(7), ingredient.getStock());
        assertTrue(ingredient.isAvailable());
    }

    @Test
    void consumeStock_consumesAllStock() {
        ingredient.consumeStock(BigDecimal.TEN);

        assertEquals(BigDecimal.ZERO, ingredient.getStock());
        assertFalse(ingredient.isAvailable());
    }

    @Test
    void consumeStock_notifiesObservers() {
        IngredientObserver observer = mock(IngredientObserver.class);
        ingredient.addObserver(observer);

        ingredient.consumeStock(BigDecimal.valueOf(5));

        verify(observer).onIngredientStockChange();
    }

    @Test
    void consumeStock_throwsException_whenQuantityNull() {
        assertThrows(IllegalArgumentException.class, () -> ingredient.consumeStock(null));
    }

    @Test
    void consumeStock_throwsException_whenQuantityNegative() {
        assertThrows(IllegalArgumentException.class, () -> ingredient.consumeStock(BigDecimal.valueOf(-1)));
    }

    @Test
    void consumeStock_throwsException_whenInsufficientStock() {
        assertThrows(IllegalArgumentException.class, () -> ingredient.consumeStock(BigDecimal.valueOf(15)));
    }

    @Test
    void consumeStock_handlesNullCurrentStock() {
        ingredient.setStock(null);

        assertThrows(IllegalArgumentException.class, () -> ingredient.consumeStock(BigDecimal.ONE));
    }

    // ==================== Constructor and Getter/Setter Tests ====================

    @Test
    void allArgsConstructor_createsIngredient() {
        Ingredient ing = new Ingredient(
                1L, "Cebolla", "kg", BigDecimal.valueOf(5),
                true, true, new HashSet<>()
        );

        assertEquals(1L, ing.getId());
        assertEquals("Cebolla", ing.getName());
        assertEquals("kg", ing.getUnitMeasure());
        assertEquals(BigDecimal.valueOf(5), ing.getStock());
        assertTrue(ing.isActive());
        assertTrue(ing.isAvailable());
    }

    @Test
    void noArgsConstructor_createsEmptyIngredient() {
        Ingredient ing = new Ingredient();

        assertNull(ing.getId());
        assertNull(ing.getName());
        assertTrue(ing.isActive()); // default value
        assertFalse(ing.isAvailable()); // default value
    }
}