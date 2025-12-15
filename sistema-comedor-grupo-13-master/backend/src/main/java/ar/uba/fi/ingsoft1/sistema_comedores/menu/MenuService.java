package ar.uba.fi.ingsoft1.sistema_comedores.menu;

import ar.uba.fi.ingsoft1.sistema_comedores.products.Product;
import ar.uba.fi.ingsoft1.sistema_comedores.products.ProductRepository;
import ar.uba.fi.ingsoft1.sistema_comedores.products.ProductCategory;
import ar.uba.fi.ingsoft1.sistema_comedores.products.combos.ComboService;
import ar.uba.fi.ingsoft1.sistema_comedores.products.combos.dto.ComboDetailsResponse;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Transactional(readOnly = true)
@Slf4j
public class MenuService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ComboService comboService;

   
    public List<MenuItemDTO> getAvailableMenu() {
        log.info("Fetching available menu items");
        
        List<MenuItemDTO> menuItems = new ArrayList<>();

        // Obtener productos disponibles
        List<Product> availableProducts = productRepository.findAll().stream()
            .filter(p -> p.getAvailable() != null && p.getAvailable())
            .filter(p -> !p.getCategory().equals(ProductCategory.COMBO))
            .collect(Collectors.toList());
        List<MenuItemDTO> productMenuItems = availableProducts.stream()
            .map(product -> MenuItemDTO.fromProduct(product, product.getCategory().name()))
            .sorted((a, b) -> a.name().compareToIgnoreCase(b.name()))
            .collect(Collectors.toList());

        menuItems.addAll(productMenuItems);

        // Obtener combos disponibles
        List<ComboDetailsResponse> availableCombos = comboService.getAllCombos().stream()
            .filter(combo -> combo.isAvailable())
            .collect(Collectors.toList());
        
        List<MenuItemDTO> comboMenuItems = availableCombos.stream()
            .map(combo -> MenuItemDTO.fromCombo(combo))
            .sorted((a, b) -> a.name().compareToIgnoreCase(b.name()))
            .collect(Collectors.toList());
        
        menuItems.addAll(comboMenuItems);

        log.info("Menu items fetched: {} total items ({} products, {} combos)", 
                   menuItems.size(), productMenuItems.size(), comboMenuItems.size());
        return menuItems;
    }

    
    public List<MenuItemDTO> getMenuByCategory(String categoryStr) {
        log.info("Fetching menu items by category: {}", categoryStr);

        List<MenuItemDTO> menuItems = new ArrayList<>();

        try {
            ProductCategory category = ProductCategory.valueOf(categoryStr.toUpperCase());

            // Obtener productos de la categoría
            if (!category.equals(ProductCategory.COMBO)) {
                List<Product> productsByCategory = productRepository.findAll().stream()
                    .filter(p -> p.getAvailable() != null && p.getAvailable())
                    .filter(p -> !p.getCategory().equals(ProductCategory.COMBO))
                    .filter(product -> product.getCategory().equals(ProductCategory.valueOf(categoryStr.toUpperCase())))
                    .collect(Collectors.toList());

                List<MenuItemDTO> productMenuItems = productsByCategory.stream()
                    .map(product -> MenuItemDTO.fromProduct(product, product.getCategory().name()))
                    .sorted((a, b) -> a.name().compareToIgnoreCase(b.name()))
                    .collect(Collectors.toList());

                menuItems.addAll(productMenuItems);
            }

            // TODO: Obtener combos de la categoría cuando estén implementados

        } catch (IllegalArgumentException e) {
            log.warn("Invalid category: {}", categoryStr);
            return new ArrayList<>();
        }

        log.info("Found {} items in category {}", menuItems.size(), categoryStr);
        return menuItems;
    }

    /**
     * Busca en el menú por nombre (case-insensitive)
     */
    public List<MenuItemDTO> searchMenu(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAvailableMenu();
        }

        log.info("Searching menu with query: {}", query);
        String searchQuery = query.toLowerCase().trim();

        List<MenuItemDTO> results = new ArrayList<>();

        // Buscar en productos
        List<Product> matchingProducts = productRepository.findAll().stream()
            .filter(p -> p.getAvailable() != null && p.getAvailable())
            .filter(p -> !p.getCategory().equals(ProductCategory.COMBO))
            .filter(product -> 
                product.getName().toLowerCase().contains(searchQuery) ||
                (product.getDescription() != null && product.getDescription().toLowerCase().contains(searchQuery))
            )
            .collect(Collectors.toList());

        List<MenuItemDTO> productResults = matchingProducts.stream()
            .map(product -> MenuItemDTO.fromProduct(product, product.getCategory().name()))
            .sorted((a, b) -> a.name().compareToIgnoreCase(b.name()))
            .collect(Collectors.toList());

        results.addAll(productResults);

        // Buscar en combos
        List<ComboDetailsResponse> matchingCombos = comboService.getAllCombos().stream()
            .filter(combo -> combo.isAvailable() &&
                (combo.name().toLowerCase().contains(searchQuery) ||
                 (combo.description() != null && combo.description().toLowerCase().contains(searchQuery)))
            )
            .collect(Collectors.toList());

        List<MenuItemDTO> comboResults = matchingCombos.stream()
            .map(combo -> MenuItemDTO.fromCombo(combo))
            .sorted((a, b) -> a.name().compareToIgnoreCase(b.name()))
            .collect(Collectors.toList());

        results.addAll(comboResults);

        log.info("Search found {} items matching query: {}", results.size(), query);
        return results;
    }

    /**
     * Obtiene la lista de categorías disponibles
     */
    public List<String> getAvailableCategories() {
        List<String> categories = new ArrayList<>();
        for (ProductCategory category : ProductCategory.values()) {
            categories.add(category.name());
        }
        return categories;
    }
}
