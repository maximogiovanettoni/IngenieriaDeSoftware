package ar.uba.fi.ingsoft1.sistema_comedores.ingredients;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.math.BigDecimal;
import ar.uba.fi.ingsoft1.sistema_comedores.common.enums.AdminOperation;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Table(name = "ingredient_audit_log")
public class IngredientAuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ingredient_name", nullable = false)
    private String ingredientName;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation", nullable = false)
    private AdminOperation operation;

    @Column(name = "modified_at", nullable = false)
    private Instant modifiedAt;

    @Column(name = "amount_delta")
    private BigDecimal amountDelta;

    @Column(length = 300)
    private String reason;
}
