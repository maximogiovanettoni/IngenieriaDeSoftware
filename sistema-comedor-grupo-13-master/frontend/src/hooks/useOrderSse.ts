import { useEffect } from "react";
import { orderNotificationService } from "@/utils/orderNotifications";
import { BASE_API_URL } from "@/config/app-query-client";

export function useOrderSse(email?: string | null) {
  useEffect(() => {

    if (!email) {
      return;
    }

    const url = `${BASE_API_URL}/orders/notifications/stream?email=${email}`;
    const eventSource = new EventSource(url, { withCredentials: true });

    eventSource.onopen = () => {
      console.log("SSE connected!");
    };

    eventSource.addEventListener("order-status-update", (event: MessageEvent) => {
      try {
        const data = JSON.parse(event.data);
        orderNotificationService.statusChanged(data.orderNumber, data.newStatus);
      } catch (e) {
        console.error("Failed to parse SSE event:", e);
      }
    });

    eventSource.onerror = (err) => {
      console.error("SSE connection error:", err);
      eventSource.close();
    };

    return () => {
      console.log("SSE closed");
      eventSource.close();
    };
  }, [email]);
}

