import { AlertCircle, X } from "lucide-react";
import styles from "./LoginErrorAlert.module.css";

interface LoginErrorAlertProps {
  message: string;
  onDismiss: () => void;
}

export function LoginErrorAlert({ message, onDismiss }: LoginErrorAlertProps) {
  // Mapear mensajes de error comunes a mensajes más amigables
  const getFriendlyMessage = (msg: string): { title: string; description: string } => {
    if (msg.includes("Invalid credentials") || msg.includes("401")) {
      return {
        title: "Credenciales Incorrectas",
        description: "El correo o contraseña que ingresaste no son válidos. Por favor, intenta de nuevo.",
      };
    }
    if (msg.includes("not found") || msg.includes("Usuario no encontrado")) {
      return {
        title: "Usuario No Encontrado",
        description: "No existe una cuenta con ese correo. ¿Quizás quieras registrarte?",
      };
    }
    if (msg.includes("network") || msg.includes("Network")) {
      return {
        title: "Error de Conexión",
        description: "No pudimos conectar con el servidor. Verifica tu conexión a internet e intenta de nuevo.",
      };
    }
    if (msg.includes("timeout")) {
      return {
        title: "Tiempo de Espera Agotado",
        description: "El servidor tardó demasiado en responder. Por favor, intenta de nuevo.",
      };
    }
    return {
      title: "Error en el Inicio de Sesión",
      description: msg || "Algo salió mal. Por favor, intenta de nuevo.",
    };
  };

  const { title, description } = getFriendlyMessage(message);

  return (
    <div className={styles.alertContainer}>
      <div className={styles.alertContent}>
        {/* Icon */}
        <div className={styles.iconWrapper}>
          <AlertCircle size={24} />
        </div>

        {/* Text Content */}
        <div className={styles.textContent}>
          <h3 className={styles.title}>{title}</h3>
          <p className={styles.description}>{description}</p>
        </div>

        {/* Dismiss Button */}
        <button
          onClick={onDismiss}
          className={styles.dismissButton}
          aria-label="Cerrar error"
        >
          <X size={18} />
        </button>
      </div>

      {/* Progress Bar */}
      <div className={styles.progressBar} />
    </div>
  );
}
