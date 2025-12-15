//package ar.uba.fi.ingsoft1.sistema_comedores.products;
//
//import ar.uba.fi.ingsoft1.sistema_comedores.ingredients.Ingredient;
//import ar.uba.fi.ingsoft1.sistema_comedores.ingredients.IngredientRepository;
//import ar.uba.fi.ingsoft1.sistema_comedores.products.dto.SimpleProductCreateDTO;
//import ar.uba.fi.ingsoft1.sistema_comedores.products.dto.ProductSearchDTO;
//import ar.uba.fi.ingsoft1.sistema_comedores.products.dto.ProductUpdateDTO;
//import ar.uba.fi.ingsoft1.sistema_comedores.products.exception.DuplicateProductNameException;
//import ar.uba.fi.ingsoft1.sistema_comedores.products.exception.InvalidIngredientException;
//import ar.uba.fi.ingsoft1.sistema_comedores.products.exception.ProductNotFoundException;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.math.BigDecimal;
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//
//class ProductServiceTest {
//
//    private ProductRepository productRepository;
//    private IngredientRepository ingredientRepository;
//    private ProductAuditLogRepository productAuditLogRepository;
//    private ProductService productService;
//
//    private static final long ID = 1;
//    private static final String NAME = "Product Name";
//    private static final String DESCRIPTION = "Product Description";
//    private static final BigDecimal PRICE = new BigDecimal("15.99");
//    private static final Boolean AVAILABLE = true;
//    private static final Product.ProductType ELABORATE_TYPE = Product.ProductType.ELABORATE;
//    private static final Product.ProductType SIMPLE_TYPE = Product.ProductType.SIMPLE;
//    private static final BigDecimal STOCK = new BigDecimal("10.0");
//
//    private static final Set<Long> INGREDIENT_IDS = Set.of(1L, 2L);
//    private static final Ingredient INGREDIENT_1;
//    private static final Ingredient INGREDIENT_2;
//    private static final Set<Ingredient> INGREDIENTS;
//
//    static {
//        INGREDIENT_1 = new Ingredient();
//        INGREDIENT_1.setId(1L);
//        INGREDIENT_1.setName("Tomato");
//
//        INGREDIENT_2 = new Ingredient();
//        INGREDIENT_2.setId(2L);
//        INGREDIENT_2.setName("Cheese");
//
//        INGREDIENTS = Set.of(INGREDIENT_1, INGREDIENT_2);
//    }
//
//    // Test ingredient quantities
//    private static final Map<Long, BigDecimal> INGREDIENT_QUANTITIES = Map.of(
//        1L, new BigDecimal("0.5"),
//        2L, new BigDecimal("0.2")
//    );
//
//    @BeforeEach
//    void setup() {
//        productRepository = mock(ProductRepository.class);
//        ingredientRepository = mock(IngredientRepository.class);
//        productAuditLogRepository = mock(ProductAuditLogRepository.class);
//
//        productService = new ProductService(productRepository, ingredientRepository, productAuditLogRepository);
//    }
//
//    @Test
//    void createWritesToDatabase() {
//        // Mock ingredient repository
//        when(ingredientRepository.findAllById(INGREDIENT_IDS)).thenReturn(List.of(INGREDIENT_1, INGREDIENT_2));
//
//        // Mock product repository
//        Product expectedProduct = new Product(NAME, DESCRIPTION, PRICE, AVAILABLE, ELABORATE_TYPE, null);
//        expectedProduct.setId(ID);
//        expectedProduct.setIngredients(INGREDIENTS);
//        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
//            Product product = invocation.getArgument(0);
//            product.setId(ID);  // Set ID on the actual product being saved
//            return product;
//        });
//        when(productRepository.existsByName(NAME)).thenReturn(false);
//
//        var newProduct = new SimpleProductCreateDTO(NAME, DESCRIPTION, PRICE, AVAILABLE, ELABORATE_TYPE, null, INGREDIENT_IDS, INGREDIENT_QUANTITIES, null);
//        productService.createProduct(newProduct);
//
//        verify(productRepository).save(any(Product.class));
//    }
//
//    @Test
//    void createReturnsCreatedProduct() {
//        // Mock ingredient repository
//        when(ingredientRepository.findAllById(INGREDIENT_IDS)).thenReturn(List.of(INGREDIENT_1, INGREDIENT_2));
//
//        // Mock product repository
//        Product savedProduct = new Product(NAME, DESCRIPTION, PRICE, AVAILABLE, ELABORATE_TYPE, null);
//        savedProduct.setId(ID);
//        savedProduct.setIngredients(INGREDIENTS);
//        // Set up ingredient quantities in the saved product
//        savedProduct.getIngredientQuantities().put(INGREDIENT_1, new BigDecimal("0.5"));
//        savedProduct.getIngredientQuantities().put(INGREDIENT_2, new BigDecimal("0.2"));
//
//        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
//            Product product = invocation.getArgument(0);
//            product.setId(ID);
//            product.setIngredients(INGREDIENTS);
//            product.getIngredientQuantities().put(INGREDIENT_1, new BigDecimal("0.5"));
//            product.getIngredientQuantities().put(INGREDIENT_2, new BigDecimal("0.2"));
//            return product;
//        });
//        when(productRepository.existsByName(NAME)).thenReturn(false);
//
//        var newProduct = new SimpleProductCreateDTO(NAME, DESCRIPTION, PRICE, AVAILABLE, ELABORATE_TYPE, null, INGREDIENT_IDS, INGREDIENT_QUANTITIES, null);
//        var response = productService.createProduct(newProduct);
//
//        assertEquals(ID, response.id());
//        assertEquals(NAME, response.name());
//        assertEquals(DESCRIPTION, response.description());
//        assertEquals(PRICE, response.price());
//        assertEquals(AVAILABLE, response.available());
//        assertEquals(2, response.ingredients().size()); // Should have 2 ingredients
//        assertEquals(2, response.ingredientQuantities().size()); // Should have 2 quantities
//    }
//
//    @Test
//    void createSimpleProductWithStock() {
//        when(productRepository.existsByName(NAME)).thenReturn(false);
//
//        Product savedProduct = new Product(NAME, DESCRIPTION, PRICE, AVAILABLE, SIMPLE_TYPE, STOCK);
//        savedProduct.setId(ID);
//
//        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
//            Product product = invocation.getArgument(0);
//            product.setId(ID);
//            return product;
//        });
//
//        var newProduct = new SimpleProductCreateDTO(NAME, DESCRIPTION, PRICE, AVAILABLE, SIMPLE_TYPE, STOCK, null, null, null);
//        var response = productService.createProduct(newProduct);
//
//        assertEquals(ID, response.id());
//        assertEquals(NAME, response.name());
//        assertEquals(SIMPLE_TYPE.name(), response.productType());
//        assertEquals(STOCK, response.stock());
//    }
//
//    @Test
//    void createWithDuplicateNameThrowsException() {
//        when(productRepository.existsByName(NAME)).thenReturn(true);
//
//        var newProduct = new SimpleProductCreateDTO(NAME, DESCRIPTION, PRICE, AVAILABLE, ELABORATE_TYPE, null, INGREDIENT_IDS, INGREDIENT_QUANTITIES, null);
//
//        assertThrows(DuplicateProductNameException.class, () -> {
//            productService.createProduct(newProduct);
//        });
//    }
//
//    @Test
//    void createElaborateProductWithNoValidIngredientsThrowsException() {
//        when(productRepository.existsByName(NAME)).thenReturn(false);
//        when(ingredientRepository.findAllById(INGREDIENT_IDS)).thenReturn(Collections.emptyList());
//
//        var newProduct = new SimpleProductCreateDTO(NAME, DESCRIPTION, PRICE, AVAILABLE, ELABORATE_TYPE, null, INGREDIENT_IDS, INGREDIENT_QUANTITIES, null);
//
//        assertThrows(InvalidIngredientException.class, () -> {
//            productService.createProduct(newProduct);
//        });
//    }
//
//    @Test
//    void updateProductReturnsUpdatedProduct() {
//        // Mock existing product - elaborate product
//        Product existingProduct = new Product("Old Name", "Old Description", new BigDecimal("15.00"), false);
//        existingProduct.setId(ID);
//        existingProduct.setProductType(ELABORATE_TYPE);
//        existingProduct.setIngredients(new HashSet<>(INGREDIENTS));
//        when(productRepository.findById(ID)).thenReturn(Optional.of(existingProduct));
//
//        // Mock save
//        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);
//
//        var updateDTO = new ProductUpdateDTO(
//            Optional.of("Updated Name"),
//            Optional.of("Updated Description"),
//            Optional.of(new BigDecimal("25.99")),
//            Optional.of(true),
//            Optional.empty(), // No stock update for elaborate product
//            Optional.empty(), // No ingredient changes
//            Optional.empty(), // No quantity changes
//            Optional.of("http://example.com/image.jpg")
//        );
//
//        var response = productService.updateProduct(ID, updateDTO);
//
//        assertTrue(response.isPresent());
//        assertEquals("Updated Name", response.get().name());
//        assertEquals("Updated Description", response.get().description());
//        assertEquals(new BigDecimal("25.99"), response.get().price());
//        assertEquals(true, response.get().available());
//    }
//
//    @Test
//    void updateSimpleProductStock() {
//        // Mock existing simple product with stock
//        Product existingProduct = new Product("Simple Product", "Description", PRICE, AVAILABLE);
//        existingProduct.setId(ID);
//        existingProduct.setProductType(SIMPLE_TYPE);
//        existingProduct.setStock(STOCK);
//        when(productRepository.findById(ID)).thenReturn(Optional.of(existingProduct));
//
//        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);
//
//        var updateDTO = new ProductUpdateDTO(
//            Optional.empty(),
//            Optional.empty(),
//            Optional.empty(),
//            Optional.empty(),
//            Optional.of(new BigDecimal("20.0")), // Update stock
//            Optional.empty(),
//            Optional.empty(),
//            Optional.empty()
//        );
//
//        var response = productService.updateProduct(ID, updateDTO);
//
//        assertTrue(response.isPresent());
//        assertEquals(new BigDecimal("20.0"), response.get().stock());
//    }
//
//    @Test
//    void updateProductWithDuplicateNameThrowsException() {
//        Product existingProduct = new Product("Original Name", DESCRIPTION, PRICE, AVAILABLE);
//        existingProduct.setId(ID);
//        existingProduct.setProductType(ELABORATE_TYPE);
//        existingProduct.setIngredients(INGREDIENTS);
//        when(productRepository.findById(ID)).thenReturn(Optional.of(existingProduct));
//
//        // Mock duplicate name check (should return true - duplicate exists)
//        when(productRepository.existsByNameAndIdNot("Duplicate Name", ID)).thenReturn(true);
//
//        var updateDTO = new ProductUpdateDTO(
//            Optional.of("Duplicate Name"), // This name already exists
//            Optional.empty(),
//            Optional.empty(),
//            Optional.empty(),
//            Optional.empty(),
//            Optional.empty(),
//            Optional.empty(),
//            Optional.empty()
//        );
//
//        assertThrows(DuplicateProductNameException.class, () -> {
//            productService.updateProduct(ID, updateDTO);
//        });
//    }
//
//    @Test
//    void updateNonExistentProductThrowsException() {
//        when(productRepository.findById(ID)).thenReturn(Optional.empty());
//
//        var updateDTO = new ProductUpdateDTO(
//            Optional.of("New Name"),
//            Optional.empty(),
//            Optional.empty(),
//            Optional.empty(),
//            Optional.empty(),
//            Optional.empty(),
//            Optional.empty(),
//            Optional.empty()
//        );
//
//        assertThrows(ProductNotFoundException.class, () -> {
//            productService.updateProduct(ID, updateDTO);
//        });
//    }
//
//    @Test
//    void getProductsReturnsFilteredList() {
//        Product product1 = new Product("Pizza", "Delicious pizza", PRICE, AVAILABLE);
//        product1.setId(1L);
//        product1.setProductType(ELABORATE_TYPE);
//        product1.setIngredients(INGREDIENTS);
//
//        Product product2 = new Product("Burger", "Tasty burger", PRICE, AVAILABLE);
//        product2.setId(2L);
//        product2.setProductType(ELABORATE_TYPE);
//        product2.setIngredients(INGREDIENTS);
//
//        when(productRepository.findByNameContaining("Piz")).thenReturn(List.of(product1));
//
//        var filter = new ProductSearchDTO(Optional.of("Piz"));
//        var result = productService.getProducts(filter);
//
//        assertEquals(1, result.size());
//        assertEquals("Pizza", result.get(0).name());
//    }
//
//    @Test
//    void getProductsReturnsAllWhenNoFilter() {
//        Product product1 = new Product("Pizza", "Delicious pizza", PRICE, AVAILABLE);
//        product1.setId(1L);
//        product1.setProductType(ELABORATE_TYPE);
//        product1.setIngredients(INGREDIENTS);
//
//        Product product2 = new Product("Burger", "Tasty burger", PRICE, AVAILABLE);
//        product2.setId(2L);
//        product2.setProductType(SIMPLE_TYPE);
//        product2.setStock(STOCK);
//
//        when(productRepository.findByNameContaining("")).thenReturn(List.of(product1, product2));
//
//        var filter = new ProductSearchDTO(Optional.empty());
//        var result = productService.getProducts(filter);
//
//        assertEquals(2, result.size());
//    }
//}