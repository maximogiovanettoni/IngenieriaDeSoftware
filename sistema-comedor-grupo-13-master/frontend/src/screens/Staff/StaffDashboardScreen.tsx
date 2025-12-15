import { ClipboardList, Loader2, LogOut, Settings, Users } from "lucide-react";
import { useEffect, useState } from "react";
import { useLocation } from "wouter";

import { useToken } from "@/services/TokenContext";

import "./staff-dashboard.css";

interface StaffDashboardProps {
  onNavigate?: (section: string) => void;
}

interface StaffStats {
  pendingCount: number;
  confirmedCount: number;
  preparingCount: number;
  readyCount: number;
  completedCount: number;
  rejectedCount: number;
  cancelledCount: number;
}

/* ---------- Subcomponentes ---------- */

function Header() {
  return (
    <div className="header">
      <div className="logo-badge">
        <Users className="logo-badge__ico" size={32} />
      </div>
      <div className="header__text">
        <h1 className="header__title">Panel de Personal del Comedor</h1>
        <p className="header__subtitle">Gestión de pedidos y preparación de alimentos</p>
      </div>
    </div>
  );
}

interface DashboardCardProps {
  icon: React.ReactNode;
  title: string;
  description: string;
  color: string;
  onClick?: () => void;
  badge?: string | number;
}

function DashboardCard({ icon, title, description, color, onClick, badge }: DashboardCardProps) {
  return (
    <button onClick={onClick} className="card-tile">
      <div className="card-tile__row">
        <div className="card-tile__left">
          <div className="card-tile__avatar" style={{ backgroundColor: color }}>
            {icon}
          </div>
          <div className="card-tile__copy">
            <p className="card-tile__title">{title}</p>
            <p className="card-tile__desc">{description}</p>
          </div>
        </div>
        {badge !== undefined && (
          <div className="badge">
            <span>{badge}</span>
          </div>
        )}
      </div>
    </button>
  );
}

function QuickStats({ stats, loading }: { stats: StaffStats | null; loading: boolean }) {
  const StatCard = ({ label, value, color }: { label: string; value: number | string; color: string }) => (
    <div
      style={{
        background: "var(--panel)",
        border: "1px solid var(--border)",
        borderRadius: "12px",
        padding: "20px",
        textAlign: "center",
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        gap: "12px",
        minWidth: "140px",
        transition: "all 0.2s",
      }}
      onMouseEnter={(e) => {
        (e.currentTarget as HTMLElement).style.borderColor = "var(--accent)";
        (e.currentTarget as HTMLElement).style.transform = "translateY(-2px)";
      }}
      onMouseLeave={(e) => {
        (e.currentTarget as HTMLElement).style.borderColor = "var(--border)";
        (e.currentTarget as HTMLElement).style.transform = "translateY(0)";
      }}
    >
      <p style={{ color: "var(--muted)", fontSize: "12px", margin: 0, fontWeight: "500" }}>{label}</p>
      <p style={{ color: color, fontSize: "32px", fontWeight: "700", margin: 0 }}>
        {loading ? <Loader2 size={24} className="inline animate-spin" /> : value}
      </p>
    </div>
  );

  return (
    <div className="section">
      <h2 className="section__title">Estadísticas Rápidas</h2>
      <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(140px, 1fr))", gap: "16px" }}>
        <StatCard label="Pendientes" value={stats?.pendingCount ?? "--"} color="#fbbf24" />
        <StatCard label="Confirmados" value={stats?.confirmedCount ?? "--"} color="#3b82f6" />
        <StatCard label="En Preparación" value={stats?.preparingCount ?? "--"} color="#f59e0b" />
        <StatCard label="Listos" value={stats?.readyCount ?? "--"} color="#06b6d4" />
        <StatCard label="Completados" value={stats?.completedCount ?? "--"} color="#10b981" />
        <StatCard label="Rechazados" value={stats?.rejectedCount ?? "--"} color="#ef4444" />
        <StatCard label="Cancelados" value={stats?.cancelledCount ?? "--"} color="#8b5cf6" />
      </div>
    </div>
  );
}

