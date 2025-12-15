import { useEffect, useRef, useCallback, useState } from 'react';
import { toast } from 'react-toastify';

export type OrderStatus = 
  | 'pending' | 'confirmed' | 'preparing' | 'ready' 
  | 'completed' | 'cancelled' | 'rejected';


interface UseAutoRefreshOptions {
  enabled: boolean;
  interval?: number; 
  onRefresh: () => Promise<void>;
  pauseWhenModalOpen?: boolean;
  isModalOpen?: boolean;
}

interface Order {
  id: number;
  status: OrderStatus;
  orderNumber?: string;
}

export const useAutoRefresh = ({
  enabled,
  interval = 60000, 
  onRefresh,
  pauseWhenModalOpen = true,
  isModalOpen = false,
}: UseAutoRefreshOptions) => {
  const intervalRef = useRef<NodeJS.Timeout | null>(null);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [hasChanges, setHasChanges] = useState(false);
  const [isDisabled, setIsDisabled] = useState(false);
  const failureCountRef = useRef(0);
  const maxFailures = 2; // Máximo 2 fallos antes de pausar
  const shouldPause = pauseWhenModalOpen && isModalOpen;

  // Reset cambios después de 2 segundos
  useEffect(() => {
    if (hasChanges) {
      const timer = setTimeout(() => setHasChanges(false), 2000);
      return () => clearTimeout(timer);
    }
  }, [hasChanges]);

  const refresh = useCallback(async () => {
    if (shouldPause || isDisabled) return;

    setIsRefreshing(true);
    try {
      await onRefresh();
      setHasChanges(true); // Marcar que hubo cambios/refresh
      failureCountRef.current = 0; // Reset contador de fallos
    } catch (error) {
      failureCountRef.current += 1;
      console.error(`Error in auto-refresh (${failureCountRef.current}/${maxFailures}):`, error);
      
      if (failureCountRef.current >= maxFailures) {
        console.warn('Too many refresh errors. Auto-refresh paused. Please refresh manually.');
        setIsDisabled(true); // Pausar auto-refresh
        return;
      }
    } finally {
      setIsRefreshing(false);
    }
  }, [shouldPause, isDisabled, onRefresh]);

  useEffect(() => {
    if (!enabled || shouldPause || isDisabled) {
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
        intervalRef.current = null;
      }
      return;
    }

    // Primer refresh después de 5 segundos
    const timeoutId = setTimeout(refresh, 5000);

    // Luego cada `interval` milisegundos
    intervalRef.current = setInterval(() => {
      void refresh();
    }, interval);

    return () => {
      clearTimeout(timeoutId);
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
        intervalRef.current = null;
      }
    };
  }, [enabled, shouldPause, isDisabled, interval, refresh]);

  const resetAndRetry = useCallback(() => {
    failureCountRef.current = 0;
    setIsDisabled(false);
  }, []);

  return { isRefreshing, hasChanges, isDisabled, resetAndRetry };
};

interface UseNotificationOptions {
  enabled: boolean;
  onNewOrder?: (orderId: number, orderNumber: string) => void;
  onStatusChange?: (orderId: number, oldStatus: OrderStatus, newStatus: OrderStatus) => void;
}

