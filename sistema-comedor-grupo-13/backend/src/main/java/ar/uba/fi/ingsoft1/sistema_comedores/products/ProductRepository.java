package ar.uba.fi.ingsoft1.sistema_comedores.products;

import ar.uba.fi.ingsoft1.sistema_comedores.products.elaborate.ElaborateProduct;
import ar.uba.fi.ingsoft1.sistema_comedores.products.simple.SimpleProduct;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    
    @Query("SELECT p FROM products p WHERE p.stock < :threshold")
    List<Product> findLowStockProducts(@Param("threshold") Integer threshold);
    
    @Query("SELECT p FROM products p WHERE TYPE(p) = SimpleProduct")
    List<SimpleProduct> findAllSimpleProducts();
    
    @Query("SELECT p FROM products p WHERE TYPE(p) = ElaborateProduct")
    List<ElaborateProduct> findAllElaborateProducts();

    @Query("SELECT p FROM products p WHERE TYPE(p) = ElaborateProduct AND p.id = :id")
    Optional<ElaborateProduct> findElaborateProductById(@Param("id") Long id);

    @Query("SELECT p FROM products p WHERE TYPE(p) = SimpleProduct AND p.id = :id")
    Optional<SimpleProduct> findSimpleProductById(@Param("id") Long id);

    Boolean existsByName(String name);

    Optional<Product> findByName(String name);

    List<Product> findByAvailableTrue();
}