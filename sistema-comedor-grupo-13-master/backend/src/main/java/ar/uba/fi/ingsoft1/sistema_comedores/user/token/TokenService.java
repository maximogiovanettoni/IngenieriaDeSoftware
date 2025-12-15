package ar.uba.fi.ingsoft1.sistema_comedores.user.token;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.uba.fi.ingsoft1.sistema_comedores.common.enums.TokenType;
import ar.uba.fi.ingsoft1.sistema_comedores.user.User;
import jakarta.transaction.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import ar.uba.fi.ingsoft1.sistema_comedores.common.utils.Random;

@Service
public class TokenService {
    private final TokenRepository tokenRepository;
    private final TokenConfig tokenConfig;

    @Autowired
    public TokenService(TokenRepository tokenRepository, TokenConfig tokenConfig) {
        this.tokenRepository = tokenRepository;
        this.tokenConfig = tokenConfig;
    }

    public Token createFor(User user, TokenType tokenType) {
        String value = Random.generateRandomString(tokenConfig.getByteSize(tokenType));
        Instant expiresAt = getExpirationFor(Instant.now(), tokenType);

        Token token = new Token(tokenType, value, user, expiresAt);
        return tokenRepository.save(token);
    }

    public Optional<Token> findByValue(String value) {
        return tokenRepository.findByValue(value).filter(Token::isValid);
    }

    public Optional<Token> findByValueAndTokenType(String value, TokenType tokenType) {
        return tokenRepository.findByValueAndTokenType(value, tokenType).filter(Token::isValid);
    }

    public List<Token> findByUser(User user) {
        return tokenRepository.findByUser(user).stream()
                .filter(Token::isValid)
                .collect(Collectors.toList());
    }

    public Optional<Token> findByUserAndTokenType(User user, TokenType tokenType) {
        return tokenRepository.findByUserAndTokenType(user, tokenType).filter(Token::isValid);
    }

    @Transactional
    public void invalidate(Token token) {
        tokenRepository.delete(token);
    }

    @Transactional
    public void invalidateAllForUser(User user, TokenType tokenType) {
        tokenRepository.deleteByUserAndTokenType(user, tokenType);
    }

    @Transactional
    public int deleteExpiredTokens(Instant now) {
        return tokenRepository.deleteExpiredTokens(now);
    }

    public Instant getExpirationFor(Instant reference, TokenType tokenType) {
        Duration expiration = tokenConfig.getExpiration(tokenType);
        return reference.plus(expiration);
    }
}