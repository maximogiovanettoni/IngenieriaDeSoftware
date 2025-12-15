// package ar.uba.fi.ingsoft1.sistema_comedores.user;

// import com.fasterxml.jackson.databind.ObjectMapper;

// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// import org.springframework.http.MediaType;
// import org.springframework.test.web.servlet.MockMvc;

// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.when;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @WebMvcTest(UserRegistrationController.class)
// class UserRestControllerTest {

//     @Autowired
//     private MockMvc mockMvc;

//     @MockBean
//     private UserService userService;

//     @Autowired
//     private ObjectMapper objectMapper;

//     @Test
//     void registerUserSuccessfully() throws Exception {
//         UserCreateDTO dto = new UserCreateDTO(
//                 "Maximo", 
//                 "Giovanettoni", 
//                 "maxi@fi.uba.ar", 
//                 "SecurePass123",
//                 25, 
//                 "male", 
//                 "Av. Siempreviva 123, CABA", 
//                 "student"
//         );
//         when(userService.createUser(any(UserCreateDTO.class))).thenReturn(true);

//         mockMvc.perform(post("/users")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(dto)))
//                 .andExpect(status().isCreated())
//                 .andExpect(jsonPath("$.success").value(true))
//                 .andExpect(jsonPath("$.message").exists());
//     }

//     @Test
//     void registerUserWithDuplicateEmailShouldFail() throws Exception {
//         UserCreateDTO dto = new UserCreateDTO(
//                 "Maximo", 
//                 "Giovanettoni", 
//                 "maxi@fi.uba.ar", 
//                 "SecurePass123",
//                 25, 
//                 "male", 
//                 "Av. Siempreviva 123, CABA", 
//                 "student"
//         );
//         when(userService.createUser(any(UserCreateDTO.class))).thenReturn(false);

//         mockMvc.perform(post("/users")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(dto)))
//                 .andExpect(status().isConflict())
//                 .andExpect(jsonPath("$.success").value(false));
//     }

//     @Test
//     void registerUserWithInvalidEmailShouldFailValidation() throws Exception {
//         UserCreateDTO dto = new UserCreateDTO(
//                 "Maximo", 
//                 "Giovanettoni", 
//                 "maxi@gmail.com", 
//                 "SecurePass123",
//                 25, 
//                 "male", 
//                 "Av. Siempreviva 123, CABA", 
//                 "student"
//         );

//         mockMvc.perform(post("/users")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(dto)))
//                 .andExpect(status().isBadRequest());
//     }

//     @Test
//     void registerUserWithShortPasswordShouldFailValidation() throws Exception {
//         UserCreateDTO dto = new UserCreateDTO(
//                 "Maximo", 
//                 "Giovanettoni", 
//                 "maxi@fi.uba.ar", 
//                 "123",
//                 25, 
//                 "male", 
//                 "Av. Siempreviva 123, CABA", 
//                 "student"
//         );

//         mockMvc.perform(post("/users")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(dto)))
//                 .andExpect(status().isBadRequest());
//     }

//     @Test
//     void registerUserWithMissingFieldsShouldFailValidation() throws Exception {
//         UserCreateDTO dto = new UserCreateDTO(
//                 "Maximo", 
//                 "", 
//                 "maxi@fi.uba.ar", 
//                 "",
//                 25, 
//                 "", 
//                 "", 
//                 "student"
//         );

//         mockMvc.perform(post("/users")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(dto)))
//                 .andExpect(status().isBadRequest());
//     }
// }