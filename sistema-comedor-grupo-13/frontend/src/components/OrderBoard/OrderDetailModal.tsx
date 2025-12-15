import { AlertCircle, Calendar, CheckCircle, ChevronRight, Clock, Eye, EyeOff, Gift, Mail, TrendingDown, User, X } from "lucide-react";
import { useState } from "react";

import { orderAPI } from "@/services/OrderAPI";

import { StaffOrder } from "./OrderBoard";

interface OrderDetailModalProps {
  order: StaffOrder | null;
  isOpen: boolean;
  onClose: () => void;
  onStatusChange: (orderId: number, newStatus: string) => void;
  token?: string;
  isLoading?: boolean;
  onRefresh?: () => Promise<void>;
  hiddenOrderIds?: Set<number>;
  onToggleHideOrder?: (orderId: number) => void;
}

const STATUS_CONFIG: Record<
  string,
  {
    label: string;
    color: string;
    icon: React.ReactNode;
    nextStatus?: string;
    nextLabel?: string;
    canBeRejected?: boolean;
  }
> = {
  PENDING: {
    label: "Pendiente",
    color: "#fbbf24",
    icon: <Clock size={18} />,
    nextStatus: "CONFIRMED",
    nextLabel: "Confirmar Preparación",
    canBeRejected: true,
  },
  CONFIRMED: {
    label: "Confirmado",
    color: "#3b82f6",
    icon: <CheckCircle size={18} />,
    nextStatus: "PREPARING",
    nextLabel: "Comenzar Preparación",
    canBeRejected: true,
  },
  PREPARING: {
    label: "En Preparación",
    color: "#f59e0b",
    icon: <CheckCircle size={18} />,
    nextStatus: "READY",
    nextLabel: "Marcar Listo",
    canBeRejected: true,
  },
  READY: {
    label: "Listo",
    color: "#06b6d4",
    icon: <CheckCircle size={18} />,
    nextStatus: "COMPLETED",
    nextLabel: "Marcar Entregado",
    canBeRejected: false,
  },
  COMPLETED: {
    label: "Entregado",
    color: "#10b981",
    icon: <CheckCircle size={18} />,
    canBeRejected: false,
  },
  REJECTED: {
    label: "Rechazado",
    color: "#ef4444",
    icon: <CheckCircle size={18} />,
    canBeRejected: false,
  },
  CANCELLED: {
    label: "Cancelado",
    color: "#6b7280",
    icon: <CheckCircle size={18} />,
    canBeRejected: false,
  },
};

const getStatusTimeline = (order: StaffOrder) => {
  const events = [
    { status: "PENDING", label: "Creado", time: order.createdAt, completed: true },
    {
      status: "CONFIRMED",
      label: "Confirmado",
      time: order.updatedAt,
      completed: ["CONFIRMED", "PREPARING", "READY", "COMPLETED"].includes(order.status),
    },
    {
      status: "PREPARING",
      label: "En Preparación",
      time: order.updatedAt,
      completed: ["PREPARING", "READY", "COMPLETED"].includes(order.status),
    },
    {
      status: "READY",
      label: "Listo",
      time: order.updatedAt,
      completed: ["READY", "COMPLETED"].includes(order.status),
    },
    {
      status: "COMPLETED",
      label: "Entregado",
      time: order.updatedAt,
      completed: order.status === "COMPLETED",
    },
  ];

  return events;
};

