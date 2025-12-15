package ar.uba.fi.ingsoft1.sistema_comedores.config.loaders;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import ar.uba.fi.ingsoft1.sistema_comedores.common.enums.AdminOperation;
import ar.uba.fi.ingsoft1.sistema_comedores.image.ImageService;
import ar.uba.fi.ingsoft1.sistema_comedores.image.ObjectPrefixConsts;
import ar.uba.fi.ingsoft1.sistema_comedores.ingredients.Ingredient;
import ar.uba.fi.ingsoft1.sistema_comedores.ingredients.IngredientAuditLog;
import ar.uba.fi.ingsoft1.sistema_comedores.ingredients.IngredientAuditLogRepository;
import ar.uba.fi.ingsoft1.sistema_comedores.ingredients.IngredientRepository;
import ar.uba.fi.ingsoft1.sistema_comedores.ingredients.exception.IngredientNotFoundException;
import ar.uba.fi.ingsoft1.sistema_comedores.products.Product;
import ar.uba.fi.ingsoft1.sistema_comedores.products.ProductAuditLog;
import ar.uba.fi.ingsoft1.sistema_comedores.products.ProductAuditLogRepository;
import ar.uba.fi.ingsoft1.sistema_comedores.products.ProductCategory;
import ar.uba.fi.ingsoft1.sistema_comedores.products.ProductRepository;
import ar.uba.fi.ingsoft1.sistema_comedores.products.combos.Combo;
import ar.uba.fi.ingsoft1.sistema_comedores.products.combos.ComboRepository;
import ar.uba.fi.ingsoft1.sistema_comedores.products.elaborate.ElaborateProduct;
import ar.uba.fi.ingsoft1.sistema_comedores.products.exception.ProductNotFoundException;
import ar.uba.fi.ingsoft1.sistema_comedores.products.simple.SimpleProduct;
import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Component
@ConfigurationPropertiesBinding
class ProductCategoryConverter implements Converter<String, ProductCategory> {
    @Override
    public ProductCategory convert(String source) {
        return ProductCategory.fromValue(source);
    }
}

@Data
@Configuration
@ConfigurationProperties(prefix = "ingredients")
class IngredientsConfig {
    private List<IngredientData> items;

    @Data
    public static class IngredientData {
        private String name;
        private String unitMeasure;
        private Integer stock;
        private Boolean active;
        private Boolean available;
    }
}

@Data
@Configuration
@ConfigurationProperties(prefix = "products")
class ProductsConfig {
    private List<SimpleProductData> simple;
    private List<ElaborateProductData> elaborate;

    @Data
    public static class SimpleProductData {
        private String name;
        private String description;
        private BigDecimal price;
        private ProductCategory category;
        private Boolean active;
        private Integer stock;
        private String imageSrc;
    }

    @Data
    public static class ElaborateProductData {
        private String name;
        private String description;
        private BigDecimal price;
        private ProductCategory category;
        private Boolean active;
        private String imageSrc;
        private List<IngredientQuantity> ingredients;
    }

    @Data
    public static class IngredientQuantity {
        private String ingredientName;
        private BigDecimal quantity;
    }
}

@Data
@Configuration
@ConfigurationProperties(prefix = "combos")
class CombosConfig {
    private List<ComboData> items;

    @Data
    public static class ComboData {
        private String name;
        private String description;
        private BigDecimal price;
        private Boolean active;
        private String imageSrc;
        private List<ProductQuantity> products;
    }

    @Data
    public static class ProductQuantity {
        private String productName;
        private Integer quantity;
    }
}

@Slf4j
@Component
public class MenuDataLoader implements CommandLineRunner {

    private final IngredientRepository ingredientRepository;
    private final ProductRepository productRepository;
    private final ComboRepository comboRepository;
    private final IngredientsConfig ingredientsConfig;
    private final ProductsConfig productsConfig;
    private final CombosConfig combosConfig;
    private final ProductAuditLogRepository productAuditLogRepository;
    private final IngredientAuditLogRepository ingredientAuditLogRepository;
    private final ImageService imageService;

