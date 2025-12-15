package ar.uba.fi.ingsoft1.sistema_comedores.ingredients;

import ar.uba.fi.ingsoft1.sistema_comedores.ingredients.dto.*;
import ar.uba.fi.ingsoft1.sistema_comedores.ingredients.exception.IngredientNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IngredientController.class)
@AutoConfigureMockMvc
class IngredientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IngredientService service;

    private Ingredient testIngredient;
    private IngredientDetailsResponse testResponse;

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

        testResponse = IngredientDetailsResponse.from(testIngredient);
    }

    // ==================== GET /ingredients ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAll_returnsListOfIngredients() throws Exception {
        when(service.getAll()).thenReturn(List.of(testResponse));

        mockMvc.perform(get("/ingredients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Tomate"))
                .andExpect(jsonPath("$[0].unitMeasure").value("kg"))
                .andExpect(jsonPath("$[0].stock").value("10"))
                .andExpect(jsonPath("$[0].available").value(true))
                .andExpect(jsonPath("$[0].active").value(true));

        verify(service).getAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAll_returnsEmptyList() throws Exception {
        when(service.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/ingredients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ==================== POST /ingredients ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_returnsCreatedIngredient() throws Exception {
        CreateIngredientRequest request = new CreateIngredientRequest("Lechuga", "unidad", BigDecimal.valueOf(20));

        Ingredient created = new Ingredient();
        created.setId(2L);
        created.setName("Lechuga");
        created.setUnitMeasure("unidad");
        created.setStock(BigDecimal.valueOf(20));
        created.setActive(true);
        created.setAvailable(true);

        when(service.createIngredient(any(CreateIngredientRequest.class))).thenReturn(created);

        mockMvc.perform(post("/ingredients")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("Lechuga"))
                .andExpect(jsonPath("$.unitMeasure").value("unidad"));

        verify(service).createIngredient(any(CreateIngredientRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_returnsBadRequest_whenNameBlank() throws Exception {
        String json = """
            {
                "name": "",
                "unitMeasure": "kg",
                "stock": 10
            }
            """;

        mockMvc.perform(post("/ingredients")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(service, never()).createIngredient(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_returnsBadRequest_whenUnitMeasureBlank() throws Exception {
        String json = """
            {
                "name": "Tomate",
                "unitMeasure": "",
                "stock": 10
            }
            """;

        mockMvc.perform(post("/ingredients")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(service, never()).createIngredient(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_returnsBadRequest_whenStockNegative() throws Exception {
        String json = """
            {
                "name": "Tomate",
                "unitMeasure": "kg",
                "stock": -5
            }
            """;

        mockMvc.perform(post("/ingredients")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(service, never()).createIngredient(any());
    }

    // ==================== PUT /ingredients/{id}/stock ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateStock_returnsUpdatedIngredient() throws Exception {
        testIngredient.setStock(BigDecimal.valueOf(25));

        when(service.updateStock(eq(1L), any(BigDecimal.class))).thenReturn(testIngredient);

        String json = """
            {
                "amount": 25
            }
            """;

        mockMvc.perform(put("/ingredients/1/stock")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value("25"));

        verify(service).updateStock(eq(1L), any(BigDecimal.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateStock_returnsBadRequest_whenAmountNull() throws Exception {
        String json = """
            {
                "amount": null
            }
            """;

        mockMvc.perform(put("/ingredients/1/stock")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(service, never()).updateStock(anyLong(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateStock_returnsBadRequest_whenAmountNotPositive() throws Exception {
        String json = """
            {
                "amount": 0
            }
            """;

        mockMvc.perform(put("/ingredients/1/stock")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(service, never()).updateStock(anyLong(), any());
    }

    // ==================== PUT /ingredients/{id}/name ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void changeName_returnsUpdatedIngredient() throws Exception {
        testIngredient.setName("Tomate Cherry");

        when(service.changeName(eq(1L), eq("Tomate Cherry"))).thenReturn(testIngredient);

        String json = """
            {
                "name": "Tomate Cherry"
            }
            """;

        mockMvc.perform(put("/ingredients/1/name")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Tomate Cherry"));

        verify(service).changeName(1L, "Tomate Cherry");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void changeName_returnsBadRequest_whenNameBlank() throws Exception {
        String json = """
            {
                "name": ""
            }
            """;

        mockMvc.perform(put("/ingredients/1/name")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(service, never()).changeName(anyLong(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void changeName_returnsBadRequest_whenNameTooLong() throws Exception {
        String longName = "A".repeat(101);
        String json = String.format("""
            {
                "name": "%s"
            }
            """, longName);

        mockMvc.perform(put("/ingredients/1/name")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(service, never()).changeName(anyLong(), any());
    }

    // ==================== DELETE /ingredients/{id} ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_returnsNoContent() throws Exception {
        when(service.deactivateIngredient(eq(1L), any())).thenReturn(testResponse);

        mockMvc.perform(delete("/ingredients/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(service).deactivateIngredient(eq(1L), isNull());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_withReason_returnsNoContent() throws Exception {
        when(service.deactivateIngredient(eq(1L), eq("Stock vencido"))).thenReturn(testResponse);

        String json = """
            {
                "reason": "Stock vencido"
            }
            """;

        mockMvc.perform(delete("/ingredients/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNoContent());

        verify(service).deactivateIngredient(1L, "Stock vencido");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_returnsBadRequest_whenReasonTooLong() throws Exception {
        String longReason = "A".repeat(301);
        String json = String.format("""
            {
                "reason": "%s"
            }
            """, longReason);

        mockMvc.perform(delete("/ingredients/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(service, never()).deactivateIngredient(anyLong(), any());
    }

    // ==================== Security Tests ====================

    @Test
    void getAll_returnsUnauthorized_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/ingredients"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void create_returnsForbidden_whenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/ingredients")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }
}