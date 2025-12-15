package ar.uba.fi.ingsoft1.sistema_comedores.promotions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PromotionCategory {
    PRODUCT_SPECIFIC,
    ORDER_LEVEL;
}
