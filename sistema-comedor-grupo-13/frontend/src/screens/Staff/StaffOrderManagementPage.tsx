import { ArrowLeft, RefreshCw } from "lucide-react";
import { useCallback, useEffect, useState } from "react";
import { useLocation } from "wouter";

import { OrderBoard, StaffOrder } from "@/components/OrderBoard/OrderBoard";
import { OrderDetailModal } from "@/components/OrderBoard/OrderDetailModal";
import { useAutoRefresh } from "@/hooks/useOrderManagement";
import { useToken } from "@/services/TokenContext";

const BASE_API_URL = "/api";

interface ApiOrder {
  id: number;
  orderNumber: string;
  status: string;
  totalAmount: number;
  discountAmount?: number;
  appliedPromotions?: Array<{
    name: string;
    type: string;
    discountAmount: number;
  }>;
  items: Array<{
    productId: number;
    productName: string;
    quantity: number;
    unitPrice: number;
    subtotal: number;
  }>;
  createdAt: string;
  updatedAt?: string;
  userId?: number;
}

export const StaffOrderManagementPage: React.FC = () => {
  const [, setLocation] = useLocation();
  const [tokenState] = useToken();
  const [orders, setOrders] = useState<StaffOrder[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedOrder, setSelectedOrder] = useState<StaffOrder | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [autoRefreshEnabled, setAutoRefreshEnabled] = useState(true);
  const [hiddenOrderIds, setHiddenOrderIds] = useState<Set<number>>(new Set());
  const [showHidden, setShowHidden] = useState(false);

  useEffect(() => {
    if (tokenState.state !== "LOGGED_IN") {
      setLocation("/login");
      return;
    }

    const role = (tokenState.tokens as { role?: string }).role;
    console.log("StaffOrderManagementPage - Token Role:", role);
    if (role !== "STAFF" && role !== "ADMIN") {
      console.log("StaffOrderManagementPage - Invalid role, redirecting to login");
      setLocation("/login");
    }
  }, [tokenState, setLocation]);

  // Load hidden orders from localStorage on mount
  useEffect(() => {
    const stored = localStorage.getItem("hiddenStaffOrderIds");
    if (stored) {
      try {
        const ids = JSON.parse(stored) as number[];
        setHiddenOrderIds(new Set(ids));
      } catch (e) {
        console.error("Error parsing stored hidden orders:", e);
      }
    }
  }, []);

  // Envolver fetchOrders en useCallback para evitar re-renders innecesarios
  const fetchOrders = useCallback(async () => {
    if (tokenState.state !== "LOGGED_IN") return;

    try {
      const token = tokenState.tokens.accessToken;
      const response = await fetch(`${BASE_API_URL}/orders`, {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
      });

      if (response.status === 401) {
        throw new Error("Sesión expirada. Por favor, vuelva a iniciar sesión.");
      }

      if (!response.ok) {
        throw new Error(`Error: ${response.statusText}`);
      }

      const data = (await response.json()) as ApiOrder[];

      // Limpiar localStorage: remover IDs de órdenes que ya no existen
      const currentOrderIds = new Set(data.map((o) => o.id));
      const stored = localStorage.getItem("hiddenStaffOrderIds");
      if (stored) {
        try {
          const storedIds = JSON.parse(stored) as number[];
          const validIds = storedIds.filter((id) => currentOrderIds.has(id));
          if (validIds.length !== storedIds.length) {
            localStorage.setItem("hiddenStaffOrderIds", JSON.stringify(validIds));
            setHiddenOrderIds(new Set(validIds));
          }
        } catch (e) {
          console.error("Error cleaning hidden order IDs:", e);
        }
      }

      // Mapear órdenes sin hacer llamadas adicionales para usuarios
      const enrichedOrders = data.map((order) => {
        const mappedOrder: StaffOrder = {
          id: order.id,
          orderNumber: order.orderNumber,
          status: order.status as
            | "PENDING"
            | "CONFIRMED"
            | "PREPARING"
            | "READY"
            | "COMPLETED"
            | "REJECTED"
            | "CANCELLED",
          totalAmount: order.totalAmount,
          discountAmount: order.discountAmount,
          appliedPromotions: order.appliedPromotions,
          items: order.items,
          createdAt: order.createdAt,
          updatedAt: order.updatedAt,
        };
        return mappedOrder;
      });

      setOrders(enrichedOrders);
      setError(null);
    } catch (err) {
      const message = err instanceof Error ? err.message : "Error desconocido";
      setError(message);
      console.error("Error loading orders:", err);
      setAutoRefreshEnabled(false);
    } finally {
      setLoading(false);
    }
  }, [tokenState]);

  const { isRefreshing, hasChanges, isDisabled, resetAndRetry } = useAutoRefresh({
    enabled: false,
    interval: 300000,
    onRefresh: fetchOrders,
    pauseWhenModalOpen: true,
    isModalOpen,
  });

  useEffect(() => {
    let isMounted = true;

    if (tokenState.state === "LOGGED_IN" && isMounted) {
      void fetchOrders();
    }

    return () => {
      isMounted = false;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleOrderClick = (order: StaffOrder) => {
    setSelectedOrder(order);
    setIsModalOpen(true);
  };

  const handleStatusChange = async (orderId: number, newStatus: string) => {
    if (tokenState.state !== "LOGGED_IN") return;

    try {
      const token = tokenState.tokens.accessToken;
      const currentOrder = orders.find((o) => o.id === orderId);

      if (!currentOrder) {
        throw new Error("Orden no encontrada");
      }

      // Mapear el nuevo estado al endpoint correcto
      let endpoint = `${BASE_API_URL}/orders/${orderId}/move-forward`;
      let method = "PUT";
      let body: string | undefined;

      if (newStatus === "REJECTED") {
        endpoint = `${BASE_API_URL}/orders/${orderId}/reject`;
        method = "PUT";
        body = JSON.stringify({ reason: "Rechazado por staff" });
      }

      const response = await fetch(endpoint, {
        method: method,
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        ...(body && { body }),
      });

      if (response.status === 401) {
        throw new Error("Sesión expirada. Por favor, vuelva a iniciar sesión.");
      }

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({ message: response.statusText }));
        throw new Error(errorData.message || `Error: ${response.statusText}`);
      }

      const data = await response.json();
      const updatedOrder = data.order || data;

      // Actualizar la orden en el estado local
      setOrders((prevOrders) =>
        prevOrders.map((order) =>
          order.id === orderId
            ? {
                ...order,
                status: updatedOrder.status as
                  | "PENDING"
                  | "CONFIRMED"
                  | "PREPARING"
                  | "READY"
                  | "COMPLETED"
                  | "REJECTED"
                  | "CANCELLED",
                updatedAt: updatedOrder.updatedAt || new Date().toISOString(),
              }
            : order,
        ),
      );

      if (selectedOrder?.id === orderId) {
        setSelectedOrder((prev) =>
          prev
            ? {
                ...prev,
                status: updatedOrder.status as
                  | "PENDING"
                  | "CONFIRMED"
                  | "PREPARING"
                  | "READY"
                  | "COMPLETED"
                  | "REJECTED"
                  | "CANCELLED",
                updatedAt: updatedOrder.updatedAt || new Date().toISOString(),
              }
            : null,
        );
      }
    } catch (err) {
      alert(`Error al actualizar el estado: ${err instanceof Error ? err.message : "Error desconocido"}`);
      console.error("Error updating order status:", err);
    }
  };

  const handleMoveBackward = async (orderId: number) => {
    if (tokenState.state !== "LOGGED_IN") return;

    try {
      const token = tokenState.tokens.accessToken;
      const currentOrder = orders.find((o) => o.id === orderId);

      if (!currentOrder) {
        throw new Error("Orden no encontrada");
      }

      const endpoint = `${BASE_API_URL}/orders/${orderId}/move-backward`;
      const response = await fetch(endpoint, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
      });

      if (response.status === 401) {
        throw new Error("Sesión expirada. Por favor, vuelva a iniciar sesión.");
      }

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({ message: response.statusText }));
        throw new Error(errorData.message || `Error: ${response.statusText}`);
      }

      const data = await response.json();
      const updatedOrder = data.order || data;

      // Actualizar la orden en el estado local
      setOrders((prevOrders) =>
        prevOrders.map((order) =>
          order.id === orderId
            ? {
                ...order,
                status: updatedOrder.status as
                  | "PENDING"
                  | "CONFIRMED"
                  | "PREPARING"
                  | "READY"
                  | "COMPLETED"
                  | "REJECTED"
                  | "CANCELLED",
                updatedAt: updatedOrder.updatedAt || new Date().toISOString(),
              }
            : order,
        ),
      );

      if (selectedOrder?.id === orderId) {
        setSelectedOrder((prev) =>
          prev
            ? {
                ...prev,
                status: updatedOrder.status as
                  | "PENDING"
                  | "CONFIRMED"
                  | "PREPARING"
                  | "READY"
                  | "COMPLETED"
                  | "REJECTED"
                  | "CANCELLED",
                updatedAt: updatedOrder.updatedAt || new Date().toISOString(),
              }
            : null,
        );
      }
    } catch (err) {
      alert(`Error al retroceder el estado: ${err instanceof Error ? err.message : "Error desconocido"}`);
      console.error("Error moving order backward:", err);
    }
  };

  const handleManualRefresh = async () => {
    await fetchOrders();
  };

  const toggleHideOrder = (orderId: number) => {
    const newHidden = new Set(hiddenOrderIds);
    if (newHidden.has(orderId)) {
      newHidden.delete(orderId);
    } else {
      newHidden.add(orderId);
    }
    setHiddenOrderIds(newHidden);
    localStorage.setItem("hiddenStaffOrderIds", JSON.stringify(Array.from(newHidden)));
  };

  if (tokenState.state !== "LOGGED_IN") {
    return null;
  }

  return (
    <div style={{ minHeight: "100vh", backgroundColor: "var(--bg)", color: "white" }}>
      {/* Header */}
      <div
        style={{
          backgroundColor: "var(--panel)",
          borderBottom: "1px solid var(--border)",
          padding: "1.5rem",
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
        }}
      >
        <div style={{ display: "flex", alignItems: "center", gap: "1.5rem" }}>
          <button
            onClick={() => setLocation("/staff")}
            style={{
              display: "flex",
              alignItems: "center",
              gap: "0.5rem",
              backgroundColor: "transparent",
              color: "var(--accent)",
              padding: "0.5rem 1rem",
              borderRadius: "8px",
              border: "2px solid var(--accent)",
              cursor: "pointer",
              fontWeight: "500",
              transition: "all 0.2s",
            }}
            onMouseEnter={(e) => {
              (e.currentTarget as HTMLElement).style.backgroundColor = "var(--accent)";
              (e.currentTarget as HTMLElement).style.color = "white";
            }}
            onMouseLeave={(e) => {
              (e.currentTarget as HTMLElement).style.backgroundColor = "transparent";
              (e.currentTarget as HTMLElement).style.color = "var(--accent)";
            }}
          >
            <ArrowLeft size={18} />
            <span>Volver</span>
          </button>

          <div>
            <h1 style={{ fontSize: "1.75rem", fontWeight: "bold", margin: "0 0 0.25rem 0" }}>Gestión de Pedidos</h1>
            <p style={{ color: "var(--muted)", fontSize: "0.875rem", margin: 0 }}>
              Panel de control para preparación y entrega
            </p>
          </div>
        </div>

        {/* Right controls */}
        <div style={{ display: "flex", alignItems: "center", gap: "1rem" }}>
          {/* Manual refresh */}
          <button
            onClick={handleManualRefresh}
            disabled={loading || isRefreshing}
            style={{
              display: "flex",
              alignItems: "center",
              gap: "0.5rem",
              backgroundColor: autoRefreshEnabled ? "var(--accent)" : "#6b7280",
              color: "white",
              padding: "0.5rem 1rem",
              borderRadius: "8px",
              border: "none",
              cursor: loading || isRefreshing || !autoRefreshEnabled ? "not-allowed" : "pointer",
              fontWeight: "500",
              opacity: loading || isRefreshing || !autoRefreshEnabled ? 0.6 : 1,
              transition: "all 0.3s",
            }}
            onMouseEnter={(e) => {
              if (!loading && !isRefreshing && autoRefreshEnabled) {
                (e.currentTarget as HTMLElement).style.opacity = "0.9";
                (e.currentTarget as HTMLElement).style.transform = "scale(1.05)";
              }
            }}
            onMouseLeave={(e) => {
              if (!loading && !isRefreshing && autoRefreshEnabled) {
                (e.currentTarget as HTMLElement).style.opacity = "1";
                (e.currentTarget as HTMLElement).style.transform = "scale(1)";
              }
            }}
            title={!autoRefreshEnabled ? "Activa el auto-refresh" : "Actualizar ahora"}
          >
            <RefreshCw
              size={18}
              style={{
                animation: isRefreshing || hasChanges ? "pulse 0.6s ease-in-out" : "none",
              }}
            />
            {autoRefreshEnabled ? "Actualizar" : "Desactivado"}
          </button>
        </div>
      </div>

      {/* Main Content */}
      <div style={{ padding: "1.5rem" }}>
        {/* Auto-refresh Disabled Alert */}
        {isDisabled && (
          <div
            style={{
              backgroundColor: "#ef444420",
              border: "1px solid #ef4444",
              borderRadius: "8px",
              padding: "1rem",
              marginBottom: "1.5rem",
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
            }}
          >
            <div style={{ color: "#ef4444" }}>
              <p style={{ margin: "0 0 0.5rem 0", fontWeight: "600" }}>⚠️ Auto-refresh pausado</p>
              <p style={{ margin: 0, fontSize: "0.875rem" }}>
                Hubo varios errores al actualizar. El auto-refresh se ha pausado automáticamente.
              </p>
            </div>
            <button
              onClick={() => {
                resetAndRetry();
                handleManualRefresh();
              }}
              style={{
                backgroundColor: "#ef4444",
                color: "white",
                padding: "0.5rem 1rem",
                borderRadius: "6px",
                border: "none",
                cursor: "pointer",
                fontWeight: "600",
                whiteSpace: "nowrap",
                marginLeft: "1rem",
                transition: "all 0.2s",
              }}
              onMouseEnter={(e) => {
                (e.currentTarget as HTMLElement).style.backgroundColor = "#dc2626";
              }}
              onMouseLeave={(e) => {
                (e.currentTarget as HTMLElement).style.backgroundColor = "#ef4444";
              }}
            >
              Reintentar
            </button>
          </div>
        )}

        {/* Error State */}
        {error && (
          <div
            style={{
              backgroundColor: "#ef444420",
              border: "1px solid #ef4444",
              borderRadius: "8px",
              padding: "1rem",
              marginBottom: "1.5rem",
              color: "#ef4444",
            }}
          >
            <p style={{ margin: 0 }}>❌ {error}</p>
          </div>
        )}

        {/* Loading State */}
        {loading ? (
          <div style={{ display: "flex", alignItems: "center", justifyContent: "center", padding: "3rem" }}>
            <div style={{ textAlign: "center" }}>
              <div
                style={{
                  animation: "spin 1s linear infinite",
                  borderRadius: "50%",
                  height: "48px",
                  width: "48px",
                  borderTop: "3px solid var(--accent)",
                  borderRight: "3px solid var(--accent)",
                  borderBottom: "3px solid transparent",
                  borderLeft: "3px solid transparent",
                  margin: "0 auto 1rem",
                }}
              ></div>
              <p style={{ color: "var(--muted)" }}>Cargando órdenes...</p>
            </div>
          </div>
        ) : (
          <>
            {/* Filter toggle button */}
            <div style={{ marginBottom: "1.5rem", display: "flex", gap: "1rem" }}>
              <button
                onClick={() => setShowHidden(!showHidden)}
                style={{
                  backgroundColor: showHidden ? "#3b82f640" : "#3b82f620",
                  border: "1px solid #3b82f6",
                  color: "#3b82f6",
                  padding: "0.5rem 1rem",
                  borderRadius: "8px",
                  cursor: "pointer",
                  fontWeight: "600",
                  transition: "all 0.2s",
                }}
                onMouseEnter={(e) => {
                  (e.currentTarget as HTMLElement).style.backgroundColor = showHidden ? "#3b82f640" : "#3b82f620";
                }}
                onMouseLeave={(e) => {
                  (e.currentTarget as HTMLElement).style.backgroundColor = showHidden ? "#3b82f640" : "#3b82f620";
                }}
              >
                {showHidden ? "👁️" : "👁️‍🗨️"} {showHidden ? "Ocultar Finalizados" : "Mostrar Ocultos"} (
                {hiddenOrderIds.size})
              </button>
            </div>

            {/* Kanban Board */}
            <OrderBoard
              orders={orders.filter((order) => {
                const shouldShow = showHidden ? hiddenOrderIds.has(order.id) : !hiddenOrderIds.has(order.id);
                console.log(
                  `Order ${order.orderNumber}: showHidden=${showHidden}, isHidden=${hiddenOrderIds.has(order.id)}, shouldShow=${shouldShow}`,
                );
                return shouldShow;
              })}
              onOrderClick={handleOrderClick}
              onStatusChange={handleStatusChange}
              onMoveBackward={handleMoveBackward}
              isLoading={loading}
              isRefreshing={isRefreshing}
            />

            {/* Empty State */}
            {orders.length === 0 && !error && (
              <div
                style={{
                  backgroundColor: "var(--panel)",
                  border: "1px solid var(--border)",
                  borderRadius: "8px",
                  padding: "3rem",
                  textAlign: "center",
                }}
              >
                <p style={{ color: "var(--muted)", fontSize: "1.125rem", margin: 0 }}>No hay órdenes en este momento</p>
              </div>
            )}
          </>
        )}
      </div>

      {/* Order Detail Modal */}
      <OrderDetailModal
        order={selectedOrder}
        isOpen={isModalOpen}
        onClose={() => {
          setIsModalOpen(false);
          setSelectedOrder(null);
        }}
        onStatusChange={handleStatusChange}
        token={tokenState.state === "LOGGED_IN" ? tokenState.tokens.accessToken : undefined}
        isLoading={loading}
        onRefresh={fetchOrders}
        hiddenOrderIds={hiddenOrderIds}
        onToggleHideOrder={toggleHideOrder}
      />

      {/* CSS Animations */}
      <style>
        {`
          @keyframes spin {
            to { transform: rotate(360deg); }
          }
          @keyframes pulse {
            0%, 100% { opacity: 1; }
            50% { opacity: 0.6; }
          }
        `}
      </style>
    </div>
  );
};
