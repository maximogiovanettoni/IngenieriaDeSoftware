package ar.uba.fi.ingsoft1.sistema_comedores.user.token;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ar.uba.fi.ingsoft1.sistema_comedores.common.enums.TokenType;
import ar.uba.fi.ingsoft1.sistema_comedores.user.User;
import ar.uba.fi.ingsoft1.sistema_comedores.config.security.JwtService;
import ar.uba.fi.ingsoft1.sistema_comedores.config.security.JwtUserDetails;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.Optional;

@Service
public class RefreshTokenService {
    
    private final TokenService tokenService;
    private final JwtService jwtService;
    
    @Autowired
    public RefreshTokenService(TokenService tokenService, JwtService jwtService) {
        this.tokenService = tokenService;
        this.jwtService = jwtService;
    }
    
    /**
     * Creates a new refresh token for the user.
     * Invalidates any existing refresh tokens for the user to ensure only one active refresh token.
     */
    @Transactional
    public Token createRefreshToken(User user) {
        // Invalidate existing refresh tokens (single refresh token per user policy)
        tokenService.invalidateAllForUser(user, TokenType.REFRESH);
        
        // Create new refresh token
        return tokenService.createFor(user, TokenType.REFRESH);
    }
    
    /**
     * Finds a valid refresh token by its value.
     */
    public Optional<Token> findByValue(String value) {
        return tokenService.findByValueAndTokenType(value, TokenType.REFRESH);
    }
    
    /**
     * Finds a valid refresh token for a specific user.
     */
    public Optional<Token> findByUser(User user) {
        return tokenService.findByUserAndTokenType(user, TokenType.REFRESH);
    }
    
    /**
     * Validates and returns the refresh token if it's valid.
     * Returns empty if token doesn't exist or is expired.
     */
    public Optional<Token> validateRefreshToken(String tokenValue) {
        return findByValue(tokenValue);
    }
    
    /**
     * Rotates a refresh token: invalidates the old one and creates a new one.
     * This is a security best practice for refresh token rotation.
     */
    @Transactional
    public Token rotateRefreshToken(Token oldToken) {
        User user = oldToken.getUser();
        
        // Invalidate the old refresh token
        tokenService.invalidate(oldToken);
        
        // Create and return a new refresh token
        return tokenService.createFor(user, TokenType.REFRESH);
    }
    
    /**
     * Refreshes both access and refresh tokens.
     * Validates the refresh token, rotates it, and generates a new access token.
     */
    @Transactional
    public Optional<TokenDTO> refresh(String refreshTokenValue) {
        return validateRefreshToken(refreshTokenValue)
            .map(oldToken -> {
                User user = oldToken.getUser();
                Token newRefreshToken = rotateRefreshToken(oldToken);
                String accessToken = generateAccessToken(user);
                return new TokenDTO(accessToken, newRefreshToken.getValue(), user.getRole().getValue());
            });
    }
    
    /**
     * Generates a complete token pair (access + refresh) for a user.
     * Used during login.
     */
    public TokenDTO generateTokenPair(User user) {
        String accessToken = generateAccessToken(user);
        Token refreshToken = createRefreshToken(user);
        return new TokenDTO(accessToken, refreshToken.getValue(), user.getRole().getValue());
    }
    
    /**
     * Revokes (invalidates) a specific refresh token.
     */
    @Transactional
    public void revokeRefreshToken(Token token) {
        if (token.getType() == TokenType.REFRESH) {
            tokenService.invalidate(token);
        }
    }
    
    /**
     * Revokes all refresh tokens for a specific user.
     * Useful for logout from all devices or security incidents.
     */
    @Transactional
    public void revokeAllRefreshTokensForUser(User user) {
        tokenService.invalidateAllForUser(user, TokenType.REFRESH);
    }
    
    /**
     * Checks if a user has a valid refresh token.
     */
    public boolean hasValidRefreshToken(User user) {
        return findByUser(user).isPresent();
    }
    
    /**
     * Cleans up expired refresh tokens.
     */
    @Transactional
    public int cleanupExpiredTokens() {
        return tokenService.deleteExpiredTokens(Instant.now());
    }
    
    /**
     * Generates an access token (JWT) for the given user.
     */
    private String generateAccessToken(User user) {
        return jwtService.createToken(new JwtUserDetails(
            user.getEmail(),
            user.getRole().toString()
        ));
    }
}