    public MenuDataLoader(
            IngredientRepository ingredientRepository,
            ProductRepository productRepository,
            ComboRepository comboRepository,
            IngredientsConfig ingredientsConfig,
            ProductsConfig productsConfig,
            CombosConfig combosConfig,
            ProductAuditLogRepository productAuditLogRepository,
            IngredientAuditLogRepository ingredientAuditLogRepository,
            ImageService imageService
    ) {
        this.ingredientRepository = ingredientRepository;
        this.productRepository = productRepository;
        this.comboRepository = comboRepository;
        this.ingredientsConfig = ingredientsConfig;
        this.productsConfig = productsConfig;
        this.combosConfig = combosConfig;
        this.productAuditLogRepository = productAuditLogRepository;
        this.ingredientAuditLogRepository = ingredientAuditLogRepository;
        this.imageService = imageService;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting data initialization...");
        loadIngredients();
        loadProducts();
        loadCombos();
        log.info("Data initialization completed!");
    }

    @Transactional
    private void loadIngredients() {
        if (ingredientRepository.count() > 0) {
            log.info("✓ Ingredients already exist in database");
            return;
        }

        List<Ingredient> ingredients = ingredientsConfig.getItems().stream()
            .map(item -> {
                Ingredient ingredient = new Ingredient();
                ingredient.setName(item.getName());
                ingredient.setUnitMeasure(item.getUnitMeasure());
                ingredient.setStock(BigDecimal.valueOf(item.getStock()));
                ingredient.setActive(item.getActive());
                ingredient.setAvailable(item.getAvailable());
                return ingredient;
            })
            .toList();

        ingredientRepository.saveAll(ingredients);
        ingredients.forEach(i -> createAuditLog(i.getName(), AdminOperation.CREATE, true));
        log.info("✓ {} ingredients loaded successfully", ingredients.size());
    }

    @Transactional
    private void loadProducts() {
        if (productRepository.count() > 0) {
            log.info("✓ Products already exist in database");
            return;
        }

        int simpleCount = 0;
        int elaborateCount = 0;

        if (productsConfig.getSimple() != null) {
            simpleCount = loadSimpleProducts();
        }

        if (productsConfig.getElaborate() != null) {
            elaborateCount = loadElaborateProducts();
        }

        log.info("✓ {} products loaded successfully ({} simple, {} elaborate)",
                simpleCount + elaborateCount, simpleCount, elaborateCount);
    }

    private int loadSimpleProducts() {
        List<SimpleProduct> products = productsConfig.getSimple().stream()
            .map(item -> {
                SimpleProduct product = new SimpleProduct(
                    item.getName(),
                    item.getDescription(),
                    item.getPrice(),
                    item.getCategory(),
                    item.getActive(),
                    item.getStock()
                );
                return product;
            })
            .toList();

        productRepository.saveAll(products);
        
        for (int i = 0; i < products.size(); i++) {
            SimpleProduct product = products.get(i);
            ProductsConfig.SimpleProductData item = productsConfig.getSimple().get(i);
            
            String imageUrl = uploadImageFromResources(
                item.getImageSrc(), 
                ObjectPrefixConsts.PRODUCTS_OBJECT_PREFIX + "/" + product.getId()
            );
            product.setImageUrl(imageUrl);
        }
        
        productRepository.saveAll(products);
        products.forEach(p -> createAuditLog(p.getName(), AdminOperation.CREATE, false));
        
        return products.size();
    }

