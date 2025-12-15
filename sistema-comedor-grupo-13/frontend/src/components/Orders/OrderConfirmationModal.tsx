import { X, ShoppingCart, Loader2 } from 'lucide-react';
import { CartItem } from '@/services/CartContext';
import './order-confirmation-modal.css';

interface OrderConfirmationModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: () => Promise<void>;
  items: CartItem[];
  total: number;
  loading: boolean;
}

export const OrderConfirmationModal = ({
  isOpen,
  onClose,
  onConfirm,
  items,
  total,
  loading,
}: OrderConfirmationModalProps) => {
  if (!isOpen) return null;

  return (
    <div className="ocm ocm--open" role="dialog" aria-modal="true" aria-labelledby="ocm-title">
      <div className="ocm__backdrop" onClick={onClose} />

      <div className="ocm__card" role="document">
        {/* Header */}
        <header className="ocm__header">
          <div className="ocm__title-wrap">
            <span className="ocm__title-icon">
              <ShoppingCart size={18} />
            </span>
            <h2 id="ocm-title" className="ocm__title">Confirmar Pedido</h2>
          </div>

          <button
            onClick={onClose}
            disabled={loading}
            className="ocm__close"
            aria-label="Cerrar"
          >
            <X size={20} />
          </button>
        </header>

        {/* Items Summary */}
        <section className="ocm__summary">
          <p className="ocm__summary-title">Resumen del Pedido</p>

          <div className="ocm__items">
            {items.map((item) => (
              <div
                key={`${item.itemType}-${item.itemId}`}
                className="ocm-item"
              >
                <div className="ocm-item__info">
                  <p className="ocm-item__name">{item.itemName}</p>
                  <p className="ocm-item__qty">Cantidad: {item.quantity}</p>
                </div>
                <p className="ocm-item__amount">
                  ${(item.unitPrice * item.quantity).toFixed(2)}
                </p>
              </div>
            ))}

            {items.length === 0 && (
              <p className="ocm__empty">No hay ítems en el pedido.</p>
            )}
          </div>
        </section>

        {/* Total */}
        <section className="ocm__total">
          <p className="ocm__total-label">Subtotal</p>
          <p className="ocm__total-value">${total.toFixed(2)}</p>
          <p className="ocm__total-note">*Se calculará el mejor descuento disponible al confirmar</p>
        </section>

        {/* Actions */}
        <footer className="ocm__actions">
          <button
            onClick={onClose}
            disabled={loading}
            className="ocm-btn ocm-btn--ghost"
          >
            Cancelar
          </button>

          <button
            onClick={onConfirm}
            disabled={loading || items.length === 0}
            className="ocm-btn ocm-btn--primary"
          >
            {loading ? (
              <>
                <Loader2 size={16} className="ocm__spinner" />
                Procesando…
              </>
            ) : (
              "Confirmar Pedido"
            )}
          </button>
        </footer>
      </div>
    </div>
  );
};
