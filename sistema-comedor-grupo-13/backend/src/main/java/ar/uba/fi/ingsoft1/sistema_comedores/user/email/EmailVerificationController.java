package ar.uba.fi.ingsoft1.sistema_comedores.user.email;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ar.uba.fi.ingsoft1.sistema_comedores.user.exception.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@Tag(name = "Email Verification", description = "Email verification endpoints")
public class EmailVerificationController {
    
    @Autowired
    private EmailVerificationService emailVerificationService;
    
    @PostMapping("/verify-email")
    @Operation(summary = "Verify email address using token")
    @ApiResponse(responseCode = "200", description = "Email verified successfully")
    @ApiResponse(responseCode = "400", description = "Invalid, expired, or already verified token")
    public ResponseEntity<?> verifyEmail(@RequestBody @Valid EmailVerificationRequestDTO request) {
        try {
            emailVerificationService.verifyEmail(request.token());
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Email verified successfully! Your account is now active."
            ));
        } catch (InvalidTokenException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "INVALID_TOKEN",
                "message", e.getMessage()
            ));
        } catch (TokenExpiredException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "TOKEN_EXPIRED", 
                "message", e.getMessage()
            ));
        } catch (EmailAlreadyVerifiedException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "ALREADY_VERIFIED",
                "message", e.getMessage()
            ));
        }
    }
    
    @PostMapping("/resend-verification")
    @Operation(summary = "Resend verification email")
    @ApiResponse(responseCode = "200", description = "Verification email sent")
    @ApiResponse(responseCode = "400", description = "User not found or already verified")
    public ResponseEntity<?> resendVerification(@RequestParam String email) {
        try {
            emailVerificationService.resendVerificationEmail(email);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Verification email sent. Please check your inbox."
            ));
        } catch (UserNotFoundException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "USER_NOT_FOUND",
                "message", e.getMessage()
            ));
        } catch (EmailAlreadyVerifiedException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "ALREADY_VERIFIED",
                "message", e.getMessage()
            ));
        }
    }
    
    @GetMapping("/verification-status")
    @Operation(summary = "Check email verification status")
    public ResponseEntity<?> checkVerificationStatus(@RequestParam String email) {
        boolean isVerified = emailVerificationService.isEmailVerified(email);
        return ResponseEntity.ok(Map.of(
            "email", email,
            "isVerified", isVerified
        ));
    }
}