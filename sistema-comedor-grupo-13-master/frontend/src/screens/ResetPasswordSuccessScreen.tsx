import styles from "../components/RecoverPassword/RecoverPassword.module.css";
import { CheckCircle } from "lucide-react";
import { motion } from "framer-motion";
import { useLocation } from "wouter";

export function PasswordResetSuccessScreen() {
  const [, setLocation] = useLocation();

  return (
    <div className={styles.recoverBg}>
      <div className={styles.logoCircle}>
        <span role="img" aria-label="logo" className={styles.logoIcon}>🎓</span>
      </div>

      <h1 className={styles.title}>Comedor Universitario</h1>
      <p className={styles.subtitle}>Contraseña restablecida 🔑</p>

      <div className={styles.recoverWrapper}>
        <motion.div
          className={styles.recoverCard}
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, ease: "easeOut" }}
        >
          <motion.div
            initial={{ scale: 0, rotate: -90 }}
            animate={{ scale: 1, rotate: 0 }}
            transition={{
              duration: 0.6,
              type: "spring",
              stiffness: 200,
              damping: 10,
              delay: 0.2,
            }}
          >
            <CheckCircle className={styles.successIcon} />
          </motion.div>

          <motion.h2
            className={styles.recoverTitle}
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 0.5 }}
          >
            ¡Tu contraseña fue actualizada!
          </motion.h2>

          <motion.p
            className={styles.recoverDesc}
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 0.7 }}
          >
            Has restablecido tu contraseña correctamente. <br />
            Ahora podés iniciar sesión con tu nueva clave.
          </motion.p>

          <motion.button
            onClick={() => setLocation("/login")}
            className={styles.submitButton}
            whileHover={{ scale: 1.05 }}
            whileTap={{ scale: 0.97 }}
            transition={{ type: "spring", stiffness: 300 }}
          >
            Ir al inicio de sesión
          </motion.button>
        </motion.div>
      </div>
    </div>
  );
}

