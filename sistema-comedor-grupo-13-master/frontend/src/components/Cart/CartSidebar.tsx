import { Minus, Plus, ShoppingCart, Trash2, X } from "lucide-react";
import { useEffect, useState } from "react";
import { useLocation } from "wouter";

import { DiscountBanner } from "@/components/DiscountBanner/DiscountBanner";
import { OrderErrorModal } from "@/components/Orders/OrderErrorModal";
import { OrderSuccessModal } from "@/components/Orders/OrderSuccessModal";
import { CalculatePromotionInfo, CreateOrderRequest, OrderItem, OrderResponse, orderAPI } from "@/services/OrderAPI";
import { productAPI } from "@/services/ProductServices";
import { useToken } from "@/services/TokenContext";
import { useCart } from "@/services/useCart";

import "./cart-sidebar.css";

interface CartSidebarProps {
  isOpen: boolean;
  onClose: () => void;
}

export const CartSidebar: React.FC<CartSidebarProps> = ({ isOpen, onClose }) => {
  const { cart, removeItem, updateQuantity, clearCart } = useCart();
  const [tokenState] = useToken();
  const [, setLocation] = useLocation();

  const [showSuccess, setShowSuccess] = useState(false);
  const [showError, setShowError] = useState(false);
  const [loading, setLoading] = useState(false);
  const [orderNumber, setOrderNumber] = useState("");
  const [orderId, setOrderId] = useState<number | null>(null);
  const [errorMessage, setErrorMessage] = useState("");
  const [errorDetails, setErrorDetails] = useState<
    | {
        productName?: string;
        requested?: number;
        available?: number;
      }
    | undefined
  >(undefined);
  const [orderSubtotal, setOrderSubtotal] = useState<number | undefined>();
  const [orderDiscount, setOrderDiscount] = useState<number | undefined>();
  const [orderTotal, setOrderTotal] = useState<number | undefined>();
  const [orderItems, setOrderItems] = useState<
    Array<{ productName: string; quantity: number; unitPrice: number }> | undefined
  >();

  // Promotion calculation states
  const [promotion, setPromotion] = useState<CalculatePromotionInfo | null>(null);

  // Cargar productos disponibles
  useEffect(() => {
    const loadProducts = async () => {
      try {
        const token = tokenState.state === "LOGGED_IN" ? tokenState.tokens.accessToken : undefined;
        const products = await productAPI.getProducts(token);
        console.log("Loaded products from API:", products.length);
        console.log(
          "Products details:",
          products.map((p) => ({ id: p.id, name: p.name, category: p.category, active: p.active })),
        );
      } catch (error) {
        console.error("Error loading products:", error);
      }
    };

    loadProducts();
  }, [tokenState]);

  // Calculate promotions when cart items change
  useEffect(() => {
    if (cart.items.length === 0) {
      setPromotion(null);
      return;
    }

    const calculatePromotion = async () => {
      try {
        const token = tokenState.state === "LOGGED_IN" ? tokenState.tokens.accessToken : undefined;

        const items = cart.items.map((item) => ({
          productId: item.itemId,
          quantity: item.quantity,
        }));

        const promoData = await orderAPI.calculatePromotionForCart(items, token);
        setPromotion(promoData);
      } catch (error) {
        console.error("Error calculating promotion:", error);
        setPromotion(null);
      }
    };

    calculatePromotion();
  }, [cart.items, tokenState]);

  const handleRemove = (itemType: "PRODUCT" | "COMBO", itemId: number) => {
    removeItem(itemType, itemId);
  };

  const handleUpdateQuantity = (itemType: "PRODUCT" | "COMBO", itemId: number, newQuantity: number) => {
    updateQuantity(itemType, itemId, newQuantity);
  };

  const handleConfirmOrder = async () => {
    setLoading(true);
    try {
      const token = tokenState.state === "LOGGED_IN" ? tokenState.tokens.accessToken : undefined;

      const items = cart.items.map((item) => ({
        productId: item.itemId,
        productName: item.itemName,
        quantity: item.quantity,
        unitPrice: item.unitPrice,
        subtotal: item.unitPrice * item.quantity,
      }));

      const request: CreateOrderRequest = { items };
      const response = await orderAPI.createOrder(request, token);

      // Backend returns { success, message, order: OrderResponse }
      const orderResponseData = "order" in response && response.order ? response.order : (response as OrderResponse);
      setOrderNumber(String(orderResponseData.orderNumber || ""));
      setOrderId(orderResponseData.id);
      setOrderSubtotal(orderResponseData.subtotal ? parseFloat(String(orderResponseData.subtotal)) : undefined);
      setOrderDiscount(
        orderResponseData.discountAmount ? parseFloat(String(orderResponseData.discountAmount)) : undefined,
      );
      setOrderTotal(orderResponseData.totalAmount ? parseFloat(String(orderResponseData.totalAmount)) : undefined);

      // Guardar items incluyendo los gratuitos
      if (orderResponseData.items) {
        setOrderItems(
          orderResponseData.items.map((item: OrderItem) => ({
            productName: item.productName,
            quantity: item.quantity,
            unitPrice: typeof item.unitPrice === "string" ? parseFloat(item.unitPrice) : item.unitPrice,
          })),
        );
      }

      clearCart();
      setShowSuccess(true);
    } catch (err: unknown) {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const e = err as any;
      if (e?.status === 409) {
        setErrorMessage(e.message || "Stock insuficiente");
        setErrorDetails(e.details);
      } else {
        setErrorMessage(e.message || "Error al crear el pedido");
      }
      setShowError(true);
    } finally {
      setLoading(false);
    }
  };

  const handleNavigateToOrder = () => {
    setShowSuccess(false);
    if (orderId) setLocation(`/orders/${orderId}`);
  };
  const handleNavigateToMenu = () => {
    setShowSuccess(false);
    setLocation("/menu");
  };

  return (
    <>
      {/* Overlay */}
      <div className={`cart-overlay ${isOpen ? "is-open" : ""}`} onClick={onClose} />

      {/* Sidebar */}
      <aside className={`cart-sidebar ${isOpen ? "is-open" : ""}`}>
        {/* Header */}
        <header className="cart-head">
          <div className="cart-head__title">
            <ShoppingCart size={20} />
            <h2>Mi Pedido</h2>
            {cart.itemCount > 0 && <span className="cart-badge">{cart.itemCount}</span>}
          </div>
          <button className="icon-btn" title="Cerrar" onClick={onClose}>
            <X size={22} />
          </button>
        </header>

        {/* Content Wrapper - Promotions + Items */}
        {cart.items.length > 0 ? (
          <div className="cart-content-wrapper">
            {/* Promotions Banner - Always at top when items exist */}
            {promotion?.appliedPromotions && promotion.appliedPromotions.length > 0 && (
              <div className="discount-banner-wrapper">
                <DiscountBanner appliedPromotions={promotion.appliedPromotions} />
              </div>
            )}

            {/* Product Items */}
            <div className="cart-list">
              {cart.items.map((item) => (
                <div key={`${item.itemType}-${item.itemId}`} className="cart-item">
                  {item.imageUrl && <img src={item.imageUrl} alt={item.itemName} className="cart-item__thumb" />}

                  <div className="cart-item__body">
                    <div className="cart-item__info">
                      <p className="cart-item__name">{item.itemName}</p>
                      <p className="cart-item__unit">${item.unitPrice.toLocaleString("es-AR")} c/u</p>
                    </div>

                    <div className="cart-item__qty">
                      <button
                        onClick={() => handleUpdateQuantity(item.itemType, item.itemId, item.quantity - 1)}
                        className="qty-btn"
                        title="Disminuir"
                      >
                        <Minus size={12} />
                      </button>
                      <span className="qty-value">{item.quantity}</span>
                      <button
                        onClick={() => handleUpdateQuantity(item.itemType, item.itemId, item.quantity + 1)}
                        className="qty-btn"
                        title="Aumentar"
                      >
                        <Plus size={12} />
                      </button>
                    </div>
                  </div>

                  <button
                    className="cart-item__remove"
                    onClick={() => handleRemove(item.itemType, item.itemId)}
                    title="Eliminar"
                  >
                    <Trash2 size={15} />
                  </button>
                </div>
              ))}
            </div>
          </div>
        ) : (
          <div className="cart-content-wrapper">
            <div className="cart-empty">
              <ShoppingCart size={46} />
              <p className="cart-empty__title">Tu carrito está vacío</p>
              <p className="cart-empty__hint">Agrega productos para comenzar tu pedido</p>
              <button
                className="link-btn"
                onClick={() => {
                  onClose();
                  setLocation("/menu");
                }}
              >
                Ir al Menú
              </button>
            </div>
          </div>
        )}
        {/* Footer */}
        {cart.items.length > 0 && (
          <footer className="cart-foot">
            <div className="cart-row">
              <span className="muted">Subtotal</span>
              <span className="value">
                ${promotion?.subtotal.toLocaleString("es-AR") ?? cart.totalPrice.toLocaleString("es-AR")}
              </span>
            </div>

            {promotion?.hasDiscount && (
              <div className="cart-row">
                <span className="muted">Descuento</span>
                <span className="value value--discount">-${promotion.discountAmount.toLocaleString("es-AR")}</span>
              </div>
            )}

            <div className="cart-row cart-row--total">
              <span>Total</span>
              <span className="value value--accent">
                ${promotion?.totalAmount.toLocaleString("es-AR") ?? cart.totalPrice.toLocaleString("es-AR")}
              </span>
            </div>

            <button className="btn btn-primary" onClick={handleConfirmOrder} disabled={loading}>
              <ShoppingCart size={18} />
              Ir a pagar
            </button>

            <button className="btn btn-ghost" onClick={clearCart}>
              Vaciar Carrito
            </button>
          </footer>
        )}

        {/* Modales */}
        <OrderSuccessModal
          isOpen={showSuccess}
          orderNumber={orderNumber}
          onNavigateToOrder={handleNavigateToOrder}
          onNavigateToMenu={handleNavigateToMenu}
          subtotal={orderSubtotal}
          discount={orderDiscount}
          total={orderTotal}
          items={orderItems}
        />
        <OrderErrorModal
          isOpen={showError}
          onClose={() => setShowError(false)}
          onClearCart={clearCart}
          errorMessage={errorMessage}
          errorDetails={errorDetails}
        />
      </aside>
    </>
  );
};
