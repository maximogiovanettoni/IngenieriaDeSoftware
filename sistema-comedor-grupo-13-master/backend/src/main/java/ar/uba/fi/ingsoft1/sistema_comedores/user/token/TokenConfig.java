package ar.uba.fi.ingsoft1.sistema_comedores.user.token;

import java.time.Duration;

import org.springframework.stereotype.Component;

import ar.uba.fi.ingsoft1.sistema_comedores.common.enums.TokenType;

@Component
public class TokenConfig {
    
    private static final int REFRESH_BYTE_SIZE = 32;
    private static final Duration REFRESH_EXPIRATION = Duration.ofDays(7);
    
    private static final int PASSWORD_RESET_BYTE_SIZE = 32;
    private static final Duration PASSWORD_RESET_EXPIRATION = Duration.ofHours(1);
    
    private static final int EMAIL_VERIFICATION_BYTE_SIZE = 24;
    private static final Duration EMAIL_VERIFICATION_EXPIRATION = Duration.ofDays(1);
    
    public int getByteSize(TokenType tokenType) {
        return switch (tokenType) {
            case REFRESH -> REFRESH_BYTE_SIZE;
            case PASSWORD_RESET -> PASSWORD_RESET_BYTE_SIZE;
            case EMAIL_VERIFICATION -> EMAIL_VERIFICATION_BYTE_SIZE;
        };
    }
    
    public Duration getExpiration(TokenType tokenType) {
        return switch (tokenType) {
            case REFRESH -> REFRESH_EXPIRATION;
            case PASSWORD_RESET -> PASSWORD_RESET_EXPIRATION;
            case EMAIL_VERIFICATION -> EMAIL_VERIFICATION_EXPIRATION;
        };
    }
}