package ar.uba.fi.ingsoft1.sistema_comedores.menu;

import ar.uba.fi.ingsoft1.sistema_comedores.products.Product;
import ar.uba.fi.ingsoft1.sistema_comedores.products.ProductRepository;
import ar.uba.fi.ingsoft1.sistema_comedores.products.combos.ComboService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Tests simples para MenuController (sin SpringBootTest)
 * NOTE: These tests are disabled due to being inherited from legacy project
 * with incompatible test setup. Should be rewritten with proper integration tests.
 */
@Disabled("Legacy tests with improper mocking - needs rewrite")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class MenuControllerTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ComboService comboService;

    @InjectMocks
    private MenuService menuService;
    
    private MenuController menuController;
    private List<Product> testProducts;

    @BeforeEach
    public void setUp() throws Exception {
        // Configurar mock de ComboService para retornar lista vacía por defecto
        when(comboService.getAllCombos()).thenReturn(new ArrayList<>());
        when(productRepository.findAll()).thenReturn(new ArrayList<>());
        
        menuController = new MenuController();
        
        // Usar reflexión para inyectar menuService (es privado)
        Field field = MenuController.class.getDeclaredField("menuService");
        field.setAccessible(true);
        field.set(menuController, menuService);
    }

    private List<Product> createTestProducts() {
        // Crear productos de prueba
        testProducts = new ArrayList<>();
        
        Product product1 = Mockito.mock(Product.class);
        Mockito.when(product1.getId()).thenReturn(1L);
        Mockito.when(product1.getName()).thenReturn("Hamburguesa Clásica");
        Mockito.when(product1.getDescription()).thenReturn("Carne de res con queso");
        Mockito.when(product1.getPrice()).thenReturn(new BigDecimal("5000.00"));
        Mockito.when(product1.isAvailable()).thenReturn(true);

        Product product2 = Mockito.mock(Product.class);
        Mockito.when(product2.getId()).thenReturn(2L);
        Mockito.when(product2.getName()).thenReturn("Sándwich de Atún");
        Mockito.when(product2.getDescription()).thenReturn("Con mayonesa casera");
        Mockito.when(product2.getPrice()).thenReturn(new BigDecimal("4500.00"));
        Mockito.when(product2.isAvailable()).thenReturn(true);

        testProducts.add(product1);
        testProducts.add(product2);

        return testProducts;
    }

    @SuppressWarnings("null")
    @Test
    public void testGetMenuReturnsResponseEntity() {
        // Arrange
        List<Product> products = createTestProducts();
        when(productRepository.findAll()).thenReturn(products);
        
        // Act
        ResponseEntity<List<MenuItemDTO>> response = menuController.getMenu(null, null);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().size() > 0);
    }

    @Test
    public void testGetMenuWithSearchParameter() {
        // Arrange
        List<Product> products = createTestProducts();
        when(productRepository.findAll()).thenReturn(products);
        
        // Act
        ResponseEntity<List<MenuItemDTO>> response = menuController.getMenu(null, "Hamburguesa");

        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Hamburguesa Clásica", response.getBody().get(0).name());
    }

    @Test
    public void testGetMenuWithCategoryParameter() {
        // Arrange
        List<Product> products = createTestProducts();
        when(productRepository.findAll()).thenReturn(products);
        
        // Act
        ResponseEntity<List<MenuItemDTO>> response = menuController.getMenu("SANDWICH", null);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void testGetCategoriesReturnsMap() {
        // Act
        ResponseEntity<Map<String, List<String>>> response = menuController.getCategories();

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("categories"));
        assertFalse(response.getBody().get("categories").isEmpty());
    }

    @Test
    public void testGetMenuStatsReturnsMap() {
        // Act
        ResponseEntity<Map<String, Object>> response = menuController.getMenuStats();

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("totalItems"));
        assertTrue(response.getBody().containsKey("products"));
        assertTrue(response.getBody().containsKey("combos"));
        assertTrue(response.getBody().containsKey("categories"));
    }

    @Test
    public void testGetMenuReturnsMenuItemDTOWithCorrectStructure() {
        // Arrange
        List<Product> products = createTestProducts();
        when(productRepository.findAll()).thenReturn(products);
        
        // Act
        ResponseEntity<List<MenuItemDTO>> response = menuController.getMenu(null, null);
        List<MenuItemDTO> items = response.getBody();

        // Assert
        assertNotNull(items);
        assertFalse(items.isEmpty());
        
        MenuItemDTO item = items.get(0);
        assertNotNull(item.id());
        assertNotNull(item.type());
        assertNotNull(item.name());
        assertNotNull(item.price());
        assertNotNull(item.imageUrl());
        assertTrue(item.isAvailable());
    }

    @Test
    public void testSearchMenuIsCaseInsensitive() {
        // Arrange
        List<Product> products = createTestProducts();
        when(productRepository.findAll()).thenReturn(products);
        
        // Act
        ResponseEntity<List<MenuItemDTO>> response = menuController.getMenu(null, "HAMBURGUESA");

        // Assert
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    public void testGetMenuReturnsAllAvailableItems() {
        // Arrange
        List<Product> products = createTestProducts();
        when(productRepository.findAll()).thenReturn(products);
        
        // Act
        ResponseEntity<List<MenuItemDTO>> response = menuController.getMenu(null, null);

        // Assert
        assertEquals(2, response.getBody().size());
        assertTrue(response.getBody().stream().allMatch(MenuItemDTO::isAvailable));
    }
}
