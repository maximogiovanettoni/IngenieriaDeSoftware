package ar.uba.fi.ingsoft1.sistema_comedores.products.combos;

import ar.uba.fi.ingsoft1.sistema_comedores.products.combos.dto.ComboDetailsResponse;
import ar.uba.fi.ingsoft1.sistema_comedores.products.combos.dto.CreateComboRequest;
import ar.uba.fi.ingsoft1.sistema_comedores.products.combos.exception.*;
import ar.uba.fi.ingsoft1.sistema_comedores.products.dto.ProductDetailsResponse;
import ar.uba.fi.ingsoft1.sistema_comedores.products.exception.ProductNotAvailableException;
import ar.uba.fi.ingsoft1.sistema_comedores.products.exception.ProductNotFoundException;
import lombok.extern.slf4j.Slf4j;
import ar.uba.fi.ingsoft1.sistema_comedores.products.Product;
import ar.uba.fi.ingsoft1.sistema_comedores.products.ProductAuditLog;
import ar.uba.fi.ingsoft1.sistema_comedores.products.ProductAuditLogRepository;
import ar.uba.fi.ingsoft1.sistema_comedores.products.ProductRepository;
import ar.uba.fi.ingsoft1.sistema_comedores.common.enums.AdminOperation;
import ar.uba.fi.ingsoft1.sistema_comedores.image.ImageService;
import ar.uba.fi.ingsoft1.sistema_comedores.image.ObjectPrefixConsts;
import ar.uba.fi.ingsoft1.sistema_comedores.orders.OrderItem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Service
@Transactional
@Slf4j
public class ComboService {

    private final ComboRepository comboRepository;
    private final ProductRepository productRepository;
    private final ProductAuditLogRepository productAuditLogRepository;
    private final ImageService imageService;

    @Autowired
    public ComboService(
            ComboRepository comboRepository,
            ProductRepository productRepository,
            ProductAuditLogRepository productAuditLogRepository,
            ImageService imageService
    ) {
        this.comboRepository = comboRepository;
        this.productRepository = productRepository;
        this.productAuditLogRepository = productAuditLogRepository;
        this.imageService = imageService;
    }

    private Map<Product, Integer> getProductsFromIds(Map<Long, Integer> productIds) throws ProductNotFoundException, ProductNotAvailableException {
        Map<Product, Integer> products = new HashMap<>();
        for (Map.Entry<Long, Integer> entry : productIds.entrySet()) {
            Product product =
                    productRepository.findById(entry.getKey()).orElseThrow(() -> new ProductNotFoundException(entry.getKey()));
            if (!product.isAvailable()) {
                throw new ProductNotAvailableException(product.getName());
            }
            Integer quantity = entry.getValue();
            products.put(product, quantity);
        }
        return products;
    }

    public ComboDetailsResponse createCombo(CreateComboRequest dto) throws ComboAlreadyExistsException, ProductNotFoundException, ProductNotAvailableException {
        if (comboRepository.existsByName(dto.name())) {
            throw new ComboAlreadyExistsException(dto.name());
        }
        Combo combo = dto.toCombo();
        combo = comboRepository.save(combo);
        Map<Product, Integer> products = getProductsFromIds(dto.products());

        for (Map.Entry<Product, Integer> entry : products.entrySet()) {
            combo.addProduct(entry.getKey(), entry.getValue());
        }
        comboRepository.save(combo);

        ProductAuditLog auditLog = new ProductAuditLog();
        auditLog.setProductName(combo.getName());
        auditLog.setOperation(AdminOperation.CREATE);
        auditLog.setModifiedAt(Instant.now());
        productAuditLogRepository.save(auditLog);

        return new ComboDetailsResponse(combo); 
    }

    public List<ComboDetailsResponse> getAllCombos() {
        return comboRepository.findAll()
                .stream()
                .map(ComboDetailsResponse::new)
                .toList();
    }

    public List<ComboDetailsResponse> getActiveCombos() {
        return comboRepository.findByActiveTrue()
                .stream()
                .map(ComboDetailsResponse::new)
                .toList();
    }

    public List<ComboDetailsResponse> getAvailableCombos() {
        return comboRepository.findByActiveTrue()
                .stream()
                .filter(Combo::isAvailable)
                .map(ComboDetailsResponse::new)
                .toList();
    }

    public ComboDetailsResponse getComboById(Long id) throws ComboNotFoundException {
        Combo combo = comboRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ComboNotFoundException(id));
        
        return new ComboDetailsResponse(combo);
    }

    public void changeComboName(Long id, String newName) throws ComboNotFoundException, ComboAlreadyExistsException {
        Combo combo = comboRepository.findById(id)
                .orElseThrow(() -> new ComboNotFoundException(id));

        if (newName != null && !newName.equals(combo.getName()) && comboRepository.existsByNameAndIdNot(newName, id)) {
            throw new ComboAlreadyExistsException(newName);
        }

        combo.setName(newName);
        comboRepository.save(combo);
    }

    public void changeComboPrice(Long id, BigDecimal newPrice) throws ComboNotFoundException{
        Combo combo = comboRepository.findById(id)
                .orElseThrow(() -> new ComboNotFoundException(id));

        if (newPrice != null) {
            BigDecimal regularPrice = combo.getRegularPrice();
            if (newPrice.compareTo(regularPrice) > 0) {
                throw new InvalidComboPriceException(newPrice, regularPrice);
            }
            combo.setPrice(newPrice);
        }

        comboRepository.save(combo);
    }

    @Transactional
    public ProductDetailsResponse changeComboImage(Long id, MultipartFile newImageFile) {
        Combo combo = comboRepository.findById(id).orElseThrow(() -> new ComboNotFoundException(id));
        
        if (combo.getImageUrl() != null && !combo.getImageUrl().isBlank()) {
            try {
                imageService.deleteImage(combo.getImageUrl());
            } catch (Exception e) {
                log.warn("Failed to delete old image for product {}: {}", id, e.getMessage());
            }
        }
        
        String objectPrefix = String.format("%s/%s", 
            ObjectPrefixConsts.COMBOS_OBJECT_PREFIX, combo.getId());
        String newImageUrl = imageService.uploadImage(newImageFile, objectPrefix);
        
        combo.setImageUrl(newImageUrl);
        productRepository.save(combo);
        
        ProductAuditLog auditLog = new ProductAuditLog();
        auditLog.setProductName(combo.getName());
        auditLog.setOperation(AdminOperation.CHANGE_IMAGE);
        auditLog.setModifiedAt(Instant.now());
        auditLog.setReason("Imagen actualizada");
        productAuditLogRepository.save(auditLog);
        
        return new ProductDetailsResponse(combo);
    }

    public void deactivateCombo(Long id) throws ComboNotFoundException {
        Combo combo = comboRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ComboNotFoundException(id));
        combo.handleDeactivation();
        comboRepository.save(combo);
    }

    public void restoreCombo(Long id) throws ComboNotFoundException {
        Combo combo = comboRepository.findByIdAndActiveFalse(id)
                .orElseThrow(() -> new ComboNotFoundException(id));
        combo.handleRestoration();
        comboRepository.save(combo);
    }

    public boolean checkAvailability(Long comboId) throws ComboNotFoundException {
        Combo combo = comboRepository.findByIdAndActiveTrue(comboId)
                .orElseThrow(() -> new ComboNotFoundException(comboId));
        return combo.isAvailable();
    }
}
