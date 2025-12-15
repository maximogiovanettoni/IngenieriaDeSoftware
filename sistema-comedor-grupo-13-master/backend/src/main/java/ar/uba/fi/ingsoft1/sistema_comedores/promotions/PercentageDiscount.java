package ar.uba.fi.ingsoft1.sistema_comedores.promotions;

import ar.uba.fi.ingsoft1.sistema_comedores.products.Product;
import ar.uba.fi.ingsoft1.sistema_comedores.products.ProductCategory;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

@Entity
@DiscriminatorValue("PERCENTAGE_DISCOUNT")
@SecondaryTable(name = "percentage_discount_details", pkJoinColumns = @PrimaryKeyJoinColumn(name = "promotion_id"))
@Getter
public class PercentageDiscount extends Promotion {
    @Column(table = "percentage_discount_details", name = "category", nullable = false)
    @Enumerated(EnumType.STRING)
    private ProductCategory category;

    @Column(table = "percentage_discount_details", name = "discount_multiplier", nullable = false, precision = 5, scale
            = 2)
    @DecimalMin(value = "0.0", message = "El porcentaje de descuento debe ser mayor o igual a 0")
    @DecimalMax(value = "1.0", message = "El porcentaje de descuento no puede superar el 100%")
    private BigDecimal multiplier;

    public PercentageDiscount() {
        super();
    }

    public PercentageDiscount(String name, String description, Boolean active,
                              LocalDate startDate, LocalDate endDate, Set<DayOfWeek> applicableDays,
                              Set<TimeRange> applicableHours, ProductCategory category, Integer discount) {
        super(name, description, active, startDate, endDate, applicableDays,
                applicableHours, PromotionCategory.PRODUCT_SPECIFIC);
        this.category = category;
        this.multiplier = BigDecimal.valueOf(1 - discount / 100.0);
    }

    @Override
    public boolean appliesTo(Map<Product, Integer> products) {
        return products
                .keySet()
                .stream()
                .anyMatch(product -> product.getCategory() == category);
    }

    @Override
    public BigDecimal calculateDiscount(Map<Product, Integer> products) {
        if (!appliesTo(products)) { return BigDecimal.ZERO; }
        BigDecimal subtotal = products.entrySet().stream()
                .filter(entry -> entry.getKey().getCategory() == category)
                .map(entry -> entry.getKey().getPrice().multiply(BigDecimal.valueOf(entry.getValue())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return subtotal.multiply(BigDecimal.ONE.subtract(multiplier));
    }

    public String getType() { return "PERCENTAGE_DISCOUNT"; }
}
