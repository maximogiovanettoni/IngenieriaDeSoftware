import { Users, Package, Pizza, Layers, LogOut, Percent } from "lucide-react";
import { useToken } from "@/services/TokenContext";
import { useLocation } from "wouter";
import "./admin-screen.css";

interface AdminScreenProps {
  onNavigate?: (section: string) => void;
}

type ModuleItem = {
  id: string;
  title: string;
  description: string;
  icon: React.ReactNode;
  tone: "blue" | "amber" | "indigo" | "pink" | "purple";
};

const MODULES: ModuleItem[] = [
  {
    id: "staff",
    title: "Staff",
    description: "Gestionar el personal del comedor",
    icon: <Users size={20} />,
    tone: "blue",
  },
  {
    id: "ingredientes",
    title: "Ingredientes",
    description: "Administrar ingredientes y materias primas",
    icon: <Package size={20} />,
    tone: "amber",
  },
  {
    id: "productos",
    title: "Productos",
    description: "Gestionar productos y platos del menú",
    icon: <Pizza size={20} />,
    tone: "indigo",
  },
  {
    id: "combos",
    title: "Combos",
    description: "Crear y modificar combos especiales",
    icon: <Layers size={20} />,
    tone: "pink",
  },
  {
    id: "promociones",
    title: "Promociones",
    description: "Gestionar promociones y descuentos",
    icon: <Percent size={20} />,
    tone: "purple",
  },
];

export default function AdminScreen({ onNavigate }: AdminScreenProps) {
  const [, setToken] = useToken();
  const [, setLocation] = useLocation();

  const navigate = (section: string) => {
    const routeMap: Record<string, string> = {
      staff: "/admin/staff",
      ingredientes: "/admin/ingredients",
      productos: "/admin/products",
      combos: "/admin/combos",
      promociones: "/admin/promotions",
    };
    if (onNavigate) onNavigate(section);
    else setLocation(routeMap[section] ?? "/admin");
  };

  const handleLogout = () => {
    const ok = window.confirm("¿Estás seguro que deseas cerrar sesión?");
    if (ok) {
      setToken({ state: "LOGGED_OUT" });
      setLocation("/login");
    }
  };

  return (
    <div className="admin-page">
      <button className="logout-btn" onClick={handleLogout} title="Cerrar Sesión">
        <LogOut size={18} />
        <span>Cerrar Sesión</span>
      </button>

      <header className="admin-header">
        <div className="header-badge">
          <Users className="header-badge__ico" size={22} />
        </div>
        <h1 className="admin-title">Panel de Administración</h1>
        <p className="admin-subtitle">Selecciona una sección para administrar</p>
      </header>

      <section className="admin-card admin-card--glow">
        <div className="card-inner">
          <div className="card-head">
            <h2 className="card-title">Módulos Disponibles</h2>
            <p className="card-sub">Gestiona el sistema del comedor universitario</p>
          </div>

          <ul className="module-list">
            {MODULES.map((m) => (
              <li key={m.id}>
                <button
                  className={`module-item tone-${m.tone}`}
                  onClick={() => navigate(m.id)}
                >
                  <span className="module-ico">{m.icon}</span>
                  <span className="module-texts">
                    <span className="module-title">{m.title}</span>
                    <span className="module-desc">{m.description}</span>
                  </span>
                </button>
              </li>
            ))}
          </ul>
        </div>
      </section>
    </div>
  );
}
