import { ArrowRight, CheckCircle } from "lucide-react";
import { useEffect, useState } from "react";

import "./order-success-modal.css";

interface OrderItem {
  productName: string;
  quantity: number;
  unitPrice: number;
}

interface OrderSuccessModalProps {
  isOpen: boolean;
  orderNumber: string;
  onNavigateToOrder: () => void;
  onNavigateToMenu: () => void;
  subtotal?: number;
  discount?: number;
  total?: number;
  items?: OrderItem[];
}

export const OrderSuccessModal = ({
  isOpen,
  orderNumber,
  onNavigateToOrder,
  onNavigateToMenu,
  subtotal,
  discount,
  total,
  items,
}: OrderSuccessModalProps) => {
  const TOTAL_SECONDS = 10;

  const [isAnimating, setIsAnimating] = useState(false);
  const [secondsLeft, setSecondsLeft] = useState(TOTAL_SECONDS);

  useEffect(() => {
    if (!isOpen) return;

    setIsAnimating(true);
    setSecondsLeft(TOTAL_SECONDS);

    const tick = setInterval(() => {
      setSecondsLeft((s) => {
        if (s <= 1) {
          clearInterval(tick);
          onNavigateToMenu();
          return 0;
        }
        return s - 1;
      });
    }, 1000);

    return () => clearInterval(tick);
  }, [isOpen, onNavigateToMenu]);

  if (!isOpen) return null;

  return (
    <div className="osm osm--open" role="dialog" aria-modal="true" aria-labelledby="osm-title">
      <div className="osm__backdrop" onClick={onNavigateToMenu} />
      <div className="osm__card" role="document">
        {/* Icono + Glow */}
        <div className={`osm__icon ${isAnimating ? "is-in" : ""}`} aria-hidden>
          <span className="osm__icon-glow" />
          <span className="osm__icon-badge">
            <CheckCircle size={44} />
          </span>
        </div>

        {/* Título */}
        <h2 id="osm-title" className="osm__title">
          ¡Pedido Confirmado!
        </h2>

        {/* Número de pedido */}
        <div className="osm__orderbox">
          <p className="osm__orderbox-label">Número de Pedido</p>
          <p className="osm__orderbox-code">{orderNumber}</p>
        </div>

        {/* Lista de items */}
        {items && items.length > 0 && (
          <div className="osm__items-list">
            <h3 className="osm__items-title">Productos</h3>
            <div className="osm__items">
              {items.map((item, idx) => (
                <div key={idx} className={`osm__item ${""}`}>
                  <div className="osm__item-info">
                    <span className="osm__item-name">{item.productName}</span>
                    <span className="osm__item-qty">x{item.quantity}</span>
                  </div>
                  <span className="osm__item-price">${(item.unitPrice * item.quantity).toFixed(2)}</span>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Desglose de precios con animación de descuento */}
        {subtotal != null && (
          <div className="osm__pricing">
            <div className="osm__price-row">
              <span>Subtotal:</span>
              <span>${subtotal.toFixed(2)}</span>
            </div>
            {discount != null && discount > 0 && (
              <>
                <div className="osm__discount-row discount-animation">
                  <span>Descuento:</span>
                  <span className="discount-value">-${discount.toFixed(2)}</span>
                </div>
                <div className="osm__price-row osm__total-row">
                  <span className="strong">Total:</span>
                  <span className="total-value">${total?.toFixed(2)}</span>
                </div>
              </>
            )}
          </div>
        )}

        {/* Mensaje */}
        <p className="osm__msg">Tu pedido está siendo preparado. Te notificaremos cuando esté listo.</p>

        {/* Timer */}
        <p className="osm__timer">
          Redirigiendo al menú en <b>{secondsLeft}s</b>…
        </p>

        {/* Acciones */}
        <div className="osm__actions">
          <button onClick={onNavigateToMenu} className="osm-btn osm-btn--ghost">
            Volver al Menú
          </button>
          <button onClick={onNavigateToOrder} className="osm-btn osm-btn--success">
            Ver Pedido <ArrowRight size={14} className="osm__arrow" />
          </button>
        </div>
      </div>
    </div>
  );
};
