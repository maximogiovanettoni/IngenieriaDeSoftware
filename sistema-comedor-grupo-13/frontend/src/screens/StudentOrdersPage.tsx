import {
  AlertCircle,
  ArrowLeft,
  CheckCircle2,
  Clock,
  Loader2,
  Package,
  ShoppingCart,
  TrendingUp,
  Utensils,
  X,
  XCircle,
} from "lucide-react";
import { useCallback, useEffect, useState } from "react";
import { useLocation } from "wouter";

import { type OrderStatus, useOrderNotifications } from "@/hooks/useOrderManagement";
import { OrderResponse, orderAPI } from "@/services/OrderAPI";
import { useToken } from "@/services/TokenContext";

import "./Student/menu-page.css";

export const StudentOrdersPage = () => {
  const [, setLocation] = useLocation();
  const [tokenState] = useToken();

  const [orders, setOrders] = useState<OrderResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [stats, setStats] = useState<{
    totalOrders: number;
    pendingCount: number;
    confirmedCount: number;
    preparingCount: number;
    readyCount: number;
    deliveredCount: number;
    rejectedCount: number;
    cancelledCount: number;
    totalSpent: number;
  } | null>(null);
  const [hiddenOrderIds, setHiddenOrderIds] = useState<Set<number>>(new Set());
  const [showHidden, setShowHidden] = useState(false);

  // Load hidden orders from localStorage on mount
  useEffect(() => {
    const stored = localStorage.getItem("hiddenOrderIds");
    if (stored) {
      try {
        const ids = JSON.parse(stored) as number[];
        setHiddenOrderIds(new Set(ids));
      } catch (e) {
        console.error("Error parsing stored hidden orders:", e);
      }
    }
  }, []);

  const loadOrders = useCallback(async () => {
    try {
        setLoading(true);
        setError(null);
        const token = tokenState.state === "LOGGED_IN" ? tokenState.tokens.accessToken : undefined;
        const data = await orderAPI.getMyOrders(token);
        setOrders(data);

      // Clean localStorage: remove IDs of orders that no longer exist
      const currentOrderIds = new Set(data.map((o) => o.id));
      const stored = localStorage.getItem("hiddenOrderIds");
      if (stored) {
        try {
          const storedIds = JSON.parse(stored) as number[];
          const validIds = storedIds.filter((id) => currentOrderIds.has(id));
          if (validIds.length !== storedIds.length) {
            localStorage.setItem("hiddenOrderIds", JSON.stringify(validIds));
            setHiddenOrderIds(new Set(validIds));
          }
        } catch (e) {
          console.error("Error cleaning hidden order IDs:", e);
        }
      }

      // Calculate statistics locally
      const totalOrders = data.length;
      const pendingCount = data.filter((o) => o.status === "PENDING").length;
      const confirmedCount = data.filter((o) => o.status === "CONFIRMED").length;
      const preparingCount = data.filter((o) => o.status === "PREPARING").length;
      const readyCount = data.filter((o) => o.status === "READY").length;
      const deliveredCount = data.filter((o) => o.status === "COMPLETED" || o.status === "DELIVERED").length;
      const rejectedCount = data.filter((o) => o.status === "REJECTED").length;
      const cancelledCount = data.filter((o) => o.status === "CANCELLED").length;
      const totalSpent = data.reduce(
        (sum, o) => sum + (typeof o.totalAmount === "string" ? parseFloat(o.totalAmount) : o.totalAmount),
        0,
      );

      setStats({
        totalOrders,
        pendingCount,
        confirmedCount,
        preparingCount,
        readyCount,
        deliveredCount,
        rejectedCount,
        cancelledCount,
        totalSpent,
      });
    } catch (err) {
        const message = err instanceof Error ? err.message : "Error cargando pedidos";
        setError(message);
        console.error("Error loading orders:", err);
    } finally {
        setLoading(false);
    }
}, [tokenState]);

  const handleDeleteOrder = async (orderId: number, e: React.MouseEvent) => {
    e.stopPropagation();

    if (!confirm("¿Ocultar este pedido?")) {
      return;
    }

    const updated = new Set(hiddenOrderIds);
    updated.add(orderId);
    setHiddenOrderIds(updated);
    localStorage.setItem("hiddenOrderIds", JSON.stringify(Array.from(updated)));
  };

  useEffect(() => {
    if (tokenState.state === "LOGGED_OUT") {
      setLocation("/login");
      return;
    }

    loadOrders();
    const interval = setInterval(loadOrders, 5000);
    return () => clearInterval(interval);
  }, [tokenState.state, loadOrders, setLocation]);

  const { detectOrderChanges } = useOrderNotifications({
    enabled: true,
    onNewOrder: (_orderId, orderNumber) => {
      console.log(`New order detected: ${orderNumber}`);
    },
    onStatusChange: (_orderId: number, oldStatus: OrderStatus, newStatus: OrderStatus): void => {
      console.log(`Order status changed from ${oldStatus} to ${newStatus}`);
    },
  });

  useEffect(() => {
    if (orders && orders.length > 0) {
      try {
        const mappedOrders = orders
          .filter((o) => o && o.id && o.status && o.orderNumber)
          .map((o) => ({
            id: o.id,
            status: (o.status || "PENDING").toLowerCase() as OrderStatus,
            orderNumber: o.orderNumber,
          }));
        if (mappedOrders.length > 0) {
          detectOrderChanges(mappedOrders);
        }
      } catch (error) {
        console.error("Error in detectOrderChanges:", error);
      }
    }
  }, [orders, detectOrderChanges]);

  const getStatusColor = (status: string) => {
    switch (status.toUpperCase()) {
      case "PENDING":
        return { bg: "bg-[#f59e0b]/20", border: "border-[#f59e0b]", text: "text-[#f59e0b]" };
      case "CONFIRMED":
        return { bg: "bg-[#3b82f6]/20", border: "border-[#3b82f6]", text: "text-[#3b82f6]" };
      case "PREPARING":
        return { bg: "bg-[#f59e0b]/20", border: "border-[#f59e0b]", text: "text-[#f59e0b]" };
      case "READY":
        return { bg: "bg-[#06b6d4]/20", border: "border-[#06b6d4]", text: "text-[#06b6d4]" };
      case "COMPLETED":
        return { bg: "bg-[#10b981]/20", border: "border-[#10b981]", text: "text-[#10b981]" };
      case "CANCELLED":
        return { bg: "bg-[#6b7280]/20", border: "border-[#6b7280]", text: "text-[#6b7280]" };
      case "REJECTED":
        return { bg: "bg-[#ef4444]/20", border: "border-[#ef4444]", text: "text-[#ef4444]" };
      default:
        return { bg: "bg-[#6b7280]/20", border: "border-[#6b7280]", text: "text-[#6b7280]" };
    }
  };

  const getStatusLabel = (status: string) => {
    switch (status.toUpperCase()) {
      case "PENDING":
        return "⏳ Pendiente de confirmación";
      case "CONFIRMED":
        return "✓ Confirmado";
      case "PREPARING":
        return "🍳 En preparación";
      case "READY":
        return "✓ Listo para retirar";
      case "COMPLETED":
        return "✓ Entregado";
      case "CANCELLED":
        return "✕ Cancelado";
      case "REJECTED":
        return "✕ Rechazado";
      default:
        return status;
    }
  };

  return (
    <div className="page page--compact">
      <div className="center">
        {/* Hero Section */}
        <div
          style={{
            marginBottom: "2rem",
            paddingBottom: "1.5rem",
            borderBottom: "1px solid var(--border)",
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between",
          }}
        >
          <div>
            <h1 style={{ color: "white", fontSize: "2rem", fontWeight: "bold", margin: "0 0 0.5rem 0" }}>
              Mis Pedidos
            </h1>
            <p style={{ color: "var(--muted)", fontSize: "0.875rem", margin: 0 }}>
              Visualiza el estado de tus pedidos y dónde retirarlos
            </p>
          </div>
          <button
            onClick={() => setLocation("/menu")}
            style={{
              display: "flex",
              alignItems: "center",
              gap: "0.5rem",
              backgroundColor: "var(--accent)",
              color: "white",
              padding: "0.625rem 1rem",
              borderRadius: "8px",
              border: "none",
              cursor: "pointer",
              fontSize: "0.875rem",
              fontWeight: "500",
              transition: "opacity 0.2s",
            }}
            onMouseEnter={(e) => ((e.currentTarget as HTMLElement).style.opacity = "0.8")}
            onMouseLeave={(e) => ((e.currentTarget as HTMLElement).style.opacity = "1")}
          >
            <ArrowLeft size={18} />
            <span>Volver al Menú</span>
          </button>
        </div>

        {/* Stats Section */}
        {stats && (
          <div
            style={{
              display: "grid",
              gridTemplateColumns: "repeat(auto-fit, minmax(180px, 1fr))",
              gap: "1rem",
              marginBottom: "2rem",
            }}
          >
            <div
              style={{
                backgroundColor: "var(--panel)",
                borderRadius: "8px",
                padding: "1.25rem",
                border: "1px solid var(--border)",
              }}
            >
              <div style={{ display: "flex", alignItems: "center", gap: "0.5rem", marginBottom: "0.5rem" }}>
                <ShoppingCart size={16} style={{ color: "var(--accent)" }} />
                <span
                  style={{ color: "var(--muted)", fontSize: "0.75rem", textTransform: "uppercase", fontWeight: "600" }}
                >
                  Total
                </span>
              </div>
              <p style={{ color: "white", fontSize: "1.75rem", fontWeight: "bold", margin: 0 }}>{stats.totalOrders}</p>
            </div>

            <div
              style={{
                backgroundColor: "var(--panel)",
                borderRadius: "8px",
                padding: "1.25rem",
                border: "1px solid var(--border)",
              }}
            >
              <div style={{ display: "flex", alignItems: "center", gap: "0.5rem", marginBottom: "0.5rem" }}>
                <Clock size={16} style={{ color: "#f59e0b" }} />
                <span
                  style={{ color: "var(--muted)", fontSize: "0.75rem", textTransform: "uppercase", fontWeight: "600" }}
                >
                  Pendiente
                </span>
              </div>
              <p style={{ color: "white", fontSize: "1.75rem", fontWeight: "bold", margin: 0 }}>{stats.pendingCount}</p>
            </div>

            <div
              style={{
                backgroundColor: "var(--panel)",
                borderRadius: "8px",
                padding: "1.25rem",
                border: "1px solid var(--border)",
              }}
            >
              <div style={{ display: "flex", alignItems: "center", gap: "0.5rem", marginBottom: "0.5rem" }}>
                <CheckCircle2 size={16} style={{ color: "#3b82f6" }} />
                <span
                  style={{ color: "var(--muted)", fontSize: "0.75rem", textTransform: "uppercase", fontWeight: "600" }}
                >
                  Confirmado
                </span>
              </div>
              <p style={{ color: "white", fontSize: "1.75rem", fontWeight: "bold", margin: 0 }}>
                {stats.confirmedCount}
              </p>
            </div>

            <div
              style={{
                backgroundColor: "var(--panel)",
                borderRadius: "8px",
                padding: "1.25rem",
                border: "1px solid var(--border)",
              }}
            >
              <div style={{ display: "flex", alignItems: "center", gap: "0.5rem", marginBottom: "0.5rem" }}>
                <Utensils size={16} style={{ color: "#f59e0b" }} />
                <span
                  style={{ color: "var(--muted)", fontSize: "0.75rem", textTransform: "uppercase", fontWeight: "600" }}
                >
                  Preparando
                </span>
              </div>
              <p style={{ color: "white", fontSize: "1.75rem", fontWeight: "bold", margin: 0 }}>
                {stats.preparingCount}
              </p>
            </div>

            <div
              style={{
                backgroundColor: "var(--panel)",
                borderRadius: "8px",
                padding: "1.25rem",
                border: "1px solid var(--border)",
              }}
            >
              <div style={{ display: "flex", alignItems: "center", gap: "0.5rem", marginBottom: "0.5rem" }}>
                <CheckCircle2 size={16} style={{ color: "#06b6d4" }} />
                <span
                  style={{ color: "var(--muted)", fontSize: "0.75rem", textTransform: "uppercase", fontWeight: "600" }}
                >
                  Listo
                </span>
              </div>
              <p style={{ color: "white", fontSize: "1.75rem", fontWeight: "bold", margin: 0 }}>{stats.readyCount}</p>
            </div>

            <div
              style={{
                backgroundColor: "var(--panel)",
                borderRadius: "8px",
                padding: "1.25rem",
                border: "1px solid var(--border)",
              }}
            >
              <div style={{ display: "flex", alignItems: "center", gap: "0.5rem", marginBottom: "0.5rem" }}>
                <CheckCircle2 size={16} style={{ color: "#10b981" }} />
                <span
                  style={{ color: "var(--muted)", fontSize: "0.75rem", textTransform: "uppercase", fontWeight: "600" }}
                >
                  Entregado
                </span>
              </div>
              <p style={{ color: "white", fontSize: "1.75rem", fontWeight: "bold", margin: 0 }}>
                {stats.deliveredCount}
              </p>
            </div>

            <div
              style={{
                backgroundColor: "var(--panel)",
                borderRadius: "8px",
                padding: "1.25rem",
                border: "1px solid var(--border)",
              }}
            >
              <div style={{ display: "flex", alignItems: "center", gap: "0.5rem", marginBottom: "0.5rem" }}>
                <XCircle size={16} style={{ color: "#ef4444" }} />
                <span
                  style={{ color: "var(--muted)", fontSize: "0.75rem", textTransform: "uppercase", fontWeight: "600" }}
                >
                  Rechazado
                </span>
              </div>
              <p style={{ color: "white", fontSize: "1.75rem", fontWeight: "bold", margin: 0 }}>
                {stats.rejectedCount}
              </p>
            </div>

            <div
              style={{
                backgroundColor: "var(--panel)",
                borderRadius: "8px",
                padding: "1.25rem",
                border: "1px solid var(--border)",
              }}
            >
              <div style={{ display: "flex", alignItems: "center", gap: "0.5rem", marginBottom: "0.5rem" }}>
                <XCircle size={16} style={{ color: "#6b7280" }} />
                <span
                  style={{ color: "var(--muted)", fontSize: "0.75rem", textTransform: "uppercase", fontWeight: "600" }}
                >
                  Cancelado
                </span>
              </div>
              <p style={{ color: "white", fontSize: "1.75rem", fontWeight: "bold", margin: 0 }}>
                {stats.cancelledCount}
              </p>
            </div>

            <div
              style={{
                backgroundColor: "var(--panel)",
                borderRadius: "8px",
                padding: "1.25rem",
                border: "2px solid var(--accent)",
              }}
            >
              <div style={{ display: "flex", alignItems: "center", gap: "0.5rem", marginBottom: "0.5rem" }}>
                <TrendingUp size={16} style={{ color: "var(--accent)" }} />
                <span
                  style={{ color: "var(--muted)", fontSize: "0.75rem", textTransform: "uppercase", fontWeight: "600" }}
                >
                  Total Gastado
                </span>
              </div>
              <p style={{ color: "var(--accent)", fontSize: "1.75rem", fontWeight: "bold", margin: 0 }}>
                ${(typeof stats.totalSpent === "string" ? parseFloat(stats.totalSpent) : stats.totalSpent).toFixed(2)}
              </p>
            </div>
          </div>
        )}

        {/* Action Buttons */}
        {hiddenOrderIds.size > 0 && (
          <div
            style={{
              display: "flex",
              gap: "1rem",
              marginBottom: "2rem",
              flexWrap: "wrap",
            }}
          >
            <button
              onClick={() => {
                setShowHidden(!showHidden);
              }}
              style={{
                flex: 1,
                minWidth: "150px",
                padding: "0.75rem 1rem",
                backgroundColor: showHidden ? "#3b82f640" : "#3b82f620",
                color: "#3b82f6",
                border: "1px solid #3b82f6",
                borderRadius: "8px",
                fontWeight: "600",
                cursor: "pointer",
                fontSize: "0.875rem",
                transition: "all 0.2s",
              }}
              onMouseEnter={(e) => {
                (e.currentTarget as HTMLElement).style.backgroundColor = "#3b82f640";
              }}
              onMouseLeave={(e) => {
                (e.currentTarget as HTMLElement).style.backgroundColor = showHidden ? "#3b82f640" : "#3b82f620";
              }}
            >
              {showHidden ? "👁️" : "👁️‍🗨️"} {showHidden ? "Ocultar de nuevo" : "Mostrar Ocultos"} ({hiddenOrderIds.size})
            </button>
          </div>
        )}

        {/* Error Alert */}
        {error && (
          <div
            style={{
              marginBottom: "1.5rem",
              backgroundColor: "#ef444420",
              border: "1px solid #ef4444",
              borderRadius: "8px",
              padding: "1rem",
              display: "flex",
              alignItems: "center",
              gap: "0.75rem",
            }}
          >
            <AlertCircle size={20} style={{ color: "#ef4444" }} />
            <p style={{ color: "#ef4444", fontSize: "0.8rem", margin: 0 }}>{error}</p>
          </div>
        )}

        {/* Loading */}
        {loading ? (
          <div
            style={{
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              paddingTop: "4rem",
              paddingBottom: "4rem",
            }}
          >
            <div style={{ textAlign: "center" }}>
              <Loader2
                size={32}
                style={{ color: "var(--accent)", animation: "spin 1s linear infinite", margin: "0 auto 1rem" }}
              />
              <p style={{ color: "var(--muted)" }}>Cargando tus pedidos...</p>
            </div>
          </div>
        ) : orders.length === 0 ? (
          <div
            style={{
              backgroundColor: "var(--panel)",
              border: "1px solid var(--border)",
              borderRadius: "8px",
              padding: "2rem",
              textAlign: "center",
            }}
          >
            <Package size={48} style={{ color: "var(--border)", margin: "0 auto 1rem" }} />
            <p style={{ color: "var(--muted)", fontSize: "0.875rem", marginBottom: "0.5rem" }}>No hay pedidos</p>
            <p style={{ color: "var(--border)", fontSize: "0.75rem", marginBottom: "1.5rem" }}>
              Aún no has realizado ningún pedido. ¡Vuelve al menú y haz tu primer pedido!
            </p>
            <button
              onClick={() => setLocation("/menu")}
              style={{
                backgroundColor: "var(--accent)",
                color: "white",
                padding: "0.5rem 1rem",
                borderRadius: "8px",
                border: "none",
                cursor: "pointer",
                fontSize: "0.8rem",
                fontWeight: "500",
              }}
            >
              Ir al Menú
            </button>
          </div>
        ) : (
          <div style={{ display: "flex", flexDirection: "column", gap: "1rem" }}>
            {orders
              .filter((order) => (showHidden ? hiddenOrderIds.has(order.id) : !hiddenOrderIds.has(order.id)))
              .map((order) => {
                const statusColor = getStatusColor(order.status);
                return (
                  <div
                    key={order.id}
                    onClick={() => setLocation(`/orders/${order.id}`)}
                    style={{
                      backgroundColor: "var(--panel)",
                      border: "1px solid var(--border)",
                      borderRadius: "8px",
                      padding: "1.25rem",
                      cursor: "pointer",
                      transition: "all 0.2s",
                    }}
                    onMouseEnter={(e) => {
                      (e.currentTarget as HTMLElement).style.borderColor = "var(--accent)";
                      (e.currentTarget as HTMLElement).style.boxShadow = "0 0 12px var(--shadow-1)";
                    }}
                    onMouseLeave={(e) => {
                      (e.currentTarget as HTMLElement).style.borderColor = "var(--border)";
                      (e.currentTarget as HTMLElement).style.boxShadow = "none";
                    }}
                  >
                    <div style={{ display: "flex", flexDirection: "column", gap: "1rem" }}>
                      {/* Order Number and Status */}
                      <div
                        style={{
                          display: "flex",
                          alignItems: "center",
                          gap: "0.75rem",
                          justifyContent: "space-between",
                        }}
                      >
                        <p style={{ color: "white", fontWeight: "bold", fontSize: "1rem", margin: 0 }}>
                          {order.orderNumber}
                        </p>
                        <div style={{ display: "flex", alignItems: "center", gap: "0.5rem" }}>
                          <span
                            style={{
                              border: `1px solid ${statusColor.text}`,
                              borderRadius: "999px",
                              padding: "0.25rem 0.75rem",
                              fontSize: "0.7rem",
                              fontWeight: "600",
                              color: statusColor.text,
                              backgroundColor: statusColor.text + "20",
                            }}
                          >
                            {getStatusLabel(order.status)}
                          </span>
                          {["COMPLETED", "CANCELLED", "REJECTED"].includes(order.status) && (
                            <button
                              onClick={(e) => handleDeleteOrder(order.id, e)}
                              style={{
                                background: "none",
                                border: "none",
                                color: "#ef4444",
                                cursor: "pointer",
                                padding: "0.25rem",
                                display: "flex",
                                alignItems: "center",
                                justifyContent: "center",
                                borderRadius: "4px",
                                transition: "background-color 0.2s",
                              }}
                              onMouseEnter={(e) => {
                                (e.currentTarget as HTMLElement).style.backgroundColor = "rgba(239, 68, 68, 0.1)";
                              }}
                              onMouseLeave={(e) => {
                                (e.currentTarget as HTMLElement).style.backgroundColor = "transparent";
                              }}
                              title="Eliminar pedido"
                            >
                              <X size={16} />
                            </button>
                          )}
                        </div>
                      </div>

                      {/* Date */}
                      <p style={{ color: "var(--muted)", fontSize: "0.75rem", margin: 0 }}>
                        {new Date(order.createdAt).toLocaleDateString("es-AR", {
                          year: "numeric",
                          month: "long",
                          day: "numeric",
                          hour: "2-digit",
                          minute: "2-digit",
                        })}
                      </p>

                      {/* Items Preview */}
                      <div style={{ fontSize: "0.7rem", color: "var(--muted)" }}>
                        {order.items.slice(0, 2).map((item, idx) => (
                          <p key={idx} style={{ margin: "0.25rem 0" }}>
                            {item.quantity}x {item.productName}
                          </p>
                        ))}
                        {order.items.length > 2 && (
                          <p style={{ color: "var(--accent)", margin: "0.25rem 0" }}>+{order.items.length - 2} más</p>
                        )}
                      </div>

                      {/* Footer: Total and Arrow */}
                      <div
                        style={{
                          display: "flex",
                          justifyContent: "space-between",
                          alignItems: "center",
                          paddingTop: "0.75rem",
                          borderTop: "1px solid var(--border)",
                        }}
                      >
                        <div>
                          <p style={{ color: "var(--accent)", fontWeight: "bold", fontSize: "1rem", margin: 0 }}>
                            $
                            {(typeof order.totalAmount === "string"
                              ? parseFloat(order.totalAmount)
                              : order.totalAmount
                            ).toFixed(2)}
                          </p>
                        </div>
                        <ArrowLeft size={16} style={{ color: "var(--muted)", transform: "rotate(180deg)" }} />
                      </div>
                    </div>
                  </div>
                );
              })}
          </div>
        )}
      </div>
    </div>
  );
};
