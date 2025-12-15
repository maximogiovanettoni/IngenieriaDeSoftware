package ar.uba.fi.ingsoft1.sistema_comedores.products;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ProductType {
    SIMPLE("SIMPLE"),
    ELABORATE("ELABORATE"),
    COMBO("COMBO");

    private final String value;

    ProductType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ProductType fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Invalid product type value: null");
        }
        String normalized = normalize(value);
        
        // First, try to match by enum name (e.g., "SIMPLE", "ELABORATE")
        try {
            return ProductType.valueOf(normalized.toUpperCase());
        } catch (IllegalArgumentException e) {
            // If not found by name, try to match by display value
            for (ProductType productType : ProductType.values()) {
                if (normalize(productType.value).equalsIgnoreCase(normalized)) {
                    return productType;
                }
            }
        }
        throw new IllegalArgumentException("Invalid product type value: " + value);
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
