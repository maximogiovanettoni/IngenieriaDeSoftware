package ar.uba.fi.ingsoft1.sistema_comedores.products;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ProductCategory {
    SANDWICH("Sándwich"),
    PIZZA("Pizza"),
    DRINK("Bebida"),
    BURGER("Hamburguesa"),
    DESSERT("Postre"),
    SALAD("Ensalada"),
    MAIN_COURSE("Plato Principal"),
    COMBO("Combo"),
    SIDE_DISH("Acompañamiento"),
    COFFEE("Café");

    private final String value;

    ProductCategory(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ProductCategory fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Invalid product type value: null");
        }
        String normalized = normalize(value);

        try {
            return ProductCategory.valueOf(normalized.toUpperCase());
        } catch (IllegalArgumentException e) {
            // If not found by name, try to match by display value
            for (ProductCategory productCategory : ProductCategory.values()) {
                if (normalize(productCategory.value).equalsIgnoreCase(normalized)) {
                    return productCategory;
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
