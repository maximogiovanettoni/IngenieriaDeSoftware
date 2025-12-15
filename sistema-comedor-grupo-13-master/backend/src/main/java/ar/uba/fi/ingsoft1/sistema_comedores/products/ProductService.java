package ar.uba.fi.ingsoft1.sistema_comedores.products;

import ar.uba.fi.ingsoft1.sistema_comedores.ingredients.Ingredient;
import ar.uba.fi.ingsoft1.sistema_comedores.ingredients.IngredientRepository;
import ar.uba.fi.ingsoft1.sistema_comedores.ingredients.exception.IngredientNotFoundException;
import ar.uba.fi.ingsoft1.sistema_comedores.orders.OrderItem;
import ar.uba.fi.ingsoft1.sistema_comedores.products.combos.ComboProductObserver;
import ar.uba.fi.ingsoft1.sistema_comedores.products.elaborate.ElaborateProductRepository;
import ar.uba.fi.ingsoft1.sistema_comedores.products.elaborate.dto.AddIngredientRequest;
import ar.uba.fi.ingsoft1.sistema_comedores.products.elaborate.dto.RemoveIngredientRequest;
import ar.uba.fi.ingsoft1.sistema_comedores.products.elaborate.exception.IngredientNotInProductException;
import ar.uba.fi.ingsoft1.sistema_comedores.products.exception.ProductAlreadyExistsException;
import ar.uba.fi.ingsoft1.sistema_comedores.products.exception.ProductNotFoundException;
import ar.uba.fi.ingsoft1.sistema_comedores.common.enums.AdminOperation;
import ar.uba.fi.ingsoft1.sistema_comedores.common.exception.base.ResourceNotFoundException;
import ar.uba.fi.ingsoft1.sistema_comedores.common.exception.base.TechnicalException;
import ar.uba.fi.ingsoft1.sistema_comedores.image.ObjectPrefixConsts;
import ar.uba.fi.ingsoft1.sistema_comedores.image.ImageService;
import ar.uba.fi.ingsoft1.sistema_comedores.products.simple.dto.CreateSimpleProductRequest;
import ar.uba.fi.ingsoft1.sistema_comedores.products.elaborate.dto.CreateElaborateProductRequest;
import ar.uba.fi.ingsoft1.sistema_comedores.products.combos.Combo;
import ar.uba.fi.ingsoft1.sistema_comedores.products.combos.exception.ComboNotFoundException;
import ar.uba.fi.ingsoft1.sistema_comedores.products.dto.ProductDetailsResponse;
import ar.uba.fi.ingsoft1.sistema_comedores.products.dto.SearchProductRequest;
import ar.uba.fi.ingsoft1.sistema_comedores.products.simple.SimpleProduct;
import ar.uba.fi.ingsoft1.sistema_comedores.products.elaborate.ElaborateProduct;
import ar.uba.fi.ingsoft1.sistema_comedores.products.dto.ProductSpecification;

