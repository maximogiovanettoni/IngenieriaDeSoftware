import { useAppForm } from "@/config/use-app-form";
import { LoginRequest, LoginRequestSchema } from "@/models/Login";
import { useState, useEffect } from "react";
import { LoginErrorAlert } from "./LoginErrorAlert";
import styles from "./Login.module.css";

type Props = {
  onSubmit: (value: LoginRequest) => void;
  submitError: Error | null;
};

export function Login({ onSubmit, submitError }: Props) {
  const [showError, setShowError] = useState(!!submitError);

  // Sincronizar showError cuando submitError cambia
  useEffect(() => {
    if (submitError) {
      setShowError(true);
    }
  }, [submitError]);

  const formData = useAppForm({
    defaultValues: {
      username: "",
      password: "",
    },
    validators: {
      onSubmit: LoginRequestSchema,
    },
    onSubmit: async ({ value }) => {
      setShowError(false);
      onSubmit(value);
    },
  });

  return (
    <div className={styles.loginBg}>
      <div className={styles.logoCircle}>
        <span role="img" aria-label="logo" className={styles.logoIcon}>🎓</span>
      </div>
      <h1 className={styles.title}>Comedor Universitario</h1>
      <p className={styles.subtitle}>Inicia sesión para acceder a tu cuenta</p>
      <div className={styles.loginWrapper}>
        <div className={styles.card}>
          <h2 className={styles.cardTitle}>Iniciar Sesión</h2>
          <p className={styles.cardDesc}>Ingresa tus credenciales universitarias</p>
          <formData.AppForm>
            <formData.FormContainer extraError={null}>
              {submitError && showError && (
                <LoginErrorAlert
                  message={submitError.message}
                  onDismiss={() => setShowError(false)}
                />
              )}
              <div className={styles.inputGroup}>
                <formData.AppField name="username">
                  {(field) => (
                    <div className={styles.fieldContainer}>
                      <label htmlFor={field.name} className={styles.label}>
                        Correo Electrónico 📧
                      </label>
                      <div className={styles.inputIconWrapper}>
                        <field.TextField
                          label=""
                        />
                      </div>
                    </div>
                  )}
                </formData.AppField>
                <formData.AppField name="password">
                  {(field) => (
                    <div className={styles.fieldContainer}>
                      <label htmlFor={field.name} className={styles.label}>
                        Contraseña 🔒
                      </label>
                      <div className={styles.inputIconWrapper}>
                        <field.PasswordField
                          label=""
                        />
                      </div>
                    </div>
                  )}
                </formData.AppField>
              </div>
              <div className={styles.optionsRow}>
                <a href="/reset-request" className={styles.link}>
                  ¿Olvidaste tu contraseña?
                </a>
              </div>
              <button type="submit" className={styles.mainButton}>
                Iniciar Sesión
              </button>
              <div className={styles.registerRow}>
                <span>¿No tienes cuenta?{" "}</span>
                <a href="/signup" className={styles.link}>
                  Regístrate aquí
                </a>
              </div>
            </formData.FormContainer>
          </formData.AppForm>
        </div>
      </div>
    </div>
  );
}