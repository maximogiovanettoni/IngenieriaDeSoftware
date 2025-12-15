import { useEffect, useState } from "react";
import { useLocation } from "wouter";
import styles from "../components/Login/Login.module.css";

export const EmailVerificationScreen = () => {
  const [, setLocation] = useLocation();
  
  // Extract token from URL query parameters
  const urlParams = new URLSearchParams(window.location.search);
  const token = urlParams.get("token");
  
  const [status, setStatus] = useState<'loading' | 'success' | 'error'>('loading');
  const [message, setMessage] = useState('');

  useEffect(() => {
    const verifyEmailToken = async (token: string) => {
      try {
        const response = await fetch('/api/auth/verify-email', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ token }),
        });
        const data = await response.json();

        if (data.success) {
          setStatus('success');
          setMessage(data.message);
          setTimeout(() => setLocation('/login'), 3000);
        } else {
          setStatus('error');
          setMessage(data.message);
        }
      } catch {
        setStatus('error');
        setMessage('Network error occurred');
      }
  };

  if (token) {
    verifyEmailToken(token);
  } else {
    setStatus('error');
    setMessage('No verification token provided');
  }
}, [token, setLocation]);

  return (
    <div className={`${styles.loginBg} ${styles.compact}`}>
      <div className={styles.logoCircle}>
        <span role="img" aria-label="logo" className={styles.logoIcon}>🎓</span>
      </div>

      <h1 className={styles.title}>Comedor Universitario</h1>
      <p className={styles.subtitle}>Verificación de correo electrónico</p>

      <div className={styles.loginWrapper}>
        <div className={styles.card}>
          {status === "loading" && (
            <>
              <h2 className={styles.cardTitle}>Verificando tu correo...</h2>
              <p className={styles.cardDesc}>Por favor espera un momento ⏳</p>
            </>
          )}

          {status === "success" && (
            <>
              <h2 className={styles.cardTitle}>✅ ¡Correo verificado!</h2>
              <p className={styles.cardDesc}>{message}</p>
              <p className={styles.cardDesc}>Redirigiendo al inicio de sesión...</p>
            </>
          )}

          {status === "error" && (
            <>
              <h2 className={styles.cardTitle}>❌ Error de verificación</h2>
              <p className={styles.cardDesc}>{message}</p>

              <div style={{ display: "flex", justifyContent: "center", gap: "10px", marginTop: "20px" }}>
                <button
                  onClick={() => setLocation("/login")}
                  className={styles.mainButton}
                  style={{ width: "45%" }}
                >
                  Ir al Login
                </button>
                <button
                  onClick={() => setLocation("/signup")}
                  className={styles.mainButton}
                  style={{
                    width: "45%",
                    background: "#3b82f6",
                    boxShadow: "0 2px 16px #3b82f680",
                  }}
                >
                  Registrarme
                </button>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
};