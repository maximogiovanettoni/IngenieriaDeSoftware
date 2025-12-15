package ar.uba.fi.ingsoft1.sistema_comedores.config.validation.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.lang.annotation.*;

import ar.uba.fi.ingsoft1.sistema_comedores.config.validation.ValidationConsts;

/**
 * Validación compuesta para género
 * Combina @NotBlank y @Pattern para valores válidos
 */
@Documented
@Constraint(validatedBy = {})
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@NotBlank(message = "${validation.user.gender.messages.required}")
@Pattern(regexp = ValidationConsts.GENDER_PATTERN, 
         message = "${validation.user.gender.messages.invalid-value}")
public @interface ValidGender {
    String message() default "${validation.user.gender.messages.invalid}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}