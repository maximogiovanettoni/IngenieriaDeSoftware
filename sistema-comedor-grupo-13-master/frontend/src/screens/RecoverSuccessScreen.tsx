import styles from "../components/RecoverPassword/RecoverPassword.module.css";
import { Mail } from "lucide-react";
import { useLocation } from "wouter";

export function RecoverSuccessScreen() {
  const [, setLocation] = useLocation();

  return (
    <div className={styles.recoverBg}>
      <div className={styles.logoCircle}>
        <span role="img" aria-label="logo" className={styles.logoIcon}>🎓</span>
      </div>

      <h1 className={styles.title}>Comedor Universitario</h1>
      <p className={styles.subtitle}>Recuperación de contraseña 🔒</p>

      <div className={styles.recoverWrapper}>
        <div className={styles.recoverCard}>
          <Mail className={styles.successIcon} />
          <h2 className={styles.recoverTitle}>¡Enlace enviado!</h2>
          <p className={styles.recoverDesc}>
            Te enviamos un enlace para restablecer tu contraseña. <br />
            Por favor, revisá tu bandeja de entrada (y la carpeta de spam).
          </p>

          <button
            onClick={() => setLocation("/login")}
            className={styles.submitButton}
          >
            Volver al inicio de sesión
          </button>
        </div>
      </div>
    </div>
  );
}