package ar.uba.fi.ingsoft1.sistema_comedores.products.combos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComboRepository extends JpaRepository<Combo, Long> {
    
    List<Combo> findByActiveTrue();
    
    Optional<Combo> findByIdAndActiveTrue(Long id);
    
    Optional<Combo> findByIdAndActiveFalse(Long id);
    
    boolean existsByName(String name);
    
    boolean existsByNameAndIdNot(String name, Long id);
    
    Optional<Combo> findByName(String name);
    
    @Query("SELECT c FROM Combo c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :namePart, '%')) AND c.active = true")
    List<Combo> findByNameContainingIgnoreCase(@Param("namePart") String namePart);
    
    @Query("""
            SELECT DISTINCT c
            FROM Combo c
            JOIN c.comboProducts cp
            WHERE cp.product.id = :productId
    """)
    List<Combo> findByProductId(@Param("productId") Long productId);
    
    @Query("""
            SELECT DISTINCT c
            FROM Combo c
            JOIN c.comboProducts cp
            WHERE cp.product.id IN :productIds
    """)
    List<Combo> findByProductIds(@Param("productIds") List<Long> productIds);
    
    @Query("""
            SELECT DISTINCT c
            FROM Combo c
            LEFT JOIN FETCH c.comboProducts cp
            LEFT JOIN FETCH cp.product
            WHERE c.id = :id
    """)
    Optional<Combo> findByIdWithProducts(@Param("id") Long id);
    
    @Query("""
            SELECT DISTINCT c
            FROM Combo c
            LEFT JOIN FETCH c.comboProducts cp
            LEFT JOIN FETCH cp.product
    """)
    List<Combo> findAllWithProducts();
}