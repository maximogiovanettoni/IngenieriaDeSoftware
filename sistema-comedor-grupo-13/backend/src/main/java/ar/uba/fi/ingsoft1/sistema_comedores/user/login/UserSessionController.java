package ar.uba.fi.ingsoft1.sistema_comedores.user.login;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import ar.uba.fi.ingsoft1.sistema_comedores.user.UserService;
import ar.uba.fi.ingsoft1.sistema_comedores.user.exception.AccountNotVerifiedException;
import ar.uba.fi.ingsoft1.sistema_comedores.user.exception.InvalidCredentialsException;
import ar.uba.fi.ingsoft1.sistema_comedores.user.token.RefreshDTO;
import ar.uba.fi.ingsoft1.sistema_comedores.user.token.TokenDTO;
import java.util.Map;

@RestController
@RequestMapping("/sessions")
@Tag(name = "Sessions")
class UserSessionController {
    
    private final UserService userService;
    
    @Autowired
    UserSessionController(UserService userService) {
        this.userService = userService;
    }
    
    @PostMapping(produces = "application/json")
    @Operation(summary = "Log in, creating a new session")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponse(responseCode = "401", description = "Invalid username or password supplied", content = @Content)
    public ResponseEntity<?> login(
        @Valid @NonNull @RequestBody UserLoginDTO data
    ) throws MethodArgumentNotValidException {
        try {
            TokenDTO tokens = userService.loginUser(data);
            return ResponseEntity.status(HttpStatus.CREATED).body(tokens);
        } catch (InvalidCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "success", false,
                "error", "INVALID_CREDENTIALS",
                "message", e.getMessage()
            ));
        } catch (AccountNotVerifiedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "success", false,
                "error", "ACCOUNT_NOT_VERIFIED",
                "message", e.getMessage()
            ));
        }
    }
    
    @PutMapping(produces = "application/json")
    @Operation(summary = "Refresh a session")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponse(responseCode = "401", description = "Invalid refresh token supplied", content = @Content)
    public TokenDTO refresh(
        @Valid @NonNull @RequestBody RefreshDTO data
    ) throws MethodArgumentNotValidException {
        return userService
            .refresh(data)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
    
    @DeleteMapping(produces = "application/json")
    @Operation(summary = "Log out, ending the current session")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponse(responseCode = "204", description = "Successfully logged out")
    public ResponseEntity<Void> logout(
        @Valid @NonNull @RequestBody RefreshDTO data
    ) throws MethodArgumentNotValidException {
        userService.logout(data);
        return ResponseEntity.noContent().build();
    }
}
