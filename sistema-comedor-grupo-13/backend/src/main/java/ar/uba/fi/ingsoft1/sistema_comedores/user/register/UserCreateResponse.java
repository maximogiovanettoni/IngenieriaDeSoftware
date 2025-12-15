package ar.uba.fi.ingsoft1.sistema_comedores.user.register;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Wrapper class for Swagger documentation of user creation response
 */
@Schema(description = "Response when creating a new user")
public record UserCreateResponse(
    @Schema(description = "Whether the operation was successful", example = "true")
    boolean success,
    
    @Schema(description = "Response message", example = "User registered successfully. Please check your email to verify your account.")
    String message,
    
    @Schema(description = "Created user information")
    UserCreateResponseDTO user
) {
    public static UserCreateResponse success(UserCreateResponseDTO user) {
        return new UserCreateResponse(
            true,
            "User registered successfully. Please check your email to verify your account.",
            user
        );
    }
    
    public static UserCreateResponse conflict() {
        return new UserCreateResponse(
            false,
            "User already exists",
            null
        );
    }
}