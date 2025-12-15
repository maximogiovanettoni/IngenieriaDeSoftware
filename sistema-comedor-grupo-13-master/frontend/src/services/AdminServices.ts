import { useMutation } from "@tanstack/react-query";

export interface CreateStaffRequest {
  firstName: string;
  lastName: string;
  email: string;
  temporaryPassword: string;
  birthDate?: string;
  gender?: string;
}

export interface CreateStaffResponse {
  id: number;
  email: string;
  role: string;
  createdBy: string;
  createdAt: string;
}

export interface DeleteStaffRequest {
  staffId: number;
}

// Define a proper error type
export interface ApiError extends Error {
  response?: {
    status: number;
    statusText: string;
    data?: {
      error?: string;
      message?: string;
    };
  };
}

// Type for import.meta.env
interface ImportMetaEnv {
  VITE_API_URL?: string;
}

interface ImportMeta {
  env: ImportMetaEnv;
}

const BASE_API_URL = (import.meta as ImportMeta)?.env?.VITE_API_URL || process?.env?.REACT_APP_API_URL || "";

async function jsonFetch<T>(url: string, init?: RequestInit): Promise<T> {
  const res = await fetch(url, init);
  if (!res.ok) {
    const msg = await res.text().catch(() => "");
    throw new Error(`HTTP ${res.status} ${res.statusText}${msg ? ` - ${msg}` : ""}`);
  }
  if (res.status === 204) return undefined as unknown as T;
  return (await res.json()) as T;
}

export const useCreateStaff = () => {
  return useMutation<CreateStaffResponse, ApiError, CreateStaffRequest>({
    mutationFn: async (data) => {
      return await jsonFetch<CreateStaffResponse>(`${BASE_API_URL}/admin/staff`, {
        method: "POST",
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
        },
        body: JSON.stringify(data),
      });
    },
    onError: (error: ApiError) => {
      console.error("❌ Error creando staff:", error.message);
      throw error;
    },
    onSuccess: (data) => {
      console.log("✅ Staff creado correctamente:", data);
    },
  });
};

export const useDeleteStaff = () => {
  return useMutation<void, ApiError, DeleteStaffRequest>({
    mutationFn: async ({ staffId }) => {
      await jsonFetch<void>(`${BASE_API_URL}/admin/staff/${staffId}`, {
        method: "DELETE",
        headers: {
          Accept: "application/json",
        },
      });
    },
    onError: (error: ApiError) => {
      console.error("❌ Error eliminando staff:", error.message);
      throw error;
    },
    onSuccess: () => {
      console.log("✅ Staff eliminado correctamente");
    },
  });
};

// Admin Dashboard Stats
export interface AdminDashboardStats {
  totalOrders: number;
  pendingCount: number;
  confirmedCount: number;
  deliveredCount: number;
  rejectedCount: number;
  totalSpent: number | string;
}

export async function getAdminDashboardStats(token?: string): Promise<AdminDashboardStats> {
  const headers: HeadersInit = {
    Accept: "application/json",
    "Content-Type": "application/json",
  };

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await jsonFetch<{ success: boolean; stats: AdminDashboardStats }>(
    `${BASE_API_URL}/api/orders/admin/stats`,
    {
      method: "GET",
      headers,
    }
  );

  return response.stats;
}
