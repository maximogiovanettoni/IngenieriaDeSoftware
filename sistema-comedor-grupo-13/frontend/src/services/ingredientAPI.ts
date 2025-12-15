import { BASE_API_URL } from "@/config/app-query-client";

export interface Ingredient {
  id: number;
  name: string;
  unitMeasure: string;
  stock: number;
  available: boolean;
}

export interface CreateIngredientDTO {
  name: string;
  unitMeasure: string;
  stock: number;
}

export interface UpdateIngredientStockRequest {
  amount: number;
}

export interface UpdateIngredientNameDTO {
  name: string;
}

export interface DeleteIngredientRequest {
  reason?: string;
}

class IngredientAPI {
  private baseUrl = `${BASE_API_URL}/ingredients`;

  private getAuthHeaders(token?: string): HeadersInit {
    const headers: HeadersInit = {
      'Content-Type': 'application/json',
    };
    
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
    
    return headers;
  }

  async getAll(token?: string): Promise<Ingredient[]> {
    const response = await fetch(this.baseUrl, {
      method: 'GET',
      headers: this.getAuthHeaders(token),
    });

    if (!response.ok) {
      if (response.status === 401) {
        throw new Error('No autorizado. Por favor, inicie sesión nuevamente.');
      }
      throw new Error(`Error obteniendo ingredientes: ${response.statusText}`);
    }

    return response.json();
  }

  async create(dto: CreateIngredientDTO, token?: string): Promise<Ingredient> {
    const response = await fetch(this.baseUrl, {
      method: 'POST',
      headers: this.getAuthHeaders(token),
      body: JSON.stringify(dto),
    });

    if (!response.ok) {
      if (response.status === 401) {
        throw new Error('No autorizado. Por favor, inicie sesión nuevamente.');
      }
      const error = await response.json().catch(() => ({ message: response.statusText }));
      throw new Error(error.message || 'Error creando ingrediente');
    }

    return response.json();
  }

  async addStock(id: number, amount: number, token?: string): Promise<Ingredient> {
    const response = await fetch(`${this.baseUrl}/${id}/stock`, {
      method: 'PUT',
      headers: this.getAuthHeaders(token),
      body: JSON.stringify({ amount }),
    });

    if (!response.ok) {
      if (response.status === 401) {
        throw new Error('No autorizado. Por favor, inicie sesión nuevamente.');
      }
      const error = await response.json().catch(() => ({ message: response.statusText }));
      throw new Error(error.message || 'Error agregando stock');
    }

    return response.json();
  }

  async changeName(id: number, name: string, token?: string): Promise<Ingredient> {
    const response = await fetch(`${this.baseUrl}/${id}/name`, {
      method: 'PUT',
      headers: this.getAuthHeaders(token),
      body: JSON.stringify({ name }),
    });

    if (!response.ok) {
      if (response.status === 401) {
        throw new Error('No autorizado. Por favor, inicie sesión nuevamente.');
      }
      const error = await response.json().catch(() => ({ message: response.statusText }));
      throw new Error(error.message || 'Error modificando nombre');
    }

    return response.json();
  }

  async deactivate(id: number, reason?: string, token?: string): Promise<void> {
    const response = await fetch(`${this.baseUrl}/${id}`, {
      method: 'DELETE',
      headers: this.getAuthHeaders(token),
      body: reason ? JSON.stringify({ reason }) : undefined,
    });

    if (!response.ok) {
      if (response.status === 401) {
        throw new Error('No autorizado. Por favor, inicie sesión nuevamente.');
      }
      const error = await response.json().catch(() => ({ message: response.statusText }));
      throw new Error(error.message || 'Error desactivando ingrediente');
    }
  }
}

export const ingredientAPI = new IngredientAPI();