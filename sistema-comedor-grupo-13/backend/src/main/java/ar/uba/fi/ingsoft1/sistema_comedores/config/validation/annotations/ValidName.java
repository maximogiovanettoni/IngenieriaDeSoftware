package ar.uba.fi.ingsoft1.sistema_comedores.config.validation.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.lang.annotation.*;

import ar.uba.fi.ingsoft1.sistema_comedores.config.validation.ValidationConsts;

/**
 * Validación compuesta para nombres (firstName, lastName)
 * Combina @NotBlank, @Size y @Pattern en una sola anotación
 */
@Documented
@Constraint(validatedBy = {})
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@NotBlank(message = "${validation.user.name.messages.required}")
@Size(min = ValidationConsts.NAME_MIN_LENGTH, max = ValidationConsts.NAME_MAX_LENGTH, 
      message = "${validation.user.name.messages.invalid-length}")
@Pattern(regexp = ValidationConsts.NAME_PATTERN, 
         message = "${validation.user.name.messages.invalid-format}")
public @interface ValidName {
    String message() default "${validation.user.name.messages.invalid}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    
    /**
     * Tipo de nombre para personalizar mensajes
     */
    NameType value() default NameType.GENERIC;
    
    enum NameType {
        FIRST_NAME("${validation.user.first-name.messages.invalid}"),
        LAST_NAME("${validation.user.last-name.messages.invalid}"),
        GENERIC("${validation.user.name.messages.invalid}");
        
        private final String defaultMessage;
        
        NameType(String defaultMessage) {
            this.defaultMessage = defaultMessage;
        }
        
        public String getDefaultMessage() {
            return defaultMessage;
        }
    }
}