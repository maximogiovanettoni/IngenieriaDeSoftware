package ar.uba.fi.ingsoft1.sistema_comedores.config.validation.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.*;
import java.time.LocalDate;
import java.time.Period;
import ar.uba.fi.ingsoft1.sistema_comedores.config.validation.ValidationConsts;
import ar.uba.fi.ingsoft1.sistema_comedores.config.validation.ValidationConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Validación compuesta para fecha de nacimiento
 * Valida que no sea nula, sea en el pasado y la edad esté entre 18-99 años
 */
@Documented
@Constraint(validatedBy = ValidBirthDate.BirthDateValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidBirthDate {
    String message() default "Fecha de nacimiento inválida";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    int minAge() default ValidationConsts.AGE_MIN_VALUE;
    int maxAge() default ValidationConsts.AGE_MAX_VALUE;

    @Component
    class BirthDateValidator implements ConstraintValidator<ValidBirthDate, LocalDate> {
        
        @Autowired(required = false)
        private ValidationConfig validationConfig;
        
        private int minAge;
        private int maxAge;

        @Override
        public void initialize(ValidBirthDate constraintAnnotation) {
            this.minAge = constraintAnnotation.minAge();
            this.maxAge = constraintAnnotation.maxAge();
        }

        @Override
        public boolean isValid(LocalDate birthDate, ConstraintValidatorContext context) {
            if (birthDate == null) {
                return true;
            }

            LocalDate now = LocalDate.now();
            
            // Verificar que sea en el pasado
            if (!birthDate.isBefore(now)) {
                context.disableDefaultConstraintViolation();
                String message = getMessageSafely("past", 
                    "La fecha de nacimiento debe ser en el pasado");
                context.buildConstraintViolationWithTemplate(message)
                        .addConstraintViolation();
                return false;
            }

            // Verificar rango de edad
            int age = Period.between(birthDate, now).getYears();
            if (age < minAge || age > maxAge) {
                context.disableDefaultConstraintViolation();
                String message = getMessageSafely("invalidRange",
                    String.format("La edad debe estar entre %d y %d años", minAge, maxAge));
                context.buildConstraintViolationWithTemplate(message)
                        .addConstraintViolation();
                return false;
            }

            return true;
        }
        
        /**
         * Helper method to safely get messages from ValidationConfig
         * Falls back to default message if config is not available
         */
        private String getMessageSafely(String messageType, String defaultMessage) {
            if (validationConfig == null) {
                return defaultMessage;
            }
            
            try {
                var userConfig = validationConfig.getUser();
                if (userConfig == null) {
                    return defaultMessage;
                }
                
                var birthDateConfig = userConfig.getBirthDate();
                if (birthDateConfig == null) {
                    return defaultMessage;
                }
                
                var messages = birthDateConfig.getMessages();
                if (messages == null) {
                    return defaultMessage;
                }
                
                String message = messageType.equals("past") 
                    ? messages.getPast() 
                    : messages.getInvalidRange();
                    
                return (message != null && !message.isEmpty()) ? message : defaultMessage;
            } catch (Exception e) {
                return defaultMessage;
            }
        }
    }
}