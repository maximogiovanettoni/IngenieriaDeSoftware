package ar.uba.fi.ingsoft1.sistema_comedores.config.validation.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.*;

import ar.uba.fi.ingsoft1.sistema_comedores.common.enums.UserRole;

/**
 * Validación para role de usuario
 * Valida que el string corresponda a un UserRole válido o sea null/blank (default STUDENT)
 */
@Documented
@Constraint(validatedBy = ValidUserRole.UserRoleValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidUserRole {
    String message() default "${validation.user.role.messages.invalid-value}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    
    class UserRoleValidator implements ConstraintValidator<ValidUserRole, String> {
        
        @Override
        public void initialize(ValidUserRole constraintAnnotation) {
            // No initialization needed
        }
        
        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            // Null o blank son válidos (se asigna STUDENT por defecto)
            if (value == null || value.isBlank()) {
                return true;
            }
            
            try {
                UserRole.fromValue(value);
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
    }
}