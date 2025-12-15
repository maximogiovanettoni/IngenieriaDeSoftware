package ar.uba.fi.ingsoft1.sistema_comedores.config.consts;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "messages")
@Data
public class MessageConfig {
    private Token token = new Token();
    private Auth auth = new Auth();
    private Email email = new Email();
    private Profile profile = new Profile();
    
    @Data
    public static class Token {
        private RefreshToken refresh = new RefreshToken();
        private EmailVerificationToken emailVerification = new EmailVerificationToken();
        private PasswordResetToken passwordReset = new PasswordResetToken();
        
        @Data
        public static class RefreshToken {
            private String expired;
            private String invalid;
            private String notFound;
            private String revoked;
            private String success;
        }
        
        @Data
        public static class EmailVerificationToken {
            private String expired;
            private String invalid;
            private String notFound;
            private String alreadyUsed;
            private String sent;
            private String resent;
        }
        
        @Data
        public static class PasswordResetToken {
            private String expired;
            private String invalid;
            private String notFound;
            private String alreadyUsed;
            private String sent;
            private String success;
        }
    }
    
    @Data
    public static class Auth {
        private String emailAlreadyVerified;
        private String emailNotVerified;
        private String emailVerificationSuccess;
        private String emailVerificationSent;
        private String userNotFound;
        private String userCreatedSuccess;
        private String invalidCredentials;
    }
    
    @Data
    public static class Email {
        private String verificationSubject;
        private String verificationSent;
        private String sendFailed;
    }

    @Data
    public static class Profile {
        private String updatedCorrectly;
    }
}