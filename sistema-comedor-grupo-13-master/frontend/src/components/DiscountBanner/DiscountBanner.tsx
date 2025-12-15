import React from "react";

import { AppliedPromotionInfo } from "@/services/OrderAPI";
import { useCart } from "@/services/useCart";

import "./discount-banner.css";

interface DiscountBannerProps {
  appliedPromotions: AppliedPromotionInfo[];
}

export const DiscountBanner: React.FC<DiscountBannerProps> = ({ appliedPromotions }) => {
  const { cart } = useCart();

  // Early return if cart is empty or no promotions
  if (cart.items.length === 0 || appliedPromotions.length === 0) {
    return null;
  }

  return (
    <div className="discount-banners-container">
      {appliedPromotions.map((promo, index) => {
        const config = getPromotionConfig(promo.type);

        return (
          <div key={index} className={`discount-banner ${config.className}`}>
            <div className="discount-banner-content">
              <div className="discount-icon">{config.icon}</div>
              <div className="discount-details">
                <div className="discount-title">{config.title}</div>

                <div className="best-discount-info">
                  <div className="promo-info">
                    <span className="promo-name">{promo.name}</span>
                    {promo.discountAmount > 0 && (
                      <span className="promo-amount">Ahorrarás ${promo.discountAmount.toLocaleString("es-AR")}</span>
                    )}
                  </div>
                </div>

                <div className="discount-note">Se aplicará automáticamente al confirmar tu pedido</div>
              </div>
            </div>
          </div>
        );
      })}
    </div>
  );
};

function getPromotionConfig(type: string) {
  switch (type) {
    case "BUY_X_GET_Y":
      return {
        icon: "🎁",
        title: "¡Promo Combo!",
        className: "discount-banner--combo",
      };
    case "BUY_X_PAY_Y":
      return {
        icon: "💰",
        title: "¡Promo 2x1!",
        className: "discount-banner--combo",
      };
    case "PERCENTAGE_DISCOUNT":
      return {
        icon: "🏷️",
        title: "¡Descuento Porcentual!",
        className: "discount-banner--percentage",
      };
    case "FIXED_DISCOUNT":
      return {
        icon: "💸",
        title: "¡Descuento Fijo!",
        className: "discount-banner--fixed",
      };
    default:
      return {
        icon: "🎉",
        title: "¡Descuento Aplicable!",
        className: "discount-banner--default",
      };
  }
}
