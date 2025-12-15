import { Promotion } from './PromotionAPI';
import { CartItem } from './CartContext';

export interface ApplicablePromotion {
  promotion: Promotion;
  estimatedDiscount: number;
  reason: string;
}

/**
 * Calcula qué promociones aplican al carrito actual y estima el descuento
 */
export const calculateApplicablePromotions = (
  promotions: Promotion[],
  cartItems: CartItem[]
): ApplicablePromotion[] => {
  const applicable: ApplicablePromotion[] = [];

  for (const promo of promotions) {
    if (!promo.active) continue;

    let estimatedDiscount = 0;
    let reason = '';

    try {
      // Crear un mapa de categorías a cantidades del carrito
      const categoryMap = new Map<string, number>();
      const productPrices = new Map<string, number>();
      
      cartItems.forEach(item => {
        // Usar categoría del item si existe, sino estimar
        const category = item.category || estimateCategory(item.itemName);
        const current = categoryMap.get(category) || 0;
        categoryMap.set(category, current + item.quantity);
        productPrices.set(category, item.unitPrice);
      });

      // Calcular total del carrito
      const cartTotal = cartItems.reduce((sum, item) => sum + item.unitPrice * item.quantity, 0);

      switch (promo.type) {
        case 'PERCENTAGE_DISCOUNT': {
          // Verificar si hay productos en la categoría
          const category = promo.category || 'SANDWICH';
          const categoryTotal = (categoryMap.get(category) || 0) * (productPrices.get(category) || 0);
          
          if (categoryTotal > 0) {
            const discountRate = 1 - (promo.multiplier || 0);
            estimatedDiscount = categoryTotal * discountRate;
            reason = `${Math.round(discountRate * 100)}% en ${category}`;
          }
          break;
        }

        case 'FIXED_DISCOUNT': {
          const minPurchase = Number(promo.minimumPurchase) || 0;
          if (cartTotal >= minPurchase) {
            estimatedDiscount = Number(promo.discountAmount) || 0;
            reason = `Compra mínima de $${minPurchase}`;
          }
          break;
        }

        case 'BUY_X_GET_Y': {
          const requiredCat = promo.requiredProductCategory || 'SANDWICH';
          const freeCat = promo.freeProductCategory || 'DRINK';
          const requiredCount = categoryMap.get(requiredCat) || 0;

          if (requiredCount > 0) {
            const freeItemPrice = productPrices.get(freeCat) || 0;
            estimatedDiscount = freeItemPrice; // Al menos 1 item gratis
            reason = `1 ${freeCat} gratis`;
          }
          break;
        }

        case 'BUY_X_PAY_Y': {
          const category = promo.category || 'SANDWICH';
          const requiredQty = promo.requiredQuantity || 1;
          const chargedQty = promo.chargedQuantity || 1;
          const categoryCount = categoryMap.get(category) || 0;

          if (categoryCount >= requiredQty) {
            const itemPrice = productPrices.get(category) || 0;
            const freeItems = requiredQty - chargedQty;
            estimatedDiscount = itemPrice * freeItems;
            reason = `Compra ${requiredQty} paga ${chargedQty}`;
          }
          break;
        }
      }

      if (estimatedDiscount > 0) {
        applicable.push({
          promotion: promo,
          estimatedDiscount: Math.round(estimatedDiscount * 100) / 100,
          reason,
        });
      }
    } catch (error) {
      console.error(`Error calculating discount for promotion ${promo.id}:`, error);
    }
  }

  return applicable.sort((a, b) => b.estimatedDiscount - a.estimatedDiscount);
};

/**
 * Estima la categoría de un producto basado en su nombre
 * (En producción, esto vendría del backend)
 */
function estimateCategory(itemName: string): string {
  const name = itemName.toLowerCase();
  
  if (name.includes('bebida') || name.includes('coca') || name.includes('sprite') || name.includes('agua')) {
    return 'BEVERAGE';
  }
  if (name.includes('postre') || name.includes('helado') || name.includes('torta')) {
    return 'DESSERT';
  }
  if (name.includes('ensalada') || name.includes('verdura')) {
    return 'SALAD';
  }
  if (name.includes('café') || name.includes('cafe')) {
    return 'COFFEE';
  }
  if (name.includes('combo')) {
    return 'COMBO';
  }
  
  // Por defecto, asumir que es un sándwich
  return 'SANDWICH';
}
