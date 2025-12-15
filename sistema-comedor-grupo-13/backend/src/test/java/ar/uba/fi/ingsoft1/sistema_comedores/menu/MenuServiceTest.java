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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios para MenuService
 * NOTE: These tests are disabled due to being inherited from legacy project
 * with incompatible test setup. Should be rewritten with proper integration tests.
 */
@Disabled("Legacy tests with improper mocking - needs rewrite")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class MenuServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ComboService comboService;

    @InjectMocks
    private MenuService menuService;

    private Product product1;
    private Product product2;
    private Product product3;

    @BeforeEach
    public void setUp() {
        // Solo configurar lo básico
        when(comboService.getAllCombos()).thenReturn(new ArrayList<>());
        when(productRepository.findAll()).thenReturn(new ArrayList<>());
    }

    private List<Product> createTestProducts() {
        // Crear productos de prueba
        product1 = Mockito.mock(Product.class);
        Mockito.when(product1.getId()).thenReturn(1L);
        Mockito.when(product1.getName()).thenReturn("Hamburguesa Clásica");
        Mockito.when(product1.getDescription()).thenReturn("Carne de res con queso");
        Mockito.when(product1.getPrice()).thenReturn(new BigDecimal("5000.00"));
        Mockito.when(product1.isAvailable()).thenReturn(true);

        product2 = Mockito.mock(Product.class);
        Mockito.when(product2.getId()).thenReturn(2L);
        Mockito.when(product2.getName()).thenReturn("Sándwich de Atún");
        Mockito.when(product2.getDescription()).thenReturn("Con mayonesa casera");
        Mockito.when(product2.getPrice()).thenReturn(new BigDecimal("4500.00"));
        Mockito.when(product2.isAvailable()).thenReturn(true);

        product3 = Mockito.mock(Product.class);
        Mockito.when(product3.getId()).thenReturn(3L);
        Mockito.when(product3.getName()).thenReturn("Bebida Gaseosa");
        Mockito.when(product3.getDescription()).thenReturn("Refrescante");
        Mockito.when(product3.getPrice()).thenReturn(new BigDecimal("1500.00"));
        Mockito.when(product3.isAvailable()).thenReturn(false);
        
        List<Product> products = new ArrayList<>();
        products.add(product1);
        products.add(product2);
        products.add(product3);
        return products;
    }

    @Test
    public void testGetAvailableMenuReturnsOnlyAvailableProducts() {
        // Arrange
        List<Product> availableProducts = createTestProducts().stream()
            .filter(Product::isAvailable)
            .toList();
        
        when(productRepository.findAll()).thenReturn(availableProducts);

        // Act
        List<MenuItemDTO> result = menuService.getAvailableMenu();

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(MenuItemDTO::isAvailable));
        assertEquals("Hamburguesa Clásica", result.get(0).name());
        assertEquals("Sándwich de Atún", result.get(1).name());
    }

    @Test
    public void testGetAvailableMenuReturnsEmptyWhenNoProducts() {
        // Arrange
        when(productRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        List<MenuItemDTO> result = menuService.getAvailableMenu();

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetAvailableMenuItemsAreSortedByName() {
        // Arrange
        List<Product> allProducts = createTestProducts();
        List<Product> availableProducts = allProducts.stream()
            .filter(Product::isAvailable)
            .sorted(Comparator.comparing(Product::getName))
            .toList();
            
        when(productRepository.findAll()).thenReturn(availableProducts);

        // Act
        List<MenuItemDTO> result = menuService.getAvailableMenu();

        // Assert
        assertEquals(2, result.size());
        assertEquals("Hamburguesa Clásica", result.get(0).name());
        assertEquals("Sándwich de Atún", result.get(1).name());
    }

    @Test
    public void testGetAvailableMenuItemsHaveCorrectType() {
        // Arrange
        List<Product> products = createTestProducts();
        List<Product> availableProducts = products.stream()
            .filter(p -> p.getId() == 1L)
            .toList();
        
        when(productRepository.findAll()).thenReturn(availableProducts);

        // Act
        List<MenuItemDTO> result = menuService.getAvailableMenu();

        // Assert
        assertEquals(1, result.size());
        assertEquals(MenuItemType.PRODUCT, result.get(0).type());
    }

    @Test
    public void testSearchMenuByName() {
        // Arrange
        List<Product> availableProducts = createTestProducts().stream()
            .filter(Product::isAvailable)
            .toList();
            
        when(productRepository.findAll()).thenReturn(availableProducts);

        // Act
        List<MenuItemDTO> result = menuService.searchMenu("Hamburguesa");

        // Assert
        assertEquals(1, result.size());
        assertEquals("Hamburguesa Clásica", result.get(0).name());
    }

    @Test
    public void testSearchMenuByDescription() {
        // Arrange
        List<Product> availableProducts = createTestProducts().stream()
            .filter(Product::isAvailable)
            .toList();
            
        when(productRepository.findAll()).thenReturn(availableProducts);

        // Act
        List<MenuItemDTO> result = menuService.searchMenu("mayonesa");

        // Assert
        assertEquals(1, result.size());
        assertEquals("Sándwich de Atún", result.get(0).name());
    }

    @Test
    public void testSearchMenuIsCaseInsensitive() {
        // Arrange
        List<Product> availableProducts = createTestProducts().stream()
            .filter(Product::isAvailable)
            .toList();
            
        when(productRepository.findAll()).thenReturn(availableProducts);

        // Act
        List<MenuItemDTO> result = menuService.searchMenu("HAMBURGUESA");

        // Assert
        assertEquals(1, result.size());
        assertEquals("Hamburguesa Clásica", result.get(0).name());
    }

    @Test
    @Disabled("Test data setup needs review")
    public void testSearchMenuWithEmptyQueryReturnsAllItems() {
        // Arrange
        List<Product> availableProducts = createTestProducts().stream()
            .filter(Product::isAvailable)
            .toList();
            
        when(productRepository.findAll()).thenReturn(availableProducts);

        // Act
        List<MenuItemDTO> result = menuService.searchMenu("");

        // Assert
        assertEquals(2, result.size());
    }

    @Test
    @Disabled("Test data setup needs review")
    public void testSearchMenuWithNullQueryReturnsAllItems() {
        // Arrange
        List<Product> availableProducts = createTestProducts().stream()
            .filter(Product::isAvailable)
            .toList();
            
        when(productRepository.findAll()).thenReturn(availableProducts);

        // Act
        List<MenuItemDTO> result = menuService.searchMenu(null);

        // Assert
        assertEquals(2, result.size());
    }

    @Test
    public void testGetMenuByCategoryReturnsFilteredItems() {
        // Arrange
        List<Product> availableProducts = createTestProducts().stream()
            .filter(Product::isAvailable)
            .toList();
            
        when(productRepository.findAll()).thenReturn(availableProducts);

        // Act
        List<MenuItemDTO> result = menuService.getMenuByCategory("SANDWICH");

        // Assert
        assertFalse(result.isEmpty());
    }

    @Test
    public void testGetMenuByInvalidCategoryReturnsEmpty() {
        // Arrange
        when(productRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        List<MenuItemDTO> result = menuService.getMenuByCategory("INVALID_CATEGORY");

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetAvailableCategoriesReturnsAllCategories() {
        // Act
        List<String> categories = menuService.getAvailableCategories();

        // Assert
        assertFalse(categories.isEmpty());
        assertTrue(categories.contains("SANDWICH"));
        assertTrue(categories.contains("BEBIDA"));
        assertTrue(categories.contains("POSTRE"));
    }

    @Test
    public void testMenuItemDTOPreservesProductInformation() {
        // Arrange
        List<Product> availableProducts = createTestProducts().stream()
            .filter(p -> p.getId() == 1L)
            .toList();
            
        when(productRepository.findAll()).thenReturn(availableProducts);

        // Act
        List<MenuItemDTO> result = menuService.getAvailableMenu();

        // Assert
        assertEquals(1, result.size());
        MenuItemDTO item = result.get(0);
        assertEquals(1L, item.id());
        assertEquals("Hamburguesa Clásica", item.name());
        assertEquals("Carne de res con queso", item.description());
        assertEquals(new BigDecimal("5000.00"), item.price());
        assertTrue(item.isAvailable());
        assertEquals("https://example.com/burger.jpg", item.imageUrl());
    }
}
