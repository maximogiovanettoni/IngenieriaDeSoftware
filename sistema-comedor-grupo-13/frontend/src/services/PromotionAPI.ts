import { BASE_API_URL } from "@/config/app-query-client";

export interface Promotion {
  id: number;
  name: string;
  description?: string;
  active: boolean;
  startDate?: string;
  endDate?: string;
  applicableDays?: string[];
  currentlyValid?: boolean;
  type: 'PERCENTAGE_DISCOUNT' | 'FIXED_DISCOUNT' | 'BUY_X_GET_Y' | 'BUY_X_PAY_Y' | 'FIUBA_EMAIL_DISCOUNT' | 'PIZZA_2X1_AFTER_HOURS';
  category?: string;
  discount?: number;
  multiplier?: number; // For percentage discounts
  minimumPurchase?: number; // For fixed discounts
  discountAmount?: number; // For fixed discounts
  requiredProductCategory?: string; // For BuyXGetY
  freeProductCategory?: string; // For BuyXGetY
  requiredQuantity?: number; // For BuyXPayY / BuyXGetY
  chargedQuantity?: number; // For BuyXPayY
  freeQuantity?: number; // For BuyXGetY
  discountPercentage?: number; // For FIUBA_EMAIL_DISCOUNT
  startHour?: number; // For PIZZA_2X1_AFTER_HOURS
}

const getHeaders = (token?: string) => {
  const headers: HeadersInit = {
    'Content-Type': 'application/json',
  };
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }
  return headers;
};

export const promotionAPI = {
  async getAllPromotions(token?: string): Promise<Promotion[]> {
    const response = await fetch(`${BASE_API_URL}/promotions`, {
      headers: getHeaders(token),
    });
    if (!response.ok) {
      throw new Error(`Error fetching promotions: ${response.statusText}`);
    }
    return response.json();
  },

  async getActivePromotions(): Promise<Promotion[]> {
    const response = await fetch(`${BASE_API_URL}/promotions/active`);
    if (!response.ok) {
      throw new Error(`Error fetching active promotions: ${response.statusText}`);
    }
    return response.json();
  },

  async getCurrentlyValidPromotions(): Promise<Promotion[]> {
    const response = await fetch(`${BASE_API_URL}/promotions/valid`);
    if (!response.ok) {
      throw new Error(`Error fetching valid promotions: ${response.statusText}`);
    }
    return response.json();
  },

  async getPromotionById(id: number, token?: string): Promise<Promotion> {
    const response = await fetch(`${BASE_API_URL}/promotions/${id}`, {
      headers: getHeaders(token),
    });
    if (!response.ok) {
      throw new Error(`Error fetching promotion: ${response.statusText}`);
    }
    return response.json();
  },

  async createPromotion(data: unknown, token?: string): Promise<void> {
    const response = await fetch(`${BASE_API_URL}/promotions`, {
      method: 'POST',
      headers: getHeaders(token),
      body: JSON.stringify(data),
    });
    if (!response.ok) {
      throw new Error(`Error creating promotion: ${response.statusText}`);
    }
  },

  async updatePromotion(
    id: number,
    data: unknown,
    token?: string
  ): Promise<void> {
    const response = await fetch(`${BASE_API_URL}/promotions/${id}`, {
      method: 'PUT',
      headers: getHeaders(token),
      body: JSON.stringify(data),
    });
    if (!response.ok) {
      throw new Error(`Error updating promotion: ${response.statusText}`);
    }
  },

  async deletePromotion(id: number, token?: string): Promise<void> {
    const response = await fetch(`${BASE_API_URL}/promotions/${id}`, {
      method: 'DELETE',
      headers: getHeaders(token),
    });
    if (!response.ok) {
      throw new Error(`Error deleting promotion: ${response.statusText}`);
    }
  },

  async activatePromotion(id: number, token?: string): Promise<Promotion> {
    const response = await fetch(`${BASE_API_URL}/promotions/${id}/activate`, {
      method: 'PATCH',
      headers: getHeaders(token),
    });
    if (!response.ok) {
      throw new Error(`Error activating promotion: ${response.statusText}`);
    }
    return response.json();
  },

  async deactivatePromotion(id: number, token?: string): Promise<Promotion> {
    const response = await fetch(`${BASE_API_URL}/promotions/${id}/deactivate`, {
      method: 'PATCH',
      headers: getHeaders(token),
    });
    if (!response.ok) {
      throw new Error(`Error deactivating promotion: ${response.statusText}`);
    }
    return response.json();
  },
};
