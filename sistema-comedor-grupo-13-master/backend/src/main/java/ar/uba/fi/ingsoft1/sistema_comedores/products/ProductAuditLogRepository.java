package ar.uba.fi.ingsoft1.sistema_comedores.products;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductAuditLogRepository extends JpaRepository<ProductAuditLog, Long> {
}
