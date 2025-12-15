package ar.uba.fi.ingsoft1.sistema_comedores.products;

import ar.uba.fi.ingsoft1.sistema_comedores.common.enums.AdminOperation;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Table(name = "product_audit_log")
public class ProductAuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "modified_at", nullable = false)
    private Instant modifiedAt;

    @Column(name = "operation", nullable = false)
    @Enumerated(EnumType.STRING)
    private AdminOperation operation;

    @Column(length = 300)
    private String reason;
}
