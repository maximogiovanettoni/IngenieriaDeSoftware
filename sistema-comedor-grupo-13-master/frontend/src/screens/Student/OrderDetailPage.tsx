import { ArrowLeft, Gift, Loader2, Package, TrendingDown } from "lucide-react";
import { useEffect, useState, useCallback } from "react";
import { useLocation, useRoute } from "wouter";

import { OrderResponse, orderAPI } from "@/services/OrderAPI";
import { useToken } from "@/services/TokenContext";

import "./order-detail-page.css";

export const OrderDetailPage = () => {
  const [, setLocation] = useLocation();
  const [match, params] = useRoute("/orders/:orderId");
  const [tokenState] = useToken();

  const [order, setOrder] = useState<OrderResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isCancelling, setIsCancelling] = useState(false);
  const [timestamps, setTimestamps] = useState<Record<string, string>>({});

  const loadOrder = useCallback(
    async (orderId: number) => {
      try {
        setLoading(true);
        setError(null);
        const token = tokenState.state === "LOGGED_IN" ? tokenState.tokens.accessToken : undefined;
        const data = await orderAPI.getOrderById(orderId, token);
        setOrder(data);

        // Map order status to determine which intermediate states to check
        // Always try to fetch timestamps for all intermediate states
        const newTimestamps: Record<string, string> = {};

        const possibleStatuses = ["CONFIRMED", "PREPARING", "READY", "COMPLETED"];

        for (const status of possibleStatuses) {
          try {
            const timestamp = await orderAPI.getTimeWhenStatusReached(orderId, status, token);
            if (timestamp) {
              newTimestamps[status] = timestamp;
              console.log(`✓ ${status}: ${timestamp}`);
            }
          } catch (err) {
            console.error("Error: ", err)
          }
        }

        setTimestamps(newTimestamps);
      } catch (err) {
        const message = err instanceof Error ? err.message : "Error cargando el pedido";
        setError(message);
        console.error("Error loading order:", err);
      } finally {
        setLoading(false);
      }
    },
    [tokenState]

  );

  const handleCancelOrder = async (orderId: number) => {
    if (tokenState.state !== "LOGGED_IN") {
      alert("Debes iniciar sesión");
      return;
    }

    setIsCancelling(true);
    try {
      const token = tokenState.state === "LOGGED_IN" ? tokenState.tokens.accessToken : "";
      const response = await fetch(`/api/orders/${orderId}/cancel?reason=Cancelación%20del%20usuario`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
      });

      if (!response.ok) {
        const errorData = (await response.json()) as Record<string, unknown>;
        throw new Error((errorData.message as string) || `Error: ${response.statusText}`);
      }

      alert("Pedido cancelado exitosamente");
      setLocation("/orders");
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : "Error al cancelar el pedido";
      alert(message);
      console.error("Error cancelling order:", err);
    } finally {
      setIsCancelling(false);
    }
  };

  useEffect(() => {
    if (tokenState.state === "LOGGED_OUT") {
      setLocation("/login");
      return;
    }

    if (match && params?.orderId && tokenState.state === "LOGGED_IN") {
      loadOrder(parseInt(params.orderId, 10));
    }
  }, [match, params?.orderId, tokenState.state, setLocation, loadOrder]);

  // Auto-refresh order if it's in an intermediate state
  useEffect(() => {
    if (!order || !match || tokenState.state !== "LOGGED_IN") return;

    // Only auto-refresh if order is in PENDING or CONFIRMED state
    const shouldAutoRefresh =
      order.status === "PENDING" || order.status === "CONFIRMED" || order.status === "PREPARING";

    if (!shouldAutoRefresh) {
      console.log("Order status:", order.status, "- No auto-refresh needed");
      return;
    }

    console.log("Auto-refresh enabled for order", order.id, "status:", order.status);

    const interval = setInterval(() => {
      console.log("Auto-refreshing order", order.id);
      loadOrder(order.id);
    }, 5000); // Refresh every 5 seconds

    return () => clearInterval(interval);
  }, [order, match, tokenState.state, loadOrder]);

  const formatDate = (dateString?: string | null) => {
    if (!dateString || dateString.trim() === "") return null;
    try {
      const date = new Date(dateString);
      // Check if date is valid
      if (isNaN(date.getTime())) return null;

      return date.toLocaleString("es-AR", {
        month: "short",
        day: "numeric",
        hour: "2-digit",
        minute: "2-digit",
      });
    } catch {
      return null;
    }
  };

  const renderStateHistory = (currentStatus: string) => {
    const events = [
      { status: "PENDING", label: "Creado", time: order?.createdAt, completed: true },
      {
        status: "CONFIRMED",
        label: "Confirmado",
        time: timestamps.CONFIRMED,
        completed: ["CONFIRMED", "PREPARING", "READY", "COMPLETED"].includes(currentStatus),
      },
      {
        status: "PREPARING",
        label: "En Preparación",
        time: timestamps.PREPARING,
        completed: ["PREPARING", "READY", "COMPLETED"].includes(currentStatus),
      },
      {
        status: "READY",
        label: "Listo para retirar",
        time: timestamps.READY,
        completed: ["READY", "COMPLETED"].includes(currentStatus),
      },
      {
        status: "COMPLETED",
        label: "Entregado",
        time: timestamps.COMPLETED,
        completed: currentStatus === "COMPLETED" || currentStatus === "DELIVERED",
      },
    ];

    console.log("renderStateHistory - currentStatus:", currentStatus);
    console.log("renderStateHistory - timestamps:", timestamps);
    console.log("renderStateHistory - events:", events);

    return (
      <div style={{ position: "relative", paddingLeft: "2.5rem" }}>
        {events.map((event, idx) => {
          const shouldShowTime = event.time && event.completed;
          console.log(`Event ${event.label}: time=${event.time}, completed=${event.completed}, shouldShow=${shouldShowTime}`);

          return (
            <div
              key={idx}
              style={{
                display: "flex",
                gap: "1rem",
                marginBottom: idx < events.length - 1 ? "1.5rem" : "0",
                position: "relative",
              }}
            >
              {/* Timeline dot */}
              <div
                style={{
                  position: "absolute",
                  left: "-2.25rem",
                  top: "0rem",
                  width: "14px",
                  height: "14px",
                  borderRadius: "50%",
                  backgroundColor: event.completed ? "#10b981" : "#4a5565",
                  border: "3px solid var(--bg)",
                  boxShadow: event.completed ? "0 0 0 2px #10b981" : "none",
                }}
              ></div>

              {/* Timeline connector */}
              {idx < events.length - 1 && (
                <div
                  style={{
                    position: "absolute",
                    left: "-2.05rem",
                    top: "14px",
                    width: "2px",
                    height: "calc(100% + 1.5rem)",
                    backgroundColor: event.completed ? "#10b981" : "#4a5565",
                  }}
                ></div>
              )}

              <div style={{ flex: 1 }}>
                <p
                  style={{
                    color: event.completed ? "white" : "#99a1af",
                    fontSize: "0.95rem",
                    fontWeight: event.completed ? "600" : "500",
                    margin: 0,
                  }}
                >
                  {event.label}
                </p>
                {event.time && event.completed && (
                  <p style={{ color: "#51a2ff", fontSize: "0.8rem", margin: "0.25rem 0 0 0" }}>
                    {formatDate(event.time)}
                  </p>
                )}
              </div>
            </div>
          );
        })}
      </div>
    );
  };

  if (loading) {
    return (
      <div className="page page--compact">
        <div className="center">
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
              <p style={{ color: "var(--muted)" }}>Cargando pedido...</p>
            </div>
          </div>
        </div>
      </div>
    );
  }

  if (error || !order) {
    return (
      <div className="page page--compact">
        <div className="center">
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
            <p style={{ color: "#ef4444", fontSize: "0.875rem", margin: 0 }}>{error || "Pedido no encontrado"}</p>
          </div>
          <button
            onClick={() => setLocation("/orders")}
            style={{
              display: "flex",
              alignItems: "center",
              gap: "0.5rem",
              color: "var(--accent)",
              background: "none",
              border: "none",
              cursor: "pointer",
              fontSize: "0.875rem",
            }}
          >
            <ArrowLeft size={18} />
            <span>Volver a mis pedidos</span>
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="page page--compact">
      <div className="center">
        {/* Header */}
        <div style={{ marginBottom: "2rem", paddingBottom: "1.5rem", borderBottom: "1px solid var(--border)" }}>
          <button
            onClick={() => setLocation("/orders")}
            style={{
              display: "flex",
              alignItems: "center",
              gap: "0.5rem",
              color: "var(--accent)",
              background: "none",
              border: "none",
              cursor: "pointer",
              fontSize: "0.875rem",
              marginBottom: "1rem",
            }}
          >
            <ArrowLeft size={18} />
            <span>Volver a mis pedidos</span>
          </button>
          <h1 style={{ color: "white", fontSize: "1.75rem", fontWeight: "bold", margin: 0 }}>{order.orderNumber}</h1>
          <p style={{ color: "var(--muted)", fontSize: "0.875rem", marginTop: "0.5rem" }}>
            {new Date(order.createdAt).toLocaleDateString("es-AR", {
              year: "numeric",
              month: "long",
              day: "numeric",
              hour: "2-digit",
              minute: "2-digit",
            })}
          </p>
        </div>

        {/* TIMELINE COMPONENT - Reemplaza al Status Card anterior */}
        <div
          style={{
            backgroundColor: "var(--panel)",
            borderRadius: "12px",
            padding: "1.5rem",
            marginBottom: "2rem",
            border: "1px solid var(--border)",
            boxShadow: "0 4px 6px -1px rgba(0, 0, 0, 0.1)",
          }}
        >
          <h3
            style={{
              color: "white",
              fontSize: "1.1rem",
              fontWeight: "bold",
              marginBottom: "1.5rem",
              borderBottom: "1px solid var(--border)",
              paddingBottom: "0.75rem",
            }}
          >
            Historial de Estados
          </h3>
          {renderStateHistory(order.status)}
        </div>

        {/* Botón de Cancelar (Solo si es pertinente) */}
        {(order.status === "PENDING" || order.status === "CONFIRMED") && (
          <button
            onClick={() => {
              if (confirm("¿Estás seguro de que deseas cancelar este pedido?")) {
                handleCancelOrder(order.id);
              }
            }}
            disabled={isCancelling}
            style={{
              width: "100%",
              padding: "0.75rem 1rem",
              marginBottom: "2rem",
              backgroundColor: "transparent",
              color: "#ef4444",
              border: "1px solid #ef4444",
              borderRadius: "8px",
              fontWeight: "600",
              cursor: isCancelling ? "not-allowed" : "pointer",
              opacity: isCancelling ? 0.6 : 1,
              transition: "background 0.2s",
            }}
            onMouseOver={(e) => (e.currentTarget.style.backgroundColor = "#ef444410")}
            onMouseOut={(e) => (e.currentTarget.style.backgroundColor = "transparent")}
          >
            {isCancelling ? "Procesando..." : "Cancelar Pedido"}
          </button>
        )}

        {/* Items List */}
        <div
          style={{
            backgroundColor: "var(--panel)",
            borderRadius: "8px",
            padding: "1.5rem",
            border: "1px solid var(--border)",
            marginBottom: "1.5rem",
          }}
        >
          <h3
            style={{
              color: "white",
              fontSize: "1rem",
              fontWeight: "bold",
              marginBottom: "1rem",
              display: "flex",
              alignItems: "center",
              gap: "0.5rem",
            }}
          >
            <Package size={18} />
            Artículos del Pedido ({order.items.length})
          </h3>
          <div style={{ display: "flex", flexDirection: "column", gap: "0.75rem" }}>
            {order.items.map((item, idx) => (
              <div
                key={idx}
                style={{
                  display: "flex",
                  justifyContent: "space-between",
                  alignItems: "center",
                  padding: "0.75rem",
                  backgroundColor: "var(--bg)",
                  borderRadius: "4px",
                  border: "1px solid var(--border)",
                }}
              >
                <div style={{ flex: 1 }}>
                  <div style={{ display: "flex", alignItems: "center", gap: "0.5rem", marginBottom: "0.25rem" }}>
                    <span style={{ color: "var(--accent)", fontWeight: "600" }}>{item.quantity}x</span>
                    <span style={{ color: "white" }}>{item.productName}</span>
                  </div>
                  <p style={{ color: "var(--muted)", fontSize: "0.75rem", margin: 0 }}>
                    ${(typeof item.unitPrice === "string" ? parseFloat(item.unitPrice) : item.unitPrice).toFixed(2)} c/u
                  </p>
                </div>
                <span style={{ color: "var(--muted)", fontSize: "0.875rem", textAlign: "right", minWidth: "80px" }}>
                  ${(typeof item.subtotal === "string" ? parseFloat(item.subtotal) : item.subtotal).toFixed(2)}
                </span>
              </div>
            ))}
          </div>
        </div>

        {/* Subtotal */}
        <div
          style={{
            backgroundColor: "var(--panel)",
            borderRadius: "8px",
            padding: "1rem",
            border: "1px solid var(--border)",
            marginBottom: "1.5rem",
          }}
        >
          <p
            style={{
              color: "var(--muted)",
              fontSize: "0.7rem",
              textTransform: "uppercase",
              marginBottom: "0.5rem",
            }}
          >
            Subtotal
          </p>
          <p style={{ color: "var(--accent)", fontSize: "1.5rem", fontWeight: "bold" }}>
            $
            {(typeof order.subtotal === "string"
              ? parseFloat(order.subtotal)
              : order.subtotal || order.totalAmount + (order.discountAmount || 0)
            ).toFixed(2)}
          </p>
        </div>

        {/* Applied Promotions Section */}
        {order.appliedPromotions && order.appliedPromotions.length > 0 && (
          <div
            style={{
              borderRadius: "8px",
              padding: "1.5rem",
              marginBottom: "1.5rem",
              backgroundColor: "#f59e0b20",
              border: "2px solid #f59e0b",
            }}
          >
            <div
              style={{
                display: "flex",
                alignItems: "center",
                gap: "0.5rem",
                marginBottom: "1rem",
              }}
            >
              <Gift size={20} style={{ color: "#f59e0b" }} />
              <h4
                style={{
                  color: "#f59e0b",
                  fontSize: "0.95rem",
                  fontWeight: "700",
                  margin: 0,
                }}
              >
                Promociones Aplicadas
              </h4>
            </div>

            <div
              style={{
                display: "flex",
                flexDirection: "column",
                gap: "0.75rem",
              }}
            >
              {order.appliedPromotions.map((promo, idx) => (
                <div
                  key={idx}
                  style={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                    padding: "0.75rem",
                    backgroundColor: "var(--bg)",
                    borderRadius: "6px",
                    border: "1px solid var(--border)",
                  }}
                >
                  <div style={{ flex: 1 }}>
                    <p
                      style={{
                        color: "white",
                        margin: 0,
                        fontSize: "0.9rem",
                        fontWeight: "600",
                      }}
                    >
                      {promo.name}
                    </p>
                    <p
                      style={{
                        color: "var(--muted)",
                        margin: "0.25rem 0 0 0",
                        fontSize: "0.75rem",
                      }}
                    >
                      {(() => {
                        const typeMap: Record<string, string> = {
                          BUY_X_GET_Y: "Compra X Lleva Y",
                          BUY_X_PAY_Y: "Compra X Paga Y",
                          PERCENTAGE_DISCOUNT: "Descuento %",
                          FIXED_DISCOUNT: "Descuento Fijo",
                          COMBO_DISCOUNT: "Descuento Combo",
                        };
                        return typeMap[promo.type] || promo.type;
                      })()}
                    </p>
                  </div>
                  <div
                    style={{
                      display: "flex",
                      alignItems: "center",
                      gap: "0.5rem",
                    }}
                  >
                    <TrendingDown size={16} style={{ color: "#10b981" }} />
                    <p
                      style={{
                        color: "#10b981",
                        margin: 0,
                        fontSize: "0.95rem",
                        fontWeight: "700",
                        minWidth: "70px",
                        textAlign: "right",
                      }}
                    >
                      -${Number(promo.discountAmount).toFixed(2)}
                    </p>
                  </div>
                </div>
              ))}
            </div>

            {/* Total discount summary */}
            {order.discountAmount && order.discountAmount > 0 && (
              <div
                style={{
                  marginTop: "1rem",
                  paddingTop: "1rem",
                  borderTop: "1px solid var(--border)",
                  display: "flex",
                  justifyContent: "space-between",
                  alignItems: "center",
                }}
              >
                <p
                  style={{
                    color: "var(--muted)",
                    margin: 0,
                    fontSize: "0.9rem",
                  }}
                >
                  Descuento Total
                </p>
                <p
                  style={{
                    color: "#10b981",
                    margin: 0,
                    fontSize: "1.1rem",
                    fontWeight: "700",
                  }}
                >
                  -$
                  {(typeof order.discountAmount === "string"
                    ? parseFloat(order.discountAmount)
                    : order.discountAmount
                  ).toFixed(2)}
                </p>
              </div>
            )}
          </div>
        )}

        {/* Total Final */}
        <div
          style={{
            backgroundColor: "var(--panel)",
            borderRadius: "8px",
            padding: "1rem",
            border: "1px solid var(--border)",
            marginBottom: "1.5rem",
          }}
        >
          <p
            style={{
              color: "var(--muted)",
              fontSize: "0.7rem",
              textTransform: "uppercase",
              marginBottom: "0.5rem",
            }}
          >
            Total Final
          </p>
          <p style={{ color: "var(--accent)", fontSize: "1.5rem", fontWeight: "bold" }}>
            ${(typeof order.totalAmount === "string" ? parseFloat(order.totalAmount) : order.totalAmount).toFixed(2)}
          </p>
        </div>

        {/* Timeline Section */}
        {(order.confirmedAt || order.deliveredAt || order.rejectionReason) && (
          <div
            style={{
              backgroundColor: "var(--panel)",
              borderRadius: "8px",
              padding: "1.5rem",
              border: "1px solid var(--border)",
              marginBottom: "2rem",
            }}
          >
            <h3
              style={{
                color: "white",
                fontSize: "1rem",
                fontWeight: "bold",
                marginBottom: "1rem",
              }}
            >
              Historial del Pedido
            </h3>
            <div style={{ display: "flex", flexDirection: "column", gap: "1rem" }}>
              {/* Created */}
              <div style={{ display: "flex", gap: "1rem" }}>
                <div style={{ display: "flex", flexDirection: "column", alignItems: "center" }}>
                  <div
                    style={{
                      width: "8px",
                      height: "8px",
                      borderRadius: "50%",
                      backgroundColor: "var(--accent)",
                    }}
                  />
                  {(order.confirmedAt || order.deliveredAt) && (
                    <div
                      style={{
                        width: "2px",
                        flex: 1,
                        backgroundColor: "var(--border)",
                        margin: "4px 0",
                      }}
                    />
                  )}
                </div>
                <div style={{ flex: 1, paddingBottom: "0.5rem" }}>
                  <p style={{ color: "white", margin: 0, fontSize: "0.875rem" }}>Pedido creado</p>
                  <p style={{ color: "var(--muted)", margin: "0.25rem 0 0 0", fontSize: "0.75rem" }}>
                    {new Date(order.createdAt).toLocaleDateString("es-AR", {
                      month: "long",
                      day: "numeric",
                      hour: "2-digit",
                      minute: "2-digit",
                    })}
                  </p>
                </div>
              </div>

              {/* Confirmed */}
              {order.confirmedAt && (
                <div style={{ display: "flex", gap: "1rem" }}>
                  <div style={{ display: "flex", flexDirection: "column", alignItems: "center" }}>
                    <div
                      style={{
                        width: "8px",
                        height: "8px",
                        borderRadius: "50%",
                        backgroundColor: "#3b82f6",
                      }}
                    />
                    {order.deliveredAt && (
                      <div
                        style={{
                          width: "2px",
                          flex: 1,
                          backgroundColor: "var(--border)",
                          margin: "4px 0",
                        }}
                      />
                    )}
                  </div>
                  <div style={{ flex: 1, paddingBottom: "0.5rem" }}>
                    <p style={{ color: "white", margin: 0, fontSize: "0.875rem" }}>Pedido confirmado</p>
                    <p style={{ color: "var(--muted)", margin: "0.25rem 0 0 0", fontSize: "0.75rem" }}>
                      {new Date(order.confirmedAt).toLocaleDateString("es-AR", {
                        month: "long",
                        day: "numeric",
                        hour: "2-digit",
                        minute: "2-digit",
                      })}
                    </p>
                  </div>
                </div>
              )}

              {/* Delivered */}
              {order.deliveredAt && (
                <div style={{ display: "flex", gap: "1rem" }}>
                  <div style={{ display: "flex", flexDirection: "column", alignItems: "center" }}>
                    <div
                      style={{
                        width: "8px",
                        height: "8px",
                        borderRadius: "50%",
                        backgroundColor: "#10b981",
                      }}
                    />
                  </div>
                  <div style={{ flex: 1 }}>
                    <p style={{ color: "white", margin: 0, fontSize: "0.875rem" }}>Pedido entregado</p>
                    <p style={{ color: "var(--muted)", margin: "0.25rem 0 0 0", fontSize: "0.75rem" }}>
                      {new Date(order.deliveredAt).toLocaleDateString("es-AR", {
                        month: "long",
                        day: "numeric",
                        hour: "2-digit",
                        minute: "2-digit",
                      })}
                    </p>
                  </div>
                </div>
              )}

              {/* Rejection Reason */}
              {order.rejectionReason && (
                <div
                  style={{
                    marginTop: "0.5rem",
                    padding: "1rem",
                    backgroundColor: "#ef444420",
                    border: "1px solid #ef4444",
                    borderRadius: "6px",
                  }}
                >
                  <p style={{ color: "#ef4444", margin: 0, fontSize: "0.875rem", fontWeight: "600" }}>
                    Razón del rechazo:
                  </p>
                  <p style={{ color: "var(--muted)", margin: "0.5rem 0 0 0", fontSize: "0.75rem" }}>
                    {order.rejectionReason}
                  </p>
                </div>
              )}
            </div>
          </div>
        )}

        {/* Back Button */}
        <button
          onClick={() => setLocation("/orders")}
          style={{
            display: "flex",
            alignItems: "center",
            gap: "0.5rem",
            color: "var(--accent)",
            background: "none",
            border: "none",
            cursor: "pointer",
            fontSize: "0.875rem",
            fontWeight: "500",
          }}
        >
          <ArrowLeft size={16} />
          <span>Volver a mis pedidos</span>
        </button>
      </div>
    </div>
  );
};
