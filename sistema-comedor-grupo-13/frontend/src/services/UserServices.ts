import { useMutation } from "@tanstack/react-query";
import { useEffect, useState } from "react";

import { BASE_API_URL } from "@/config/app-query-client";
import { AuthResponseSchema, LoginRequest, SignupRequest } from "@/models/Login";
import { ValidationConfig } from "@/models/ValidationConfig";
import { useToken } from "@/services/TokenContext";
import { ApiError } from "@/types/api";

export function useLogin() {
  const [, setToken] = useToken();

  return useMutation({
    mutationFn: async (req: LoginRequest) => {
      try {
        const tokens = await auth("POST", "/sessions", req);
        setToken({ state: "LOGGED_IN", tokens });
        return tokens;
      } catch (err: unknown) {
        try {
          const match = (err as Error).message.match(/\{.*\}/);
          if (match) {
            const parsed = JSON.parse(match[0]);
            throw new Error(parsed.message || "Error desconocido");
          }
        } catch {
          // Si no hay JSON o no se pudo parsear, mostramos algo genérico
          throw new Error((err as Error).message || "Error de conexión");
        }
      }
    },
  });
}

export function useRefresh() {
  const [token, setToken] = useToken();

  return useMutation({
    mutationFn: async () => {
      if (token.state !== "LOGGED_IN") {
        return;
      }

      try {
        const refreshToken = token.tokens.refreshToken;
        const newTokens = await auth("PUT", "/sessions", { refreshToken });
        setToken({ state: "LOGGED_IN", tokens: newTokens });
        return newTokens;
      } catch (err) {
        setToken({ state: "LOGGED_OUT" });
        throw err;
      }
    },
  });
}

export function useSignup() {
  return useMutation({
    mutationFn: async (req: SignupRequest) => {
      const payload = {
        firstName: req.firstName,
        lastName: req.lastName,
        email: req.email,
        password: req.password,
        birthDate: req.birthDate,
        gender: req.gender,
        address: req.address,
      };

      const res = await fetch(`${BASE_API_URL}/users`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(payload),
      });

      if (!res.ok) {
        const errorData = await res.json().catch(() => ({}));
        throw {
          status: res.status,
          message: errorData.message || "Error desconocido",
          success: errorData.success ?? false,
        };
      }

      const data = await res.json();
      return data;
    },
  });
}

export function useRecoverPassword() {
  return useMutation({
    mutationFn: async (req: { email: string }) => {
      console.log("[recover-password] payload:", req);

      const res = await fetch(BASE_API_URL + "/password/reset-request", {
        method: "POST",
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
        },
        body: JSON.stringify(req),
      });

      if (!res.ok) {
        const errorData = await res.json().catch(() => ({}));
        throw {
          status: res.status,
          message: errorData.message || "Error desconocido",
          success: errorData.success ?? false,
        };
      }

      const data = await res.json();
      return data;
    },
  });
}

type ResetPasswordRequest = {
  token: string;
  newPassword: string;
};

type ResetPasswordResponse = {
  success: boolean;
  message?: string;
};

export function useResetPassword() {
  return useMutation<ResetPasswordResponse, Error, ResetPasswordRequest>({
    mutationFn: async (req: ResetPasswordRequest) => {
      const response = await fetch(`${BASE_API_URL}/password/reset`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(req),
      });

      const data = await response.json();

      if (!response.ok) {
        const err = new Error(data.message || "Error al restablecer la contraseña");
        (err as ApiError).status = response.status;
        throw err;
      }

      return data;
    },
  });
}

async function auth(method: "PUT" | "POST" | "DELETE", endpoint: string, data: object) {
  const response = await fetch(BASE_API_URL + endpoint, {
    method,
    headers: {
      Accept: "application/json",
      "Content-Type": "application/json",
    },
    body: JSON.stringify(data),
  });

  if (response.ok) {
    return AuthResponseSchema.parse(await response.json());
  } else {
    throw new Error(`Failed with status ${response.status}: ${await response.text()}`);
  }
}

