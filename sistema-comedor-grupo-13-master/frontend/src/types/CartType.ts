export interface CartItem {
  itemType: "PRODUCT" | "COMBO";
  itemId: number;
  itemName: string;
  quantity: number;
  unitPrice: number;
  imageUrl?: string;
}

export interface CartState {
  items: CartItem[];
  totalPrice: number;
  itemCount: number;
}

export interface CartContextType {
  cart: CartState;
  addItem: (item: Omit<CartItem, "quantity">) => void;
  removeItem: (itemType: "PRODUCT" | "COMBO", itemId: number) => void;
  updateQuantity: (itemType: "PRODUCT" | "COMBO", itemId: number, newQuantity: number) => void;
  clearCart: () => void;
  getTotal: () => number;
  getItemCount: () => number;
}

export const CART_STORAGE_KEY = "sistema_comedores_cart";
