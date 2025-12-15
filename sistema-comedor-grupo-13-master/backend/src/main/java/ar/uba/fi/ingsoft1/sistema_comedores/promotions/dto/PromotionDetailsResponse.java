package ar.uba.fi.ingsoft1.sistema_comedores.promotions.dto;

import ar.uba.fi.ingsoft1.sistema_comedores.promotions.*;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PromotionDetailsResponse (
        Long id,
        String name,
        String description,
        Boolean active,
        LocalDate startDate,
        LocalDate endDate,
        Set<String> applicableDays,
        Set<TimeRangeResponse> applicableHours,
        Boolean currentlyValid,
        String type,
        // For PERCENTAGE_DISCOUNT
        String category,
        Integer discount,
        BigDecimal multiplier,
        // For FIXED_DISCOUNT
        BigDecimal minimumPurchase,
        BigDecimal discountAmount,
        // For BUY_X_GET_Y
        String requiredProductCategory,
        String freeProductCategory,
        // For BUY_X_PAY_Y
        Integer requiredQuantity,
        Integer chargedQuantity
) {
    public PromotionDetailsResponse(Promotion promotion) {
        this(
                promotion.getId(),
                promotion.getName(),
                promotion.getDescription(),
                promotion.getActive(),
                promotion.getStartDate(),
                promotion.getEndDate(),
                promotion.getApplicableDays().stream().map(Enum::name).collect(Collectors.toSet()),
                promotion.getApplicableHours().stream()
                        .map(tr -> new TimeRangeResponse(tr.getStartTime(), tr.getEndTime()))
                        .collect(Collectors.toSet()),
                promotion.isCurrentlyValid(),
                getPromotionType(promotion),
                // PERCENTAGE_DISCOUNT fields
                promotion instanceof PercentageDiscount pd ? (pd.getCategory() != null ? pd.getCategory().name() : null) : null,
                null, // discount is calculated from multiplier on frontend
                promotion instanceof PercentageDiscount pd ? pd.getMultiplier() : null,
                // FIXED_DISCOUNT fields
                promotion instanceof FixedDiscount fd ? fd.getMinimumPurchase() : null,
                promotion instanceof FixedDiscount fd ? fd.getDiscountAmount() : null,
                // BUY_X_GET_Y fields
                promotion instanceof BuyXGetY bxy ? bxy.getRequiredCategory().name() : null,
                promotion instanceof BuyXGetY bxy ? bxy.getFreeCategory().name() : null,
                // BUY_X_PAY_Y fields
                promotion instanceof BuyXPayY bxpy ? bxpy.getRequiredQuantity() : null,
                promotion instanceof BuyXPayY bxpy ? bxpy.getChargedQuantity() : null
        );
    }

    private static String getPromotionType(Promotion promotion) {
        if (promotion instanceof PercentageDiscount) {
            return "PERCENTAGE_DISCOUNT";
        } else if (promotion instanceof FixedDiscount) {
            return "FIXED_DISCOUNT";
        } else if (promotion instanceof BuyXGetY) {
            return "BUY_X_GET_Y";
        } else if (promotion instanceof BuyXPayY) {
            return "BUY_X_PAY_Y";
        }
        return null;
    }
}