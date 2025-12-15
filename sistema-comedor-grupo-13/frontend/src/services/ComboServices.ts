import { BASE_API_URL } from "@/config/app-query-client";

export interface ComboProduct {
  id: number;
  name: string;
  type: string;
  quantity: number | string;
}

export interface Combo {
  id: number;
  name: string;
  description?: string;
  price: number;
  regularPrice: number;
  discount: number;
  imageUrl?: string;
  isActive: boolean;
  isAvailable: boolean;
  products: ComboProduct[];
  createdAt: string;
  updatedAt: string;
}

export interface CreateComboDTO {
  name: string;
  description?: string;
  price: number;
  active: boolean;
  products: Record<number, number>; // productId -> quantity
}

export interface UpdateComboNameDTO {
  name: string;
}

export interface UpdateComboPriceDTO {
  price: number;
}

class ComboService {
  private baseUrl = `${BASE_API_URL}/combos`;

  private getAuthHeaders(token?: string): HeadersInit {
    const headers: HeadersInit = {
      "Content-Type": "application/json",
    };

    if (token) {
      headers["Authorization"] = `Bearer ${token}`;
    }

    return headers;
  }

  async getAll(token?: string): Promise<Combo[]> {
    const response = await fetch(`${this.baseUrl}/all`, {
      method: "GET",
      headers: this.getAuthHeaders(token),
    });

    if (!response.ok) {
      if (response.status === 401) {
        throw new Error("No autorizado. Por favor, inicie sesión nuevamente.");
      }
      throw new Error(`Error obteniendo combos: ${response.statusText}`);
    }

    return response.json();
  }

  async getById(id: number, token?: string): Promise<Combo> {
    const response = await fetch(`${this.baseUrl}/${id}`, {
      method: "GET",
      headers: this.getAuthHeaders(token),
    });

    if (!response.ok) {
      if (response.status === 401) {
        throw new Error("No autorizado. Por favor, inicie sesión nuevamente.");
      }
      const error = await response.json().catch(() => ({ message: response.statusText }));
      throw new Error(error.message || "Error obteniendo combo");
    }

    return response.json();
  }

  async getActiveCombos(token?: string): Promise<Combo[]> {
    const response = await fetch(this.baseUrl, {
      method: "GET",
      headers: this.getAuthHeaders(token),
    });

    if (!response.ok) {
      if (response.status === 401) {
        throw new Error("No autorizado. Por favor, inicie sesión nuevamente.");
      }
      throw new Error(`Error obteniendo combos activos: ${response.statusText}`);
    }

    return response.json();
  }

  async getAvailableCombos(token?: string): Promise<Combo[]> {
    const response = await fetch(`${this.baseUrl}/available`, {
      method: "GET",
      headers: this.getAuthHeaders(token),
    });

    if (!response.ok) {
      if (response.status === 401) {
        throw new Error("No autorizado. Por favor, inicie sesión nuevamente.");
      }
      throw new Error(`Error obteniendo combos disponibles: ${response.statusText}`);
    }

    return response.json();
  }

  async checkAvailability(id: number, token?: string): Promise<{ comboId: number; isAvailable: boolean }> {
    const response = await fetch(`${this.baseUrl}/${id}/availability`, {
      method: "GET",
      headers: this.getAuthHeaders(token),
    });

    if (!response.ok) {
      if (response.status === 401) {
        throw new Error("No autorizado. Por favor, inicie sesión nuevamente.");
      }
      const error = await response.json().catch(() => ({ message: response.statusText }));
      throw new Error(error.message || "Error verificando disponibilidad");
    }

    return response.json();
  }

  async create(dto: CreateComboDTO, token?: string): Promise<Combo> {
    const response = await fetch(this.baseUrl, {
      method: "POST",
      headers: this.getAuthHeaders(token),
      body: JSON.stringify(dto),
    });

    if (!response.ok) {
      if (response.status === 401) {
        throw new Error("No autorizado. Por favor, inicie sesión nuevamente.");
      }
      const errorData = await response.json().catch(() => ({ message: response.statusText }));
      throw new Error(errorData.message || "Error creando combo");
    }

    const result = await response.json();
    return result.combo || result;
  }

  async updateName(id: number, name: string, token?: string): Promise<Combo> {
    const response = await fetch(`${this.baseUrl}/${id}/name`, {
      method: "PUT",
      headers: this.getAuthHeaders(token),
      body: JSON.stringify({ name }),
    });

    if (!response.ok) {
      if (response.status === 401) {
        throw new Error("No autorizado. Por favor, inicie sesión nuevamente.");
      }
      const errorData = await response.json().catch(() => ({ message: response.statusText }));
      throw new Error(errorData.message || "Error actualizando nombre");
    }

    const result = await response.json();
    return result.combo || result;
  }

  async updatePrice(id: number, price: number, token?: string): Promise<Combo> {
    const response = await fetch(`${this.baseUrl}/${id}/price`, {
      method: "PUT",
      headers: this.getAuthHeaders(token),
      body: JSON.stringify({ price }),
    });

    if (!response.ok) {
      if (response.status === 401) {
        throw new Error("No autorizado. Por favor, inicie sesión nuevamente.");
      }
      const errorData = await response.json().catch(() => ({ message: response.statusText }));
      throw new Error(errorData.message || "Error actualizando precio");
    }

    const result = await response.json();
    return result.combo || result;
  }

  async updateImage(id: number, file: File, token?: string): Promise<Combo> {
    const formData = new FormData();
    formData.append("file", file);

    const headers: HeadersInit = {};
    if (token) {
      headers["Authorization"] = `Bearer ${token}`;
    }

    const uploadResponse = await fetch(`${BASE_API_URL}/combos/${id}/image`, {
      method: "PUT",
      headers,
      body: formData,
    });

    if (!uploadResponse.ok) {
      if (uploadResponse.status === 401) {
        throw new Error("No autorizado. Por favor, inicie sesión nuevamente.");
      }
      const uploadData = await uploadResponse.json().catch(() => ({ message: uploadResponse.statusText }));
      throw new Error(uploadData.message || "Error actualizando imagen");
    }

    const uploadData = await uploadResponse.json();
    return uploadData.combo || uploadData;
  }

  async deactivate(id: number, token?: string): Promise<void> {
    const response = await fetch(`${this.baseUrl}/${id}`, {
      method: "DELETE",
      headers: this.getAuthHeaders(token),
    });

    if (!response.ok) {
      if (response.status === 401) {
        throw new Error("No autorizado. Por favor, inicie sesión nuevamente.");
      }
      const errorData = await response.json().catch(() => ({ message: response.statusText }));
      throw new Error(errorData.message || "Error desactivando combo");
    }
  }

  async restore(id: number, token?: string): Promise<void> {
    const response = await fetch(`${this.baseUrl}/${id}/restore`, {
      method: "PUT",
      headers: this.getAuthHeaders(token),
    });

    if (!response.ok) {
      if (response.status === 401) {
        throw new Error("No autorizado. Por favor, inicie sesión nuevamente.");
      }
      const errorData = await response.json().catch(() => ({ message: response.statusText }));
      throw new Error(errorData.message || "Error restaurando combo");
    }
  }
}

export const comboService = new ComboService();
