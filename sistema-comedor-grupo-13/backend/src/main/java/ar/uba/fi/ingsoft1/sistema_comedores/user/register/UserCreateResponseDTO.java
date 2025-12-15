package ar.uba.fi.ingsoft1.sistema_comedores.user.register;

import java.time.Instant;
import java.time.LocalDate;

import ar.uba.fi.ingsoft1.sistema_comedores.user.User;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response DTO returned after creating a user.
 * Contains identifying and audit information.
 */
@Schema(description = "User information returned after successful registration")
public record UserCreateResponseDTO(
		@Schema(description = "User ID", example = "123")
		Long id,
		
		@Schema(description = "User email", example = "juan.perez@fi.uba.ar")
		String email,
		
		@Schema(description = "Username (same as email)", example = "juan.perez@fi.uba.ar")
		String username,
		
		@Schema(description = "First name", example = "Juan")
		String firstName,
		
		@Schema(description = "Last name", example = "Pérez")
		String lastName,
		
		@Schema(description = "Birth date", example = "1995-03-15")
        LocalDate birthDate,
        
        @Schema(description = "Address", example = "Av. Paseo Colón 850, CABA")
		String address,
		
		@Schema(description = "Gender", example = "male", allowableValues = {"male", "female", "other"})
		String gender,
		
		@Schema(description = "User role", example = "Estudiante")
		String role,
		
		@Schema(description = "Account creation timestamp", example = "2023-11-03T14:30:00Z")
		Instant createdAt
) {

	public static UserCreateResponseDTO from(User user) {
		if (user == null) return null;
		return new UserCreateResponseDTO(
				user.getId(),
				user.getEmail(),
				user.getUsername(),
				user.getFirstName(),
				user.getLastName(),
                user.getBirthDate(),
				user.getAddress(),
				user.getGender().getValue(),
				user.getRole().getDisplayName(),
				user.getCreatedAt()
		);
	}
}
