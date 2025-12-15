package ar.uba.fi.ingsoft1.sistema_comedores.user.password;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import java.util.Map;

import ar.uba.fi.ingsoft1.sistema_comedores.user.exception.UserNotFoundException;
import ar.uba.fi.ingsoft1.sistema_comedores.user.exception.InvalidTokenException;
import ar.uba.fi.ingsoft1.sistema_comedores.user.exception.TokenExpiredException;


@RestController
@RequestMapping("/password")
@Tag(name = "Reset Password", description = "Reset Password endpoints")
@RequiredArgsConstructor
public class PasswordResetController {
    private final PasswordResetService passwordResetService;

    @PostMapping("/reset-request")
    @Operation(summary = "Verify em using token")
    @ApiResponse(responseCode = "200", description = "Email sent successfully")
    @ApiResponse(responseCode = "400", description = "User with given email does not exist")
    public ResponseEntity<?> requestPasswordReset(@Valid @RequestBody PasswordResetRequestDTO request) {
        
        try{
            passwordResetService.requestPasswordReset(request.email());
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Email enviado correctamente. Por favor, revise su bandeja de entrada."
            ));
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(400).body(Map.of(
                "success", false,
                "error", "USER_NOT_FOUND",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "INTERNAL_SERVER_ERROR",
                "message", e.getMessage()
            ));
        }

    }

    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody PasswordResetDTO resetDTO) {

        try{
            passwordResetService.resetPassword(resetDTO.token(), resetDTO.newPassword());
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Contraseña restablecida correctamente."
            ));
        } catch (InvalidTokenException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "INVALID_TOKEN",
                "message", e.getMessage()
            ));
        } catch (TokenExpiredException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "success", false,
                "error", "TOKEN_EXPIRED",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "INTERNAL_SERVER_ERROR",
                "message", e.getMessage()
            ));
        }
    }
}