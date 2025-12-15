import "./footer.css";
import { useLocation } from "wouter";

export function Footer() {
  const [, setLocation] = useLocation();

  return (
    <footer className="ft">
      <div className="ft__inner">
        {/* Brand */}
        <div className="ft__col">
          <div className="ft__brand">
            <div className="ft__logo" aria-hidden>🎓</div>
            <div className="ft__brandText">
              <h3 className="ft__title">Comedor Universitario</h3>
              <p className="ft__subtitle">Menú del día — rico, rápido y accesible</p>
            </div>
          </div>
          <p className="ft__desc">
            Pedí desde la web, retiralo en ventanilla o recibilo en mesa.
          </p>
        </div>

        <div className="ft__col">
          <h4 className="ft__heading">Navegación</h4>
          <ul className="ft__links">
            <li><button className="ft__link" onClick={() => setLocation("/menu")}>Menú</button></li>
            <li><button className="ft__link" onClick={() => setLocation("/orders")}>Mis pedidos</button></li>
            <li><button className="ft__link" onClick={() => setLocation("/profile")}>Mi perfil</button></li>
            <li><button className="ft__link" onClick={() => setLocation("/dashboard")}>Panel</button></li>
          </ul>
        </div>



      </div>

      <div className="ft__bottom">
        <p>© {new Date().getFullYear()} Comedor Universitario · Todos los derechos reservados</p>
        <p className="ft__tiny">Hecho con ❤ por el equipo del Grupo 13 - Ing. de Software I (Cátedra Montaldo)</p>
      </div>
    </footer>
  );
}
