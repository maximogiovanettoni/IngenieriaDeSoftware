import React, { useState, useEffect } from 'react';
import { CartContext, CartState, CartItem } from './CartContextTypes';

export type { CartItem, CartState } from './CartContextTypes';
// Export useCart hook from a separate file to allow fast refresh for CartProvider component
// eslint-disable-next-line react-refresh/only-export-components
export { useCart } from './useCart';

const CART_STORAGE_KEY = 'sistema_comedores_cart';

export const CartProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [cart, setCart] = useState<CartState>({
    items: [],
    totalPrice: 0,
    itemCount: 0,
  });

  // Cargar carrito del localStorage al montar
  useEffect(() => {
    const storedCart = localStorage.getItem(CART_STORAGE_KEY);
    if (storedCart) {
      try {
        const parsedCart = JSON.parse(storedCart);
        setCart(parsedCart);
      } catch (error) {
        console.error("Error loading cart from localStorage:", error);
      }
    }
  }, []);

  // Guardar carrito en localStorage cuando cambie
  useEffect(() => {
    localStorage.setItem(CART_STORAGE_KEY, JSON.stringify(cart));
  }, [cart]);

  // Calcular total y cantidad
  const calculateTotals = (items: CartItem[]) => {
    const total = items.reduce((sum, item) => sum + item.unitPrice * item.quantity, 0);
    const count = items.reduce((sum, item) => sum + item.quantity, 0);
    return { total, count };
  };

  const addItem = (item: Omit<CartItem, "quantity">) => {
    setCart((prevCart) => {
      const existingItem = prevCart.items.find(
        (cartItem) => cartItem.itemType === item.itemType && cartItem.itemId === item.itemId,
      );

      let newItems: CartItem[];
      if (existingItem) {
        // Si existe, incrementar cantidad
        newItems = prevCart.items.map((cartItem) =>
          cartItem.itemType === item.itemType && cartItem.itemId === item.itemId
            ? { ...cartItem, quantity: cartItem.quantity + 1 }
            : cartItem,
        );
      } else {
        // Si no existe, agregarlo con cantidad 1
        newItems = [...prevCart.items, { ...item, quantity: 1 }];
      }

      const { total, count } = calculateTotals(newItems);
      return {
        items: newItems,
        totalPrice: total,
        itemCount: count,
      };
    });
  };

  const removeItem = (itemType: "PRODUCT" | "COMBO", itemId: number) => {
    setCart((prevCart) => {
      const newItems = prevCart.items.filter((item) => !(item.itemType === itemType && item.itemId === itemId));

      const { total, count } = calculateTotals(newItems);
      return {
        items: newItems,
        totalPrice: total,
        itemCount: count,
      };
    });
  };

  const updateQuantity = (itemType: "PRODUCT" | "COMBO", itemId: number, newQuantity: number) => {
    if (newQuantity <= 0) {
      removeItem(itemType, itemId);
      return;
    }

    setCart((prevCart) => {
      const newItems = prevCart.items.map((item) =>
        item.itemType === itemType && item.itemId === itemId ? { ...item, quantity: newQuantity } : item,
      );

      const { total, count } = calculateTotals(newItems);
      return {
        items: newItems,
        totalPrice: total,
        itemCount: count,
      };
    });
  };

  const clearCart = () => {
    setCart({
      items: [],
      totalPrice: 0,
      itemCount: 0,
    });
  };

  const getTotal = () => cart.totalPrice;
  const getItemCount = () => cart.itemCount;

  return (
    <CartContext.Provider
      value={{
        cart,
        addItem,
        removeItem,
        updateQuantity,
        clearCart,
        getTotal,
        getItemCount,
      }}
    >
      {children}
    </CartContext.Provider>
  );
};
