import { useState, useEffect, useCallback } from 'react';
import { useLocation } from 'wouter';
import { useToken } from '@/services/TokenContext';
import { orderAPI, OrderResponse } from '@/services/OrderAPI';
import { ArrowLeft, Loader2, AlertCircle, TrendingUp, Package, Clock, CheckCircle2, XCircle } from 'lucide-react';

export const StudentTrackingPage = () => {
  const [, setLocation] = useLocation();
  const [tokenState] = useToken();
  
  const [orders, setOrders] = useState<OrderResponse[]>([]);
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const [stats, setStats] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<'active' | 'history'>('active');

  const loadData = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const token = tokenState.state === 'LOGGED_IN' ? tokenState.tokens.accessToken : undefined;
      
      const ordersData = await orderAPI.getMyOrders(token);
      
      // Calcular estadísticas localmente
      const totalOrders = ordersData.length;
      const pendingCount = ordersData.filter(o => o.status === 'PENDING').length;
      const confirmedCount = ordersData.filter(o => o.status === 'CONFIRMED').length;
      const deliveredCount = ordersData.filter(o => o.status === 'DELIVERED').length;
      const rejectedCount = ordersData.filter(o => o.status === 'REJECTED').length;
      const totalSpent = ordersData.reduce((sum, o) => sum + (typeof o.totalAmount === 'string' ? parseFloat(o.totalAmount) : o.totalAmount), 0);
      
      const statsData = {
        totalOrders,
        pendingCount,
        confirmedCount,
        deliveredCount,
        rejectedCount,
        totalSpent,
      };
      
      setOrders(ordersData);
      setStats(statsData);
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Error cargando datos';
      setError(message);
      console.error('Error loading tracking data:', err);
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
    return orders.filter(o => o.status.toLowerCase() === 'pendiente' || o.status.toLowerCase() === 'confirmado');
  };

  const getHistoryOrders = () => {
    return orders.filter(o => o.status.toLowerCase() === 'entregado' || o.status.toLowerCase() === 'rechazado');
  };

  const getStatusIcon = (status: string) => {
    switch (status.toLowerCase()) {
      case 'pendiente':
        return <Clock size={20} className="text-[#f59e0b]" />;
      case 'confirmado':
        return <CheckCircle2 size={20} className="text-[#3b82f6]" />;
      case 'entregado':
        return <Package size={20} className="text-[#10b981]" />;
      case 'rechazado':
        return <XCircle size={20} className="text-[#ef4444]" />;
      default:
        return <Clock size={20} className="text-[#6b7280]" />;
    }
  };

  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case 'pendiente':
        return 'bg-[#f59e0b]/20 border-[#f59e0b] text-[#f59e0b]';
      case 'confirmado':
        return 'bg-[#3b82f6]/20 border-[#3b82f6] text-[#3b82f6]';
      case 'entregado':
        return 'bg-[#10b981]/20 border-[#10b981] text-[#10b981]';
      case 'rechazado':
        return 'bg-[#ef4444]/20 border-[#ef4444] text-[#ef4444]';
      default:
        return 'bg-[#6b7280]/20 border-[#6b7280] text-[#6b7280]';
    }
  };

  const displayOrders = activeTab === 'active' ? getActiveOrders() : getHistoryOrders();

  if (loading) {
    return (
      <div className="bg-white content-stretch flex flex-col items-start relative min-h-screen overflow-hidden">
        <div className="bg-[#1a1a1a] min-h-screen w-full flex items-center justify-center">
          <div className="text-center">
            <Loader2 size={32} className="text-[#51a2ff] animate-spin mx-auto mb-4" />
            <p className="text-[#99a1af]">Cargando seguimiento...</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-white content-stretch flex flex-col items-start relative min-h-screen overflow-hidden">
      <div className="bg-[#1a1a1a] min-h-screen w-full">
        {/* Header */}
        <div className="bg-gradient-to-b from-[#2a2a2a] to-[#1a1a1a] border-b border-[#4a5565] px-4 md:px-8 py-8">
          <div className="max-w-[1400px] mx-auto">
            <button
              onClick={() => setLocation('/')}
              className="flex items-center gap-2 text-[#51a2ff] hover:text-[#2b7fff] transition-colors mb-6"
            >
              <ArrowLeft size={16} />
              <p className="text-[13px] font-medium">Volver al Dashboard</p>
            </button>

            <h1 className="text-white text-[28px] md:text-[36px] font-bold mb-2">
              Seguimiento de Órdenes
            </h1>
            <p className="text-[#99a1af] text-[14px]">
              Monitorea el estado de tus pedidos en tiempo real
            </p>
          </div>
        </div>

        {/* Stats Section */}
        {stats && (
          <div className="max-w-[1400px] mx-auto px-4 md:px-8 py-8">
            <div className="grid grid-cols-2 md:grid-cols-5 gap-4 mb-8">
              <div className="bg-[#2a2a2a] border border-[#4a5565] rounded-lg p-4">
                <p className="text-[#99a1af] text-[12px] mb-2">Total de Pedidos</p>
                <p className="text-[#51a2ff] font-bold text-[24px]">{stats.totalOrders}</p>
              </div>

              <div className="bg-[#2a2a2a] border border-[#4a5565] rounded-lg p-4">
                <p className="text-[#99a1af] text-[12px] mb-2">Pendientes</p>
                <p className="text-[#f59e0b] font-bold text-[24px]">{stats.pendingCount}</p>
              </div>

              <div className="bg-[#2a2a2a] border border-[#4a5565] rounded-lg p-4">
                <p className="text-[#99a1af] text-[12px] mb-2">Confirmados</p>
                <p className="text-[#3b82f6] font-bold text-[24px]">{stats.confirmedCount}</p>
              </div>

              <div className="bg-[#2a2a2a] border border-[#4a5565] rounded-lg p-4">
                <p className="text-[#99a1af] text-[12px] mb-2">Entregados</p>
                <p className="text-[#10b981] font-bold text-[24px]">{stats.deliveredCount}</p>
              </div>

              <div className="bg-[#2a2a2a] border border-[#4a5565] rounded-lg p-4">
                <p className="text-[#99a1af] text-[12px] mb-2 flex items-center gap-1">
                  <TrendingUp size={14} /> Gastado
                </p>
                <p className="text-[#51a2ff] font-bold text-[24px]">
                  ${(typeof stats?.totalSpent === 'number' ? stats.totalSpent : parseFloat(stats?.totalSpent || '0')).toFixed(2)}
                </p>
              </div>
            </div>
          </div>
        )}

        {/* Error Alert */}
        {error && (
          <div className="max-w-[1400px] mx-auto px-4 md:px-8">
            <div className="mb-6 bg-[#ef4444]/20 border border-[#ef4444] rounded-lg p-4 flex items-center gap-3">
              <AlertCircle size={20} className="text-[#ef4444]" />
              <p className="text-[#ef4444] text-[13px]">{error}</p>
            </div>
          </div>
        )}

        {/* Tabs */}
        <div className="max-w-[1400px] mx-auto px-4 md:px-8 py-6 border-b border-[#4a5565]">
          <div className="flex gap-4">
            <button
              onClick={() => setActiveTab('active')}
              className={`px-4 py-2 text-[13px] font-semibold transition-colors border-b-2 ${
                activeTab === 'active'
                  ? 'border-[#51a2ff] text-[#51a2ff]'
                  : 'border-transparent text-[#99a1af] hover:text-white'
              }`}
            >
              Órdenes Activas ({getActiveOrders().length})
            </button>
            <button
              onClick={() => setActiveTab('history')}
              className={`px-4 py-2 text-[13px] font-semibold transition-colors border-b-2 ${
                activeTab === 'history'
                  ? 'border-[#51a2ff] text-[#51a2ff]'
                  : 'border-transparent text-[#99a1af] hover:text-white'
              }`}
            >
              Historial ({getHistoryOrders().length})
            </button>
          </div>
        </div>

        {/* Orders List */}
        <div className="max-w-[1400px] mx-auto px-4 md:px-8 py-8">
          {displayOrders.length === 0 ? (
            <div className="bg-[#2a2a2a] border border-[#4a5565] rounded-lg p-12 text-center">
              <Package size={48} className="text-[#4a5565] mx-auto mb-4" />
              <p className="text-[#99a1af] text-[14px]">
                {activeTab === 'active' ? 'No hay órdenes activas' : 'No hay historial'}
              </p>
            </div>
          ) : (
            <div className="space-y-4">
              {displayOrders.map((order) => (
                <div
                  key={order.id}
                  onClick={() => setLocation(`/orders/${order.id}`)}
                  className="bg-[#2a2a2a] border border-[#4a5565] rounded-lg p-4 hover:border-[#51a2ff] transition-colors cursor-pointer"
                >
                  <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
                    {/* Left Section */}
                    <div className="flex-1">
                      <div className="flex items-center gap-3 mb-2">
                        {getStatusIcon(order.status)}
                        <p className="text-white font-bold text-[14px]">{order.orderNumber}</p>
                        <span className={`border rounded-full px-3 py-1 text-[11px] font-semibold ${getStatusColor(order.status)}`}>
                          {order.status}
                        </span>
                      </div>
                      <p className="text-[#99a1af] text-[12px]">
                        {new Date(order.createdAt).toLocaleDateString('es-AR', {
                          year: 'numeric',
                          month: 'long',
                          day: 'numeric',
                          hour: '2-digit',
                          minute: '2-digit',
                        })}
                      </p>
                    </div>

                    {/* Right Section */}
                    <div className="flex items-center gap-6">
                      {order.pickupPoint && (
                        <div className="text-right">
                          <p className="text-[#99a1af] text-[11px] mb-1">Retiro en</p>
                          <p className="text-[#51a2ff] font-semibold text-[12px]">{order.pickupPoint.name}</p>
                        </div>
                      )}
                      <div className="text-right">
                        <p className="text-[#99a1af] text-[11px] mb-1">Total</p>
                        <p className="text-[#51a2ff] font-bold text-[16px]">
                          ${(typeof order.totalAmount === 'string' ? parseFloat(order.totalAmount) : order.totalAmount).toFixed(2)}
                        </p>
                      </div>
                    </div>
                  </div>

                  {/* Items Preview */}
                  <div className="mt-3 pt-3 border-t border-[#4a5565] text-[11px] text-[#99a1af]">
                    {order.items.slice(0, 2).map((item, idx) => (
                      <p key={idx}>
                        {item.quantity}x {item.productName}
                      </p>
                    ))}
                    {order.items.length > 2 && (
                      <p className="text-[#51a2ff]">
                        +{order.items.length - 2} más
                      </p>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
