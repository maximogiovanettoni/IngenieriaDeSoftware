package ar.uba.fi.ingsoft1.sistema_comedores.promotions;

import ar.uba.fi.ingsoft1.sistema_comedores.products.Product;
import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

@Entity
@DiscriminatorValue("FIXED_DISCOUNT")
@SecondaryTable(name = "fixed_discount_details", pkJoinColumns = @PrimaryKeyJoinColumn(name = "promotion_id"))
@Getter
public class FixedDiscount extends Promotion {
    @Column(table = "fixed_discount_details", name = "minimum_purchase", nullable = false, precision = 10, scale = 2)
    @PositiveOrZero(message = "El monto mínimo debe ser positivo o cero")
    private BigDecimal minimumPurchase;

    @Column(table = "fixed_discount_details", name = "discount_amount", nullable = false, precision = 10, scale = 2)
    @PositiveOrZero(message = "El monto de descuento debe ser positivo o cero")
    private BigDecimal discountAmount;

    public FixedDiscount() {
        super();
    }

    public FixedDiscount(String name, String description, Boolean active,
                         LocalDate startDate, LocalDate endDate, Set<DayOfWeek> applicableDays,
                         Set<TimeRange> applicableHours, BigDecimal minPurchaseTotal, BigDecimal fixedDiscount) {
        super(name, description, active, startDate, endDate, applicableDays,
                applicableHours, PromotionCategory.ORDER_LEVEL);
        this.minimumPurchase = minPurchaseTotal;
        this.discountAmount = fixedDiscount;
    }

    @Override
    public boolean appliesTo(Map<Product, Integer> products) {
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Product, Integer> entry : products.entrySet()) {
            BigDecimal unitPrice = entry.getKey().getPrice();
            Integer quantity = entry.getValue();
            total = total.add(unitPrice.multiply(BigDecimal.valueOf(quantity)));
        }
        return total.compareTo(minimumPurchase) >= 0;
    }

    @Override
    public BigDecimal calculateDiscount(Map<Product, Integer> products) {
        return appliesTo(products) ? discountAmount : BigDecimal.ZERO;
    }
   
    public String getType() { return "FIXED_DISCOUNT"; }
}
