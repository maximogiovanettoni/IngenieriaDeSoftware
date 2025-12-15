package ar.uba.fi.ingsoft1.sistema_comedores.orders.dto.promotion;

import ar.uba.fi.ingsoft1.sistema_comedores.orders.AppliedPromotion;
import ar.uba.fi.ingsoft1.sistema_comedores.promotions.dto.PromotionDetailsResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

public record CalculatePromotionResponse(
    @JsonProperty("subtotal")
    BigDecimal subtotal,
    
    @JsonProperty("discountAmount")
    BigDecimal discountAmount,
    
    @JsonProperty("totalAmount")
    BigDecimal totalAmount,
    
    @JsonProperty("hasDiscount")
    Boolean hasDiscount, 
    
    @JsonProperty("appliedPromotions")
    List<AppliedPromotionResponse> appliedPromotions
) {}