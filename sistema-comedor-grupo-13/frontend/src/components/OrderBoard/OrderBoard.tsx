import { ChevronLeft, ChevronRight, Clock, Eye, Package, Users } from "lucide-react";
import { useCallback, useState } from "react";

import { type AppliedPromotionInfo } from "@/services/OrderAPI";
import styles from "./order-board.module.css";

export interface StaffOrder {
  id: number;
  orderNumber: string;
  status: "PENDING" | "CONFIRMED" | "PREPARING" | "READY" | "COMPLETED" | "REJECTED" | "CANCELLED";
  totalAmount: number;
  discountAmount?: number;
  appliedPromotions?: AppliedPromotionInfo[];
  items: Array<{
    productId: number;
    productName: string;
    quantity: number;
    unitPrice: number;
    subtotal: number;
  }>;
  createdAt: string;
  updatedAt?: string;
  studentName?: string;
  studentEmail?: string;
}

interface OrderBoardProps {
  orders: StaffOrder[];
  onOrderClick: (order: StaffOrder) => void;
  onStatusChange: (orderId: number, newStatus: string) => void;
  onMoveBackward: (orderId: number) => void;
  isLoading?: boolean;
  isRefreshing?: boolean;
}

interface ColumnConfig {
  id: string;
  title: string;
  status: "PENDING" | "CONFIRMED" | "PREPARING" | "READY" | "COMPLETED" | "REJECTED" | "CANCELLED";
  color: string;
  badgeClass: string;
  nextStatus?: string;
  actionLabel?: string;
  icon: React.ReactNode;
}

const COLUMNS: ColumnConfig[] = [
  {
    id: "pending",
    title: "Pendientes",
    status: "PENDING",
    color: "#fbbf24",
    badgeClass: styles.columnBadgePending,
    nextStatus: "CONFIRMED",
    actionLabel: "Confirmar Preparación",
    icon: <Clock size={18} />,
  },
  {
    id: "confirmed",
    title: "Confirmados",
    status: "CONFIRMED",
    color: "#3b82f6",
    badgeClass: styles.columnBadgePreparing,
    nextStatus: "PREPARING",
    actionLabel: "Comenzar Preparación",
    icon: <Package size={18} />,
  },
  {
    id: "preparing",
    title: "En Preparación",
    status: "PREPARING",
    color: "#f59e0b",
    badgeClass: styles.columnBadgePreparing,
    nextStatus: "READY",
    actionLabel: "Marcar Listo",
    icon: <Package size={18} />,
  },
  {
    id: "ready",
    title: "Listos",
    status: "READY",
    color: "#06b6d4",
    badgeClass: styles.columnBadgeReady,
    nextStatus: "COMPLETED",
    actionLabel: "Marcar Entregado",
    icon: <ChevronRight size={18} />,
  },
  {
    id: "completed",
    title: "Completados",
    status: "COMPLETED",
    color: "#10b981",
    badgeClass: styles.columnBadgeReady,
    icon: <ChevronRight size={18} />,
  },
  {
    id: "rejected",
    title: "Rechazados",
    status: "REJECTED",
    color: "#ef4444",
    badgeClass: styles.columnBadgeCompleted,
    icon: <Users size={18} />,
  },
  {
    id: "cancelled",
    title: "Cancelados",
    status: "CANCELLED",
    color: "#6b7280",
    badgeClass: styles.columnBadgeCompleted,
    icon: <Users size={18} />,
  },
];

const getRelativeTime = (dateString: string): string => {
  const date = new Date(dateString);
  const now = new Date();
  const diffMs = now.getTime() - date.getTime();
  const diffMins = Math.floor(diffMs / 60000);
  const diffHours = Math.floor(diffMins / 60);
  const diffDays = Math.floor(diffHours / 24);

  if (diffMins < 1) return "hace unos segundos";
  if (diffMins < 60) return `hace ${diffMins} min`;
  if (diffHours < 24) return `hace ${diffHours}h`;
  return `hace ${diffDays}d`;
};

