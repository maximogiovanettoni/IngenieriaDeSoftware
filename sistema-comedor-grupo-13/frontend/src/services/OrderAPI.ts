import { OrderErrorDetails } from "@/components/Orders/OrderErrorModal";
import { BASE_API_URL } from "@/config/app-query-client";

export interface OrderItem {
  productId: number;
  productName: string;
  quantity: number;
  unitPrice: number;
  subtotal?: number;
}

export interface OrderError {
  status: number;
  message: string;
  details?: OrderErrorDetails;
  data?: unknown;
}

export interface CreateOrderRequest {
  items: OrderItem[];
  promotionId?: number;
}

export interface PickupPoint {
  id: number;
  name: string;
  description?: string;
  location: string;
  active?: boolean;
}

export interface OrderResponse {
  id: number;
  orderNumber: string;
  status: string;
  subtotal?: number;
  totalAmount: number;
  discountAmount: number;
  finalAmount: number;
  items: Array<Omit<OrderItem, "subtotal"> & { subtotal: number }>;
  appliedPromotions?: AppliedPromotionInfo[];
  createdAt: string;
  confirmedAt?: string;
  preparedAt?: string;
  deliveredAt?: string;
  rejectionReason?: string;
  pickupPoint?: PickupPoint;
}

export interface AppliedPromotionInfo {
  name: string;
  type: string;
  discountAmount: number;
  startDate?: string;
  endDate?: string;
  applicableDays?: string;
}

export interface CalculatePromotionInfo {
  subtotal: number;
  discountAmount: number;
  totalAmount: number;
  hasDiscount: boolean;
  appliedPromotions?: AppliedPromotionInfo[];
}

class OrderAPI {
  private baseUrl = `${BASE_API_URL}/orders`;

  private getAuthHeaders(token?: string): HeadersInit {
    const headers: HeadersInit = {
      "Content-Type": "application/json",
    };

    if (token) {
      headers["Authorization"] = `Bearer ${token}`;
    }

    return headers;
  }

  private normalizeOrder(data: Record<string, unknown>): OrderResponse {
    return {
      // Backend now includes 'id' directly in OrderResponse
      id:
        typeof data.id === "number"
          ? data.id
          : typeof data.orderNumber === "number"
            ? data.orderNumber
            : parseInt(String(data.orderNumber), 10),
      // Keep orderNumber as string for display consistency
      orderNumber: data.orderNumber != null ? String(data.orderNumber) : "",
      status: data.status,
      subtotal: typeof data.subtotal === "string" ? parseFloat(data.subtotal) : data.subtotal,
      createdAt: data.createdAt,
      confirmedAt: data.confirmedAt,
      deliveredAt: data.deliveredAt,
      rejectionReason: data.rejectionReason,
      pickupPoint: data.pickupPoint,
      totalAmount: typeof data.totalAmount === "string" ? parseFloat(data.totalAmount) : data.totalAmount,
      discountAmount: typeof data.discountAmount === "string" ? parseFloat(data.discountAmount) : data.discountAmount,
      finalAmount:
        typeof data.finalAmount === "string"
          ? parseFloat(data.finalAmount as string)
          : (data.totalAmount &&
              (typeof data.totalAmount === "string"
                ? parseFloat(data.totalAmount as string)
                : (data.totalAmount as number))) ||
            0,
      items: ((data.items as Array<Record<string, unknown>>) || []).map((item: Record<string, unknown>) => ({
        productId: item.productId as number,
        productName: item.productName as string,
        quantity: item.quantity as number,
        unitPrice:
          typeof item.unitPrice === "string" ? parseFloat(item.unitPrice as string) : (item.unitPrice as number),
        subtotal: typeof item.subtotal === "string" ? parseFloat(item.subtotal as string) : (item.subtotal as number),
      })),
      appliedPromotions: ((data.appliedPromotions as Array<Record<string, unknown>>) || []).map(
        (promo: Record<string, unknown>) => ({
          name: promo.name as string,
          type: promo.type as string,
          discountAmount:
            typeof promo.discountAmount === "string"
              ? parseFloat(promo.discountAmount as string)
              : (promo.discountAmount as number),
          startDate: promo.startDate as string | undefined,
          endDate: promo.endDate as string | undefined,
          applicableDays: promo.applicableDays as string | undefined,
        }),
      ),
    } as OrderResponse;
  }

  async createOrder(
    request: CreateOrderRequest,
    token?: string,
  ): Promise<{ order?: OrderResponse; success?: boolean; message?: string } | OrderResponse> {
    const response = await fetch(this.baseUrl, {
      method: "POST",
      headers: this.getAuthHeaders(token),
      body: JSON.stringify(request),
    });

    if (!response.ok) {
      if (response.status === 409) {
        // Stock insuficiente
        const error = await response.json();
        throw {
          status: 409,
          message: error.message || "Stock insuficiente",
          details: error.details,
          data: error,
        };
      }

      if (response.status === 401) {
        throw {
          status: 401,
          message: "No autorizado. Por favor, inicie sesión nuevamente.",
          details: undefined,
          data: undefined,
        };
      }

      const error = await response.json().catch(() => ({ message: response.statusText }));
      throw {
        status: response.status,
        message: error.message || "Error creando el pedido",
        details: error.details,
        data: error,
      };
    }

    return response.json();
  }

