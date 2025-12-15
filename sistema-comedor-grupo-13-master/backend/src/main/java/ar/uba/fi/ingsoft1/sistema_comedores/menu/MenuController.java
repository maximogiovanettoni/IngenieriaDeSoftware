package ar.uba.fi.ingsoft1.sistema_comedores.menu;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para el menú unificado
 * Proporciona endpoints para obtener el menú completo con productos y combos
 */
@RestController
@RequestMapping("/menu")
@Tag(name = "Menu", description = "Endpoints para el menú unificado de productos y combos")
public class MenuController {

    @Autowired
    private MenuService menuService;

    /**
     * Obtiene el menú completo
     * 
     * @param category Filtro opcional de categoría
     * @param search Término de búsqueda opcional
     * @return Lista de items del menú
     */
    @GetMapping
    @Operation(
        summary = "Obtener menú completo",
        description = "Retorna todos los productos y combos disponibles. " +
                     "Opcionalmente filtrados por categoría o búsqueda.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Menú obtenido exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
        }
    )
    public ResponseEntity<List<MenuItemDTO>> getMenu(
        @RequestParam(required = false) String category,
        @RequestParam(required = false) String search
    ) {
        List<MenuItemDTO> menuItems;

        if (search != null && !search.trim().isEmpty()) {
            menuItems = menuService.searchMenu(search);
        } else if (category != null && !category.trim().isEmpty()) {
            menuItems = menuService.getMenuByCategory(category);
        } else {
            menuItems = menuService.getAvailableMenu();
        }

        return ResponseEntity.ok(menuItems);
    }

    /**
     * Obtiene la lista de categorías disponibles
     * 
     * @return Lista de nombres de categorías
     */
    @GetMapping("/categories")
    @Operation(
        summary = "Obtener categorías disponibles",
        description = "Retorna la lista de todas las categorías disponibles en el menú",
        responses = {
            @ApiResponse(responseCode = "200", description = "Categorías obtenidas exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
        }
    )
    public ResponseEntity<Map<String, List<String>>> getCategories() {
        List<String> categories = menuService.getAvailableCategories();
        return ResponseEntity.ok(Map.of("categories", categories));
    }

    /**
     * Obtiene estadísticas del menú
     * 
     * @return Información sobre el menú
     */
    @GetMapping("/stats")
    @Operation(
        summary = "Obtener estadísticas del menú",
        description = "Retorna información estadística sobre el menú",
        responses = {
            @ApiResponse(responseCode = "200", description = "Estadísticas obtenidas exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
        }
    )
    public ResponseEntity<Map<String, Object>> getMenuStats() {
        List<MenuItemDTO> allItems = menuService.getAvailableMenu();
        
        long productCount = allItems.stream()
            .filter(item -> item.type() == MenuItemType.PRODUCT)
            .count();
        
        long comboCount = allItems.stream()
            .filter(item -> item.type() == MenuItemType.COMBO)
            .count();

        return ResponseEntity.ok(Map.of(
            "totalItems", allItems.size(),
            "products", productCount,
            "combos", comboCount,
            "categories", menuService.getAvailableCategories().size()
        ));
    }
}
