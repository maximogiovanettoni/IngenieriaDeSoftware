package ar.uba.fi.ingsoft1.sistema_comedores.promotions;

import ar.uba.fi.ingsoft1.sistema_comedores.orders.dto.promotion.AppliedPromotionResponse;
import ar.uba.fi.ingsoft1.sistema_comedores.products.Product;

import java.math.BigDecimal;
import java.util.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DiscountOptimizer {
    private final Set<Promotion> promotions;
    private final Map<Product, Integer> cart;

    public record PromotionCombination(Map<Promotion, BigDecimal> promotions, BigDecimal discount) {}

    public DiscountOptimizer(Set<Promotion> promotions, Map<Product, Integer> cart) {
        this.promotions = promotions;
        this.cart = cart;
    }

    public PromotionCombination getBestPromotions() {
        // Log cart contents
        log.debug("🛒 Cart contents: {}", cart.entrySet().stream()
                .map(e -> e.getKey().getName() + " (category: " + e.getKey().getCategory() + ") x" + e.getValue())
                .toList());
        
        List<Promotion> applicablePromotions = new ArrayList<>();
        for (Promotion p : promotions) {
            boolean applies = p.appliesTo(cart);
            if (applies) {
                applicablePromotions.add(p);
            }
        }

        if (applicablePromotions.isEmpty()) {
            return new PromotionCombination(new HashMap<>(), BigDecimal.ZERO);
        }

        // Separate product-specific and order-level promotions
        List<PromotionWithDiscount> productPromotions = new ArrayList<>();
        List<PromotionWithDiscount> orderPromotions = new ArrayList<>();
        
        for (Promotion promo : applicablePromotions) {
            BigDecimal discount = promo.calculateDiscount(cart);
            Set<Product> affectedProducts = getProductsInPromotion(promo);
            PromotionWithDiscount pwd = new PromotionWithDiscount(promo, discount, affectedProducts);
            
            if (promo.getPromotionCategory() == PromotionCategory.ORDER_LEVEL) {
                orderPromotions.add(pwd);
            } else {
                productPromotions.add(pwd);
            }
        }
        
        // Sort product promotions by discount (highest first)
        productPromotions.sort(Comparator.comparing(PromotionWithDiscount::discount).reversed());

        Map<Promotion, BigDecimal> selectedPromotions = new HashMap<>();
        Set<Product> usedProducts = new HashSet<>();
        BigDecimal totalDiscount = BigDecimal.ZERO;

        // Phase 1: Greedy selection of product-specific promotions
        for (PromotionWithDiscount pwd : productPromotions) {
            // Check if any products from this promotion are already used by another promotion
            boolean hasConflict = pwd.affectedProducts.stream().anyMatch(usedProducts::contains);
            
            if (!hasConflict) {
                // Filter cart to only affected products for this promotion
                Map<Product, Integer> filteredCart = filterCart(pwd.affectedProducts);
                
                // Recalculate discount with filtered cart to ensure accurate result
                BigDecimal discount = pwd.promotion.calculateDiscount(filteredCart);
                
                if (discount.compareTo(BigDecimal.ZERO) > 0) {
                    selectedPromotions.put(pwd.promotion, discount);
                    totalDiscount = totalDiscount.add(discount);
                    usedProducts.addAll(pwd.affectedProducts);
                    
                } else {
                    log.debug("❌ Promotion {} yielded zero discount", pwd.promotion.getName());
                }
            } else {
                log.debug("⚠️ Skipping promotion '{}' - products already used by another promotion", pwd.promotion.getName());
            }
        }

        // Phase 2: Apply order-level promotions (they can apply alongside product promotions)
        for (PromotionWithDiscount pwd : orderPromotions) {
            // Check if promotion still applies to cart after product-specific discounts
            if (pwd.promotion.appliesTo(cart)) {
                BigDecimal discount = pwd.promotion.calculateDiscount(cart);
                
                if (discount.compareTo(BigDecimal.ZERO) > 0) {
                    selectedPromotions.put(pwd.promotion, discount);
                    totalDiscount = totalDiscount.add(discount);
                    
                    log.debug("✅ Applied order-level promotion: {}, discount: {}", pwd.promotion.getName(), discount);
                }
            } else {
                log.debug("❌ Order-level promotion no longer applies after product discounts: {}", pwd.promotion.getName());
            }
        }

        PromotionCombination result = new PromotionCombination(selectedPromotions, totalDiscount);
        log.debug("🎯 Final greedy combination - Total discount: {}", totalDiscount);
        
        return result;
    }

    /**
     * Determines which products are affected by a promotion based on its type.
     * 
     * This is crucial for the greedy optimizer to avoid conflicts between promotions
     * that might affect overlapping products.
     */
    private Set<Product> getProductsInPromotion(Promotion promo) {
        Set<Product> affected = new HashSet<>();
        
        if (promo instanceof PercentageDiscount pd) {
            // Products of the specific category
            for (Product p : cart.keySet()) {
                if (p.getCategory() == pd.getCategory()) {
                    affected.add(p);
                }
            }
        } else if (promo instanceof BuyXGetY bxgy) {
            // Products of both required and free categories
            for (Product p : cart.keySet()) {
                if (p.getCategory() == bxgy.getRequiredCategory() || p.getCategory() == bxgy.getFreeCategory()) {
                    affected.add(p);
                }
            }
        } else if (promo instanceof BuyXPayY bxpy) {
            // Products of the category
            for (Product p : cart.keySet()) {
                if (p.getCategory() == bxpy.getCategory()) {
                    affected.add(p);
                }
            }
        } else {
            // FixedDiscount applies to entire order, include all products
            affected.addAll(cart.keySet());
        }
        
        return affected;
    }

    /**
     * Filters the cart to contain only the specified products.
     */
    private Map<Product, Integer> filterCart(Set<Product> products) {
        Map<Product, Integer> filtered = new HashMap<>();
        for (Product p : products) {
            if (cart.containsKey(p)) {
                filtered.put(p, cart.get(p));
            }
        }
        return filtered;
    }

    private record PromotionWithDiscount(Promotion promotion, BigDecimal discount, Set<Product> affectedProducts) {}
}