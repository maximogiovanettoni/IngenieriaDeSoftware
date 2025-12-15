package ar.uba.fi.ingsoft1.sistema_comedores.orders.dto.promotion;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import ar.uba.fi.ingsoft1.sistema_comedores.orders.AppliedPromotion;
import ar.uba.fi.ingsoft1.sistema_comedores.products.dto.ProductDetailsResponse;

public record AppliedPromotionResponse (
    String name,

    String type,

    @JsonSerialize(using = ToStringSerializer.class)
    BigDecimal discountAmount,

    LocalDate startDate,

    LocalDate endDate,

    String applicableDays
) {

    public static AppliedPromotionResponse from(AppliedPromotion appliedPromotion) {
        return new AppliedPromotionResponse(
            appliedPromotion.getAppliedPromotionName(),
            appliedPromotion.getAppliedPromotionType(),
            appliedPromotion.getAppliedDiscount(),
            appliedPromotion.getStartDate(),
            appliedPromotion.getEndDate(),
            appliedPromotion.getApplicableDays()
        );
    }
}