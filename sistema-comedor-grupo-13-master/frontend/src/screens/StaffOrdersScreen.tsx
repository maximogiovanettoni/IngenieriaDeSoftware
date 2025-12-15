import { ArrowLeft, CheckCircle, XCircle } from "lucide-react";
import { useEffect, useState } from "react";
import { useLocation } from "wouter";

import { useToken } from "@/services/TokenContext";
import { confirmOrder, getPendingOrders, rejectOrder } from "@/services/UserServices";

import "./Student/menu-page.css";

interface Order {
  id: number;
  orderNumber: string;
  status: string;
  totalAmount: number;
  items: Array<{
    productId: number;
    productName: string;
    quantity: number;
    unitPrice: number;
    subtotal: number;
  }>;
  createdAt: string;
  confirmedAt?: string;
  deliveredAt?: string;
  rejectionReason?: string;
  pickupPoint?: {
    id: number;
    name: string;
    description: string;
    location: string;
  };
}

export function StaffOrdersScreen() {
  const [, setLocation] = useLocation();
  const [tokenState] = useToken();
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedOrder, setSelectedOrder] = useState<number | null>(null);
  const [rejectReason, setRejectReason] = useState("");
  const [actionLoading, setActionLoading] = useState(false);

  useEffect(() => {
    const loadOrders = async () => {
      if (tokenState.state !== "LOGGED_IN") {
        setLocation("/login");
        return;
      }

      try {
        setLoading(true);
        setError(null);
        const token = tokenState.tokens.accessToken;
        const ordersData = await getPendingOrders(token);
        setOrders(ordersData);
      } catch (err) {
        const message = err instanceof Error ? err.message : "Error desconocido";
        setError(message);
        console.error("Error loading pending orders:", err);
      } finally {
        setLoading(false);
      }
    };

    loadOrders();
  }, [tokenState, setLocation]);

  const handleConfirmOrder = async (orderId: number) => {
    if (tokenState.state !== "LOGGED_IN") return;

    try {
      setActionLoading(true);
      const token = tokenState.tokens.accessToken;
      await confirmOrder(orderId, token);

      // Actualizar lista local
      setOrders(orders.filter((o) => o.id !== orderId));
      setSelectedOrder(null);
    } catch (err) {
      alert("Error al confirmar el pedido: " + (err instanceof Error ? err.message : "Error desconocido"));
    } finally {
      setActionLoading(false);
    }
  };

  const handleRejectOrder = async (orderId: number) => {
    if (tokenState.state !== "LOGGED_IN" || !rejectReason.trim()) {
      alert("Por favor ingresa un motivo para rechazar el pedido");
      return;
    }

    try {
      setActionLoading(true);
      const token = tokenState.tokens.accessToken;
      await rejectOrder(orderId, rejectReason, token);

      // Actualizar lista local
      setOrders(orders.filter((o) => o.id !== orderId));
      setSelectedOrder(null);
      setRejectReason("");
    } catch (err) {
      alert("Error al rechazar el pedido: " + (err instanceof Error ? err.message : "Error desconocido"));
    } finally {
      setActionLoading(false);
    }
  };

  return (
    <div className="page page--compact">
      <div className="center">
        {/* Header */}
        <div
          style={{
            display: "flex",
            alignItems: "center",
            gap: "1rem",
            marginBottom: "2rem",
            paddingBottom: "1.5rem",
            borderBottom: "1px solid var(--border)",
          }}
        >
          <button
            onClick={() => setLocation("/staff")}
            style={{
              display: "flex",
              alignItems: "center",
              gap: "0.5rem",
              backgroundColor: "var(--accent)",
              color: "white",
              padding: "0.5rem 1rem",
              borderRadius: "8px",
              border: "none",
              cursor: "pointer",
              fontWeight: "500",
              transition: "opacity 0.2s",
            }}
            onMouseEnter={(e) => ((e.currentTarget as HTMLElement).style.opacity = "0.8")}
            onMouseLeave={(e) => ((e.currentTarget as HTMLElement).style.opacity = "1")}
          >
            <ArrowLeft size={18} />
            <span>Volver</span>
          </button>
          <div>
            <h1 style={{ color: "white", fontSize: "2rem", fontWeight: "bold", margin: 0 }}>Órdenes Pendientes</h1>
            <p style={{ color: "var(--muted)", fontSize: "0.875rem", marginTop: "0.5rem", margin: 0 }}>
              Gestiona los pedidos que requieren confirmación
            </p>
          </div>
        </div>

        {/* Loading State */}
        {loading && (
          <div
            style={{
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              paddingTop: "6rem",
              paddingBottom: "6rem",
            }}
          >
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
            }}
          >
            <p style={{ color: "#ef4444", margin: 0 }}>{error}</p>
          </div>
        )}

        {/* Empty State */}
        {!loading && orders.length === 0 && !error && (
          <div
            style={{
              backgroundColor: "var(--panel)",
              border: "1px solid var(--border)",
              borderRadius: "8px",
              padding: "3rem",
              textAlign: "center",
            }}
          >
            <p style={{ color: "var(--muted)", fontSize: "1.125rem", margin: 0 }}>
              No hay órdenes pendientes en este momento
            </p>
          </div>
        )}

        {/* Orders Grid */}
        {!loading && orders.length > 0 && (
          <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(360px, 1fr))", gap: "1.5rem" }}>
            {orders.map((order) => (
              <div
                key={order.id}
                style={{
                  backgroundColor: "var(--panel)",
                  border: "1px solid var(--border)",
                  borderRadius: "8px",
                  padding: "1.5rem",
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
                {/* Order Header */}
                <div
                  style={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "flex-start",
                    marginBottom: "1rem",
                    paddingBottom: "1rem",
                    borderBottom: "1px solid var(--border)",
                  }}
                >
                  <div>
                    <h2 style={{ color: "white", fontSize: "1.25rem", fontWeight: "bold", margin: "0 0 0.5rem 0" }}>
                      {order.orderNumber}
                    </h2>
                    <p style={{ color: "var(--muted)", fontSize: "0.75rem", margin: 0 }}>
                      {new Date(order.createdAt).toLocaleString("es-AR", {
                        year: "numeric",
                        month: "long",
                        day: "numeric",
                        hour: "2-digit",
                        minute: "2-digit",
                      })}
                    </p>
                  </div>
                  <div style={{ textAlign: "right" }}>
                    <p style={{ color: "var(--accent)", fontSize: "1.5rem", fontWeight: "bold", margin: 0 }}>
                      $
                      {(typeof order.totalAmount === "string"
                        ? parseFloat(order.totalAmount)
                        : order.totalAmount
                      ).toFixed(2)}
                    </p>
                  </div>
                </div>

                {/* Order Items */}
                <div style={{ marginBottom: "1.5rem" }}>
                  <h3
                    style={{
                      color: "white",
                      fontSize: "0.875rem",
                      fontWeight: "600",
                      textTransform: "uppercase",
                      marginBottom: "0.75rem",
                      margin: "0 0 0.75rem 0",
                    }}
                  >
                    Items del Pedido
                  </h3>
                  <div style={{ display: "flex", flexDirection: "column", gap: "0.5rem" }}>
                    {order.items.map((item, idx) => (
                      <div
                        key={idx}
                        style={{
                          display: "flex",
                          justifyContent: "space-between",
                          fontSize: "0.875rem",
                          color: "var(--muted)",
                          padding: "0.5rem",
                          backgroundColor: "var(--bg)",
                          borderRadius: "4px",
                          border: "none",
                        }}
                      >
                        <span>
                          <span style={{ color: "var(--accent)", fontWeight: "600" }}>{item.quantity}x</span>{" "}
                          {item.productName}
                        </span>
                        <span>{`$${(typeof item.subtotal === "string" ? parseFloat(item.subtotal) : item.subtotal).toFixed(2)}`}</span>
                      </div>
                    ))}
                  </div>
                </div>

                {/* Action Section */}
                {selectedOrder === order.id ? (
                  <div style={{ display: "flex", flexDirection: "column", gap: "0.75rem" }}>
                    {/* Reject Reason Input */}
                    <div>
                      <label
                        style={{
                          display: "block",
                          color: "var(--muted)",
                          fontSize: "0.75rem",
                          textTransform: "uppercase",
                          marginBottom: "0.5rem",
                        }}
                      >
                        Motivo del rechazo (opcional):
                      </label>
                      <input
                        type="text"
                        value={rejectReason}
                        onChange={(e) => setRejectReason(e.target.value)}
                        placeholder="Ej: Ingrediente no disponible"
                        style={{
                          width: "100%",
                          backgroundColor: "var(--bg)",
                          border: "1px solid var(--border)",
                          borderRadius: "6px",
                          padding: "0.625rem",
                          color: "white",
                          fontSize: "0.875rem",
                          boxSizing: "border-box",
                        }}
                        onFocus={(e) => {
                          (e.target as HTMLInputElement).style.borderColor = "var(--accent)";
                          (e.target as HTMLInputElement).style.boxShadow = "0 0 8px var(--accent)40";
                        }}
                        onBlur={(e) => {
                          (e.target as HTMLInputElement).style.borderColor = "var(--border)";
                          (e.target as HTMLInputElement).style.boxShadow = "none";
                        }}
                      />
                    </div>

                    {/* Action Buttons */}
                    <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "0.5rem" }}>
                      <button
                        onClick={() => handleConfirmOrder(order.id)}
                        disabled={actionLoading}
                        style={{
                          display: "flex",
                          alignItems: "center",
                          justifyContent: "center",
                          gap: "0.5rem",
                          backgroundColor: "var(--ok)",
                          color: "white",
                          padding: "0.625rem",
                          borderRadius: "6px",
                          border: "none",
                          fontSize: "0.8rem",
                          fontWeight: "600",
                          cursor: actionLoading ? "not-allowed" : "pointer",
                          opacity: actionLoading ? 0.6 : 1,
                          transition: "opacity 0.2s",
                        }}
                        onMouseEnter={(e) => {
                          if (!actionLoading) (e.currentTarget as HTMLElement).style.opacity = "0.8";
                        }}
                        onMouseLeave={(e) => {
                          if (!actionLoading) (e.currentTarget as HTMLElement).style.opacity = "1";
                        }}
                      >
                        <CheckCircle size={16} />
                        Confirmar
                      </button>
                      <button
                        onClick={() => handleRejectOrder(order.id)}
                        disabled={actionLoading || !rejectReason.trim()}
                        style={{
                          display: "flex",
                          alignItems: "center",
                          justifyContent: "center",
                          gap: "0.5rem",
                          backgroundColor: "#ef4444",
                          color: "white",
                          padding: "0.625rem",
                          borderRadius: "6px",
                          border: "none",
                          fontSize: "0.8rem",
                          fontWeight: "600",
                          cursor: actionLoading || !rejectReason.trim() ? "not-allowed" : "pointer",
                          opacity: actionLoading || !rejectReason.trim() ? 0.6 : 1,
                          transition: "opacity 0.2s",
                        }}
                        onMouseEnter={(e) => {
                          if (!actionLoading && rejectReason.trim())
                            (e.currentTarget as HTMLElement).style.opacity = "0.8";
                        }}
                        onMouseLeave={(e) => {
                          if (!actionLoading && rejectReason.trim())
                            (e.currentTarget as HTMLElement).style.opacity = "1";
                        }}
                      >
                        <XCircle size={16} />
                        Rechazar
                      </button>
                    </div>

                    {/* Cancel */}
                    <button
                      onClick={() => {
                        setSelectedOrder(null);
                        setRejectReason("");
                      }}
                      style={{
                        width: "100%",
                        backgroundColor: "var(--bg)",
                        color: "var(--muted)",
                        padding: "0.625rem",
                        borderRadius: "6px",
                        border: "1px solid var(--border)",
                        fontSize: "0.8rem",
                        fontWeight: "600",
                        cursor: "pointer",
                        transition: "all 0.2s",
                      }}
                      onMouseEnter={(e) => {
                        (e.currentTarget as HTMLElement).style.backgroundColor = "var(--panel)";
                        (e.currentTarget as HTMLElement).style.color = "white";
                      }}
                      onMouseLeave={(e) => {
                        (e.currentTarget as HTMLElement).style.backgroundColor = "var(--bg)";
                        (e.currentTarget as HTMLElement).style.color = "var(--muted)";
                      }}
                    >
                      Cancelar
                    </button>
                  </div>
                ) : (
                  <button
                    onClick={() => setSelectedOrder(order.id)}
                    style={{
                      width: "100%",
                      backgroundColor: "var(--accent)",
                      color: "white",
                      padding: "0.625rem",
                      borderRadius: "6px",
                      border: "none",
                      fontWeight: "600",
                      cursor: "pointer",
                      transition: "opacity 0.2s",
                    }}
                    onMouseEnter={(e) => ((e.currentTarget as HTMLElement).style.opacity = "0.8")}
                    onMouseLeave={(e) => ((e.currentTarget as HTMLElement).style.opacity = "1")}
                  >
                    Gestionar Pedido
                  </button>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
