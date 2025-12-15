package ar.uba.fi.ingsoft1.sistema_comedores.products.dto;

import ar.uba.fi.ingsoft1.sistema_comedores.products.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    public static Specification<Product> filterBy(SearchProductRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter != null) {
                if (filter.category() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("category"), filter.category()));
                }
                if (filter.minPrice() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), filter.minPrice()));
                }
                if (filter.maxPrice() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), filter.maxPrice()));
                }
                if (filter.available() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("available"), filter.available()));
                }
                if (filter.active() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("active"), filter.active()));
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}