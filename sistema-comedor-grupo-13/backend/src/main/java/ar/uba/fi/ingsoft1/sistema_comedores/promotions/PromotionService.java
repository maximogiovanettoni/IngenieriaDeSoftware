package ar.uba.fi.ingsoft1.sistema_comedores.promotions;

import ar.uba.fi.ingsoft1.sistema_comedores.products.Product;
import ar.uba.fi.ingsoft1.sistema_comedores.products.ProductService;
import ar.uba.fi.ingsoft1.sistema_comedores.orders.OrderItem;
import ar.uba.fi.ingsoft1.sistema_comedores.orders.dto.OrderItemRequest;
import ar.uba.fi.ingsoft1.sistema_comedores.promotions.DiscountOptimizer.PromotionCombination;
import ar.uba.fi.ingsoft1.sistema_comedores.promotions.dto.*;
import ar.uba.fi.ingsoft1.sistema_comedores.promotions.exception.PromotionAlreadyExistsException;
import ar.uba.fi.ingsoft1.sistema_comedores.promotions.exception.PromotionNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final ProductService productService;

    @Transactional(readOnly = true)
    public List<PromotionDetailsResponse> getAllPromotions() {
        return promotionRepository.findAll().stream()
                .map(PromotionDetailsResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PromotionDetailsResponse> getActivePromotions() {
        return promotionRepository.findByActiveTrue().stream()
                .map(PromotionDetailsResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PromotionDetailsResponse> getCurrentlyValidPromotions() {
        return promotionRepository.findCurrentlyValidPromotions().stream()
                .map(PromotionDetailsResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PromotionDetailsResponse getPromotionById(Long id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new PromotionNotFoundException(id));
        return new PromotionDetailsResponse(promotion);
    }

    @Transactional
    public void createPromotion(PromotionRequest request) {
        if (promotionRepository.existsByName(request.name())) {
            throw new PromotionAlreadyExistsException("Ya existe una promoción con ese nombre");
        }

        Promotion promotion = switch (request) {
            case PercentageDiscountRequest r -> r.toEntity();
            case FixedDiscountRequest r -> r.toEntity();
            case BuyXGetYRequest r -> r.toEntity();
            case BuyXPayYRequest r -> r.toEntity();
        };

        promotionRepository.save(promotion);
    }

    @Transactional
    public void updatePromotion(Long id, UpdatePromotionRequest request) {
        Promotion promotion = promotionRepository.findById(id).orElseThrow(() -> new PromotionNotFoundException(id));
        if (request.name() != null) {
            if (promotionRepository.existsByNameAndIdNot(request.name(), id)) {
                throw new PromotionAlreadyExistsException("Ya existe una promoción con ese nombre");
            }
            promotion.setName(request.name());
        }
        if (request.startDate() != null && request.endDate() != null) {
            if (!request.startDate().isBefore(request.endDate())) {
                throw new IllegalArgumentException("La fecha de inicio debe ser anterior a la fecha de fin");
            }
        }
        if (request.description() != null) {
            promotion.setDescription(request.description());
        }
        if (request.active() != null) {
            promotion.setActive(request.active());
        }
        if (request.startDate() != null) {
            promotion.setStartDate(request.startDate());
        }
        if (request.endDate() != null) {
            promotion.setEndDate(request.endDate());
        }
        if (request.applicableDays() != null) {
            promotion.setApplicableDays(request.applicableDays());
        }
        if (request.applicableHours() != null) {
            promotion.setApplicableHours(
                    request.applicableHours().stream()
                            .map(TimeRangeRequest::toEntity)
                            .collect(Collectors.toSet())
            );
        }

        promotionRepository.save(promotion);
    }

    @Transactional
    public void deletePromotion(Long id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new PromotionNotFoundException(id));
        promotionRepository.delete(promotion);
    }

    @Transactional
    public PromotionDetailsResponse activatePromotion(Long id) {
        Promotion promotion = promotionRepository.findByIdAndActiveFalse(id)
                .orElseThrow(() -> new PromotionNotFoundException(id));
        promotion.setActive(true);
        Promotion updatedPromotion = promotionRepository.save(promotion);
        return new PromotionDetailsResponse(updatedPromotion);
    }

    @Transactional
    public PromotionDetailsResponse deactivatePromotion(Long id) {
        Promotion promotion = promotionRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new PromotionNotFoundException(id));
        promotion.setActive(false);
        Promotion updatedPromotion = promotionRepository.save(promotion);
        return new PromotionDetailsResponse(updatedPromotion);
    }

    @Transactional
    public PromotionCombination getAppliablePromotions(Map<Product, Integer> productQuantityMap) {

        Set<Promotion> validPromotions = new HashSet<>(promotionRepository.findCurrentlyValidPromotions());

        DiscountOptimizer discountOptimizer = new DiscountOptimizer(validPromotions, productQuantityMap);

        return discountOptimizer.getBestPromotions();
    }

}