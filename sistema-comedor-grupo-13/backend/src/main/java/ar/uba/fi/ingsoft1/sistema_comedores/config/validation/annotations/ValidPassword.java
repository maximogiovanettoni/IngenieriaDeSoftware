package ar.uba.fi.ingsoft1.sistema_comedores.config.validation.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.lang.annotation.*;

import ar.uba.fi.ingsoft1.sistema_comedores.config.validation.ValidationConsts;

/**
 * Validación compuesta para contraseñas
 * Combina @NotBlank, @Size y @Pattern para complejidad
 */
@Documented
@Constraint(validatedBy = {})
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@NotBlank(message = "${validation.user.password.messages.required}")
@Size(min = ValidationConsts.PASSWORD_MIN_LENGTH, max = ValidationConsts.PASSWORD_MAX_LENGTH, 
      message = "${validation.user.password.messages.invalid-length}")
@Pattern(regexp = ValidationConsts.PASSWORD_PATTERN, 
         message = "${validation.user.password.messages.invalid-format}")
public @interface ValidPassword {
    String message() default "${validation.user.password.messages.invalid}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}