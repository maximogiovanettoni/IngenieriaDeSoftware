package ar.uba.fi.ingsoft1.sistema_comedores.products;

import ar.uba.fi.ingsoft1.sistema_comedores.ingredients.Ingredient;
import ar.uba.fi.ingsoft1.sistema_comedores.ingredients.dto.IngredientDetailsResponse;
import ar.uba.fi.ingsoft1.sistema_comedores.ingredients.dto.UpdateIngredientStockRequest;
import ar.uba.fi.ingsoft1.sistema_comedores.ingredients.exception.IngredientNotFoundException;
import ar.uba.fi.ingsoft1.sistema_comedores.products.dto.*;
import ar.uba.fi.ingsoft1.sistema_comedores.products.simple.dto.CreateSimpleProductRequest;
import ar.uba.fi.ingsoft1.sistema_comedores.products.elaborate.dto.CreateElaborateProductRequest;

import java.util.List;
import java.util.Map;

import ar.uba.fi.ingsoft1.sistema_comedores.products.simple.dto.UpdateSimpleProductStockRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/products")
@Tag(name = "Products")
class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener todos los productos")
    @ApiResponse(responseCode = "200", description = "Lista de todos los productos obtenida exitosamente")
    @ApiResponse(responseCode = "403", description = "Acceso denegado - requiere rol ADMIN", content = @Content)
    public ResponseEntity<?> getAllProducts() {
        List<ProductDetailsResponse> products = productService.getProducts(null);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/admin-search")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener productos por filtro de búsqueda")
    @ApiResponse(responseCode = "200", description = "Lista de productos activos obtenida exitosamente")
    public ResponseEntity<List<ProductDetailsResponse>> getProductsByFilter(
        @Valid @NonNull @RequestBody SearchProductRequest filter
    ) {
        List<ProductDetailsResponse> products = productService.getProducts(filter);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search")
    @Operation(summary = "Obtener productos por filtro de búsqueda")
    @ApiResponse(responseCode = "200", description = "Lista de productos activos obtenida exitosamente")
    public ResponseEntity<List<ProductDetailsResponse>> getActiveProductsByFilter(
        @Valid @NonNull @RequestBody SearchProductRequest filter
    ) {
        SearchProductRequest activeFilter = new SearchProductRequest(filter.category(), filter.minPrice(), filter.maxPrice(), filter.available(), true);
        List<ProductDetailsResponse> products = productService.getProducts(activeFilter);
        return ResponseEntity.ok(products);
    }

    @GetMapping
    @Operation(summary = "Obtener productos activos")
    @ApiResponse(responseCode = "200", description = "Lista de productos activos obtenida exitosamente")
    public ResponseEntity<List<ProductDetailsResponse>> getActiveProducts() {
        SearchProductRequest filter = new SearchProductRequest(null, null, null, null, true);
        List<ProductDetailsResponse> products = productService.getProducts(filter);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/available")
    @Operation(summary = "Obtener productos disponibles")
    @ApiResponse(responseCode = "200", description = "Lista de productos disponibles obtenida exitosamente")
    public ResponseEntity<List<ProductDetailsResponse>> getAvailableProducts() {
        SearchProductRequest filter = new SearchProductRequest(null, null, null, true, null);
        List<ProductDetailsResponse> products = productService.getProducts(filter);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener producto por ID")
    @ApiResponse(responseCode = "200", description = "Producto encontrado")
    @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content)
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        ProductDetailsResponse product = productService.getProductDTOById(id);
        return ResponseEntity.ok(product);
    }

    @PostMapping("/simple")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear un nuevo producto simple")
    @ApiResponse(responseCode = "201", description = "Producto simple creado exitosamente")
    @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    @ApiResponse(responseCode = "403", description = "Acceso denegado - requiere rol ADMIN", content = @Content)
    public ResponseEntity<?> createSimpleProduct(  
            @Valid @NonNull @RequestBody CreateSimpleProductRequest data
    ) {
        ProductDetailsResponse result = productService.createSimpleProduct(data);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping("/elaborate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear un nuevo producto elaborado")
    @ApiResponse(responseCode = "201", description = "Producto elaborado creado exitosamente")
    @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    @ApiResponse(responseCode = "403", description = "Acceso denegado - requiere rol ADMIN", content = @Content)
    public ResponseEntity<?> createElaborateProduct(  
            @Valid @NonNull @RequestBody CreateElaborateProductRequest data
    ) {
        ProductDetailsResponse result = productService.createElaborateProduct(data);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }


    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Desactivar un producto")
    @ApiResponse(responseCode = "200", description = "Producto desactivado exitosamente")
    @ApiResponse(responseCode = "403", description = "Acceso denegado - requiere rol ADMIN", content = @Content)
    @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content)
    public ResponseEntity<?> deactivateProduct(
        @PathVariable Long id,
        @RequestBody(required = false) @Valid DeleteProductRequest req
    ) {
        String reason = req == null ? null : req.reason();
        productService.deactivateProduct(id, reason);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Producto desactivado exitosamente"
        ));
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Restaurar un producto desactivado")
    @ApiResponse(responseCode = "200", description = "Producto restaurado exitosamente")
    @ApiResponse(responseCode = "403", description = "Acceso denegado - requiere rol ADMIN", content = @Content)
    @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content)
    public ResponseEntity<?> restoreProduct(@PathVariable Long id) {
        productService.restoreProduct(id);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Producto restaurado exitosamente"
        ));
    }

    @PutMapping("/{id}/name")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cambiar nombre del producto")
    @ApiResponse(responseCode = "200", description = "Nombre del producto actualizado exitosamente")
    @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    @ApiResponse(responseCode = "403", description = "Acceso denegado - requiere rol ADMIN", content = @Content)
    @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content)
    @ApiResponse(responseCode = "409", description = "El nombre ya existe", content = @Content)
    public ResponseEntity<?> changeProductName(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductNameRequest data
    ) {
        productService.changeProductName(id, data.name());
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Nombre del producto actualizado exitosamente",
            "product", data.name()
        ));
    }

    @PutMapping("/{id}/price")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cambiar precio del producto")
    @ApiResponse(responseCode = "200", description = "Precio del producto actualizado exitosamente")
    @ApiResponse(responseCode = "400", description = "Datos inválidos o precio superior al precio regular", content = @Content)
    @ApiResponse(responseCode = "403", description = "Acceso denegado - requiere rol ADMIN", content = @Content)
    @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content)
    public ResponseEntity<?> changeProductPrice(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductPriceRequest data
    ) {
        productService.changeProductPrice(id, data.price());
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Precio del producto actualizado exitosamente"
        ));
    }

    @PutMapping("/{id}/image")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cambiar imagen del producto")
    @ApiResponse(responseCode = "200", description = "Imagen del producto actualizada exitosamente")
    @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    @ApiResponse(responseCode = "403", description = "Acceso denegado - requiere rol ADMIN", content = @Content)
    @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content)
    public ResponseEntity<?> changeProductImage(
            @NotNull @PathVariable Long id,
            @NotNull @RequestParam("file") MultipartFile newImageFile
    ) {
        ProductDetailsResponse result = productService.changeProductImage(id, newImageFile);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Imagen del producto actualizada exitosamente",
            "product", result
        ));
    }

    @PutMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cambiar stock del producto")
    @ApiResponse(responseCode = "200", description = "Stock del producto actualizada exitosamente")
    @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    @ApiResponse(responseCode = "403", description = "Acceso denegado - requiere rol ADMIN", content = @Content)
    @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content)
    public ResponseEntity<?> changeProductImage(
            @NotNull @PathVariable Long id,
            @Valid @NotNull @RequestBody UpdateSimpleProductStockRequest data
    ) {
        productService.changeSimpleProductStock(id, data);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Stock del producto actualizada exitosamente"
        ));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar un producto")
    @ApiResponse(responseCode = "200", description = "Producto desactivado exitosamente")
    @ApiResponse(responseCode = "403", description = "Acceso denegado - requiere rol ADMIN", content = @Content)
    @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content)
    public ResponseEntity<?> deleteProduct(
        @PathVariable Long id,
        @RequestBody(required = false) @Valid DeleteProductRequest req
    ) {
        String reason = req == null ? null : req.reason();
        productService.deactivateProduct(id, reason);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Producto eliminado exitosamente"
        ));
    }
}