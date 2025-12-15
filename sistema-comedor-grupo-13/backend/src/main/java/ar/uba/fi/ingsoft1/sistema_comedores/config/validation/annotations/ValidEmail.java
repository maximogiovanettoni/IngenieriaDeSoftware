package ar.uba.fi.ingsoft1.sistema_comedores.config.validation.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.lang.annotation.*;

import ar.uba.fi.ingsoft1.sistema_comedores.config.validation.ValidationConsts;

/**
 * Validación compuesta para email
 * Combina @NotBlank, @Email y @Pattern para dominio específico
 */
@Documented
@Constraint(validatedBy = {})
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@NotBlank(message = "${validation.email.messages.required}")
@Email(message = "${validation.email.messages.invalid-format}")
@Pattern(regexp = ValidationConsts.EMAIL_DOMAIN_PATTERN, 
         message = "${validation.email.messages.invalid-domain}")
public @interface ValidEmail {
    String message() default "${validation.email.messages.invalid}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}