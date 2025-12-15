import { useState, useEffect, useCallback } from 'react';
import { useLocation } from 'wouter';
import { useToken } from '@/services/TokenContext';
import { orderAPI, OrderResponse } from '@/services/OrderAPI';
import { 
  ArrowLeft, Loader2, AlertCircle, Package, Clock, CheckCircle2, 
  XCircle, Zap
} from 'lucide-react';
import './Student/menu-page.css';

export const StudentDashboardPage = () => {
  const [, setLocation] = useLocation();
  const [tokenState] = useToken();
  
  const [orders, setOrders] = useState<OrderResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadData = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const token = tokenState.state === 'LOGGED_IN' ? tokenState.tokens.accessToken : undefined;
      
      const ordersData = await orderAPI.getMyOrders(token);
      
      setOrders(ordersData);
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Error cargando datos';
      setError(message);
      console.error('Error loading dashboard data:', err);
    } finally {
      setLoading(false);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [tokenState.state]);

  useEffect(() => {
    if (tokenState.state === 'LOGGED_OUT') {
      setLocation('/login');
      return;
    }

    loadData();
  }, [tokenState.state, loadData, setLocation]);

  const getActiveOrders = () => {
    return orders.filter(o => {
      const status = o.status.toUpperCase();
      return status === 'PENDING' || status === 'CONFIRMED' || status === 'PREPARING' || status === 'READY' || status === 'COMPLETED';
    });
  };

  const getStatusIcon = (status: string) => {
    const statusUpper = status.toUpperCase();
    switch (statusUpper) {
      case 'PENDING':
        return <Clock size={20} className="text-[#f59e0b]" />;
      case 'CONFIRMED':
        return <CheckCircle2 size={20} className="text-[#3b82f6]" />;
      case 'PREPARING':
        return <Package size={20} className="text-[#8b5cf6]" />;
      case 'READY':
        return <CheckCircle2 size={20} className="text-[#06b6d4]" />;
      case 'COMPLETED':
        return <CheckCircle2 size={20} className="text-[#10b981]" />;
      case 'REJECTED':
        return <XCircle size={20} className="text-[#ef4444]" />;
      case 'CANCELLED':
        return <XCircle size={20} className="text-[#ef4444]" />;
      default:
        return <Package size={20} className="text-[#6b7280]" />;
    }
  };

  const getStatusLabel = (status: string) => {
    const statusUpper = status.toUpperCase();
    switch (statusUpper) {
      case 'PENDING':
        return 'Pendiente';
      case 'CONFIRMED':
        return 'Confirmado';
      case 'PREPARING':
        return 'En Preparación';
      case 'READY':
        return 'Listo para Retiro';
      case 'COMPLETED':
        return 'Entregado';
      case 'REJECTED':
        return 'Rechazado';
      case 'CANCELLED':
        return 'Cancelado';
      default:
        return status;
    }
  };

  const getStatusColor = (status: string) => {
    const statusUpper = status.toUpperCase();
    switch (statusUpper) {
      case 'PENDING':
        return { bg: 'bg-[#f59e0b]/20', border: 'border-[#f59e0b]', text: 'text-[#f59e0b]' };
      case 'CONFIRMED':
        return { bg: 'bg-[#3b82f6]/20', border: 'border-[#3b82f6]', text: 'text-[#3b82f6]' };
      case 'PREPARING':
        return { bg: 'bg-[#8b5cf6]/20', border: 'border-[#8b5cf6]', text: 'text-[#8b5cf6]' };
      case 'READY':
        return { bg: 'bg-[#06b6d4]/20', border: 'border-[#06b6d4]', text: 'text-[#06b6d4]' };
      case 'COMPLETED':
        return { bg: 'bg-[#10b981]/20', border: 'border-[#10b981]', text: 'text-[#10b981]' };
      case 'REJECTED':
        return { bg: 'bg-[#ef4444]/20', border: 'border-[#ef4444]', text: 'text-[#ef4444]' };
      case 'CANCELLED':
        return { bg: 'bg-[#ef4444]/20', border: 'border-[#ef4444]', text: 'text-[#ef4444]' };
      default:
        return { bg: 'bg-[#6b7280]/20', border: 'border-[#6b7280]', text: 'text-[#6b7280]' };
    }
  };

  const getProgressPercentage = (status: string) => {
    const statusUpper = status.toUpperCase();
    switch (statusUpper) {
      case 'PENDING':
        return 25;
      case 'CONFIRMED':
        return 40;
      case 'PREPARING':
        return 60;
      case 'READY':
        return 85;
      case 'COMPLETED':
        return 100;
      default:
        return 0;
    }
  };

  const getProgressColor = (status: string) => {
    const statusUpper = status.toUpperCase();
    switch (statusUpper) {
      case 'PENDING':
        return '#f59e0b'; // Amarillo
      case 'CONFIRMED':
        return '#3b82f6'; // Azul
      case 'PREPARING':
        return '#8b5cf6'; // Púrpura
      case 'READY':
        return '#06b6d4'; // Cian
      case 'COMPLETED':
        return '#10b981'; // Verde
      default:
        return '#6b7280'; // Gris
    }
  };

  return (
    <div className="page page--compact">
      {/* Header with Back Button */}
      <div style={{
        background: 'var(--panel)',
        borderBottom: '1px solid var(--border)',
        padding: '12px 16px',
        position: 'sticky',
        top: 0,
        zIndex: 10
      }}>
        <div className="container" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: '16px', padding: 0 }}>
          <h1 style={{ color: 'var(--text)', fontSize: '18px', fontWeight: '700', margin: 0, display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Zap size={20} style={{ color: 'var(--accent)' }} />
            ⚡ Tracking en Vivo
          </h1>
          <button
            onClick={() => setLocation('/menu')}
            className="btn btn--ghost"
            style={{ marginBottom: 0, gap: '4px', fontSize: '12px' }}
          >
            <ArrowLeft size={14} />
            Volver al Menú
          </button>
        </div>
      </div>

      {/* Main Content */}
      <main className="container" style={{ paddingTop: '16px', paddingBottom: '40px' }}>
        {/* Error Alert */}
        {error && (
          <div style={{
            marginBottom: '16px',
            padding: '12px',
            background: 'rgba(239, 68, 68, 0.1)',
            border: '1px solid var(--bad)',
            borderRadius: '10px',
            display: 'flex',
            gap: '8px',
            alignItems: 'flex-start'
          }}>
            <AlertCircle size={18} style={{ color: 'var(--bad)', flexShrink: 0 }} />
            <p style={{ color: 'var(--bad)', fontSize: '12px', margin: 0 }}>{error}</p>
          </div>
        )}

        {/* Loading State */}
        {loading ? (
          <div style={{ textAlign: 'center', padding: '40px 0' }}>
            <Loader2 size={28} style={{ animation: 'spin 1s linear infinite', margin: '0 auto 16px', color: 'var(--accent)' }} />
            <p style={{ color: 'var(--muted)', fontSize: '13px' }}>Cargando tracking en vivo...</p>
          </div>
        ) : (
          <>
            {/* Tracking Content - Direct Display */}
            <div style={{
              background: 'var(--panel)',
              border: '1px solid var(--border)',
              borderRadius: '10px',
              padding: '14px'
            }}>
              <p style={{ color: 'var(--muted)', fontSize: '12px', margin: '0 0 12px' }}>
                Estado actual de todos tus pedidos
              </p>

                {getActiveOrders().length === 0 ? (
                  <div style={{ textAlign: 'center', padding: '24px 0' }}>
                    <Package size={32} style={{ color: 'var(--border)', margin: '0 auto 8px', display: 'block' }} />
                    <p style={{ color: 'var(--muted)', fontSize: '13px' }}>No tienes pedidos en proceso</p>
                  </div>
                ) : (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                    {getActiveOrders().map((order) => {
                      const statusColor = getStatusColor(order.status);
                      return (
                        <div
                          key={order.id}
                          onClick={() => setLocation('/orders')}
                          style={{
                            background: 'var(--panel-2)',
                            border: '1px solid var(--border)',
                            borderRadius: '8px',
                            padding: '10px',
                            cursor: 'pointer',
                            transition: 'border-color 0.2s'
                          }}
                          onMouseEnter={(e) => e.currentTarget.style.borderColor = 'var(--accent)'}
                          onMouseLeave={(e) => e.currentTarget.style.borderColor = 'var(--border)'}
                        >
                          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '8px' }}>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flex: 1 }}>
                              {getStatusIcon(order.status)}
                              <div>
                                <p style={{ color: 'var(--text)', fontSize: '12px', fontWeight: '600', margin: 0 }}>{order.orderNumber}</p>
                                <p style={{ color: 'var(--muted)', fontSize: '11px', margin: 0 }}>
                                  {new Date(order.createdAt).toLocaleDateString('es-AR')}
                                </p>
                              </div>
                            </div>
                            <span style={{
                              fontSize: '11px',
                              fontWeight: '600',
                              padding: '3px 8px',
                              borderRadius: '999px',
                              border: `1px solid ${statusColor.text}`,
                              color: statusColor.text,
                              background: statusColor.bg
                            }}>
                              {getStatusLabel(order.status)}
                            </span>
                          </div>

                          {/* Progress Bar */}
                          <div style={{ marginBottom: '8px' }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '10px', color: 'var(--muted)', marginBottom: '4px' }}>
                              <span>Progreso</span>
                              <span>{getProgressPercentage(order.status)}%</span>
                            </div>
                            <div style={{
                              width: '100%',
                              height: '4px',
                              background: 'var(--border)',
                              borderRadius: '999px',
                              overflow: 'hidden'
                            }}>
                              <div style={{
                                height: '100%',
                                background: getProgressColor(order.status),
                                width: `${getProgressPercentage(order.status)}%`,
                                transition: 'width 0.3s'
                              }} />
                            </div>
                          </div>

                          {/* Details */}
                          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '8px', fontSize: '11px', textAlign: 'center' }}>
                            <div>
                              <p style={{ color: 'var(--muted)', margin: '0 0 2px' }}>Ítems</p>
                              <p style={{ color: 'var(--text)', fontWeight: '600', margin: 0 }}>{order.items.length}</p>
                            </div>
                            <div>
                              <p style={{ color: 'var(--muted)', margin: '0 0 2px' }}>Total</p>
                              <p style={{ color: 'var(--accent)', fontWeight: '600', margin: 0 }}>${(typeof order.totalAmount === 'string' ? parseFloat(order.totalAmount) : order.totalAmount).toFixed(2)}</p>
                            </div>
                            {order.pickupPoint && (
                              <div>
                                <p style={{ color: 'var(--muted)', margin: '0 0 2px' }}>Retiro</p>
                                <p style={{ color: 'var(--ok)', fontWeight: '600', margin: 0, fontSize: '10px' }}>{order.pickupPoint.name}</p>
                              </div>
                            )}
                          </div>
                        </div>
                      );
                    })}
                  </div>
                )}
              </div>
            </>
          )}
        </main>
      </div>
    );
  };
