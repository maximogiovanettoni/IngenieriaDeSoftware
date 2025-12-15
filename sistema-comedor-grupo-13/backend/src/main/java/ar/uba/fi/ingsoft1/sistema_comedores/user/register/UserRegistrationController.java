package ar.uba.fi.ingsoft1.sistema_comedores.user.register;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ar.uba.fi.ingsoft1.sistema_comedores.user.UserService;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/users")
@Tag(name = "Users")
public class UserRegistrationController {
    private final UserService userService;

    @Autowired
    UserRegistrationController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(produces = "application/json")
    @Operation(summary = "Create a new user", description = "Register a new user in the system")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", 
            description = "User created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserCreateResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "409", 
            description = "User already exists", 
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid input data", 
            content = @Content(mediaType = "application/json")
        )
    })
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<Map<String, Object>> signUp(
            @Valid @NonNull @RequestBody UserCreateRequestDTO data
    ) throws MethodArgumentNotValidException {
        Optional<UserCreateResponseDTO> userCreated = userService.createUser(data);
        if (userCreated.isPresent()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "User registered successfully. Please check your email to verify your account.",
                "user", userCreated.get()
            ));
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "success", false,
                "message", "User already exists"
            ));
        }
    }
}
