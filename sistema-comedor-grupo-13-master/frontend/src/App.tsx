import { QueryClientProvider } from "@tanstack/react-query";

import { Navigation } from "@/Navigation";
import { appQueryClient } from "@/config/app-query-client";
import { TokenProvider } from "@/services/TokenContext";
import { CartProvider } from "@/services/CartContext";
import { Toaster } from "sonner";

export function App() {
  return (
    <QueryClientProvider client={appQueryClient}>
      <TokenProvider>
        <CartProvider>
          <Toaster theme="dark" richColors position="top-right" />
          <Navigation />
        </CartProvider>
      </TokenProvider>
    </QueryClientProvider>
  );
}
