package ar.uba.fi.ingsoft1.sistema_comedores.config.validation;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "validation")
@Data
public class ValidationConfig {
    
    private Email email = new Email();
    private User user = new User();
    
    @Data
    public static class Email {
        private final String allowedEmailDomain = ValidationConsts.ALLOWED_EMAIL_DOMAIN;
        private final String domainPattern = ValidationConsts.EMAIL_DOMAIN_PATTERN;
        
        private Messages messages = new Messages();
        
        @Data
        public static class Messages {
            private String required;
            private String invalidFormat;
            private String invalidDomain;
        }
    }
    
    @Data
    public static class User {
        private BirthDate birthDate = new BirthDate();
        private Name firstName = new Name();
        private Name lastName = new Name();
        private Password password = new Password();
        private Address address = new Address();
        private Gender gender = new Gender();

        @Data
        public static class BirthDate {
            private final int minAge = ValidationConsts.AGE_MIN_VALUE;
            private final int maxAge = ValidationConsts.AGE_MAX_VALUE;
            
            private Messages messages = new Messages();
            
            @Data
            public static class Messages {
                private String required;
                private String invalidFormat;
                private String invalidRange;
                private String past;
            }
        }
        
        @Data
        public static class Name {
            private final int minLength = ValidationConsts.NAME_MIN_LENGTH;
            private final int maxLength = ValidationConsts.NAME_MAX_LENGTH;
            private final String pattern = ValidationConsts.NAME_PATTERN;
            
            private Messages messages = new Messages();
            
            @Data
            public static class Messages {
                private String required;
                private String invalidFormat;
                private String invalidLength;
            }
        }

        @Data
        public static class Password {
            private final int minLength = ValidationConsts.PASSWORD_MIN_LENGTH;
            private final int maxLength = ValidationConsts.PASSWORD_MAX_LENGTH;
            private final String pattern = ValidationConsts.PASSWORD_PATTERN;
            
            private Messages messages = new Messages();
            
            @Data
            public static class Messages {
                private String required;
                private String invalidFormat;
                private String invalidLength;
            }
        }

        @Data
        public static class Address {
            private final int maxLength = ValidationConsts.ADDRESS_MAX_LENGTH;
            private final String pattern = ValidationConsts.ADDRESS_PATTERN;
            
            private Messages messages = new Messages();
            
            @Data
            public static class Messages {
                private String required;
                private String invalidFormat;
                private String invalidLength;
            }
        }

        @Data
        public static class Gender {
            private final String pattern = ValidationConsts.GENDER_PATTERN;
            
            private Messages messages = new Messages();
            
            @Data
            public static class Messages {
                private String required;
                private String invalidFormat;
                private String invalidValue;
            }
        }
    }
}