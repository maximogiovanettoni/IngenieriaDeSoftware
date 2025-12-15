package ar.uba.fi.ingsoft1.sistema_comedores.menu;

import ar.uba.fi.ingsoft1.sistema_comedores.products.combos.dto.ComboDetailsResponse;

import java.math.BigDecimal;

/**
 * DTO que representa un ítem unificado del menú
 * Puede ser un producto o un combo
 */
public record MenuItemDTO(
    long id,
    MenuItemType type,
    String name,
    String description,
    BigDecimal price,
    String imageUrl,
    String category,
    BigDecimal regularPrice,
    BigDecimal discount,
    boolean isAvailable,
    Integer stock
) {
    /**
     * Constructor para crear MenuItemDTO a partir de un producto
     */
    public static MenuItemDTO fromProduct(ar.uba.fi.ingsoft1.sistema_comedores.products.Product product, String category) {
        return new MenuItemDTO(
            product.getId(),
            MenuItemType.PRODUCT,
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getImageUrl(),
            category != null ? category : "GENERAL",
            product.getPrice(),  // regularPrice = price para productos
            BigDecimal.ZERO,     // descuento = 0
            product.getActive(),
            product.getStock()
        );
    }

    /**
     * Constructor para crear MenuItemDTO a partir de un combo
     */
    public static MenuItemDTO fromCombo(ComboDetailsResponse combo) {
        return new MenuItemDTO(
            combo.id(),
            MenuItemType.COMBO,
            combo.name(),
            combo.description(),
            combo.price(),
            combo.imageUrl(),
            "COMBO",
            combo.regularPrice(),  // regularPrice del combo
            combo.discount(),      // descuento = regularPrice - price
            combo.isAvailable(),
            combo.stock()        // usar stock real del combo
        );
    }
}
