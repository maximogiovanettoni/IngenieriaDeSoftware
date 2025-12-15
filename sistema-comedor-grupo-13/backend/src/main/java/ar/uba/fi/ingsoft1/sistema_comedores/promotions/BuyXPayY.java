package ar.uba.fi.ingsoft1.sistema_comedores.promotions;

import ar.uba.fi.ingsoft1.sistema_comedores.products.Product;
import ar.uba.fi.ingsoft1.sistema_comedores.products.ProductCategory;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Entity
@DiscriminatorValue("BUY_X_PAY_Y")
@SecondaryTable(name = "buy_x_pay_y_details", pkJoinColumns = @PrimaryKeyJoinColumn(name = "promotion_id"))
@Getter
public class BuyXPayY extends Promotion {
    @Column(table = "buy_x_pay_y_details", name = "category", nullable = false)
    @Enumerated(EnumType.STRING)
    private ProductCategory category;

    @Column(table = "buy_x_pay_y_details", name = "required_quantity", nullable = false)
    @Positive(message = "La cantidad requerida debe ser positiva")
    private int requiredQuantity;

    @Column(table = "buy_x_pay_y_details", name = "charged_quantity", nullable = false)
    @Positive(message = "La cantidad a pagar debe ser positiva")
    private int chargedQuantity;

    public BuyXPayY() {
        super();
    }

    public BuyXPayY(String name, String description, Boolean active,
                    LocalDate startDate, LocalDate endDate, Set<DayOfWeek> applicableDays,
                    Set<TimeRange> applicableHours, ProductCategory category,
                    int required, int charged) {
        super(name, description, active, startDate, endDate, applicableDays,
                applicableHours, PromotionCategory.PRODUCT_SPECIFIC);
        this.category = category;
        this.requiredQuantity = required;
        this.chargedQuantity = charged;
    }

    @Override
    public boolean appliesTo(Map<Product, Integer> products) {
        int totalQuantity = products.entrySet().stream()
                .filter(entry -> entry.getKey().getCategory() == category)
                .mapToInt(Map.Entry::getValue)
                .sum();

        return totalQuantity >= requiredQuantity;
    }

    @Override
    public BigDecimal calculateDiscount(Map<Product, Integer> products) {
        List<Map.Entry<Product, Integer>> categoryProducts = products.entrySet().stream()
                .filter(entry -> entry.getKey().getCategory() == category)
                .sorted(Comparator.comparing(entry -> entry.getKey().getPrice()))
                .toList();
        int totalQuantity = categoryProducts.stream()
                .mapToInt(Map.Entry::getValue)
                .sum();
        int timesApplied = totalQuantity / requiredQuantity;
        int freeProducts = timesApplied * (requiredQuantity - chargedQuantity);
        BigDecimal discount = BigDecimal.ZERO;
        int remaining = freeProducts;
        for (Map.Entry<Product, Integer> entry : categoryProducts) {
            if (remaining == 0) break;
            int quantityToDiscount = Math.min(remaining, entry.getValue());
            BigDecimal pricePerUnit = entry.getKey().getPrice();
            discount = discount.add(pricePerUnit.multiply(BigDecimal.valueOf(quantityToDiscount)));
            remaining -= quantityToDiscount;
        }
        return discount;
    }

    @Override
    public String getType() { return "BUY_X_PAY_Y"; }
}
