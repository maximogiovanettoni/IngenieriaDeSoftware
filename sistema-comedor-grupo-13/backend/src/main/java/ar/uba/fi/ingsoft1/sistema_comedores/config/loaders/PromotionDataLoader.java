package ar.uba.fi.ingsoft1.sistema_comedores.config.loaders;

import ar.uba.fi.ingsoft1.sistema_comedores.products.ProductCategory;
import ar.uba.fi.ingsoft1.sistema_comedores.promotions.*;
import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@ConfigurationPropertiesBinding
class DayOfWeekConverter implements Converter<String, DayOfWeek> {
    @Override
    public DayOfWeek convert(@NotNull String source) {
        return DayOfWeek.fromValue(source);
    }
}

@Data
@Configuration
@ConfigurationProperties(prefix = "promotions")
class PromotionsConfig {
    private List<PromotionData> items;

    @Data
    public static class PromotionData {
        private String type;
        private String name;
        private String description;
        private Boolean active;
        private LocalDate startDate;
        private LocalDate endDate;
        private List<DayOfWeek> applicableDays;
        private List<TimeRangeData> applicableHours;

        // BUY_X_GET_Y fields
        private ProductCategory requiredCategory;
        private ProductCategory freeCategory;
        private Integer requiredQuantity;
        private Integer freeQuantity;

        // BUY_X_PAY_Y fields
        private ProductCategory category;
        private Integer chargedQuantity;

        // PERCENTAGE_DISCOUNT fields
        private Integer discountPercentage;

        // FIXED_DISCOUNT fields
        private BigDecimal minimumPurchase;
        private BigDecimal discountAmount;

        // FIUBA_EMAIL_DISCOUNT fields
        private Integer fiubaDiscountPercentage;

        // PIZZA_2X1_AFTER_HOURS fields
        private Integer startHour;
    }

    @Data
    public static class TimeRangeData {
        private String startTime;
        private String endTime;

        public TimeRange toEntity() {
            LocalTime start = LocalTime.parse(startTime);
            LocalTime end = LocalTime.parse(endTime);
            return new TimeRange(start, end);
        }
    }
}

@Slf4j
@Component
public class PromotionDataLoader implements CommandLineRunner {

    private final PromotionRepository promotionRepository;
    private final PromotionsConfig promotionsConfig;

    public PromotionDataLoader(
            PromotionRepository promotionRepository,
            PromotionsConfig promotionsConfig
    ) {
        this.promotionRepository = promotionRepository;
        this.promotionsConfig = promotionsConfig;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting promotions initialization...");
        loadPromotions();
        log.info("Promotions initialization completed!");
    }

    @Transactional
    private void loadPromotions() {
        try {
            if (promotionRepository.count() == 0 && promotionsConfig.getItems() != null) {
                int count = 0;

                for (PromotionsConfig.PromotionData item : promotionsConfig.getItems()) {
                    Promotion promotion = createPromotion(item);
                    promotionRepository.save(promotion);
                    count++;
                    log.debug("✓ Promotion '{}' of type {} loaded successfully",
                            promotion.getName(), item.getType());
                }

                log.info("✓ {} promotions loaded successfully", count);
            } else {
                log.info("✓ Promotions already exist in database");
            }
        } catch (Exception e) {
            log.error("Error loading promotions", e);
            throw new RuntimeException("Failed to load promotions", e);
        }
    }

    private Promotion createPromotion(PromotionsConfig.PromotionData data) {
        Set<DayOfWeek> applicableDays = data.getApplicableDays() != null
                ? new HashSet<>(data.getApplicableDays())
                : new HashSet<>();

        Set<TimeRange> applicableHours = data.getApplicableHours() != null
                ? data.getApplicableHours().stream()
                .map(PromotionsConfig.TimeRangeData::toEntity)
                .collect(Collectors.toSet())
                : new HashSet<>();

        return switch (data.getType().toUpperCase()) {
            case "BUY_X_GET_Y" -> new BuyXGetY(
                    data.getName(),
                    data.getDescription(),
                    data.getActive(),
                    data.getStartDate(),
                    data.getEndDate(),
                    applicableDays,
                    applicableHours,
                    data.getRequiredCategory(),
                    data.getFreeCategory(),
                    data.getRequiredQuantity(),
                    data.getFreeQuantity()
            );

            case "BUY_X_PAY_Y" -> new BuyXPayY(
                    data.getName(),
                    data.getDescription(),
                    data.getActive(),
                    data.getStartDate(),
                    data.getEndDate(),
                    applicableDays,
                    applicableHours,
                    data.getCategory(),
                    data.getRequiredQuantity(),
                    data.getChargedQuantity()
            );

            case "PERCENTAGE_DISCOUNT" -> new PercentageDiscount(
                    data.getName(),
                    data.getDescription(),
                    data.getActive(),
                    data.getStartDate(),
                    data.getEndDate(),
                    applicableDays,
                    applicableHours,
                    data.getCategory(),
                    data.getDiscountPercentage()
            );

            case "FIXED_DISCOUNT" -> new FixedDiscount(
                    data.getName(),
                    data.getDescription(),
                    data.getActive(),
                    data.getStartDate(),
                    data.getEndDate(),
                    applicableDays,
                    applicableHours,
                    data.getMinimumPurchase(),
                    data.getDiscountAmount()
            );

            default -> throw new IllegalArgumentException(
                    "Unknown promotion type: " + data.getType()
            );
        };
    }
}