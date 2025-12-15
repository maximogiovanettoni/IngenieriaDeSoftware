//package ar.uba.fi.ingsoft1.sistema_comedores.products;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import java.math.BigDecimal;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class ProductRepositoryTest {
//
//    private ProductRepository productRepository;
//    private ProductService productService;
//
//    @BeforeEach
//    void setUp() {
//        productRepository = mock(ProductRepository.class);
//        // If you need to test ProductService with mocked repository
//        // productService = new ProductService(productRepository, ...);
//    }
//
//    @Test
//    void findByNameContaining_Found() {
//        // Given
//        Product product1 = new Product("Pizza Margherita", "Classic pizza", new BigDecimal("15.99"), true);
//        Product product2 = new Product("Cheese Pizza", "Cheesy pizza", new BigDecimal("16.99"), true);
//
//        when(productRepository.findByNameContaining("Pizza"))
//            .thenReturn(Arrays.asList(product1, product2));
//
//        // When
//        List<Product> result = productRepository.findByNameContaining("Pizza");
//
//        // Then
//        assertEquals(2, result.size());
//        assertEquals("Pizza Margherita", result.get(0).getName());
//        assertEquals("Cheese Pizza", result.get(1).getName());
//        verify(productRepository, times(1)).findByNameContaining("Pizza");
//    }
//
//    @Test
//    void findByNameContaining_NotFound() {
//        // Given
//        when(productRepository.findByNameContaining("Burger")).thenReturn(Arrays.asList());
//
//        // When
//        List<Product> result = productRepository.findByNameContaining("Burger");
//
//        // Then
//        assertTrue(result.isEmpty());
//        verify(productRepository, times(1)).findByNameContaining("Burger");
//    }
//
//    @Test
//    void existsByName_Exists() {
//        // Given
//        when(productRepository.existsByName("Existing Product")).thenReturn(true);
//        when(productRepository.existsByName("Non-existent Product")).thenReturn(false);
//
//        // When & Then
//        assertTrue(productRepository.existsByName("Existing Product"));
//        assertFalse(productRepository.existsByName("Non-existent Product"));
//
//        verify(productRepository, times(1)).existsByName("Existing Product");
//        verify(productRepository, times(1)).existsByName("Non-existent Product");
//    }
//
//    @Test
//    void findByName_Found() {
//        // Given
//        Product product = new Product("Specific Product", "Description", new BigDecimal("20.99"), true);
//        when(productRepository.findByName("Specific Product")).thenReturn(Optional.of(product));
//
//        // When
//        Optional<Product> result = productRepository.findByName("Specific Product");
//
//        // Then
//        assertTrue(result.isPresent());
//        assertEquals("Specific Product", result.get().getName());
//        verify(productRepository, times(1)).findByName("Specific Product");
//    }
//
//    @Test
//    void findByName_NotFound() {
//        // Given
//        when(productRepository.findByName("Unknown Product")).thenReturn(Optional.empty());
//
//        // When
//        Optional<Product> result = productRepository.findByName("Unknown Product");
//
//        // Then
//        assertFalse(result.isPresent());
//        verify(productRepository, times(1)).findByName("Unknown Product");
//    }
//
//    @Test
//    void findByAvailableTrue() {
//        // Given
//        Product availableProduct = new Product("Available", "Description", new BigDecimal("15.99"), true);
//        Product unavailableProduct = new Product("Unavailable", "Description", new BigDecimal("12.99"), false);
//
//        when(productRepository.findByAvailableTrue()).thenReturn(Arrays.asList(availableProduct));
//
//        // When
//        List<Product> result = productRepository.findByAvailableTrue();
//
//        // Then
//        assertEquals(1, result.size());
//        assertEquals("Available", result.get(0).getName());
//        assertTrue(result.get(0).getActive());
//        verify(productRepository, times(1)).findByAvailableTrue();
//    }
//
//    @Test
//    void findByAvailable() {
//        // Given
//        Product availableProduct = new Product("Available", "Description", new BigDecimal("15.99"), true);
//        Product unavailableProduct = new Product("Unavailable", "Description", new BigDecimal("12.99"), false);
//
//        when(productRepository.findByAvailable(true)).thenReturn(Arrays.asList(availableProduct));
//        when(productRepository.findByAvailable(false)).thenReturn(Arrays.asList(unavailableProduct));
//
//        // When
//        List<Product> availableResults = productRepository.findByAvailable(true);
//        List<Product> unavailableResults = productRepository.findByAvailable(false);
//
//        // Then
//        assertEquals(1, availableResults.size());
//        assertEquals("Available", availableResults.get(0).getName());
//
//        assertEquals(1, unavailableResults.size());
//        assertEquals("Unavailable", unavailableResults.get(0).getName());
//
//        verify(productRepository, times(1)).findByAvailable(true);
//        verify(productRepository, times(1)).findByAvailable(false);
//    }
//
//    @Test
//    void existsByNameAndIdNot() {
//        // Given
//        when(productRepository.existsByNameAndIdNot("Duplicate Name", 1L)).thenReturn(true);
//        when(productRepository.existsByNameAndIdNot("Unique Name", 1L)).thenReturn(false);
//
//        // When & Then
//        assertTrue(productRepository.existsByNameAndIdNot("Duplicate Name", 1L));
//        assertFalse(productRepository.existsByNameAndIdNot("Unique Name", 1L));
//
//        verify(productRepository, times(1)).existsByNameAndIdNot("Duplicate Name", 1L);
//        verify(productRepository, times(1)).existsByNameAndIdNot("Unique Name", 1L);
//    }
//
//    @Test
//    void saveProduct() {
//        // Given
//        Product product = new Product("New Product", "Description", new BigDecimal("10.99"), true);
//        when(productRepository.save(product)).thenReturn(product);
//
//        // When
//        Product saved = productRepository.save(product);
//
//        // Then
//        assertNotNull(saved);
//        assertEquals("New Product", saved.getName());
//        verify(productRepository, times(1)).save(product);
//    }
//
//    @Test
//    void deleteProduct() {
//        // Given
//        Product product = new Product("To Delete", "Description", new BigDecimal("5.99"), true);
//        product.setId(1L);
//
//        // When
//        productRepository.delete(product);
//
//        // Then
//        verify(productRepository, times(1)).delete(product);
//    }
//
//    @Test
//    void findById_Found() {
//        // Given
//        Product product = new Product("Test Product", "Description", new BigDecimal("15.99"), true);
//        product.setId(1L);
//        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
//
//        // When
//        Optional<Product> result = productRepository.findById(1L);
//
//        // Then
//        assertTrue(result.isPresent());
//        assertEquals(1L, result.get().getId());
//        assertEquals("Test Product", result.get().getName());
//        verify(productRepository, times(1)).findById(1L);
//    }
//
//    @Test
//    void findById_NotFound() {
//        // Given
//        when(productRepository.findById(999L)).thenReturn(Optional.empty());
//
//        // When
//        Optional<Product> result = productRepository.findById(999L);
//
//        // Then
//        assertFalse(result.isPresent());
//        verify(productRepository, times(1)).findById(999L);
//    }
//}