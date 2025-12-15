import { BASE_API_URL } from "@/config/app-query-client";

export interface Staff {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  createdAt: string;
  role: string;
}

export interface CreateStaffDTO {
  firstName: string;
  lastName: string;
  email: string;
  temporaryPassword: string;
  birthDate?: string;
  gender?: string;
}

export interface DeleteStaffRequest {
  reason?: string;
}

class StaffAPI {
  private baseUrl = `${BASE_API_URL}/admin/staff`;

  private getAuthHeaders(token?: string): HeadersInit {
    const headers: HeadersInit = {
      'Content-Type': 'application/json',
    };
    
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
    
    return headers;
  }

  async getAll(token?: string): Promise<Staff[]> {
    const response = await fetch(this.baseUrl, {
      method: 'GET',
      headers: this.getAuthHeaders(token),
    });

    if (!response.ok) {
      if (response.status === 401) {
        throw new Error('No autorizado. Por favor, inicie sesión nuevamente.');
      }
      if (response.status === 403) {
        throw new Error('No tiene permisos de administrador.');
      }
      throw new Error(`Error obteniendo staff: ${response.statusText}`);
    }

    return response.json();
  }

  async create(dto: CreateStaffDTO, token?: string): Promise<Staff> {
    const response = await fetch(this.baseUrl, {
      method: 'POST',
      headers: this.getAuthHeaders(token),
      body: JSON.stringify(dto),
    });

    if (!response.ok) {
      if (response.status === 401) {
        throw new Error('No autorizado. Por favor, inicie sesión nuevamente.');
      }
      if (response.status === 403) {
        throw new Error('No tiene permisos de administrador.');
      }
      const error = await response.json().catch(() => ({ message: response.statusText }));
      throw new Error(error.message || error.error || 'Error creando staff');
    }

    return response.json();
  }

  async delete(email: string, reason?: string, token?: string): Promise<void> {
    const response = await fetch(`${this.baseUrl}/${email}`, {
      method: 'DELETE',
      headers: this.getAuthHeaders(token),
      body: reason ? JSON.stringify({ reason }) : undefined,
    });

    if (!response.ok) {
      if (response.status === 401) {
        throw new Error('No autorizado. Por favor, inicie sesión nuevamente.');
      }
      if (response.status === 403) {
        throw new Error('No tiene permisos de administrador.');
      }
      const error = await response.json().catch(() => ({ message: response.statusText }));
      throw new Error(error.message || error.error || 'Error eliminando staff');
    }
  }
}

export const staffAPI = new StaffAPI();