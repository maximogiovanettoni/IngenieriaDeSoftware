package ar.uba.fi.ingsoft1.sistema_comedores.promotions;

import ar.uba.fi.ingsoft1.sistema_comedores.products.Product;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity(name = "promotions")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor
public abstract class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    @NotBlank(message = "El nombre de la promoción es obligatorio")
    private String name;

    @Column(nullable = false, name = "promotion_category")
    @Enumerated(EnumType.STRING)
    protected PromotionCategory promotionCategory;

    @Size(max = 300, message = "La descripción no puede superar los 300 caracteres")
    @Column(length = 300)
    private String description;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "promotion_days", joinColumns = @JoinColumn(name = "promotion_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week")
    private Set<DayOfWeek> applicableDays = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "promotion_hours", joinColumns = @JoinColumn(name = "promotion_id"))
    private Set<TimeRange> applicableHours = new HashSet<>();

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public Promotion(String name, String description, Boolean active,
                     LocalDate startDate, LocalDate endDate, Set<DayOfWeek> applicableDays,
                     Set<TimeRange> applicableHours, PromotionCategory category) {
        this.name = name;
        this.description = description;
        this.active = active;
        this.startDate = startDate;
        this.endDate = endDate;
        this.applicableDays = applicableDays;
        this.applicableHours = applicableHours;
        this.promotionCategory = category;
        onCreate();
    }

    public boolean isCurrentlyValid() {
        if (!active) {
            return false;
        }

        LocalDate now = LocalDate.now();

        if (startDate != null && now.isBefore(startDate)) {
            return false;
        }
        if (endDate != null && now.isAfter(endDate)) {
            return false;
        }

        if (!applicableDays.isEmpty()) {
            DayOfWeek currentDay = DayOfWeek.valueOf(
                    java.time.DayOfWeek.from(now).name()
            );
            if (!applicableDays.contains(currentDay)) {
                return false;
            }
        }

        if (!applicableHours.isEmpty()) {
            LocalTime currentTime = LocalTime.now();
            return applicableHours.stream()
                    .anyMatch(timeRange -> timeRange.contains(currentTime));
        }

        return true;
    }

    public abstract boolean appliesTo(Map<Product, Integer> products);

    public abstract BigDecimal calculateDiscount(Map<Product, Integer> products);

    public abstract String getType();

}