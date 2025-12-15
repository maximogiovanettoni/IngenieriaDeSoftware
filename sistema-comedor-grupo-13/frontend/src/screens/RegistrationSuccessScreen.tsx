import styles from "../components/RecoverPassword/RecoverPassword.module.css";
import { MailCheck } from "lucide-react";
import { useLocation } from "wouter";

export function RegistrationSuccessScreen() {
  const [, setLocation] = useLocation();

  return (
    <div className={styles.recoverBg}>
      <div className={styles.logoCircle}>
        <span role="img" aria-label="logo" className={styles.logoIcon}>🎓</span>
      </div>

      <h1 className={styles.title}>Comedor Universitario</h1>
      <p className={styles.subtitle}>Verificación de correo electrónico 📧</p>

      <div className={styles.recoverWrapper}>
        <div className={styles.recoverCard}>
          <MailCheck className={styles.successIcon} />
          <h2 className={styles.recoverTitle}>¡Registro exitoso!</h2>
          <p className={styles.recoverDesc}>
            Te enviamos un correo para activar tu cuenta. <br />
            Por favor, revisá tu bandeja de entrada (y la carpeta de spam).
          </p>

          <button
            onClick={() => setLocation("/login")}
            className={styles.submitButton}
          >
            Ir al inicio de sesión
          </button>
        </div>
      </div>
    </div>
  );
}
