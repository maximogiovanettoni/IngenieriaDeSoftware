import { AlertCircle, ArrowLeft, ChevronDown, Loader2, Package } from "lucide-react";
import { useEffect, useState } from "react";
import { useLocation } from "wouter";

import { Footer } from "@/components/Footer/footer";
import type { OrderStatus } from "@/hooks/useOrderManagement";
import { useAutoRefresh, useOrderNotifications } from "@/hooks/useOrderManagement";
import { OrderResponse, orderAPI } from "@/services/OrderAPI";
import { useToken } from "@/services/TokenContext";

import "./my-orders-page.css";

// Helper function to format currency safely
const formatPrice = (value: string | number | null | undefined): string => {
  if (value == null || value === undefined) return "0.00";
  if (typeof value === "number") {
    return isNaN(value) ? "0.00" : value.toFixed(2);
  }
  if (typeof value === "string") {
    const num = parseFloat(value);
    return isNaN(num) ? "0.00" : num.toFixed(2);
  }
  return "0.00";
};

export const MyOrdersPage = () => {
  const [, setLocation] = useLocation();
  const [tokenState] = useToken();

  const [orders, setOrders] = useState<OrderResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [cancelDialog, setCancelDialog] = useState<{ open: boolean; orderId: number | null }>({
    open: false,
    orderId: null,
  });
  const [cancelReason, setCancelReason] = useState("");
  const [canceling, setCanceling] = useState(false);
  const [collapsedOrderIds, setCollapsedOrderIds] = useState<Set<number>>(new Set());
  // Normalize API status strings into internal OrderStatus values
  const normalizeStatus = (s: string | undefined): OrderStatus => {
    const raw = (s || "").toString().trim().toLowerCase();
    if (raw === "pending" || raw === "pendiente" || raw === "received") return "pending";
    if (raw === "confirmed" || raw === "confirmado") return "confirmed";
    if (raw === "preparing" || raw === "preparando") return "preparing";
    if (raw === "ready" || raw === "listo") return "ready";
    if (raw === "completed" || raw === "entregado" || raw === "delivered") return "completed";
    if (raw === "rejected" || raw === "rechazado") return "rejected";
    if (raw === "cancelled" || raw === "cancelado" || raw === "canceled") return "cancelled";
    return "pending";
  };

  // loadOrders must be declared before any useEffect or hook that references it
  const loadOrders = async () => {
    try {
      setLoading(true);
      setError(null);
      const token = tokenState.state === "LOGGED_IN" ? tokenState.tokens.accessToken : undefined;
      const data = await orderAPI.getMyOrders(token);
      setOrders(data);
    } catch (err) {
      const message = err instanceof Error ? err.message : "Error cargando pedidos";
      setError(message);
      console.error("Error loading orders:", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (tokenState.state === "LOGGED_OUT") {
      setLocation("/login");
      return;
    }
    void loadOrders();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [tokenState, setLocation]);

  const { isRefreshing, hasChanges } = useAutoRefresh({
    enabled: true,
    interval: 30000, // 30 seconds
    onRefresh: loadOrders,
    pauseWhenModalOpen: false, // Adjust based on your needs
  });

  const { detectOrderChanges } = useOrderNotifications({
    enabled: true,
    // Use underscore for unused param to avoid TS6133
    onNewOrder: (_orderId, orderNumber) => {
      console.log(`New order detected: ${orderNumber}`);
    },
    onStatusChange: (orderId, oldStatus, newStatus) => {
      console.log(`Order ${orderId} changed from ${oldStatus} to ${newStatus}`);
    },
  });

  useEffect(() => {
    if (orders.length > 0 && !loading) {
      // Map OrderResponse[] -> minimal Order[] shape expected by detectOrderChanges
      const mapped = orders.map((o) => ({ id: o.id, status: normalizeStatus(o.status), orderNumber: o.orderNumber }));
      detectOrderChanges(mapped);
    }
  }, [orders, loading, detectOrderChanges]);

  const statusClass = (status: string) => {
    const s = status.trim().toLowerCase();
    if (s === "confirmado") return "chip chip--ok";
    if (s === "pendiente") return "chip chip--warn";
    if (s === "rechazado") return "chip chip--bad";
    if (s === "entregado") return "chip chip--done";
    return "chip chip--muted";
  };

  const handleCancelOrder = async () => {
    if (!cancelDialog.orderId) return;
    setCanceling(true);
    try {
      const token = tokenState.state === "LOGGED_IN" ? tokenState.tokens.accessToken : undefined;
      await orderAPI.cancelOrder(cancelDialog.orderId, cancelReason, token);
      setCancelDialog({ open: false, orderId: null });
      setCancelReason("");
      await loadOrders();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Error cancelando pedido");
    } finally {
      setCanceling(false);
    }
  };

  return (
    <div className="page">
      <div className="hero hero--center">
        <div className="container">
          <h1 className="hero__title">Mis Pedidos</h1>
          <p className="hero__subtitle">Historial de tus pedidos en el comedor</p>

          {/* Refresh Status Indicator */}
          <div className="refresh-status">
            {isRefreshing && (
              <div className="refreshing-indicator">
                <Loader2 size={16} className="spin" />
                <span>Actualizando pedidos...</span>
              </div>
            )}
            {hasChanges && (
              <div className="changes-indicator">
                <span>✓ Pedidos actualizados</span>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Main */}
      <main className="container container--narrow">
        {/* Error */}
        {error && (
          <div className="alert alert--error">
            <AlertCircle size={18} />
            <span>{error}</span>
          </div>
        )}

        {/* Loading */}
        {loading ? (
          <div className="loader">
            <Loader2 size={32} className="spin" />
            <p className="muted">Cargando tus pedidos…</p>
          </div>
        ) : orders.length === 0 ? (
          <div className="card card--empty">
            <Package size={48} className="ico-muted" />
            <p className="muted mb-2">No hay pedidos</p>
            <p className="muted small mb-6">
              Aún no has realizado ningún pedido. ¡Vuelve al menú y haz tu primer pedido!
            </p>
            <button className="btn btn--grad" onClick={() => setLocation("/menu")}>
              Ir al Menú
            </button>
          </div>
        ) : (
          <div className="orders">
            {orders.map((order) => {
              const isCollapsed = collapsedOrderIds.has(order.id);
              const isCompleted =
                order.status.toLowerCase() === "completed" || order.status.toLowerCase() === "entregado";

              return (
                <div key={order.id} className={`order ${isCollapsed ? "order--collapsed" : ""}`}>
                  <div
                    className="order__row"
                    onClick={() => {
                      if (isCompleted && isCollapsed) {
                        setCollapsedOrderIds((prev) => {
                          const next = new Set(prev);
                          next.delete(order.id);
                          return next;
                        });
                      } else if (isCompleted) {
                        setCollapsedOrderIds((prev) => {
                          const next = new Set(prev);
                          next.add(order.id);
                          return next;
                        });
                      } else {
                        setLocation(`/orders/${order.id}`);
                      }
                    }}
                  >
                    {/* Info principal */}
                    <div className="order__main">
                      <div className="order__title">
                        <p className="order__number">{order.orderNumber}</p>
                        <span className={statusClass(order.status)}>{order.status}</span>
                      </div>
                      {!isCollapsed && (
                        <p className="order__date">
                          {new Date(order.createdAt).toLocaleDateString("es-AR", {
                            year: "numeric",
                            month: "long",
                            day: "numeric",
                            hour: "2-digit",
                            minute: "2-digit",
                          })}
                        </p>
                      )}
                    </div>

                    {/* Cantidad de ítems */}
                    {!isCollapsed && (
                      <div className="order__items muted">
                        {order.items.length} {order.items.length === 1 ? "ítem" : "ítems"}
                      </div>
                    )}

                    {/* Total */}
                    {!isCollapsed && (
                      <div className="order__total">
                        <p className="order__total-label muted">Total</p>
                        <p className="order__total-value">${formatPrice(order.totalAmount)}</p>
                      </div>
                    )}

                    {/* Icono de colapsar para órdenes completadas */}
                    {isCompleted && (
                      <div className="order__collapse-icon">
                        <ChevronDown
                          size={20}
                          style={{
                            transform: isCollapsed ? "rotate(-90deg)" : "rotate(0deg)",
                            transition: "transform 0.2s",
                          }}
                        />
                      </div>
                    )}
                  </div>

                  {/* Preview de ítems - solo si no está colapsada */}
                  {!isCollapsed && (
                    <>
                      <div className="order__preview">
                        {order.items.slice(0, 2).map((item, idx) => (
                          <p key={idx} className="small">
                            {item.quantity}x {item.productName}
                          </p>
                        ))}
                        {order.items.length > 2 && <p className="small link">+{order.items.length - 2} más</p>}
                      </div>

                      {/* Botón de cancelar si está en estado pendiente o confirmado */}
                      {(order.status.toLowerCase() === "pendiente" || order.status.toLowerCase() === "confirmado") && (
                        <button
                          className="btn btn--danger btn--small"
                          onClick={(e) => {
                            e.stopPropagation();
                            setCancelDialog({ open: true, orderId: order.id });
                          }}
                          style={{ marginTop: "0.5rem" }}
                        >
                          Cancelar Pedido
                        </button>
                      )}
                    </>
                  )}
                </div>
              );
            })}
          </div>
        )}

        {/* Modal de cancelación */}
        {cancelDialog.open && (
          <div className="modal">
            <div className="modal__backdrop" onClick={() => setCancelDialog({ open: false, orderId: null })} />
            <div className="modal__card">
              <h2 className="modal__title">Cancelar Pedido</h2>
              <p className="modal__sub">¿Estás seguro de que quieres cancelar este pedido?</p>
              <label className="field">
                <span>Razón (opcional)</span>
                <textarea
                  rows={3}
                  value={cancelReason}
                  onChange={(e) => setCancelReason(e.target.value)}
                  placeholder="Cuéntanos por qué deseas cancelar..."
                />
              </label>
              <div className="modal__actions">
                <button
                  className="btn btn--ghost"
                  onClick={() => {
                    setCancelDialog({ open: false, orderId: null });
                    setCancelReason("");
                  }}
                  disabled={canceling}
                >
                  Conservar Pedido
                </button>
                <button className="btn btn--danger" onClick={handleCancelOrder} disabled={canceling}>
                  {canceling ? <Loader2 size={16} className="spin" /> : null}
                  Cancelar Pedido
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Back */}
        <button className="back" onClick={() => setLocation("/menu")}>
          <ArrowLeft size={16} />
          <span>Volver al Menú</span>
        </button>
      </main>

      <Footer />
    </div>
  );
};