/* ---------- Pantalla principal ---------- */

export function StaffDashboardScreen({ onNavigate }: StaffDashboardProps) {
  const [tokenState, setToken] = useToken();
  const [, setLocation] = useLocation();
  const [stats, setStats] = useState<StaffStats | null>(null);
  const [loading, setLoading] = useState(true);

  // Obtener el token del estado
  const token =
    tokenState.state === "LOGGED_IN" && tokenState.tokens
      ? (tokenState.tokens as { accessToken?: string }).accessToken
      : null;

  useEffect(() => {
    const fetchStats = async () => {
      if (!token) {
        console.error("No token available");
        setLoading(false);
        return;
      }

      try {
        setLoading(true);
        // Obtener órdenes del staff en lugar de usar endpoint de stats
        const response = await fetch("http://localhost:21300/api/orders", {
          method: "GET",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
        });

        if (response.ok) {
          const orders = await response.json();

          // Contar órdenes por estado
          const pendingCount = orders.filter((o: { status: string }) => o.status === "PENDING").length;
          const confirmedCount = orders.filter((o: { status: string }) => o.status === "CONFIRMED").length;
          const preparingCount = orders.filter((o: { status: string }) => o.status === "PREPARING").length;
          const readyCount = orders.filter((o: { status: string }) => o.status === "READY").length;
          const completedCount = orders.filter((o: { status: string }) => o.status === "COMPLETED").length;
          const rejectedCount = orders.filter((o: { status: string }) => o.status === "REJECTED").length;
          const cancelledCount = orders.filter((o: { status: string }) => o.status === "CANCELLED").length;

          setStats({
            pendingCount,
            confirmedCount,
            preparingCount,
            readyCount,
            completedCount,
            rejectedCount,
            cancelledCount,
          });
        } else if (response.status === 401) {
          console.error("Unauthorized - token may be invalid");
        }
      } catch (error) {
        console.error("Error fetching staff stats:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchStats();
    // Refresh stats every 5 seconds
    const interval = setInterval(fetchStats, 5000);

    return () => clearInterval(interval);
  }, [token]);

  const handleLogout = () => {
    const ok = window.confirm("¿Estás seguro que deseas cerrar sesión?");
    if (ok) {
      setToken({ state: "LOGGED_OUT" });
      setLocation("/login");
    }
  };

  const handleNavigate = (section: string) => {
    const routeMap: Record<string, string> = {
      orders: "/staff/orders",
      preparation: "/staff/preparation",
      inventory: "/staff/inventory",
      schedule: "/staff/schedule",
    };
    if (onNavigate) onNavigate(section);
    else if (routeMap[section]) setLocation(routeMap[section]);
  };

  return (
    <div className="page">
      <div className="center">
        {/* Botones fijos */}
        <button className="fab fab--profile" onClick={() => setLocation("/profile")} title="Mi Perfil">
          <Settings size={18} />
          <span>Mi Perfil</span>
        </button>

        <button className="fab fab--logout" onClick={handleLogout} title="Cerrar Sesión">
          <LogOut size={18} />
          <span>Cerrar Sesión</span>
        </button>

        {/* Encabezado */}
        <Header />

        {/* Stats */}
        <QuickStats stats={stats} loading={loading} />

        {/* Acciones */}
        <div className="section">
          <h2 className="section__title">Acciones Disponibles</h2>
          <div className="tiles">
            <DashboardCard
              icon={<ClipboardList className="ico-white" size={24} />}
              title="Gestión de Pedidos"
              description="Ver y gestionar todas las órdenes"
              color="rgba(81, 162, 255, 0.3)"
              onClick={() => handleNavigate("orders")}
              badge={stats?.pendingCount ?? 0}
            />
          </div>
        </div>

        {/* Actividad reciente */}
        <div className="section">
          <h2 className="section__title">Actividad Reciente</h2>
          <div className="card card--soft">
            <p className="empty">No hay actividad reciente</p>
          </div>
        </div>
      </div>
    </div>
  );
}
