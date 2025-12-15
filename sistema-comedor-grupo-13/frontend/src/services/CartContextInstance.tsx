import { createContext } from "react";

import { CartContextType } from "@/types/CartType";

export const CartContext = createContext<CartContextType | undefined>(undefined);
