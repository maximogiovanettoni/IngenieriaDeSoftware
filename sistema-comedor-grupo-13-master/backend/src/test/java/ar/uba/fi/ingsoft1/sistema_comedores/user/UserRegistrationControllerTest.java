package ar.uba.fi.ingsoft1.sistema_comedores.user;

import com.fasterxml.jackson.databind.ObjectMapper;

import ar.uba.fi.ingsoft1.sistema_comedores.config.validation.ValidationConfig;
import ar.uba.fi.ingsoft1.sistema_comedores.user.register.UserCreateRequestDTO;
import ar.uba.fi.ingsoft1.sistema_comedores.user.register.UserCreateResponseDTO;
import ar.uba.fi.ingsoft1.sistema_comedores.user.register.UserRegistrationController;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

@WebMvcTest(UserRegistrationController.class)
@Import(ValidationConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class UserRegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerUserSuccessfully() throws Exception {

        UserCreateRequestDTO dto = new UserCreateRequestDTO(
                "Maximo",
                "Giovanettoni",
                "maxi@fi.uba.ar",
                "SecurePass123",
                LocalDate.of(1995, 3, 1), 
                "male", 
                "Av. Siempreviva 123, CABA"
        );
        
        // Mock successful response
        UserCreateResponseDTO mockResponse = new UserCreateResponseDTO(
            1L,                           // id
            "maxi@fi.uba.ar",            // email
            "maxi@fi.uba.ar",            // username
            "Maximo",                    // firstName
            "Giovanettoni",              // lastName
            LocalDate.of(1995, 3, 1),    // birthDate
            "Av. Siempreviva 123, CABA", // address
            "male",                      // gender
            "student",                // role
            Instant.now()                // createdAt
        );
        
        when(userService.createUser(any(UserCreateRequestDTO.class)))
            .thenReturn(Optional.of(mockResponse));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User registered successfully. Please check your email to verify your account."))
                .andExpect(jsonPath("$.user.id").value(1))
                .andExpect(jsonPath("$.user.email").value("maxi@fi.uba.ar"))
                .andExpect(jsonPath("$.user.firstName").value("Maximo"));
    }

    @Test
    void registerUserWithDuplicateEmailShouldFail() throws Exception {
        UserCreateRequestDTO dto = new UserCreateRequestDTO(
                "Maximo", 
                "Giovanettoni", 
                "maxi@fi.uba.ar", 
                "SecurePass123",
                LocalDate.of(1995, 3, 1), 
                "male", 
                "Av. Siempreviva 123, CABA"
        );
        
        // Mock failure (user already exists)
        when(userService.createUser(any(UserCreateRequestDTO.class)))
            .thenReturn(Optional.empty());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User already exists"));
    }    @Test
    void registerUserWithInvalidEmailShouldFailValidation() throws Exception {
        UserCreateRequestDTO dto = new UserCreateRequestDTO(
                "Maximo", 
                "Giovanettoni", 
                "maxi@gmail.com ", 
                "SecurePass123",
                LocalDate.of(1995, 3, 1), 
                "male", 
                "Av. Siempreviva 123, CABA"
        );

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerUserWithShortPasswordShouldFailValidation() throws Exception {
        UserCreateRequestDTO dto = new UserCreateRequestDTO(
                "Maximo", 
                "Giovanettoni", 
                "maxi@fi.uba.ar", 
                "123",
                LocalDate.of(1995, 3, 1), 
                "male",
                "Av. Siempreviva 123, CABA"
        );

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerUserWithMissingFieldsShouldFailValidation() throws Exception {
        UserCreateRequestDTO dto = new UserCreateRequestDTO(
                "Maximo", 
                "", 
                "maxi@fi.uba.ar", 
                "",
                LocalDate.of(1995, 3, 1), 
                "male", // Gender is required and must be valid
                ""
                );

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }
}