  async getMyOrders(token?: string): Promise<OrderResponse[]> {
    const response = await fetch(`${this.baseUrl}/my-orders`, {
      method: "GET",
      headers: this.getAuthHeaders(token),
    });

    if (!response.ok) {
      if (response.status === 401) {
        throw new Error("No autorizado. Por favor, inicie sesión nuevamente.");
      }
      throw new Error(`Error obteniendo pedidos: ${response.statusText}`);
    }

    const data = (await response.json()) as Array<Record<string, unknown>>;
    return Array.isArray(data) ? data.map((order: Record<string, unknown>) => this.normalizeOrder(order)) : [];
  }

  async getOrderById(orderId: number, token?: string): Promise<OrderResponse> {
    const response = await fetch(`${this.baseUrl}/${orderId}`, {
      method: "GET",
      headers: this.getAuthHeaders(token),
    });

    if (!response.ok) {
      if (response.status === 404) {
        throw new Error("Pedido no encontrado");
      }
      if (response.status === 401) {
        throw new Error("No autorizado. Por favor, inicie sesión nuevamente.");
      }
      throw new Error(`Error obteniendo pedido: ${response.statusText}`);
    }

    const data = await response.json();
    return this.normalizeOrder(data);
  }

  async confirmOrder(orderId: number, token?: string): Promise<OrderResponse> {
    const response = await fetch(`${this.baseUrl}/${orderId}/move-forward`, {
      method: "PUT",
      headers: this.getAuthHeaders(token),
    });

    if (!response.ok) {
      throw new Error("Error confirmando el pedido");
    }

    const data = await response.json();
    return this.normalizeOrder(data.order);
  }

  async rejectOrder(orderId: number, reason: string, token?: string): Promise<OrderResponse> {
    const response = await fetch(`${this.baseUrl}/${orderId}/reject`, {
      method: "PUT",
      headers: this.getAuthHeaders(token),
      body: JSON.stringify({ reason }),
    });

    if (!response.ok) {
      throw new Error("Error rechazando el pedido");
    }

    const data = await response.json();
    return this.normalizeOrder(data.order);
  }

  async cancelOrder(orderId: number, reason: string, token?: string): Promise<OrderResponse> {
    const response = await fetch(`${this.baseUrl}/${orderId}/cancel`, {
      method: "POST",
      headers: this.getAuthHeaders(token),
      body: JSON.stringify({ reason }),
    });

    if (!response.ok) {
      if (response.status === 400) {
        const error = await response.json().catch(() => ({ message: response.statusText }));
        throw new Error(error.message || "No se puede cancelar este pedido en su estado actual");
      }
      throw new Error("Error cancelando el pedido");
    }

    const data = await response.json();
    return this.normalizeOrder(data.order);
  }

  async getMyStats(token?: string): Promise<{
    totalOrders: number;
    pendingCount: number;
    confirmedCount: number;
    deliveredCount: number;
    rejectedCount: number;
    totalSpent: number;
  }> {
    const response = await fetch(`${this.baseUrl}/tracking/stats`, {
      method: "GET",
      headers: this.getAuthHeaders(token),
    });

    if (!response.ok) {
      throw new Error("Error obteniendo estadísticas");
    }

    const data = await response.json();
    console.log("Stats response:", data);

    // Convertir totalSpent a número si viene como string
    const stats = data.stats;
    return {
      totalOrders: stats.totalOrders,
      pendingCount: stats.pendingCount,
      confirmedCount: stats.confirmedCount,
      deliveredCount: stats.deliveredCount,
      rejectedCount: stats.rejectedCount,
      totalSpent: typeof stats.totalSpent === "string" ? parseFloat(stats.totalSpent) : stats.totalSpent,
    };
  }

  async calculatePromotionForCart(
    items: Array<{ productId: number; quantity: number }>,
    token?: string,
  ): Promise<CalculatePromotionInfo> {
    const response = await fetch(`${this.baseUrl}/calculate-promotion`, {
      method: "POST",
      headers: this.getAuthHeaders(token),
      body: JSON.stringify({ items }),
    });

    if (!response.ok) {
      throw new Error("Error calculating promotion");
    }

    const result = await response.json();
    const data = result.data;

    console.log("Raw API response:", result);
    console.log("Data from response:", data);
    console.log("Promotion type from backend:", data?.promotionType || data?.type);

    return {
      subtotal: typeof data.subtotal === "string" ? parseFloat(data.subtotal) : data.subtotal,
      discountAmount: typeof data.discountAmount === "string" ? parseFloat(data.discountAmount) : data.discountAmount,
      totalAmount: typeof data.totalAmount === "string" ? parseFloat(data.totalAmount) : data.totalAmount,
      hasDiscount: data.hasDiscount,
      appliedPromotions: data.appliedPromotions,
    };
  }

  async getTimeWhenStatusReached(
    orderId: number,
    status: string,
    token?: string,
  ): Promise<string> {
  const response = await fetch(`${this.baseUrl}/${orderId}/status/${status}`, {
    method: "GET",
    headers: this.getAuthHeaders(token),
  });

  if (response.status === 404) {
    // Status not reached yet, throw a specific error that we can catch silently
    throw new Error("STATUS_NOT_REACHED");
  }

  if (!response.ok) {
    throw new Error(`Error fetching timestamp for status ${status}`);
  }

  // The backend returns a JSON string (e.g., "2025-12-01T20:19:14.143804Z")
  // We need to parse it first to get the actual string value
  const jsonValue = await response.json();
  // jsonValue is now a string without outer quotes
  return jsonValue;
  }
}

export const orderAPI = new OrderAPI();
