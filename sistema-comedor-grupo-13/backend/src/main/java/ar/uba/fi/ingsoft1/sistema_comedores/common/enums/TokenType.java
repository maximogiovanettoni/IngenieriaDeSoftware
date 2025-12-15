package ar.uba.fi.ingsoft1.sistema_comedores.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TokenType {
    REFRESH("refresh_token", "Refresh Token"),
    EMAIL_VERIFICATION("email_verification_token", "Email Verification Token"),
    PASSWORD_RESET("password_reset_token", "Password Reset Token");

    private final String value;
    private final String displayName;

    TokenType(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static TokenType fromValue(String value) {
        for (TokenType token_type : TokenType.values()) {
            if (token_type.value.equalsIgnoreCase(value)) {
                return token_type;
            }
        }
        throw new IllegalArgumentException("Invalid token type: " + value);
    }

    @Override
    public String toString() {
        return value;
    }
}

