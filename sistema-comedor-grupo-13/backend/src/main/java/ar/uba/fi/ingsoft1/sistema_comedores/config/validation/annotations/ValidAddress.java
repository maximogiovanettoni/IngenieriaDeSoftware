package ar.uba.fi.ingsoft1.sistema_comedores.config.validation.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.lang.annotation.*;

import ar.uba.fi.ingsoft1.sistema_comedores.config.validation.ValidationConsts;

/**
 * Validación compuesta para dirección
 * Combina @NotBlank, @Size y @Pattern
 */
@Documented
@Constraint(validatedBy = {})
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@NotBlank(message = "${validation.user.address.messages.required}")
@Size(max = ValidationConsts.ADDRESS_MAX_LENGTH, 
      message = "${validation.user.address.messages.invalid-length}")
@Pattern(regexp = ValidationConsts.ADDRESS_PATTERN, 
         message = "${validation.user.address.messages.invalid-format}")
public @interface ValidAddress {
    String message() default "${validation.user.address.messages.invalid}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}