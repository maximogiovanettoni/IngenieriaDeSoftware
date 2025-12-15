//package ar.uba.fi.ingsoft1.sistema_comedores.products;
//
//import ar.uba.fi.ingsoft1.sistema_comedores.config.security.JwtService;
//import ar.uba.fi.ingsoft1.sistema_comedores.config.security.SecurityConfig;
//import ar.uba.fi.ingsoft1.sistema_comedores.products.dto.SimpleProductCreateDTO;
//import ar.uba.fi.ingsoft1.sistema_comedores.products.dto.ProductDTO;
//import ar.uba.fi.ingsoft1.sistema_comedores.products.dto.ProductSearchDTO;
//import ar.uba.fi.ingsoft1.sistema_comedores.products.exception.InvalidQuantityException;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.context.annotation.Import;
//import org.springframework.http.MediaType;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.servlet.MockMvc;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.when;
//
//import java.math.BigDecimal;
//import java.util.*;
//
//@WebMvcTest(controllers = ProductRestController.class)
//@Import({SecurityConfig.class, JwtService.class})
//class ProductRestControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockitoBean
//    private ProductService productService;
//
//    @Test
//    @WithMockUser
//    void getExistingProductById() throws Exception {
//        final long id = 1;
//        final String name = "Name";
//        final String description = "Description";
//        final BigDecimal price = new BigDecimal("15.99");
//        final Boolean available = true;
//
//        var dto = new ProductDTO(id, name, description, price, available,
//                                "ELABORATE",
//                                null,
//                                Set.of("Tomato", "Cheese"),
//                                Map.of("Tomato", new BigDecimal("0.5"), "Cheese", new BigDecimal("0.2")),
//                                null,
//                                true); // Add inStock parameter
//        when(productService.getProductById(id)).thenReturn(Optional.of(dto));
//
//        var request = get("/products/" + id)
//                .contentType(MediaType.APPLICATION_JSON);
//        mockMvc.perform(request).andExpectAll(
//                status().isOk(),
//                jsonPath("$.id").value(id),
//                jsonPath("$.name").value(name),
//                jsonPath("$.description").value(description),
//                jsonPath("$.price").value(15.99),
//                jsonPath("$.available").value(available),
//                jsonPath("$.productType").value("ELABORATE"),
//                jsonPath("$.ingredients").isArray(),
//                jsonPath("$.ingredients.length()").value(2),
//                jsonPath("$.ingredients[?(@ == 'Tomato')]").exists(),
//                jsonPath("$.ingredients[?(@ == 'Cheese')]").exists(),
//                jsonPath("$.ingredientQuantities.Tomato").value(0.5),
//                jsonPath("$.ingredientQuantities.Cheese").value(0.2)
//        );
//    }
//
//    @Test
//    @WithMockUser
//    void getSimpleProductById() throws Exception {
//        final long id = 2;
//        final String name = "Bottled Water";
//        final String description = "Pure water";
//        final BigDecimal price = new BigDecimal("1.50");
//        final Boolean available = true;
//
//        var dto = new ProductDTO(id, name, description, price, available,
//                                "SIMPLE",
//                                new BigDecimal("100"),
//                                Set.of(),
//                                Map.of(),
//                                null,
//                                true); // Add inStock parameter
//        when(productService.getProductById(id)).thenReturn(Optional.of(dto));
//
//        var request = get("/products/" + id)
//                .contentType(MediaType.APPLICATION_JSON);
//        mockMvc.perform(request).andExpectAll(
//                status().isOk(),
//                jsonPath("$.id").value(id),
//                jsonPath("$.name").value(name),
//                jsonPath("$.description").value(description),
//                jsonPath("$.price").value(1.50),
//                jsonPath("$.available").value(available),
//                jsonPath("$.productType").value("SIMPLE"),
//                jsonPath("$.stock").value(100),
//                jsonPath("$.ingredients").isArray(),
//                jsonPath("$.ingredients.length()").value(0)
//        );
//    }
//
//    @Test
//    @WithMockUser
//    void getAbsentProductById() throws Exception {
//        final long id = 1;
//
//        when(productService.getProductById(id)).thenReturn(Optional.empty());
//
//        var request = get("/products/" + id)
//                .contentType(MediaType.APPLICATION_JSON);
//        mockMvc.perform(request).andExpectAll(
//                status().isNotFound()
//        );
//    }
//
//    @Test
//    @WithMockUser(authorities = {"ROLE_ADMIN"})
//    void createElaboratedProductSuccessfully() throws Exception {
//        var requestBody = """
//            {
//                "name": "Pizza Margherita",
//                "description": "Classic pizza",
//                "price": 15.99,
//                "available": true,
//                "productType": "ELABORATE",
//                "ingredientIds": [1, 2],
//                "ingredientQuantities": {
//                    "1": 0.5,
//                    "2": 0.2
//                },
//                "image": "pizza.jpg"
//            }
//            """;
//
//        var resultProduct = new ProductDTO(1L, "Pizza Margherita", "Classic pizza",
//                                         new BigDecimal("15.99"), true,
//                                         "ELABORATE",
//                                         null,
//                                         Set.of("Tomato", "Cheese"),
//                                         Map.of("Tomato", new BigDecimal("0.5"), "Cheese", new BigDecimal("0.2")),
//                                         "pizza.jpg",
//                                         true); // Add inStock parameter
//
//        when(productService.createProduct(any(SimpleProductCreateDTO.class))).thenReturn(resultProduct);
//
//        var request = post("/products")
//                .content(requestBody)
//                .contentType(MediaType.APPLICATION_JSON);
//        mockMvc.perform(request).andExpectAll(
//            status().isCreated(),
//            jsonPath("$.id").value(1L),
//            jsonPath("$.name").value("Pizza Margherita"),
//            jsonPath("$.description").value("Classic pizza"),
//            jsonPath("$.price").value(15.99),
//            jsonPath("$.available").value(true),
//            jsonPath("$.productType").value("ELABORATE"),
//            jsonPath("$.ingredients").isArray(),
//            jsonPath("$.ingredients.length()").value(2),
//            jsonPath("$.ingredients[?(@ == 'Tomato')]").exists(),
//            jsonPath("$.ingredients[?(@ == 'Cheese')]").exists(),
//            jsonPath("$.ingredientQuantities.Tomato").value(0.5),
//            jsonPath("$.image").value("pizza.jpg")
//        );
//    }
//
//    @Test
//    @WithMockUser(authorities = {"ROLE_ADMIN"})
//    void createSimpleProductSuccessfully() throws Exception {
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
//        var resultProduct = new ProductDTO(2L, "Coca-Cola", "Refreshing beverage",
//                                         new BigDecimal("2.50"), true,
//                                         "SIMPLE",
//                                         new BigDecimal("50"),
//                                         Set.of(),
//                                         Map.of(),
//                                         null,
//                                         true); // Add inStock parameter
//
//        when(productService.createProduct(any(SimpleProductCreateDTO.class))).thenReturn(resultProduct);
//
//        var request = post("/products")
//                .content(requestBody)
//                .contentType(MediaType.APPLICATION_JSON);
//        mockMvc.perform(request).andExpectAll(
//            status().isCreated(),
//            jsonPath("$.id").value(2L),
//            jsonPath("$.name").value("Coca-Cola"),
//            jsonPath("$.description").value("Refreshing beverage"),
//            jsonPath("$.price").value(2.50),
//            jsonPath("$.available").value(true),
//            jsonPath("$.productType").value("SIMPLE"),
//            jsonPath("$.stock").value(50)
//        );
//    }
//
//    @Test
//    @WithMockUser
//    void createProductWithRegularUser() throws Exception {
//        var requestBody = """
//            {
//                "name": "Pizza",
//                "description": "Test pizza",
//                "price": 15.99,
//                "productType": "ELABORATE",
//                "ingredientIds": [1, 2],
//                "ingredientQuantities": {
//                    "1": 0.5,
//                    "2": 0.2
//                }
//            }
//            """;
//
//        when(productService.createProduct(any(SimpleProductCreateDTO.class)))
//        .thenThrow(new IllegalArgumentException("Todas las cantidades deben ser positivas"));
//
//        var request = post("/products")
//                .content(requestBody)
//                .contentType(MediaType.APPLICATION_JSON);
//        mockMvc.perform(request).andExpectAll(
//                status().isForbidden()
//        );
//    }
//
//    @Test
//    @WithMockUser(authorities = {"ROLE_ADMIN"})
//    void createProductWithMalformedJson() throws Exception {
//        var request = post("/products")
//                .content("{")
//                .contentType(MediaType.APPLICATION_JSON);
//        mockMvc.perform(request).andExpectAll(
//                status().isBadRequest()
//        );
//    }
//
//    @Test
//    @WithMockUser(authorities = {"ROLE_ADMIN"})
//    void createProductWithBadJson() throws Exception {
//        var request = post("/products")
//                .content("{\"error\":1}")
//                .contentType(MediaType.APPLICATION_JSON);
//        mockMvc.perform(request).andExpectAll(
//                status().isBadRequest()
//        );
//    }
//
//    @Test
//    @WithMockUser(authorities = {"ROLE_ADMIN"})
//    void createProductWithMissingRequiredFields() throws Exception {
//        var requestBody = """
//            {
//                "description": "Only description, missing name and price"
//            }
//            """;
//
//        var request = post("/products")
//                .content(requestBody)
//                .contentType(MediaType.APPLICATION_JSON);
//        mockMvc.perform(request).andExpectAll(
//                status().isBadRequest()
//        );
//    }
//
//    @Test
//    @WithMockUser(authorities = {"ROLE_ADMIN"})
//    void createProductWithInvalidQuantities() throws Exception {
//        var requestBody = """
//            {
//                "name": "Pizza",
//                "description": "Test pizza",
//                "price": 15.99,
//                "productType": "ELABORATE",
//                "ingredientIds": [1, 2],
//                "ingredientQuantities": {
//                    "1": -0.5,
//                    "2": 0.2
//                }
//            }
//            """;
//
//        when(productService.createProduct(any(SimpleProductCreateDTO.class)))
//        .thenThrow(new InvalidQuantityException("Todas las cantidades deben ser positivas"));
//
//        var request = post("/products")
//                .content(requestBody)
//                .contentType(MediaType.APPLICATION_JSON);
//        mockMvc.perform(request).andExpectAll(
//                status().isBadRequest()
//        );
//    }
//
//    @Test
//    @WithMockUser
//    void getProductsWithFilter() throws Exception {
//        var product1 = new ProductDTO(1L, "Pizza", "Delicious pizza",
//                                    new BigDecimal("15.99"), true,
//                                    "ELABORATE",
//                                    null,
//                                    Set.of("Tomato", "Cheese"),
//                                    Map.of("Tomato", new BigDecimal("0.5"), "Cheese", new BigDecimal("0.2")),
//                                    null,
//                                    true); // Add inStock parameter
//
//        when(productService.getProducts(any(ProductSearchDTO.class))).thenReturn(List.of(product1));
//
//        var request = get("/products?name=Piz")
//                .contentType(MediaType.APPLICATION_JSON);
//        mockMvc.perform(request).andExpectAll(
//                status().isOk(),
//                jsonPath("$[0].id").value(1L),
//                jsonPath("$[0].name").value("Pizza"),
//                jsonPath("$[0].productType").value("ELABORATE"),
//                jsonPath("$[0].ingredientQuantities.Tomato").value(0.5)
//        );
//    }
//
//    @Test
//    @WithMockUser
//    void getProductsWithoutFilter() throws Exception {
//        var product1 = new ProductDTO(1L, "Pizza", "Delicious pizza",
//                                    new BigDecimal("15.99"), true,
//                                    "ELABORATE",
//                                    null,
//                                    Set.of("Tomato", "Cheese"),
//                                    Map.of("Tomato", new BigDecimal("0.5"), "Cheese", new BigDecimal("0.2")),
//                                    null,
//                                    true); // Add inStock parameter
//        var product2 = new ProductDTO(2L, "Burger", "Tasty burger",
//                                    new BigDecimal("12.99"), true,
//                                    "ELABORATE",
//                                    null,
//                                    Set.of("Bread", "Meat"),
//                                    Map.of("Bread", new BigDecimal("0.3"), "Meat", new BigDecimal("0.4")),
//                                    null,
//                                    true); // Add inStock parameter
//
//        when(productService.getProducts(any(ProductSearchDTO.class))).thenReturn(List.of(product1, product2));
//
//        var request = get("/products")
//                .contentType(MediaType.APPLICATION_JSON);
//        mockMvc.perform(request).andExpectAll(
//                status().isOk(),
//                jsonPath("$.length()").value(2),
//                jsonPath("$[0].name").value("Pizza"),
//                jsonPath("$[0].productType").value("ELABORATE"),
//                jsonPath("$[1].name").value("Burger"),
//                jsonPath("$[1].productType").value("ELABORATE")
//        );
//    }
//
//    // Add new test cases for inStock functionality
//    @Test
//    @WithMockUser
//    void getProductWithInStockFalse() throws Exception {
//        final long id = 3;
//        var dto = new ProductDTO(id, "Out of Stock Pizza", "Temporarily unavailable",
//                                new BigDecimal("15.99"), true,
//                                "ELABORATE",
//                                null,
//                                Set.of("Tomato", "Cheese"),
//                                Map.of("Tomato", new BigDecimal("0.5"), "Cheese", new BigDecimal("0.2")),
//                                null,
//                                false); // inStock = false
//        when(productService.getProductById(id)).thenReturn(Optional.of(dto));
//
//        var request = get("/products/" + id)
//                .contentType(MediaType.APPLICATION_JSON);
//        mockMvc.perform(request).andExpectAll(
//                status().isOk(),
//                jsonPath("$.id").value(id),
//                jsonPath("$.name").value("Out of Stock Pizza"),
//                jsonPath("$.available").value(true),
//                jsonPath("$.inStock").value(false)
//        );
//    }
//
//    @Test
//    @WithMockUser
//    void getInactiveProduct() throws Exception {
//        final long id = 4;
//        var dto = new ProductDTO(id, "Discontinued Product", "No longer available",
//                                new BigDecimal("9.99"), false, // available = false
//                                "SIMPLE",
//                                new BigDecimal("0"),
//                                Set.of(),
//                                Map.of(),
//                                null,
//                                false); // inStock = false
//        when(productService.getProductById(id)).thenReturn(Optional.of(dto));
//
//        var request = get("/products/" + id)
//                .contentType(MediaType.APPLICATION_JSON);
//        mockMvc.perform(request).andExpectAll(
//                status().isOk(),
//                jsonPath("$.id").value(id),
//                jsonPath("$.name").value("Discontinued Product"),
//                jsonPath("$.available").value(false),
//                jsonPath("$.inStock").value(false)
//        );
//    }
//}