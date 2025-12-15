package ar.uba.fi.ingsoft1.sistema_comedores.promotions.dto;

import ar.uba.fi.ingsoft1.sistema_comedores.products.ProductCategory;
import ar.uba.fi.ingsoft1.sistema_comedores.promotions.DayOfWeek;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.Set;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PercentageDiscountRequest.class, name = "PERCENTAGE_DISCOUNT"),
        @JsonSubTypes.Type(value = FixedDiscountRequest.class, name = "FIXED_DISCOUNT"),
        @JsonSubTypes.Type(value = BuyXGetYRequest.class, name = "BUY_X_GET_Y"),
        @JsonSubTypes.Type(value = BuyXPayYRequest.class, name = "BUY_X_PAY_Y")
})
public sealed interface PromotionRequest permits BuyXGetYRequest, BuyXPayYRequest, FixedDiscountRequest,
        PercentageDiscountRequest {

    @NotBlank(message = "El nombre de la promoción es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    String name();

    @Size(max = 300, message = "La descripción no puede superar los 300 caracteres")
    String description();

    Boolean active();
    LocalDate startDate();
    LocalDate endDate();
    Set<DayOfWeek> applicableDays();
    Set<TimeRangeRequest> applicableHours();
}