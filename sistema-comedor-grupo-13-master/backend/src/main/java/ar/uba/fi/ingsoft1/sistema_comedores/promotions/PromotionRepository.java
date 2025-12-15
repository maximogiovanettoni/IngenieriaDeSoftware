package ar.uba.fi.ingsoft1.sistema_comedores.promotions;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    Optional<Promotion> findByName(String name);

    List<Promotion> findByActiveTrue();

    Optional<Promotion> findByIdAndActiveTrue(Long id);

    Optional<Promotion> findByIdAndActiveFalse(Long id);

    @Query("SELECT p FROM promotions p WHERE p.active = true " +
            "AND (p.startDate IS NULL OR p.startDate <= CURRENT_DATE) " +
            "AND (p.endDate IS NULL OR p.endDate >= CURRENT_DATE) " +
            "ORDER BY p.id DESC")
    List<Promotion> findCurrentlyValidPromotions();

    boolean existsByName(String name);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM promotions p " +
            "WHERE p.name = :name AND p.id != :id")
    boolean existsByNameAndIdNot(@Param("name") String name, @Param("id") Long id);
}