package ar.uba.fi.ingsoft1.sistema_comedores.products.elaborate;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ElaborateProductRepository extends JpaRepository<ElaborateProduct, Long> {
    
    @Query("""
            SELECT DISTINCT ep 
            FROM ElaborateProduct ep 
            JOIN ep.productIngredients pi 
            WHERE pi.ingredient.id = :ingredientId
    """)
    List<ElaborateProduct> findByIngredientId(@Param("ingredientId") Long ingredientId);
    
    @Query("""
            SELECT COUNT(ep) > 0
            FROM ElaborateProduct ep
            JOIN ep.productIngredients pi
            WHERE pi.ingredient.id = :ingredientId
            AND ep.id = :productId
    """)
    boolean existsByIngredientIdAndProductId(
        @Param("ingredientId") Long ingredientId, 
        @Param("productId") Long productId
    );
    
    @Query("""
            SELECT DISTINCT ep 
            FROM ElaborateProduct ep 
            JOIN ep.productIngredients pi 
            WHERE pi.ingredient.id IN :ingredientIds
    """)
    List<ElaborateProduct> findByIngredientIds(@Param("ingredientIds") List<Long> ingredientIds);
    
    @Query("""
            SELECT DISTINCT ep 
            FROM ElaborateProduct ep 
            LEFT JOIN FETCH ep.productIngredients pi
            LEFT JOIN FETCH pi.ingredient
            WHERE ep.id = :id
    """)
    Optional<ElaborateProduct> findByIdWithIngredients(@Param("id") Long id);
    
    @Query("""
            SELECT DISTINCT ep 
            FROM ElaborateProduct ep 
            LEFT JOIN FETCH ep.productIngredients pi
            LEFT JOIN FETCH pi.ingredient
    """)
    List<ElaborateProduct> findAllWithIngredients();
}