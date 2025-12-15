import { Minus, Plus, ShoppingCart } from "lucide-react";

import { CartItem, useCart } from "@/services/CartContext";

import "./menu-grid.css";

export interface MenuItem {
  id: number;
  name: string;
  description: string;
  price: number;
  imageUrl: string;
  category: string;
  isCombo: boolean;
  originalPrice: number;
  discount: number;
  available: boolean;
  stock: number;
}

interface MenuGridProps {
  items: MenuItem[];
  loading?: boolean;
  error?: string;
}

const SkeletonCard = () => (
  <div className="mg-card mg-card--skeleton">
    <div className="mg-skel mg-skel__img" />
    <div className="mg-card__body">
      <div className="mg-skel mg-skel__line w75" />
      <div className="mg-skel mg-skel__line" />
      <div className="mg-skel mg-skel__line w85" />
      <div className="mg-skel mg-skel__line w50 tall" />
    </div>
  </div>
);

interface MenuCardProps {
  item: MenuItem;
}

const MenuCard: React.FC<MenuCardProps> = ({ item }) => {
  const { cart, addItem, removeItem, updateQuantity } = useCart();

  const cartItem = cart.items.find(
    (c: CartItem) => c.itemType === (item.isCombo ? "COMBO" : "PRODUCT") && c.itemId === item.id,
  );

  const handleAddToCart = () => {
    addItem({
      itemType: item.isCombo ? "COMBO" : "PRODUCT",
      itemId: item.id,
      itemName: item.name,
      unitPrice: item.price,
      imageUrl: item.imageUrl,
      category: item.category,
    });
  };

  const handleRemove = () => removeItem(item.isCombo ? "COMBO" : "PRODUCT", item.id);
  const handleIncrement = () =>
    cartItem && updateQuantity(item.isCombo ? "COMBO" : "PRODUCT", item.id, cartItem.quantity + 1);
  const handleDecrement = () =>
    cartItem && updateQuantity(item.isCombo ? "COMBO" : "PRODUCT", item.id, cartItem.quantity - 1);

  const hasReachedStock = cartItem && item.stock !== undefined && cartItem.quantity >= item.stock;
  const isOutOfStock = !item.available || item.stock === 0;

  // Solo combos tienen descuento
  const showDiscount = !!item.isCombo && (item.discount ?? 0) > 0;
  const showPriceWithDiscount = showDiscount && (item.originalPrice ?? 0) > 0;

  return (
    <div className="mg-card">
      {/* Imagen */}
      <div className="mg-card__media">
        {item.imageUrl ? (
          <img src={item.imageUrl} alt={item.name} className="mg-card__img" />
        ) : (
          <div className="mg-card__placeholder">
            <ShoppingCart size={46} />
          </div>
        )}

        <div className="mg-media__overlay" />

        <div className="mg-badges">
          {item.isCombo && <span className="mg-badge mg-badge--combo">COMBO</span>}
          {item.available === false && <span className="mg-badge mg-badge--danger">NO DISPONIBLE</span>}
          {item.available !== false && !item.isCombo && <span className="mg-badge mg-badge--ok">DISPONIBLE</span>}
          {showDiscount && (
            <span className="mg-badge mg-badge--warn">¡Ahorrás ${Number(item.discount).toLocaleString("es-AR")}!</span>
          )}
        </div>
      </div>

      {/* Contenido */}
      <div className="mg-card__body">
        <h3 className="mg-title" title={item.name}>
          {item.name}
        </h3>
        <p className="mg-desc" title={item.description}>
          {item.description}
        </p>

        <div className="mg-price">
          {showPriceWithDiscount ? (
            <>
              <span className="mg-price__old">${Number(item.originalPrice).toLocaleString("es-AR")}</span>
              <span className="mg-price__now">${Number(item.price).toLocaleString("es-AR")}</span>
            </>
          ) : (
            <span className="mg-price__now">${Number(item.price).toLocaleString("es-AR")}</span>
          )}
        </div>

        {/* Acciones */}
        {cartItem ? (
          <div className="mg-qty">
            <button className="qty-btn" onClick={handleDecrement} title="Disminuir">
              <Minus size={16} />
            </button>
            <span className="qty-value">{cartItem.quantity}</span>
            <button
              className="qty-btn"
              onClick={handleIncrement}
              disabled={hasReachedStock || isOutOfStock} // ✅ FIXED: Added isOutOfStock check
              title={isOutOfStock ? "No disponible" : hasReachedStock ? "Stock máximo alcanzado" : "Aumentar"}
            >
              <Plus size={16} />
            </button>
            <button className="mg-remove" onClick={handleRemove} title="Quitar">
              ✕
            </button>
          </div>
        ) : (
          <button
            className="btn btn-primary"
            onClick={handleAddToCart}
            disabled={isOutOfStock} // ✅ SIMPLIFIED: Using isOutOfStock
            title={!item.available ? "No disponible" : item.stock === 0 ? "Sin stock" : "Agregar al pedido"}
          >
            <ShoppingCart size={16} />
            Agregar al Pedido
          </button>
        )}
      </div>
    </div>
  );
};

export const MenuGrid: React.FC<MenuGridProps> = ({ items, loading = false, error }) => {
  if (error) {
    return <div className="mg-feedback error">Error al cargar los productos: {error}</div>;
  }

  if (loading) {
    return (
      <div className="mg-grid">
        {Array.from({ length: 8 }).map((_, i) => (
          <SkeletonCard key={i} />
        ))}
      </div>
    );
  }

  if (items.length === 0) {
    return (
      <div className="mg-feedback">
        <ShoppingCart size={46} />
        <p className="title">No se encontraron productos</p>
        <p className="hint">Intenta cambiando los filtros o la búsqueda</p>
      </div>
    );
  }

  return (
    <div className="mg-grid fade-in">
      {items.map((item) => (
        <MenuCard key={`${item.isCombo ? "combo" : "product"}-${item.id}`} item={item} />
      ))}
    </div>
  );
};