export function useValidationConfig() {
  const [config, setConfig] = useState<ValidationConfig | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchConfig = async () => {
      try {
        const res = await fetch(`${BASE_API_URL}/config/validation`);
        if (!res.ok) throw new Error(`Error ${res.status}`);
        const data = await res.json();
        setConfig(data);
      } catch (err: unknown) {
        if (err instanceof Error) {
          setError(err.message);
        } else {
          setError("Error desconocido");
        }
      } finally {
        setLoading(false);
      }
    };
    fetchConfig();
  }, []);

  return { config, loading, error };
}

export function useLogout() {
  const [token, setToken] = useToken();

  return useMutation({
    mutationFn: async () => {
      try {
        // Only attempt logout if we have a refresh token
        if (token.state === "LOGGED_IN" && token.tokens?.refreshToken) {
          await auth("DELETE", "/sessions", {
            refreshToken: token.tokens.refreshToken,
          });
        }
        // Clear tokens from state regardless of API call result
        setToken({ state: "LOGGED_OUT" });
      } catch (err: unknown) {
        // Even if the API call fails, clear local tokens
        setToken({ state: "LOGGED_OUT" });

        // Optionally log the error but don't throw
        console.warn("Logout API call failed, but local session cleared:", err);
      }
    },
  });
}

export async function getPendingOrders(token: string) {
  const response = await fetch(`${BASE_API_URL}/orders/pending`, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    throw new Error(`Error ${response.status}: No se pudieron cargar los pedidos pendientes`);
  }

  return await response.json();
}

export async function confirmOrder(orderId: number, token: string) {
  const response = await fetch(`${BASE_API_URL}/orders/${orderId}/confirm`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    throw new Error(`Error ${response.status}: No se pudo confirmar el pedido`);
  }

  return await response.json();
}

export async function rejectOrder(orderId: number, reason: string, token: string) {
  const response = await fetch(`${BASE_API_URL}/orders/${orderId}/reject`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
    },
    body: JSON.stringify({ reason }),
  });

  if (!response.ok) {
    throw new Error(`Error ${response.status}: No se pudo rechazar el pedido`);
  }

  return await response.json();
}

export async function getPickupPoints(token: string) {
  const response = await fetch(`${BASE_API_URL}/orders/pickup-points`, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    throw new Error(`Error ${response.status}: No se pudieron cargar los puntos de retiro`);
  }

  return await response.json();
}

export async function setPickupPoint(orderId: number, pickupPointId: number, token: string) {
  const response = await fetch(`${BASE_API_URL}/orders/${orderId}/pickup-point/${pickupPointId}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    throw new Error(`Error ${response.status}: No se pudo establecer el punto de retiro`);
  }

  return await response.json();
}

export async function createRating(orderId: number, rating: number, comment: string, token: string) {
  const response = await fetch(`${BASE_API_URL}/orders/${orderId}/rating`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
    },
    body: JSON.stringify({ rating, comment }),
  });

  if (!response.ok) {
    throw new Error(`Error ${response.status}: No se pudo guardar la calificación`);
  }

  return await response.json();
}

export async function getRating(orderId: number, token: string) {
  const response = await fetch(`${BASE_API_URL}/orders/${orderId}/rating`, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    if (response.status === 404) {
      return null;
    }
    throw new Error(`Error ${response.status}: No se pudo cargar la calificación`);
  }

  return await response.json();
}

export async function getOrderTrackingStats(token: string) {
  const response = await fetch(`${BASE_API_URL}/orders/tracking/stats`, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    throw new Error(`Error ${response.status}: No se pudieron cargar las estadísticas de seguimiento`);
  }

  const data = await response.json();
  const stats = data.stats;
  
  // Convertir totalSpent a número si viene como string (BigDecimal de Java)
  return {
    ...stats,
    totalSpent: typeof stats.totalSpent === 'string' ? parseFloat(stats.totalSpent) : stats.totalSpent,
  };
}
