package ar.uba.fi.ingsoft1.sistema_comedores.promotions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PromotionType {
    PERCENTAGE_DISCOUNT("Descuento Porcentual"),
    FIXED_DISCOUNT("Descuento Fijo"),
    BUY_X_GET_Y("Compra X Lleva Y"),
    FREE_PRODUCT("Producto Gratis"),
    FIUBA_EMAIL_DISCOUNT("Descuento FIUBA"),
    PIZZA_2X1_AFTER_HOURS("2x1 Pizzas después de hora");

    private final String value;

    PromotionType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static PromotionType fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Invalid promotion type value: null");
        }
        String normalized = normalize(value);
        for (PromotionType type : PromotionType.values()) {
            if (normalize(type.value).equalsIgnoreCase(normalized)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid promotion type value: " + value);
    }

    @Override
    public String toString() {
        return value;
    }

    private static String normalize(String s) {
        if (s == null) return "";
        return java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").trim().toLowerCase();
    }
}