export const OrderBoard: React.FC<OrderBoardProps> = ({
  orders,
  onOrderClick,
  onStatusChange,
  onMoveBackward,
  isLoading,
  isRefreshing,
}) => {
  const [expandedCard, setExpandedCard] = useState<number | null>(null);

  const filterOrdersByStatus = useCallback(
    (status: string) => {
      return orders.filter((order) => order.status === status);
    },
    [orders],
  );

  const handleActionClick = (e: React.MouseEvent, orderId: number, nextStatus?: string) => {
    e.stopPropagation();
    if (nextStatus) {
      onStatusChange(orderId, nextStatus);
    }
  };

  const handleBackwardClick = (e: React.MouseEvent, orderId: number) => {
    e.stopPropagation();
    onMoveBackward(orderId);
  };

  const Column: React.FC<{
    config: ColumnConfig;
    columnOrders: StaffOrder[];
    isNew?: Set<number>;
  }> = ({ config, columnOrders, isNew }) => (
    <div className={styles.column}>
      <div className={styles.columnHeader}>
        <div style={{ display: "flex", alignItems: "center", gap: "0.75rem" }}>
          <span style={{ color: config.color }}>{config.icon}</span>
          <h3 className={styles.columnTitle}>{config.title}</h3>
        </div>
        <div className={`${styles.columnBadge} ${config.badgeClass}`}>{columnOrders.length}</div>
      </div>

      <div className={styles.cardsContainer}>
        {columnOrders.length === 0 ? (
          <div className={styles.emptyState}>
            <div className={styles.emptyStateIcon}>{config.icon}</div>
            <p className={styles.emptyStateText}>Sin órdenes</p>
          </div>
        ) : (
          columnOrders.map((order) => (
            <div
              key={order.id}
              className={`${styles.orderCard} ${isNew?.has(order.id) ? styles.newOrder : ""}`}
              onClick={() => onOrderClick(order)}
              onMouseEnter={() => setExpandedCard(order.id)}
              onMouseLeave={() => setExpandedCard(null)}
            >
              {/* Card Header */}
              <div className={styles.cardHeader}>
                <div>
                  <h4 className={styles.orderNumber}>{order.orderNumber}</h4>
                  <p className={styles.orderTime}>{getRelativeTime(order.createdAt)}</p>
                </div>
                <div style={{ textAlign: "right" }}>
                  <p className={styles.orderAmount}>
                    $
                    {(typeof order.totalAmount === "string"
                      ? parseFloat(order.totalAmount)
                      : order.totalAmount
                    ).toFixed(2)}
                  </p>
                </div>
              </div>

              {/* Card Content */}
              <div className={styles.cardContent}>
                {order.studentName && <p className={styles.customerName}>{order.studentName}</p>}
                <div className={styles.itemsInfo}>
                  <span className={styles.itemCount}>{order.items.length}</span>
                  <span>
                    {order.items.length} {order.items.length === 1 ? "ítem" : "ítems"}
                  </span>
                </div>
              </div>

              {/* Card Actions */}
              <div className={styles.cardActions}>
                {expandedCard === order.id ? (
                  <>
                    {config.status !== "PENDING" && config.status !== "COMPLETED" && config.status !== "REJECTED" && config.status !== "CANCELLED" && (
                      <button
                        className={`${styles.actionButton} ${styles.backward}`}
                        onClick={(e) => handleBackwardClick(e, order.id)}
                        title="Retroceder estado"
                      >
                        <ChevronLeft size={14} />
                        <span style={{ fontSize: "0.65rem" }}>Atrás</span>
                      </button>
                    )}
                    {config.nextStatus && config.actionLabel && (
                      <button
                        className={`${styles.actionButton} ${styles.primary}`}
                        onClick={(e) => handleActionClick(e, order.id, config.nextStatus)}
                        title={config.actionLabel}
                      >
                        <ChevronRight size={14} />
                        <span style={{ fontSize: "0.65rem" }}>{config.actionLabel.split(" ")[0]}</span>
                      </button>
                    )}
                    <button
                      className={`${styles.actionButton} ${styles.secondary}`}
                      onClick={(e) => {
                        e.stopPropagation();
                        onOrderClick(order);
                      }}
                      title="Ver detalle"
                    >
                      <Eye size={14} />
                    </button>
                  </>
                ) : (
                  <button
                    className={`${styles.actionButton} ${styles.secondary}`}
                    onClick={(e) => {
                      e.stopPropagation();
                      onOrderClick(order);
                    }}
                    title="Ver detalle"
                  >
                    <Eye size={14} />
                    Ver detalle
                  </button>
                )}
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );

  if (isLoading) {
    return (
      <div className={styles.kanbanContainer}>
        {COLUMNS.map((config) => (
          <div key={config.id} className={styles.column}>
            <div className={styles.columnHeader}>
              <h3 className={styles.columnTitle}>{config.title}</h3>
              <div className={`${styles.columnBadge} ${config.badgeClass}`}>-</div>
            </div>
            <div className={styles.cardsContainer}>
              {[...Array(3)].map((_, idx) => (
                <div key={idx} className={`${styles.orderCard} ${styles.loadingCard}`}>
                  <div className={styles.cardHeader}>
                    <h4 className={styles.orderNumber}>ORD-...</h4>
                    <p className={styles.orderAmount}>$...</p>
                  </div>
                  <div className={styles.cardContent}>
                    <p
                      className={styles.customerName}
                      style={{ background: "var(--bg)", height: "1rem", borderRadius: "4px", marginBottom: "0.5rem" }}
                    ></p>
                  </div>
                </div>
              ))}
            </div>
          </div>
        ))}
      </div>
    );
  }

  const newOrders = new Set<number>();

  return (
    <div style={{ position: "relative" }}>
      {isRefreshing && (
        <div className={styles.refreshIndicator} style={{ position: "absolute", top: "-2rem", right: "0" }}>
          <div className={styles.refreshDot}></div>
          <span>Actualizando...</span>
        </div>
      )}
      <div className={styles.kanbanContainer}>
        {COLUMNS.map((config) => {
          const columnOrders = filterOrdersByStatus(config.status);
          return <Column key={config.id} config={config} columnOrders={columnOrders} isNew={newOrders} />;
        })}
      </div>
    </div>
  );
};
