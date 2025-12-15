package ar.uba.fi.ingsoft1.sistema_comedores.orders;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Embeddable
@NoArgsConstructor
@Getter
@Setter
public class AppliedPromotion {
    @Column(name = "applied_promotion_name", nullable = false)
    private String appliedPromotionName;
    
    @Column(name = "applied_promotion_type", nullable = false)
    private String appliedPromotionType;

    @Column(name = "applied_discount", nullable = false)
    @Positive
    private BigDecimal appliedDiscount;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "applicable_days", columnDefinition = "TEXT")
    private String applicableDays;
}