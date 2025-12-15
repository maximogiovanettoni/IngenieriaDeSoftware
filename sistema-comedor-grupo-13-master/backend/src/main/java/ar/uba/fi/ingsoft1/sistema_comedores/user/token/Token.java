package ar.uba.fi.ingsoft1.sistema_comedores.user.token;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Instant;

import ar.uba.fi.ingsoft1.sistema_comedores.common.enums.TokenType;
import ar.uba.fi.ingsoft1.sistema_comedores.user.User;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "token_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private TokenType tokenType;
    
    @Column(name = "`value`", nullable = false, unique = true, length = 512)
    private String value;
    
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private Instant expiresAt;
    
    @Column(nullable = false)
    private Instant createdAt;
    
    private Instant usedAt;

    public Token(TokenType tokenType, String value, User user, Instant expiresAt) {
        this.tokenType = tokenType;
        this.value = value;
        this.user = user;
        this.expiresAt = expiresAt;
        this.createdAt = Instant.now();
    }
    
    public String getValue() { return value; }
    public User getUser() { return user; }
    public TokenType getType() { return tokenType; }
    
    public boolean isValid() {
        return usedAt == null;
    }

    public boolean isExpired() {
        return expiresAt.isBefore(Instant.now());
    }

    public void markAsUsed() {
        this.usedAt = Instant.now();
    }

}
