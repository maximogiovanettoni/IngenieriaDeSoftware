//package ar.uba.fi.ingsoft1.sistema_comedores.products;
//
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.web.servlet.MockMvc;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.JsonNode;
//import ar.uba.fi.ingsoft1.sistema_comedores.config.security.SecurityConfig;
//
//import ar.uba.fi.ingsoft1.sistema_comedores.config.security.JwtService;
//import ar.uba.fi.ingsoft1.sistema_comedores.ingredients.Ingredient;
//import ar.uba.fi.ingsoft1.sistema_comedores.ingredients.IngredientRepository;
//import ar.uba.fi.ingsoft1.sistema_comedores.ingredients.IngredientService;
//import ar.uba.fi.ingsoft1.sistema_comedores.ingredients.dto.CreateIngredientDTO;
//import ar.uba.fi.ingsoft1.sistema_comedores.products.dto.SimpleProductCreateDTO;
//import ar.uba.fi.ingsoft1.sistema_comedores.products.dto.ProductDTO;
//import ar.uba.fi.ingsoft1.sistema_comedores.products.dto.ProductSearchDTO;
//import ar.uba.fi.ingsoft1.sistema_comedores.products.dto.ProductUpdateDTO;
//import ar.uba.fi.ingsoft1.sistema_comedores.products.dto.ProductDeleteDTO;
//
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.context.annotation.Import;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//
//import java.math.BigDecimal;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//import java.util.Set;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.when;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.times;
//import org.springframework.boot.test.mock.mockito.*;
//import org.mockito.Mock;
//import org.mockito.InjectMocks;
//import org.mockito.MockitoAnnotations;
//import org.mockito.junit.MockitoJUnitRunner;
//import org.mockito.junit.jupiter.MockitoExtension;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.ArgumentMatchers.eq;
//
//import ar.uba.fi.ingsoft1.sistema_comedores.config.security.JwtService;
//import ar.uba.fi.ingsoft1.sistema_comedores.config.security.SecurityConfig;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@WebMvcTest(controllers = ProductRestController.class)
//@Import({SecurityConfig.class, JwtService.class})
//class ProductIntegrationTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private ProductService productService;
//
//    @Test
//    @WithMockUser(username = "user")
//    void getAllProductsWhenNoProductsExist_ReturnsEmptyList() throws Exception {
//        when(productService.getProducts(any(ProductSearchDTO.class))).thenReturn(List.of());
//
//        mockMvc.perform(get("/products")
//                .contentType(MediaType.APPLICATION_JSON))
//
//        .andExpect(status().isOk())
//        .andExpect(jsonPath("$").isArray())
//        .andExpect(jsonPath("$.length()").value(0));
//    }
//
//    @Test
//    @WithMockUser(authorities = "ROLE_ADMIN")
//    void createElaboratedProductSuccessfully() throws Exception {
//        var resultProduct = new ProductDTO(1L, "Margherita Pizza",
//                "Classic pizza with tomato and cheese",
//                new BigDecimal("15.99"), true,
//                "ELABORATE",
//                null,
//                Set.of("Tomato", "Cheese"),
//                Map.of("Tomato", new BigDecimal("0.5"), "Cheese", new BigDecimal("0.3")),
//                null,
//                true); // Add inStock parameter
//
//        when(productService.createProduct(any(SimpleProductCreateDTO.class))).thenReturn(resultProduct);
//
//        var requestBody = """
//            {
//                "name": "Margherita Pizza",
//                "description": "Classic pizza with tomato and cheese",
//                "price": 15.99,
//                "available": true,
//                "productType": "ELABORATE",
//                "ingredientIds": [1, 2],
//                "ingredientQuantities": {
//                    "1": 0.5,
//                    "2": 0.3
//                }
//            }
//            """;
//
//        mockMvc.perform(post("/products")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(requestBody))
//
//        .andExpect(status().isCreated())
//        .andExpect(jsonPath("$.id").value(1))
//        .andExpect(jsonPath("$.name").value("Margherita Pizza"))
//        .andExpect(jsonPath("$.price").value(15.99))
//        .andExpect(jsonPath("$.productType").value("ELABORATE"));
//    }
//
//    @Test
//    @WithMockUser(authorities = "ROLE_ADMIN")
//    void createSimpleProductSuccessfully() throws Exception {
//        var resultProduct = new ProductDTO(2L, "Coca-Cola",
//                "Refreshing beverage",
//                new BigDecimal("2.50"), true,
//                "SIMPLE",
//                new BigDecimal("50"),
//                Set.of(),
//                Map.of(),
//                null,
//                true); // Add inStock parameter
//
//        when(productService.createProduct(any(SimpleProductCreateDTO.class))).thenReturn(resultProduct);
//
//        var requestBody = """
//            {
//                "name": "Coca-Cola",
//                "description": "Refreshing beverage",
//                "price": 2.50,
//                "available": true,
//                "productType": "SIMPLE",
//                "stock": 50
//            }
//            """;
//
//        mockMvc.perform(post("/products")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(requestBody))
//
//        .andExpect(status().isCreated())
//        .andExpect(jsonPath("$.id").value(2))
//        .andExpect(jsonPath("$.name").value("Coca-Cola"))
//        .andExpect(jsonPath("$.price").value(2.50))
//        .andExpect(jsonPath("$.productType").value("SIMPLE"))
//        .andExpect(jsonPath("$.stock").value(50));
//    }
//
//    @Test
//    @WithMockUser(username = "user")
//    void getProductByIdSuccessfully() throws Exception {
//        var product = new ProductDTO(1L, "Test Pizza", "Test description",
//                new BigDecimal("12.99"), true,
//                "ELABORATE",
//                null,
//                Set.of("Cheese"),
//                Map.of("Cheese", new BigDecimal("1.0")),
//                null,
//                true); // Add inStock parameter
//
//        when(productService.getProductById(1L)).thenReturn(Optional.of(product));
//
//        mockMvc.perform(get("/products/1")
//                .contentType(MediaType.APPLICATION_JSON))
//
//        .andExpect(status().isOk())
//        .andExpect(jsonPath("$.id").value(1))
//        .andExpect(jsonPath("$.name").value("Test Pizza"))
//        .andExpect(jsonPath("$.price").value(12.99))
//        .andExpect(jsonPath("$.productType").value("ELABORATE"));
//    }
//
//    @Test
//    @WithMockUser(username = "user")
//    void getSimpleProductByIdSuccessfully() throws Exception {
//        var product = new ProductDTO(3L, "Bottled Water", "Pure water",
//                new BigDecimal("1.50"), true,
//                "SIMPLE",
//                new BigDecimal("100"),
//                Set.of(),
//                Map.of(),
//                null,
//                true); // Add inStock parameter
//
//        when(productService.getProductById(3L)).thenReturn(Optional.of(product));
//
//        mockMvc.perform(get("/products/3")
//                .contentType(MediaType.APPLICATION_JSON))
//
//        .andExpect(status().isOk())
//        .andExpect(jsonPath("$.id").value(3))
//        .andExpect(jsonPath("$.name").value("Bottled Water"))
//        .andExpect(jsonPath("$.price").value(1.50))
//        .andExpect(jsonPath("$.productType").value("SIMPLE"))
//        .andExpect(jsonPath("$.stock").value(100));
//    }
//
//    @Test
//    @WithMockUser(username = "user")
//    void getNonExistentProductReturns404() throws Exception {
//        when(productService.getProductById(999L)).thenReturn(Optional.empty());
//
//        mockMvc.perform(get("/products/999")
//                .contentType(MediaType.APPLICATION_JSON))
//
//        .andExpect(status().isNotFound());
//    }
//
//    @Test
//    @WithMockUser(authorities = "ROLE_USER")
//    void createProductForbiddenForNonAdmin() throws Exception {
//        var requestBody = """
//            {
//                "name": "Forbidden Pizza",
//                "description": "Should be forbidden",
//                "price": 10.99,
//                "productType": "ELABORATE",
//                "ingredientIds": [1],
//                "ingredientQuantities": {
//                    "1": 1.0
//                }
//            }
//            """;
//
//        mockMvc.perform(post("/products")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(requestBody))
//
//        .andExpect(status().isForbidden());
//    }
//
//    @Test
//    @WithMockUser(authorities = "ROLE_ADMIN")
//    void updateProductSuccessfully() throws Exception {
//        var updatedProduct = new ProductDTO(1L, "Updated Pizza",
//                "Updated description",
//                new BigDecimal("18.99"), true,
//                "ELABORATE",
//                null,
//                Set.of("Tomato", "Cheese", "Basil"),
//                Map.of("Tomato", new BigDecimal("0.5"), "Cheese", new BigDecimal("0.3"), "Basil", new BigDecimal("0.1")),
//                null,
//                true); // Add inStock parameter
//
//        when(productService.updateProduct(eq(1L), any(ProductUpdateDTO.class)))
//            .thenReturn(Optional.of(updatedProduct));
//
//        var requestBody = """
//            {
//                "name": "Updated Pizza",
//                "description": "Updated description",
//                "price": 18.99,
//                "ingredientQuantities": {
//                    "1": 0.5,
//                    "2": 0.3,
//                    "3": 0.1
//                }
//            }
//            """;
//
//        mockMvc.perform(put("/products/1")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(requestBody))
//
//        .andExpect(status().isOk())
//        .andExpect(jsonPath("$.success").value(true))
//        .andExpect(jsonPath("$.message").value("Producto actualizado correctamente"));
//    }
//
//    @Test
//    @WithMockUser(authorities = "ROLE_ADMIN")
//    void updateStockForSimpleProduct() throws Exception {
//        var updatedProduct = new ProductDTO(2L, "Coca-Cola",
//                "Refreshing beverage",
//                new BigDecimal("2.50"), true,
//                "SIMPLE",
//                new BigDecimal("75"),
//                Set.of(),
//                Map.of(),
//                null,
//                true); // Add inStock parameter
//
//        when(productService.updateProduct(eq(2L), any(ProductUpdateDTO.class)))
//            .thenReturn(Optional.of(updatedProduct));
//
//        var requestBody = """
//            {
//                "stock": 75
//            }
//            """;
//
//        mockMvc.perform(put("/products/2")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(requestBody))
//
//        .andExpect(status().isOk())
//        .andExpect(jsonPath("$.success").value(true));
//    }
//
//    @Test
//    @WithMockUser(authorities = "ROLE_ADMIN")
//    void deleteProductSuccessfully() throws Exception {
//        when(productService.deleteProduct(eq(1L), any(ProductDeleteDTO.class), anyString()))
//            .thenReturn("SOFT_DELETE");
//
//        var requestBody = """
//            {
//                "reason": "Test deletion"
//            }
//            """;
//
//        mockMvc.perform(delete("/products/1")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(requestBody))
//
//        .andExpect(status().isOk())
//        .andExpect(jsonPath("$.success").value(true))
//        .andExpect(jsonPath("$.deletionType").value("SOFT_DELETE"));
//    }
//
//    // Add new test cases for inStock functionality
//    @Test
//    @WithMockUser(username = "user")
//    void getProductWithInStockFalse() throws Exception {
//        var product = new ProductDTO(4L, "Out of Stock Pizza", "Temporarily unavailable",
//                new BigDecimal("15.99"), true,
//                "ELABORATE",
//                null,
//                Set.of("Tomato", "Cheese"),
//                Map.of("Tomato", new BigDecimal("0.5"), "Cheese", new BigDecimal("0.3")),
//                null,
//                false); // inStock = false
//
//        when(productService.getProductById(4L)).thenReturn(Optional.of(product));
//
//        mockMvc.perform(get("/products/4")
//                .contentType(MediaType.APPLICATION_JSON))
//
//        .andExpect(status().isOk())
//        .andExpect(jsonPath("$.id").value(4))
//        .andExpect(jsonPath("$.name").value("Out of Stock Pizza"))
//        .andExpect(jsonPath("$.inStock").value(false));
//    }
//
//    @Test
//    @WithMockUser(username = "user")
//    void getInactiveProduct() throws Exception {
//        var product = new ProductDTO(5L, "Discontinued Product", "No longer available",
//                new BigDecimal("9.99"), false, // available = false
//                "SIMPLE",
//                new BigDecimal("0"),
//                Set.of(),
//                Map.of(),
//                null,
//                false); // inStock = false
//
//        when(productService.getProductById(5L)).thenReturn(Optional.of(product));
//
//        mockMvc.perform(get("/products/5")
//                .contentType(MediaType.APPLICATION_JSON))
//
//        .andExpect(status().isOk())
//        .andExpect(jsonPath("$.id").value(5))
//        .andExpect(jsonPath("$.available").value(false))
//        .andExpect(jsonPath("$.inStock").value(false));
//    }
//}