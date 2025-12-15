package ar.uba.fi.ingsoft1.sistema_comedores.ingredients;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientAuditLogRepository extends JpaRepository<IngredientAuditLog, Long> {
}
