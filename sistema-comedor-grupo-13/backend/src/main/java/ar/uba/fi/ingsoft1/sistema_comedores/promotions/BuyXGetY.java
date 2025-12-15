package ar.uba.fi.ingsoft1.sistema_comedores.promotions;

import ar.uba.fi.ingsoft1.sistema_comedores.products.Product;
import ar.uba.fi.ingsoft1.sistema_comedores.products.ProductCategory;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Entity
@DiscriminatorValue("BUY_X_GET_Y")
@SecondaryTable(name = "buy_x_get_y_details", pkJoinColumns = @PrimaryKeyJoinColumn(name = "promotion_id"))
@Getter
public class BuyXGetY extends Promotion {
    @Column(table = "buy_x_get_y_details", name = "required_category", nullable = false)
    @Enumerated(EnumType.STRING)
    private ProductCategory requiredCategory;

    @Column(table = "buy_x_get_y_details", name = "free_category", nullable = false)
    @Enumerated(EnumType.STRING)
    private ProductCategory freeCategory;

    @Column(table = "buy_x_get_y_details", name = "required_quantity", nullable = false)
    private Integer requiredQuantity;

    @Column(table = "buy_x_get_y_details", name = "free_quantity", nullable = false)
    private Integer freeQuantity;

    public BuyXGetY() {
        super();
    }

    public BuyXGetY(String name, String description, Boolean active,
                    LocalDate startDate, LocalDate endDate, Set<DayOfWeek> applicableDays,
                    Set<TimeRange> applicableHours, ProductCategory required,
                    ProductCategory free, Integer requiredQty, Integer freeQty) {
        super(name, description, active, startDate, endDate, applicableDays,
                applicableHours, PromotionCategory.PRODUCT_SPECIFIC);
        this.requiredCategory = required;
        this.freeCategory = free;
        this.requiredQuantity = requiredQty;
        this.freeQuantity = freeQty;
    }

    @Override
    public boolean appliesTo(Map<Product, Integer> products) {
        int requiredCount = products.entrySet()
                .stream()
                .filter(entry -> {
                    Product product = entry.getKey();
                    return product.getCategory() == requiredCategory;
                })
                .mapToInt(Map.Entry::getValue)
                .sum();
        return requiredCount >= requiredQuantity;
    }

    @Override
    public BigDecimal calculateDiscount(Map<Product, Integer> products) {
        int requiredCount = products.entrySet()
                .stream()
                .filter(entry -> {
                    Product product = entry.getKey();
                    return product.getCategory() == requiredCategory;
                })
                .mapToInt(Map.Entry::getValue)
                .sum();

        int timesApplied = requiredCount / requiredQuantity;
        int totalFreeProducts = timesApplied * freeQuantity;
        
        List<Map.Entry<Product, Integer>> freeProducts = products.entrySet()
                .stream()
                .filter(entry -> entry.getKey().getCategory() == freeCategory)
                .sorted(Comparator.comparing(entry -> entry.getKey().getPrice()))
                .toList();
        
        BigDecimal discount = BigDecimal.ZERO;
        
        if (!freeProducts.isEmpty()) {
            int remaining = totalFreeProducts;
            for (Map.Entry<Product, Integer> entry : freeProducts) {
                if (remaining == 0) { break; }
                int quantityToDiscount = Math.min(remaining, entry.getValue());
                BigDecimal pricePerUnit = entry.getKey().getPrice();
                discount = discount.add(pricePerUnit.multiply(BigDecimal.valueOf(quantityToDiscount)));
                remaining -= quantityToDiscount;
            }
        }
        
        return discount;
    }

    @Override
    public String getType() { return "BUY_X_GET_Y"; }
}