export const OrderDetailModal: React.FC<OrderDetailModalProps> = ({
  order,
  isOpen,
  onClose,
  onStatusChange,
  token,
  isLoading,
  onRefresh,
  hiddenOrderIds,
  onToggleHideOrder,
}) => {
  const [isChangingStatus, setIsChangingStatus] = useState(false);
  const [showRejectForm, setShowRejectForm] = useState(false);
  const [rejectReason, setRejectReason] = useState("");
  const [isRejecting, setIsRejecting] = useState(false);

  if (!isOpen || !order) return null;

  const currentStatusConfig = STATUS_CONFIG[order.status];
  const timeline = getStatusTimeline(order);
  const isOrderFinished = ["COMPLETED", "REJECTED", "CANCELLED"].includes(order.status);
  const isOrderHidden = hiddenOrderIds?.has(order.id) ?? false;

  const handleStatusChange = async () => {
    if (currentStatusConfig.nextStatus) {
      setIsChangingStatus(true);
      try {
        onStatusChange(order.id, currentStatusConfig.nextStatus);
        // Pequeño delay para que la UI se actualice
        setTimeout(() => {
          setIsChangingStatus(false);
        }, 500);
      } catch (error) {
        console.error("Error changing status:", error);
        setIsChangingStatus(false);
      }
    }
  };

  const handleRejectOrder = async () => {
    if (!order || !rejectReason.trim()) {
      alert("Por favor ingresa un motivo para rechazar");
      return;
    }

    setIsRejecting(true);
    try {
      await orderAPI.rejectOrder(order.id, rejectReason, token);
      // El backend ya rechazó la orden exitosamente
      setShowRejectForm(false);
      setRejectReason("");
      alert("Pedido rechazado exitosamente");
      // Refrescar la lista y cerrar el modal
      if (onRefresh) {
        await onRefresh();
      }
      onClose();
    } catch (error) {
      alert(`Error: ${error instanceof Error ? error.message : "Error desconocido"}`);
      console.error("Error rejecting order:", error);
    } finally {
      setIsRejecting(false);
    }
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleString("es-AR", {
      year: "numeric",
      month: "long",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  return (
    <div
      style={{
        position: "fixed",
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        backgroundColor: "rgba(0, 0, 0, 0.7)",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        zIndex: 1000,
        padding: "1rem",
      }}
      onClick={onClose}
    >
      <div
        style={{
          backgroundColor: "var(--panel)",
          borderRadius: "12px",
          maxWidth: "600px",
          width: "100%",
          maxHeight: "90vh",
          overflow: "auto",
          boxShadow: "0 20px 60px rgba(0, 0, 0, 0.3)",
        }}
        onClick={(e) => e.stopPropagation()}
      >
        {/* Modal Header */}
        <div
          style={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "flex-start",
            padding: "1.5rem",
            borderBottom: "1px solid var(--border)",
            backgroundColor: "var(--bg)",
            borderRadius: "12px 12px 0 0",
          }}
        >
          <div>
            <h2 style={{ color: "white", fontSize: "1.5rem", fontWeight: "bold", margin: "0 0 0.5rem 0" }}>
              {order.orderNumber}
            </h2>
            <div
              style={{
                display: "inline-flex",
                alignItems: "center",
                gap: "0.5rem",
                paddingInline: "0.75rem",
                paddingBlock: "0.4rem",
                borderRadius: "20px",
                backgroundColor: `${currentStatusConfig.color}20`,
                border: `2px solid ${currentStatusConfig.color}`,
              }}
            >
              <span style={{ color: currentStatusConfig.color }}>{currentStatusConfig.icon}</span>
              <span style={{ color: currentStatusConfig.color, fontSize: "0.875rem", fontWeight: "600" }}>
                {currentStatusConfig.label}
              </span>
            </div>
          </div>
          <button
            onClick={onClose}
            style={{
              background: "none",
              border: "none",
              color: "var(--muted)",
              cursor: "pointer",
              padding: "0.5rem",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              transition: "color 0.2s",
            }}
            onMouseEnter={(e) => (e.currentTarget.style.color = "white")}
            onMouseLeave={(e) => (e.currentTarget.style.color = "var(--muted)")}
          >
            <X size={24} />
          </button>
        </div>

        {/* Modal Body */}
        <div style={{ padding: "1.5rem", display: "flex", flexDirection: "column", gap: "1.5rem" }}>
          {/* Customer Info */}
          <div
            style={{
              backgroundColor: "var(--bg)",
              border: "1px solid var(--border)",
              borderRadius: "8px",
              padding: "1rem",
            }}
          >
            <h3
              style={{
                color: "var(--accent)",
                fontSize: "0.875rem",
                textTransform: "uppercase",
                fontWeight: "600",
                margin: "0 0 1rem 0",
              }}
            >
              Información del Cliente
            </h3>
            <div style={{ display: "flex", flexDirection: "column", gap: "0.75rem" }}>
              {order.studentName && (
                <div style={{ display: "flex", alignItems: "center", gap: "0.75rem", color: "white" }}>
                  <User size={18} />
                  <span>{order.studentName}</span>
                </div>
              )}
              {order.studentEmail && (
                <div style={{ display: "flex", alignItems: "center", gap: "0.75rem", color: "var(--muted)" }}>
                  <Mail size={18} />
                  <span>{order.studentEmail}</span>
                </div>
              )}
              <div style={{ display: "flex", alignItems: "center", gap: "0.75rem", color: "var(--muted)" }}>
                <Calendar size={18} />
                <span>{formatDate(order.createdAt)}</span>
              </div>
            </div>
          </div>

          {/* Items Table */}
          <div>
            <h3
              style={{
                color: "var(--accent)",
                fontSize: "0.875rem",
                textTransform: "uppercase",
                fontWeight: "600",
                margin: "0 0 1rem 0",
              }}
            >
              Ítems del Pedido
            </h3>
            <div style={{ overflowX: "auto" }}>
              <table style={{ width: "100%", borderCollapse: "collapse" }}>
                <thead>
                  <tr style={{ borderBottom: "2px solid var(--border)" }}>
                    <th
                      style={{
                        textAlign: "left",
                        padding: "0.75rem",
                        color: "var(--muted)",
                        fontSize: "0.75rem",
                        textTransform: "uppercase",
                        fontWeight: "600",
                      }}
                    >
                      Ítem
                    </th>
                    <th
                      style={{
                        textAlign: "center",
                        padding: "0.75rem",
                        color: "var(--muted)",
                        fontSize: "0.75rem",
                        textTransform: "uppercase",
                        fontWeight: "600",
                      }}
                    >
                      Cant.
                    </th>
                    <th
                      style={{
                        textAlign: "right",
                        padding: "0.75rem",
                        color: "var(--muted)",
                        fontSize: "0.75rem",
                        textTransform: "uppercase",
                        fontWeight: "600",
                      }}
                    >
                      Precio
                    </th>
                    <th
                      style={{
                        textAlign: "right",
                        padding: "0.75rem",
                        color: "var(--muted)",
                        fontSize: "0.75rem",
                        textTransform: "uppercase",
                        fontWeight: "600",
                      }}
                    >
                      Subtotal
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {order.items.map((item, idx) => (
                    <tr
                      key={idx}
                      style={{
                        borderBottom: "1px solid var(--border)",
                        backgroundColor: idx % 2 === 0 ? "transparent" : "var(--bg)",
                        borderLeft: "none",
                      }}
                    >
                      <td style={{ padding: "0.75rem", color: "white", fontSize: "0.875rem", fontWeight: "normal" }}>
                        {item.productName}
                      </td>
                      <td
                        style={{ textAlign: "center", padding: "0.75rem", color: "var(--muted)", fontSize: "0.875rem" }}
                      >
                        {item.quantity}
                      </td>
                      <td
                        style={{ textAlign: "right", padding: "0.75rem", color: "var(--muted)", fontSize: "0.875rem" }}
                      >
                        ${(typeof item.unitPrice === "string" ? parseFloat(item.unitPrice) : item.unitPrice).toFixed(2)}
                      </td>
                      <td
                        style={{
                          textAlign: "right",
                          padding: "0.75rem",
                          color: "var(--accent)",
                          fontSize: "0.875rem",
                          fontWeight: "600",
                        }}
                      >
                        {`$${(typeof item.subtotal === "string" ? parseFloat(item.subtotal) : item.subtotal).toFixed(2)}`}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {/* Total and Promotions */}
            <div
              style={{
                display: "flex",
                flexDirection: "column",
                gap: "1.5rem",
                marginTop: "1rem",
              }}
            >
              {/* Subtotal */}
              <div
                style={{
                  display: "flex",
                  justifyContent: "flex-end",
                  paddingTop: "1rem",
                  borderTop: "2px solid var(--border)",
                }}
              >
                <div style={{ textAlign: "right" }}>
                  <p style={{ color: "var(--muted)", fontSize: "0.875rem", margin: "0 0 0.5rem 0" }}>Subtotal:</p>
                  <p style={{ color: "var(--accent)", fontSize: "1.5rem", fontWeight: "700", margin: 0 }}>
                    $
                    {order.items
                      .reduce((sum, item) => {
                        const subtotal = typeof item.subtotal === "string" ? parseFloat(item.subtotal) : item.subtotal;
                        return sum + (subtotal || 0);
                      }, 0)
                      .toFixed(2)}
                  </p>
                </div>
              </div>

              {/* Applied Promotions */}
              {order.appliedPromotions && order.appliedPromotions.length > 0 && (
                <div
                  style={{
                    borderRadius: "8px",
                    padding: "1.5rem",
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
                          <TrendingDown
                            size={16}
                            style={{ color: "#10b981" }}
                          />
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
                        -${(typeof order.discountAmount === "string"
                          ? parseFloat(order.discountAmount)
                          : order.discountAmount).toFixed(2)}
                      </p>
                    </div>
                  )}
                </div>
              )}

              {/* Total Final */}
              <div
                style={{
                  display: "flex",
                  justifyContent: "flex-end",
                  paddingTop: "1rem",
                  borderTop: "2px solid var(--border)",
                }}
              >
                <div style={{ textAlign: "right" }}>
                  <p style={{ color: "var(--muted)", fontSize: "0.875rem", margin: "0 0 0.5rem 0" }}>Total Final:</p>
                  <p style={{ color: "var(--ok)", fontSize: "1.5rem", fontWeight: "700", margin: 0 }}>
                    $
                    {(typeof order.totalAmount === "string"
                      ? parseFloat(order.totalAmount)
                      : order.totalAmount
                    ).toFixed(2)}
                  </p>
                </div>
              </div>
            </div>
          </div>

          {/* Timeline */}
          <div>
            <h3
              style={{
                color: "var(--accent)",
                fontSize: "0.875rem",
                textTransform: "uppercase",
                fontWeight: "600",
                margin: "0 0 1rem 0",
              }}
            >
              Historial de Estados
            </h3>
            <div style={{ position: "relative", paddingLeft: "2rem" }}>
              {timeline.map((event, idx) => (
                <div
                  key={idx}
                  style={{
                    display: "flex",
                    gap: "1rem",
                    marginBottom: idx < timeline.length - 1 ? "1.5rem" : "0",
                  }}
                >
                  {/* Timeline dot */}
                  <div
                    style={{
                      position: "absolute",
                      left: 0,
                      top: "0.25rem",
                      width: "12px",
                      height: "12px",
                      borderRadius: "50%",
                      backgroundColor: event.completed ? "var(--ok)" : "var(--border)",
                      border: "2px solid var(--bg)",
                    }}
                  ></div>

                  {/* Timeline connector */}
                  {idx < timeline.length - 1 && (
                    <div
                      style={{
                        position: "absolute",
                        left: "5px",
                        top: "12px",
                        width: "2px",
                        height: "calc(100% + 1.5rem)",
                        backgroundColor: event.completed ? "var(--ok)" : "var(--border)",
                      }}
                    ></div>
                  )}

                  <div style={{ flex: 1 }}>
                    <p style={{ color: "white", fontSize: "0.875rem", fontWeight: "600", margin: 0 }}>{event.label}</p>
                    {event.time && event.completed && (
                      <p style={{ color: "var(--muted)", fontSize: "0.75rem", margin: "0.25rem 0 0 0" }}>
                        {formatDate(event.time)}
                      </p>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Action Buttons */}
          {!showRejectForm && currentStatusConfig.nextStatus && (
            <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "1rem", marginTop: "1rem" }}>
              <button
                onClick={handleStatusChange}
                disabled={isChangingStatus || isLoading}
                style={{
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                  gap: "0.5rem",
                  backgroundColor: "var(--accent)",
                  color: "white",
                  padding: "0.75rem 1rem",
                  borderRadius: "8px",
                  border: "none",
                  fontWeight: "600",
                  cursor: isChangingStatus || isLoading ? "not-allowed" : "pointer",
                  opacity: isChangingStatus || isLoading ? 0.6 : 1,
                  transition: "all 0.2s",
                }}
                onMouseEnter={(e) => {
                  if (!isChangingStatus && !isLoading) {
                    (e.currentTarget as HTMLElement).style.opacity = "0.9";
                  }
                }}
                onMouseLeave={(e) => {
                  if (!isChangingStatus && !isLoading) {
                    (e.currentTarget as HTMLElement).style.opacity = "1";
                  }
                }}
              >
                {isChangingStatus ? (
                  <>
                    <div
                      style={{
                        width: "16px",
                        height: "16px",
                        border: "2px solid transparent",
                        borderTopColor: "white",
                        borderRadius: "50%",
                        animation: "spin 0.6s linear infinite",
                      }}
                    ></div>
                    Actualizando...
                  </>
                ) : (
                  <>
                    <ChevronRight size={18} />
                    {currentStatusConfig.nextLabel}
                  </>
                )}
              </button>
              {currentStatusConfig.canBeRejected && (
                <button
                  onClick={() => setShowRejectForm(true)}
                  style={{
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    gap: "0.5rem",
                    backgroundColor: "#ef444420",
                    color: "#ef4444",
                    padding: "0.75rem 1rem",
                    borderRadius: "8px",
                    border: "1px solid #ef4444",
                    fontWeight: "600",
                    cursor: "pointer",
                    transition: "all 0.2s",
                  }}
                  onMouseEnter={(e) => {
                    (e.currentTarget as HTMLElement).style.backgroundColor = "#ef4444";
                    (e.currentTarget as HTMLElement).style.color = "white";
                  }}
                  onMouseLeave={(e) => {
                    (e.currentTarget as HTMLElement).style.backgroundColor = "#ef444420";
                    (e.currentTarget as HTMLElement).style.color = "#ef4444";
                  }}
                >
                  <AlertCircle size={18} />
                  Rechazar
                </button>
              )}
            </div>
          )}

          {/* Reject Form */}
          {showRejectForm && (
            <div
              style={{
                backgroundColor: "#ef444120",
                border: "1px solid #ef4444",
                borderRadius: "8px",
                padding: "1rem",
                marginTop: "1rem",
              }}
            >
              <h4
                style={{ color: "#ef4444", margin: "0 0 1rem 0", display: "flex", alignItems: "center", gap: "0.5rem" }}
              >
                <AlertCircle size={18} />
                Rechazar Pedido
              </h4>
              <textarea
                value={rejectReason}
                onChange={(e) => setRejectReason(e.target.value)}
                placeholder="Ingresa el motivo del rechazo..."
                style={{
                  width: "100%",
                  minHeight: "80px",
                  padding: "0.75rem",
                  borderRadius: "6px",
                  border: "1px solid var(--border)",
                  backgroundColor: "var(--bg)",
                  color: "white",
                  marginBottom: "1rem",
                  fontSize: "0.875rem",
                  fontFamily: "inherit",
                  resize: "vertical",
                }}
              />
              <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "1rem" }}>
                <button
                  onClick={handleRejectOrder}
                  disabled={isRejecting || !rejectReason.trim()}
                  style={{
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    gap: "0.5rem",
                    backgroundColor: "#ef4444",
                    color: "white",
                    padding: "0.75rem 1rem",
                    borderRadius: "6px",
                    border: "none",
                    fontWeight: "600",
                    cursor: isRejecting ? "not-allowed" : "pointer",
                    opacity: isRejecting ? 0.6 : 1,
                  }}
                >
                  {isRejecting ? "Rechazando..." : "Confirmar Rechazo"}
                </button>
                <button
                  onClick={() => {
                    setShowRejectForm(false);
                    setRejectReason("");
                  }}
                  style={{
                    backgroundColor: "var(--bg)",
                    color: "var(--muted)",
                    padding: "0.75rem 1rem",
                    borderRadius: "6px",
                    border: "1px solid var(--border)",
                    fontWeight: "600",
                    cursor: "pointer",
                  }}
                >
                  Cancelar
                </button>
              </div>
            </div>
          )}

          {!currentStatusConfig.nextStatus && !showRejectForm && (
            <div
              style={{
                backgroundColor: "#10b98120",
                border: "1px solid #10b981",
                borderRadius: "8px",
                padding: "1rem",
                textAlign: "center",
                marginTop: "1rem",
              }}
            >
              <p
                style={{
                  color: "#10b981",
                  margin: 0,
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                  gap: "0.5rem",
                }}
              >
                <CheckCircle size={18} />
                <strong>Pedido completado</strong>
              </p>
            </div>
          )}

          {/* Hide/Unhide Button for finished orders */}
          {isOrderFinished && onToggleHideOrder && (
            <button
              onClick={() => {
                onToggleHideOrder(order.id);
                onClose();
              }}
              style={{
                width: "100%",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                gap: "0.5rem",
                backgroundColor: isOrderHidden ? "#3b82f620" : "#64748b20",
                color: isOrderHidden ? "#3b82f6" : "#64748b",
                padding: "0.75rem 1rem",
                borderRadius: "8px",
                border: isOrderHidden ? "1px solid #3b82f6" : "1px solid #64748b",
                fontWeight: "600",
                cursor: "pointer",
                marginTop: "1rem",
                transition: "all 0.2s",
              }}
              onMouseEnter={(e) => {
                (e.currentTarget as HTMLElement).style.backgroundColor = isOrderHidden ? "#3b82f6" : "#64748b";
                (e.currentTarget as HTMLElement).style.color = "white";
              }}
              onMouseLeave={(e) => {
                (e.currentTarget as HTMLElement).style.backgroundColor = isOrderHidden ? "#3b82f620" : "#64748b20";
                (e.currentTarget as HTMLElement).style.color = isOrderHidden ? "#3b82f6" : "#64748b";
              }}
            >
              {isOrderHidden ? (
                <>
                  <Eye size={18} />
                  Mostrar Orden
                </>
              ) : (
                <>
                  <EyeOff size={18} />
                  Ocultar Orden
                </>
              )}
            </button>
          )}

          {!currentStatusConfig.nextStatus && !showRejectForm && !isOrderFinished && (
            <div
              style={{
                backgroundColor: "#10b98120",
                border: "1px solid #10b981",
                borderRadius: "8px",
                padding: "1rem",
                textAlign: "center",
                marginTop: "1rem",
              }}
            >
              <p
                style={{
                  color: "#10b981",
                  margin: 0,
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                  gap: "0.5rem",
                }}
              >
                <CheckCircle size={18} />
                <strong>Pedido completado</strong>
              </p>
            </div>
          )}

          {/* Close Button when no actions available */}
          {!currentStatusConfig.nextStatus && !showRejectForm && (
            <button
              onClick={onClose}
              style={{
                width: "100%",
                backgroundColor: "var(--accent)",
                color: "white",
                padding: "0.75rem 1rem",
                borderRadius: "8px",
                border: "none",
                fontWeight: "600",
                cursor: "pointer",
                marginTop: "1rem",
              }}
            >
              Cerrar
            </button>
          )}
        </div>

        {/* CSS para la animación de spin */}
        <style>
          {`
            @keyframes spin {
              to { transform: rotate(360deg); }
            }
          `}
        </style>
      </div>
    </div>
  );
};
