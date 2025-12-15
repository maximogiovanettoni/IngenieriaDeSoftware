package ar.uba.fi.ingsoft1.sistema_comedores.ingredients;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
    List<Ingredient> findByAvailableTrue();
    List<Ingredient> findByActiveTrue();

    Optional<Ingredient> findByName(String name);
    Optional<Ingredient> findByNameAndActiveTrue(String name);
}
