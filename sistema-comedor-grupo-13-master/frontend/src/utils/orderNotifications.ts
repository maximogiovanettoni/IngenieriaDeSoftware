import { toast } from "sonner";

export enum ORDER_STATUS {
  PENDING = "pending",
  CONFIRMED = "confirmed",
  PREPARING = "preparing",
  READY = "ready",
  COMPLETED = "completed",
  CANCELLED = "cancelled",
  REJECTED = "rejected"
}

export type OrderStatus = `${ORDER_STATUS}`;

const STATUS_MAP: Record<string, OrderStatus> = {
  pending: ORDER_STATUS.PENDING,
  confirmed: ORDER_STATUS.CONFIRMED,
  preparing: ORDER_STATUS.PREPARING,
  ready: ORDER_STATUS.READY,
  completed: ORDER_STATUS.COMPLETED,
  delivered: ORDER_STATUS.COMPLETED,
  cancelled: ORDER_STATUS.CANCELLED,
  canceled: ORDER_STATUS.CANCELLED,
  rejected: ORDER_STATUS.REJECTED
};

export function normalizeStatus(status: string): OrderStatus {
  if (!status) return ORDER_STATUS.PENDING;

  const normalized = status.trim().toLowerCase();
  return STATUS_MAP[normalized] ?? ORDER_STATUS.PENDING;
}

const STATUS_MESSAGES: Record<OrderStatus, string> = {
  [ORDER_STATUS.PENDING]: `Tu pedido #{{id}} ha sido recibido`,
  [ORDER_STATUS.CONFIRMED]: `Tu pedido #{{id}} ha sido confirmado`,
  [ORDER_STATUS.PREPARING]: `Tu pedido #{{id}} se está preparando`,
  [ORDER_STATUS.READY]: `¡Tu pedido #{{id}} está listo para retirar!`,
  [ORDER_STATUS.COMPLETED]: `Tu pedido #{{id}} ha sido completado`,
  [ORDER_STATUS.CANCELLED]: `Tu pedido #{{id}} ha sido cancelado`,
  [ORDER_STATUS.REJECTED]: `Tu pedido #{{id}} ha sido rechazado`
};

const STATUS_CONFIG: Record<
  OrderStatus,
  { type: "success" | "info" | "error" | "warning"; duration: number }
> = {
  [ORDER_STATUS.PENDING]: { type: "info", duration: 4000 },
  [ORDER_STATUS.CONFIRMED]: { type: "success", duration: 4000 },
  [ORDER_STATUS.PREPARING]: { type: "info", duration: 4000 },
  [ORDER_STATUS.READY]: { type: "success", duration: 8000 },
  [ORDER_STATUS.COMPLETED]: { type: "success", duration: 4000 },
  [ORDER_STATUS.CANCELLED]: { type: "error", duration: 5000 },
  [ORDER_STATUS.REJECTED]: { type: "error", duration: 5000 }
};

export const orderNotificationService = {
  statusChanged(orderNumber: string | number, rawStatus: string): void {
    const newStatus = normalizeStatus(rawStatus);
    const msgTemplate = STATUS_MESSAGES[newStatus];
    const { type, duration } = STATUS_CONFIG[newStatus];

    const message = msgTemplate.replace("{{id}}", String(orderNumber));

    toast[type](message, {
      position: "top-center",
      duration
    });
  }
};