import ar.uba.fi.ingsoft1.sistema_comedores.products.simple.dto.UpdateSimpleProductStockRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final IngredientRepository ingredientRepository;
    private final ProductAuditLogRepository productAuditLogRepository;
    private final ImageService imageService;

    private void checkNotExistsByNameOrThrow(String name) throws ProductAlreadyExistsException {
        if (productRepository.existsByName(name)) {
            throw new ProductAlreadyExistsException("Ya existe un producto con ese nombre");
        }
    }

    @Autowired
    ProductService(
            ProductRepository productRepository,
            ElaborateProductRepository elaborateProductRepository,
            IngredientRepository ingredientRepository,
            ProductAuditLogRepository productAuditLogRepository,
            ImageService imageService
    ) {
        this.productRepository = productRepository;
        this.ingredientRepository = ingredientRepository;
        this.productAuditLogRepository = productAuditLogRepository;
        this.imageService = imageService;
    }
  
    public List<ProductDetailsResponse> getProducts(SearchProductRequest filter) {
        return productRepository
                .findAll(ProductSpecification.filterBy(filter))
                .stream()
                .map(ProductDetailsResponse::new)
                .toList();
    }

    public ProductDetailsResponse getProductDTOById(long id) throws ProductNotFoundException {
        Product product = productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
        return new ProductDetailsResponse(product);
    }

    public Product getProductById(Long id) throws ProductNotFoundException {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    public ProductType getProductTypeById(Long productId) {
        return productRepository.findById(productId)
            .map(Product::getProductType)
            .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    public ProductDetailsResponse createSimpleProduct(CreateSimpleProductRequest data) {
        checkNotExistsByNameOrThrow(data.name());
        SimpleProduct product = data.toSimpleProduct();
        productRepository.save(product);

        ProductAuditLog log = new ProductAuditLog();
        log.setProductName(product.getName());
        log.setOperation(AdminOperation.CREATE);
        log.setModifiedAt(Instant.now());
        productAuditLogRepository.save(log);

        return new ProductDetailsResponse(product);
    }

    @Transactional
    public ProductDetailsResponse createElaborateProduct(CreateElaborateProductRequest data) throws IngredientNotFoundException {
    
        checkNotExistsByNameOrThrow(data.name());
        
        ElaborateProduct product = data.toElaborateProduct();

        product = productRepository.save(product);
        
        Map<Long, BigDecimal> ingredientQuantities = data.ingredients();
        for (Map.Entry<Long, BigDecimal> entry : ingredientQuantities.entrySet()) {
            Ingredient ingredient = ingredientRepository.findById(entry.getKey())
                .orElseThrow(() -> new IngredientNotFoundException(entry.getKey()));
            
            product.addIngredient(ingredient, entry.getValue());
        }
        
        productRepository.save(product);
        
        ProductAuditLog productAuditlog = new ProductAuditLog();
        productAuditlog.setProductName(product.getName());
        productAuditlog.setOperation(AdminOperation.CREATE);
        productAuditlog.setModifiedAt(Instant.now());
        productAuditLogRepository.save(productAuditlog);

        return new ProductDetailsResponse(product);
    }

    @Transactional
    public void addIngredient(AddIngredientRequest dto) throws ResourceNotFoundException {
        ElaborateProduct product = productRepository.findElaborateProductById(dto.productId())
            .orElseThrow(() -> new ProductNotFoundException(dto.productId()));
        
        Ingredient ingredient = ingredientRepository.findById(dto.ingredientId())
            .orElseThrow(() -> new IngredientNotFoundException(dto.ingredientId()));
        
        product.addIngredient(ingredient, dto.quantity());
        
        productRepository.save(product);
    }

    @Transactional
    public void removeIngredient(RemoveIngredientRequest dto) 
            throws ResourceNotFoundException, IngredientNotInProductException {
        
        ElaborateProduct product = productRepository.findElaborateProductById(dto.productId())
            .orElseThrow(() -> new ProductNotFoundException(dto.productId()));
        
        Ingredient ingredient = ingredientRepository.findById(dto.ingredientId())
            .orElseThrow(() -> new IngredientNotFoundException(dto.ingredientId()));
        
        boolean hasIngredient = product.getProductIngredients().stream()
            .anyMatch(pi -> pi.getIngredient().getId().equals(dto.ingredientId()));
        
        if (!hasIngredient) {
            throw new IngredientNotInProductException(
                product.getId(), 
                product.getName(), 
                ingredient.getId(), 
                ingredient.getName()
            );
        }
        
        product.removeIngredient(ingredient);
        
        productRepository.save(product);
    }

    public boolean hasStock(Long productId, Integer requiredQuantity) throws ProductNotFoundException {
        Product product = getProductById(productId);
        return product.isActive() && product.isAvailable() && product.getStock() != null && product.getStock() >= requiredQuantity;
    }

    @Transactional
    public void restoreProductStock(Long id, Integer quantity) {
        Product product = this.getProductById(id);
        product.restoreStock(quantity);
        productRepository.save(product);
        log.info("Restored {} units to product {} (type: {})", 
                quantity, id, product.getClass().getSimpleName());
    }

    @Transactional
    public void deactivateProduct(Long id, String reason) throws ProductNotFoundException {
        Product product = getProductById(id);

        Set<Combo> affectedCombos = product.getObservers().stream()
                .filter(observer -> observer instanceof ComboProductObserver)
                .map(observer -> ((ComboProductObserver) observer).getCombo())
                .collect(Collectors.toSet());

        product.handleDeactivation();
        productRepository.save(product);

        ProductAuditLog log = new ProductAuditLog();
        log.setProductName(product.getName());
        log.setOperation(AdminOperation.DEACTIVATE);
        log.setModifiedAt(Instant.now());
        log.setReason(reason);
        productAuditLogRepository.save(log);
    }

    @Transactional
    public void restoreProduct(Long id) throws ProductNotFoundException {
        Product product = getProductById(id);

        Set<Combo> affectedCombos = product.getObservers().stream()
                .filter(observer -> observer instanceof ComboProductObserver)
                .map(observer -> ((ComboProductObserver) observer).getCombo())
                .collect(Collectors.toSet());

        product.handleRestoration();
        productRepository.save(product);

        ProductAuditLog log = new ProductAuditLog();
        log.setProductName(product.getName());
        log.setOperation(AdminOperation.REACTIVATE);
        log.setModifiedAt(Instant.now());
        productAuditLogRepository.save(log);
    }

    public void changeProductName(Long id, String newName) throws ProductNotFoundException, ProductAlreadyExistsException {
        Product product = getProductById(id);
        checkNotExistsByNameOrThrow(newName);
        String oldName = product.getName();
        product.setName(newName);
        productRepository.save(product);

        ProductAuditLog log = new ProductAuditLog();
        log.setProductName(product.getName());
        log.setOperation(AdminOperation.CHANGE_NAME);
        log.setModifiedAt(Instant.now());
        log.setReason("oldName=" + oldName);
        productAuditLogRepository.save(log);
    }

    public void changeProductPrice(Long id, BigDecimal newPrice) throws ProductNotFoundException {
        Product product = getProductById(id);
        BigDecimal oldPrice = product.getPrice();
        product.setPrice(newPrice);
        productRepository.save(product);

        ProductAuditLog log = new ProductAuditLog();
        log.setProductName(product.getName());
        log.setOperation(AdminOperation.CHANGE_PRICE);
        log.setModifiedAt(Instant.now());
        log.setReason("oldPrice=" + oldPrice);
        productAuditLogRepository.save(log);
    }

    @Transactional
    public ProductDetailsResponse changeProductImage(Long id, MultipartFile newImageFile) {
        Product product = getProductById(id);
        
        if (product.getImageUrl() != null && !product.getImageUrl().isBlank()) {
            try {
                imageService.deleteImage(product.getImageUrl());
            } catch (Exception e) {
                log.warn("Failed to delete old image for product {}: {}", id, e.getMessage());
            }
        }
        
        String objectPrefix = String.format("%s/%s", 
            ObjectPrefixConsts.PRODUCTS_OBJECT_PREFIX, product.getId());
        String newImageUrl = imageService.uploadImage(newImageFile, objectPrefix);
        
        product.setImageUrl(newImageUrl);
        productRepository.save(product);
        
        ProductAuditLog auditLog = new ProductAuditLog();
        auditLog.setProductName(product.getName());
        auditLog.setOperation(AdminOperation.CHANGE_IMAGE);
        auditLog.setModifiedAt(Instant.now());
        auditLog.setReason("Imagen actualizada");
        productAuditLogRepository.save(auditLog);
        
        return new ProductDetailsResponse(product);
    }

    @Transactional
    public void changeSimpleProductStock(Long id, UpdateSimpleProductStockRequest data) throws ProductNotFoundException {
        SimpleProduct simpleProduct = productRepository.findSimpleProductById(id)
            .orElseThrow(() -> new ProductNotFoundException(id));

        Integer newStock = data.stock();
        Integer oldStock = simpleProduct.getStock();
        simpleProduct.setStock(newStock);
        simpleProduct.setAvailable(newStock > 0 && simpleProduct.isActive());
        simpleProduct.notifyObserversStockChange();
        simpleProduct = productRepository.save(simpleProduct);

        ProductAuditLog log = new ProductAuditLog();
        log.setProductName(simpleProduct.getName());
        log.setOperation(AdminOperation.UPDATE_STOCK);
        log.setModifiedAt(Instant.now());
        log.setReason("oldStock=" + oldStock + ", newStock=" + newStock);
        productAuditLogRepository.save(log);
    }

}