    private int loadElaborateProducts() {
        int count = 0;
        for (ProductsConfig.ElaborateProductData item : productsConfig.getElaborate()) {
            ElaborateProduct product = new ElaborateProduct(
                item.getName(),
                item.getDescription(),
                item.getPrice(),
                item.getCategory(),
                item.getActive()
            );
            
            product = productRepository.save(product);
            
            String imageUrl = uploadImageFromResources(
                item.getImageSrc(), 
                ObjectPrefixConsts.PRODUCTS_OBJECT_PREFIX + "/" + product.getId()
            );
            product.setImageUrl(imageUrl);

            if (item.getIngredients() != null) {
                for (ProductsConfig.IngredientQuantity ingredientQty : item.getIngredients()) {
                    Ingredient ingredient = ingredientRepository
                        .findByName(ingredientQty.getIngredientName())
                        .orElseThrow(() -> new IngredientNotFoundException(ingredientQty.getIngredientName()));
                    product.addIngredient(ingredient, ingredientQty.getQuantity());
                    ingredientRepository.save(ingredient);
                }
            }

            productRepository.save(product);
            createAuditLog(product.getName(), AdminOperation.CREATE, false);
            count++;
            
            log.debug("✓ Elaborate product '{}' loaded with {} ingredients",
                product.getName(), product.getProductIngredients().size());
        }
        return count;
    }

    @Transactional
    private void loadCombos() {
        if (comboRepository.count() > 0 || combosConfig.getItems() == null) {
            log.info("✓ Combos already exist in database");
            return;
        }

        int count = 0;
        for (CombosConfig.ComboData item : combosConfig.getItems()) {
            Combo combo = new Combo(
                item.getName(),
                item.getDescription(),
                item.getPrice(),
                item.getActive()
            );
            
            combo = comboRepository.save(combo);
            
            String imageUrl = uploadImageFromResources(
                item.getImageSrc(), 
                ObjectPrefixConsts.COMBOS_OBJECT_PREFIX + "/" + combo.getId()
            );
            combo.setImageUrl(imageUrl);

            if (item.getProducts() != null) {
                for (CombosConfig.ProductQuantity pq : item.getProducts()) {
                    Product product = productRepository
                        .findByName(pq.getProductName())
                        .orElseThrow(() -> new ProductNotFoundException(pq.getProductName()));
                    combo.addProduct(product, pq.getQuantity());
                    productRepository.save(product);
                }
            }

            comboRepository.save(combo);
            createAuditLog(combo.getName(), AdminOperation.CREATE, false);
            count++;
            
            log.debug("✓ Combo '{}' loaded with {} products",
                combo.getName(), combo.getComboProducts().size());
        }
        log.info("✓ {} combos loaded successfully", count);
    }


    private void createAuditLog(String name, AdminOperation operation, boolean isIngredient) {
        if (isIngredient) {
            IngredientAuditLog auditLog = new IngredientAuditLog();
            auditLog.setIngredientName(name);
            auditLog.setOperation(operation);
            auditLog.setModifiedAt(Instant.now());
            ingredientAuditLogRepository.save(auditLog);
        } else {
            ProductAuditLog auditLog = new ProductAuditLog();
            auditLog.setProductName(name);
            auditLog.setOperation(operation);
            auditLog.setModifiedAt(Instant.now());
            productAuditLogRepository.save(auditLog);
        }
    }

    private String uploadImageFromResources(String imageSrc, String objectPrefix) {
        if (imageSrc == null || imageSrc.isBlank()) {
            return null;
        }
        
        try {
            String resourcePath = "static/images/" + imageSrc;
            ClassPathResource resource = new ClassPathResource(resourcePath);
            
            if (!resource.exists()) {
                log.warn("Image not found: {}", resourcePath);
                return null;
            }
            
            try (InputStream inputStream = resource.getInputStream()) {
                long size = resource.contentLength();
                String contentType = determineContentType(imageSrc);
                
                String fileName = imageSrc;
                
                return imageService.uploadImage(inputStream, fileName, contentType, size, objectPrefix);
            }
        } catch (Exception e) {
            log.error("Error loading image: {}", imageSrc, e);
            return null;
        }
    }
    private String determineContentType(String fileName) {
        if (fileName.endsWith(".png")) return "image/png";
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg";
        if (fileName.endsWith(".gif")) return "image/gif";
        if (fileName.endsWith(".webp")) return "image/webp";
        return "image/png";
    }
}