package ar.uba.fi.ingsoft1.sistema_comedores.config.validation.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.*;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidImageUrl.ValidImageUrlValidator.class)
@Documented
public @interface ValidImageUrl {
    String message() default "Invalid image URL. Images must be uploaded through the system.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    @Component
    public class ValidImageUrlValidator implements ConstraintValidator<ValidImageUrl, String> {
        
        private final String minioPublicUrl;
        
        public ValidImageUrlValidator(@Qualifier("minioPublicUrl") String minioPublicUrl) {
            this.minioPublicUrl = minioPublicUrl;
        }
        
        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            if (value == null || value.isEmpty()) {
                return true;
            }
            return value.startsWith(minioPublicUrl);
        }
    }
}