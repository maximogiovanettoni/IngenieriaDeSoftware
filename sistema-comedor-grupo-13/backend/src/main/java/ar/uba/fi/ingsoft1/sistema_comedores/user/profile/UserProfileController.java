package ar.uba.fi.ingsoft1.sistema_comedores.user.profile;

import ar.uba.fi.ingsoft1.sistema_comedores.user.exception.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;
import jakarta.validation.*;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import java.util.HashMap;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/profile")
@Tag(name = "User Profile", description = "Operaciones relacionadas con el perfil del usuario")
public class UserProfileController {
    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @Operation(
        summary = "Obtener perfil del usuario autenticado",
        description = "Devuelve la información del usuario asociada al token JWT actual.",
        security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @GetMapping("/me")
    public ResponseEntity<?> getProfile(
        @org.springframework.security.core.annotation.AuthenticationPrincipal ar.uba.fi.ingsoft1.sistema_comedores.config.security.JwtUserDetails userDetails
    ) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(401).body(
                    java.util.Map.of("success", false, "message", "No autenticado")
                );
            }
            
            UserProfileDTO profile = userProfileService.getUserProfile(userDetails.username());
            return ResponseEntity.ok(profile);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(404).body(
                java.util.Map.of("success", false, "message", e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                java.util.Map.of("success", false, "message", "Error interno del servidor")
            );
        }
    }

    @Operation(
    summary = "Actualizar perfil del usuario autenticado",
    description = "Permite modificar los datos del usuario autenticado (nombre, apellido, edad, género, dirección).",
    security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @PutMapping("/me")
    public ResponseEntity<?> updateProfile(
        @org.springframework.security.core.annotation.AuthenticationPrincipal ar.uba.fi.ingsoft1.sistema_comedores.config.security.JwtUserDetails userDetails,
        @Valid @RequestBody UserProfileUpdateDTO updateDTO,
        BindingResult bindingResult
    ) {
    if (userDetails == null) {
        return ResponseEntity.status(401).body(
            Map.of("success", false, "message", "No autenticado")
        );
    }
    
    if (bindingResult.hasErrors()) {
        Map<String, Object> errors = new HashMap<>();
        errors.put("success", false);
        String error = "Datos inválidos en: " + bindingResult.getFieldErrors().stream()
            .map(FieldError::getField)
            .collect(Collectors.joining(", "));
        errors.put("message", error);
  
        return ResponseEntity.badRequest().body(errors);
    }
        try {
            userProfileService.updateUserProfile(userDetails.username(), updateDTO);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Perfil actualizado correctamente"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                Map.of("success", false, "message", e.getMessage())
            );
        }
    }

    @PostMapping("/me/photo")
    @Operation(
        summary = "Actualizar foto de perfil del usuario autenticado",
        description = "Permite subir una nueva foto de perfil. Guarda el archivo en el servidor y actualiza el path en la base de datos.",
        security = { @SecurityRequirement(name = "bearerAuth") }
    )
    public ResponseEntity<?> uploadProfilePhoto(
        @org.springframework.security.core.annotation.AuthenticationPrincipal ar.uba.fi.ingsoft1.sistema_comedores.config.security.JwtUserDetails userDetails,
        @RequestParam("file") MultipartFile file
    ) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false, "message", "No autenticado"
                ));
            }
            
            userProfileService.updateProfilePhoto(userDetails.username(), file);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Foto de perfil actualizada correctamente"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false, "message", e.getMessage()
            ));
        }
    }
}
