import { Activity, X as CloseIcon, ListOrdered, LogOut, Menu as MenuIcon, User } from "lucide-react";
import { useEffect, useState } from "react";
import { useLocation } from "wouter";

import { CartSidebar } from "@/components/Cart/CartSidebar";
import { Footer } from "@/components/Footer/footer";
import { MenuFilters, MenuFiltersState } from "@/components/MenuFilters/MenuFilters";
import { MenuGrid, MenuItem } from "@/components/MenuGrid/MenuGrid";
import { PromotionsCarousel } from "@/components/PromotionsCarousel/PromotionsCarousel";
import { BASE_API_URL } from "@/config/app-query-client";
import { useCart } from "@/services/CartContext";
import { useToken } from "@/services/TokenContext";

import "./menu-page.css";

export const MenuPage = () => {
  const [, setLocation] = useLocation();
  const [tokenState, setToken] = useToken();
  const { cart } = useCart();
  const [actionsOpen, setActionsOpen] = useState(false);
  const [items, setItems] = useState<MenuItem[]>([]);
  const [filteredItems, setFilteredItems] = useState<MenuItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isCartOpen, setIsCartOpen] = useState(false);
  const [filters, setFilters] = useState<MenuFiltersState>({
    category: "all",
    searchQuery: "",
  });

  useEffect(() => {
    if (tokenState.state === "LOGGED_OUT") {
      setLocation("/login");
    }
  }, [tokenState, setLocation]);

  useEffect(() => {
    const loadMenu = async () => {
      try {
        setLoading(true);
        setError(null);

        const token = tokenState.state === "LOGGED_IN" ? tokenState.tokens.accessToken : null;

        const response = await fetch(`${BASE_API_URL}/menu`, {
          method: "GET",
          headers: {
            "Content-Type": "application/json",
            ...(token && { Authorization: `Bearer ${token}` }),
          },
        });

        if (!response.ok) {
          throw new Error(`Error ${response.status}: No se pudo cargar el menú`);
        }

        const data = await response.json();

        type RawMenuItem = {
          id: number;
          name: string;
          description?: string;
          price: number;
          imageUrl?: string;
          category?: string;
          type?: string;
          regularPrice?: number;
          discount?: number;
          isAvailable?: boolean;
          stock?: number;
        };

        const transformedItems: MenuItem[] = (data as RawMenuItem[]).map((item) => {
          return {
            id: item.id,
            name: item.name,
            description: item.description || "",
            price: item.price,
            imageUrl: item.imageUrl ?? "",
            category: item.category || "general",
            isCombo: item.type === "COMBO" || false,
            originalPrice: item.regularPrice || item.price, // Usar regularPrice del backend
            discount: item.discount || 0,
            available: item.isAvailable !== false,
            stock: item.stock ?? 0,
          };
        });

        setItems(transformedItems);
        setFilteredItems(transformedItems);
      } catch (err) {
        const message = err instanceof Error ? err.message : "Error desconocido";
        setError(message);
        console.error("Error loading menu:", err);
      } finally {
        setLoading(false);
      }
    };

    if (tokenState.state === "LOGGED_IN") {
      loadMenu();
    }
    // Abrir carrito por query param
    try {
      const params = new URLSearchParams(window.location.search);
      if (params.get("openCart") === "1") setIsCartOpen(true);
    } catch (err) {
      // ignore URL parsing errors
      console.warn("Error parsing URL params", err);
    }
  }, [tokenState]);

  useEffect(() => {
    let result = items;

    if (filters.category !== "all") {
      result = result.filter((item) => {
        return item.category === filters.category;
      });
    }

    if (filters.searchQuery) {
      const query = filters.searchQuery.toLowerCase().trim();
      // Normalizar acentos para la búsqueda
      const normalizeText = (text: string) => {
        return text
          .toLowerCase()
          .normalize("NFD")
          .replace(/[\u0300-\u036f]/g, "");
      };
      const normalizedQuery = normalizeText(query);

      result = result.filter(
        (item) =>
          normalizeText(item.name).includes(normalizedQuery) ||
          normalizeText(item.description).includes(normalizedQuery),
      );
    }

    setFilteredItems(result);
  }, [items, filters]);

  return (
    <div className="page page--compact">
      {/* Actions: desktop = stack fijo; mobile = hamburguesa */}
      <div className={`actions ${actionsOpen ? "is-open" : ""}`}>
        {/* Toggle solo visible en mobile vía CSS */}
        <button
          className="actions__toggle"
          aria-label={actionsOpen ? "Cerrar menú" : "Abrir menú"}
          onClick={() => setActionsOpen((v) => !v)}
        >
          {actionsOpen ? <CloseIcon size={16} /> : <MenuIcon size={16} />}
        </button>

        {/* Menú (en desktop siempre visible; en mobile solo si is-open) */}
        <div className="actions__menu">
          <button
            className="action"
            title="Mis Pedidos"
            onClick={() => {
              setActionsOpen(false);
              setLocation("/orders");
            }}
          >
            <ListOrdered size={16} /> <span>Mis Pedidos</span>
          </button>

          <button
            className="action"
            title="Mi Perfil"
            onClick={() => {
              setActionsOpen(false);
              setLocation("/profile");
            }}
          >
            <User size={16} /> <span>Mi Perfil</span>
          </button>

          <button
            className="action"
            title="Tracking en Tiempo Real"
            onClick={() => {
              setActionsOpen(false);
              setLocation("/dashboard");
            }}
          >
            <Activity size={16} /> <span>Tracking en Vivo</span>
          </button>

          <button
            className="action action--danger"
            title="Cerrar Sesión"
            onClick={() => {
              const ok = window.confirm("¿Estás seguro que deseas cerrar sesión?");
              if (ok) {
                setToken({ state: "LOGGED_OUT" });
                setLocation("/login");
              }
              setActionsOpen(false);
            }}
          >
            <LogOut size={16} /> <span>Cerrar Sesión</span>
          </button>
        </div>
      </div>

      <div className="hero">
        <div className="container">
          <div className="brand">
            <div className="brand__logo" aria-hidden="true">
              🎓
            </div>
            <div className="brand__text">
              <h1 className="brand__title">Comedor Universitario</h1>
              <p className="brand__subtitle">Menú del día — rico, rápido y accesible</p>
            </div>
          </div>
        </div>
      </div>

      <main className="container">
        <div className="filters-wrap">
          <MenuFilters onFiltersChange={setFilters} itemsFound={filteredItems.length} />
        </div>

        <PromotionsCarousel />

        <MenuGrid items={filteredItems} loading={loading} error={error || undefined} />

        <CartSidebar isOpen={isCartOpen} onClose={() => setIsCartOpen(false)} />

        {cart.itemCount > 0 && !isCartOpen && (
          <div className="cart-fabWrap">
            <button className="cart-fab" onClick={() => setIsCartOpen(true)}>
              <span>Ver Carrito</span>
              <span className="cart-fab__badge">{cart.itemCount}</span>
            </button>
          </div>
        )}
      </main>

      <Footer />
    </div>
  );
};
