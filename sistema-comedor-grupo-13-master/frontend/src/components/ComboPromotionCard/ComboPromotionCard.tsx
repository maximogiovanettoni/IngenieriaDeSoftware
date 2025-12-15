import React from 'react';
import './combo-promotion-card.css';

interface PromotionDetails {
  requiredCategory?: string | null;
  freeCategory?: string | null;
  requiredQuantity?: number | null;
  chargedQuantity?: number | null;
  freeQuantityApplied?: number | null;
  minimumPurchase?: number | null;
}

interface ComboPromotionCardProps {
  promotionType: string;
  promotionName: string;
  discountAmount: number;
  details?: PromotionDetails | null;
}

export const ComboPromotionCard: React.FC<ComboPromotionCardProps> = ({
  promotionType,
  promotionName,
  discountAmount,
  details,
}) => {
  const getCategoryLabel = (category?: string | null): string => {
    if (!category) return 'Producto';
    const labels: Record<string, string> = {
      SANDWICH: '🥪 Sándwich',
      BEVERAGE: '🥤 Bebida',
      DESSERT: '🍰 Postre',
      SNACK: '🍪 Snack',
      COMBO: '🍱 Combo',
    };
    return labels[category] || category;
  };

  return (
    <div className="combo-promo-card">
      <div className="promo-header">
        <span className="promo-icon">🎉</span>
        <span className="promo-title">¡OFERTA APLICABLE!</span>
      </div>

      <div className="promo-body">
        {promotionType === 'BUY_X_GET_Y' && details && (
          <div className="combo-visual">
            <div className="combo-item required">
              <div className="item-label">Compra</div>
              <div className="item-emoji">🛒</div>
              <div className="item-category">{getCategoryLabel(details.requiredCategory)}</div>
            </div>

            <div className="combo-arrow">↓</div>

            <div className="combo-item free">
              <div className="item-label">Lleva</div>
              <div className="item-emoji">🎁</div>
              <div className="item-category">{getCategoryLabel(details.freeCategory)}</div>
              {details.freeQuantityApplied && details.freeQuantityApplied > 0 && (
                <div className="quantity-badge">×{details.freeQuantityApplied}</div>
              )}
              <div className="free-label">GRATIS</div>
            </div>
          </div>
        )}

        {promotionType === 'BUY_X_PAY_Y' && details && (
          <div className="combo-visual">
            <div className="buy-pay-details">
              <div className="quantity-box">
                <div className="quantity-number">{details.requiredQuantity}</div>
                <div className="quantity-label">Compra</div>
              </div>

              <div className="equals">→</div>

              <div className="quantity-box charged">
                <div className="quantity-number">{details.chargedQuantity}</div>
                <div className="quantity-label">Paga</div>
              </div>

              <div className="equals">→</div>

              <div className="quantity-box free">
                <div className="quantity-number">
                  {(details.requiredQuantity || 0) - (details.chargedQuantity || 0)}
                </div>
                <div className="quantity-label">GRATIS</div>
              </div>
            </div>
            {details.freeQuantityApplied && details.freeQuantityApplied > 0 && (
              <div className="applied-info">
                Se aplicarán {details.freeQuantityApplied} gratis en tu carrito
              </div>
            )}
          </div>
        )}

        {promotionType === 'PERCENTAGE_DISCOUNT' && (
          <div className="simple-promo">
            <div className="promo-emoji">💰</div>
            <div className="promo-text">Descuento porcentual en tu compra</div>
          </div>
        )}

        {promotionType === 'FIXED_DISCOUNT' && (
          <div className="simple-promo">
            <div className="promo-emoji">💵</div>
            <div className="promo-text">Descuento fijo por compra mínima</div>
          </div>
        )}
      </div>

      <div className="promo-footer">
        <div className="promo-name">{promotionName}</div>
        <div className="discount-amount">
          Ahorras ${discountAmount.toLocaleString('es-AR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
        </div>
      </div>

      <div className="promo-note">
        ✓ Se aplicará automáticamente al confirmar tu pedido
      </div>
    </div>
  );
};
