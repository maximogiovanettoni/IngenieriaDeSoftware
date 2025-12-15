package ar.uba.fi.ingsoft1.sistema_comedores.products.combos.dto;

import ar.uba.fi.ingsoft1.sistema_comedores.products.combos.Combo;
import ar.uba.fi.ingsoft1.sistema_comedores.products.dto.ProductComponentResponse;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public record ComboDetailsResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        BigDecimal regularPrice,
        BigDecimal discount,
        List<ProductComponentResponse> products,
        String imageUrl,
        Boolean isAvailable,
        Boolean isActive,
        Instant updatedAt,
        Integer stock
) {
    public ComboDetailsResponse(Combo combo) {
        this(
            combo.getId(),
            combo.getName(),
            combo.getDescription(),
            combo.getPrice(),
            combo.getRegularPrice(),
            combo.getDiscount(),
            combo.getComboProducts()
                .stream()
                .map(cp -> new ProductComponentResponse(
                    cp.getProduct().getId(),
                    cp.getProduct().getName(),
                    cp.getProduct().getProductType().getValue(),
                    new BigDecimal(cp.getQuantity())
                ))
                .toList(),
            combo.getImageUrl(),
            combo.isAvailable(),
            combo.getActive(),
            combo.getUpdatedAt(),
            combo.getStock()
        );
    }
}