export const useOrderNotifications = ({ enabled, onNewOrder, onStatusChange }: UseNotificationOptions) => {
  const lastOrdersRef = useRef<Map<number, OrderStatus>>(new Map());
  
  // Use localStorage to persist notified changes across component re-renders and polling cycles
  const getNotifiedChanges = (): Set<string> => {
    try {
      const stored = localStorage.getItem('notifiedOrderChanges');
      return new Set(stored ? JSON.parse(stored) : []);
    } catch {
      return new Set();
    }
  };
  
  const saveNotifiedChanges = (changes: Set<string>) => {
    try {
      localStorage.setItem('notifiedOrderChanges', JSON.stringify(Array.from(changes)));
    } catch {
      console.error('Failed to save notified changes to localStorage');
    }
  };
  
  const notifiedChangesRef = useRef<Set<string>>(getNotifiedChanges());

  const playNotificationSound = useCallback(() => {
    try {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const AudioCtx = (window as any).AudioContext || (window as any).webkitAudioContext;
      const audioContext = new AudioCtx() as AudioContext;
      const oscillator = audioContext.createOscillator();
      const gainNode = audioContext.createGain();

      oscillator.connect(gainNode);
      gainNode.connect(audioContext.destination);

      oscillator.frequency.value = 800;
      oscillator.type = 'sine';

      gainNode.gain.setValueAtTime(0.3, audioContext.currentTime);
      gainNode.gain.exponentialRampToValueAtTime(0.01, audioContext.currentTime + 0.5);

      oscillator.start(audioContext.currentTime);
      oscillator.stop(audioContext.currentTime + 0.5);
    } catch (error) {
      console.error('Error playing notification sound:', error);
    }
  }, []);

  const detectOrderChanges = useCallback(
    (orders: Order[]) => {
      if (!enabled || !orders || orders.length === 0) return { newOrders: [], statusChanges: [] };

      const newOrders: number[] = [];
      const statusChanges: Array<{orderId: number, oldStatus: OrderStatus, newStatus: OrderStatus}> = [];

      orders.forEach(order => {
        // Validar que order sea válido
        if (!order || !order.id || !order.status) {
          console.warn('[detectOrderChanges] Skipping invalid order:', order);
          return;
        }

        const previousStatus = lastOrdersRef.current.get(order.id);
        console.log(`[detectOrderChanges] Order ${order.id}: previous=${previousStatus}, current=${order.status}`);
        
        if (!previousStatus) {
          // This is a brand new order
          if (order.status === 'pending') {
            const changeKey = `new-${order.id}`;
            // Only notify once per new order
            if (!notifiedChangesRef.current.has(changeKey)) {
              newOrders.push(order.id);
              onNewOrder?.(order.id, order.orderNumber || `#${order.id}`);
              playNotificationSound();
              notifiedChangesRef.current.add(changeKey);
              saveNotifiedChanges(notifiedChangesRef.current);
              console.log(`[NEW ORDER] ${order.id}`);
            }
          }
        } else if (previousStatus !== order.status) {
          // Status changed
          const changeKey = `${order.id}-${previousStatus}-${order.status}`;
          // Only notify once per status change
          if (!notifiedChangesRef.current.has(changeKey)) {
            statusChanges.push({
              orderId: order.id,
              oldStatus: previousStatus,
              newStatus: order.status
            });
            
            onStatusChange?.(order.id, previousStatus, order.status);
            showStatusToast(order.id, order.status, order.orderNumber);
            notifiedChangesRef.current.add(changeKey);
            saveNotifiedChanges(notifiedChangesRef.current);
            console.log(`[STATUS CHANGE] Order ${order.id}: ${previousStatus} → ${order.status}`);
            
            // Play sound for important status changes
            if (order.status === 'ready' || order.status === 'rejected') {
              console.log(`[PLAYING SOUND] for status: ${order.status}`);
              playNotificationSound();
            }
          }
        }
        
        // Update our tracking
        lastOrdersRef.current.set(order.id, order.status);
      });

      return { newOrders, statusChanges };
    },
    [enabled, onNewOrder, onStatusChange, playNotificationSound]
  );

  const showStatusToast = (orderId: number, status: OrderStatus, orderNumber?: string) => {
    const displayNumber = orderNumber || `#${orderId}`;
    
    const messages = {
      pending: `Your order ${displayNumber} has been received and is pending confirmation`,
      confirmed: `Your order ${displayNumber} has been confirmed and will start preparing soon`,
      preparing: `Your order ${displayNumber} is now being prepared`,
      ready: `Your order ${displayNumber} is ready for pickup!`,
      completed: `Your order ${displayNumber} has been completed. Thank you!`,
      cancelled: `Your order ${displayNumber} has been cancelled`,
      rejected: `Your order ${displayNumber} has been rejected`
    };

    const configs = {
      pending: { type: 'info' as const, autoClose: 4000 },
      confirmed: { type: 'success' as const, autoClose: 4000 },
      preparing: { type: 'info' as const, autoClose: 4000 },
      ready: { type: 'success' as const, autoClose: 8000 },
      completed: { type: 'success' as const, autoClose: 4000 },
      cancelled: { type: 'error' as const, autoClose: 5000 },
      rejected: { type: 'error' as const, autoClose: 5000 }
    };

    const { type, autoClose } = configs[status];
    // Use a unique ID to prevent duplicate notifications
    const toastId = `order-${orderId}-${status}`;
    toast[type](messages[status], { 
      position: "top-right", 
      autoClose,
      toastId,  // Prevents duplicate toasts
      isLoading: false,
      closeButton: true
    });
  };

  return { detectOrderChanges }; 
};
