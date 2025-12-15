import { createContext } from 'react';

export interface CartItem {
  itemType: 'PRODUCT' | 'COMBO';
  itemId: number;
  itemName: string;
  quantity: number;
  unitPrice: number;
  imageUrl?: string;
  category?: string;
}

export interface CartState {
  items: CartItem[];
  totalPrice: number;
  itemCount: number;
}

interface CartContextType {
  cart: CartState;
  addItem: (item: Omit<CartItem, 'quantity'>) => void;
  removeItem: (itemType: 'PRODUCT' | 'COMBO', itemId: number) => void;
  updateQuantity: (itemType: 'PRODUCT' | 'COMBO', itemId: number, newQuantity: number) => void;
  clearCart: () => void;
  getTotal: () => number;
  getItemCount: () => number;
}

export const CartContext = createContext<CartContextType | undefined>(undefined);
