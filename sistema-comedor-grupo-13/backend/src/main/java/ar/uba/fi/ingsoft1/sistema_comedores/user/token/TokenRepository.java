package ar.uba.fi.ingsoft1.sistema_comedores.user.token;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ar.uba.fi.ingsoft1.sistema_comedores.common.enums.TokenType;
import ar.uba.fi.ingsoft1.sistema_comedores.user.User;

import org.springframework.transaction.annotation.Transactional;

public interface TokenRepository extends JpaRepository<Token, String> {
    Optional<Token> findByValue(String value);
    Optional<Token> findByValueAndTokenType(String value, TokenType tokenType);
    List<Token> findByUser(User user);
    Optional<Token> findByUserAndTokenType(User user, TokenType tokenType);
    List<Token> findByExpiresAtAfter(Instant now);

    void deleteByUserAndTokenType(User user, TokenType tokenType);

    @Modifying
    @Transactional
    @Query("DELETE FROM Token t WHERE t.expiresAt < :now")
    int deleteExpiredTokens(@Param("now") Instant now);
}
