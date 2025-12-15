import { useAppForm } from "@/config/use-app-form";
import styles from "../components/RecoverPassword/RecoverPassword.module.css";
import { AlertCircle } from "lucide-react";
import { useLocation } from "wouter";
import { useResetPassword } from "@/services/UserServices";
import { useState } from "react";

export function ResetPasswordScreen() {
  const mutation = useResetPassword();
  const [, setLocation] = useLocation();
  const [submitError, setSubmitError] = useState<string | null>(null);
  const token = new URLSearchParams(window.location.search).get("token");

  const formData = useAppForm({
    defaultValues: {
      password: "",
      confirmPassword: "",
    },
    validators: {
      onChange: ({ value }) => {
        const password = value.password.trim();
        const confirmPassword = value.confirmPassword.trim();

        if (password.length < 8) {
          setSubmitError("La contraseña debe tener al menos 8 caracteres");
          return;
        }
        if (confirmPassword !== password) {
          setSubmitError("Las contraseñas no coinciden");
          return;
        }

        setSubmitError(null);
      },
    },

    onSubmit: async ({ value }) => {
      if (!token) {
        setSubmitError("El enlace de recuperación no es válido o ha expirado");
        return;
      }

      try {
        const res = await mutation.mutateAsync({
          token,
          newPassword: value.password,
        });

        if (res?.success) {
          setLocation("/reset-success");
        } else {
          setSubmitError("No se pudo restablecer la contraseña.");
        }
      } catch (err: unknown) {
        if (err instanceof Error) {
          setSubmitError(err.message);
        } else {
          setSubmitError("Error al restablecer la contraseña.");
        }
      }
    },
  });

  return (
    <div className={styles.recoverBg}>
      <div className={styles.logoCircle}>
        <span role="img" aria-label="logo" className={styles.logoIcon}>🎓</span>
      </div>

      <h1 className={styles.title}>Comedor Universitario</h1>
      <p className={styles.subtitle}>Restablecer tu contraseña</p>

      <div className={styles.recoverWrapper}>
        <div className={styles.recoverCard}>
          <h2 className={styles.recoverTitle}>Nueva Contraseña</h2>
          <p className={styles.recoverDesc}>
            Ingresá y confirmá tu nueva contraseña para acceder nuevamente a tu cuenta.
          </p>

          <formData.AppForm>
            <formData.FormContainer
              extraError={submitError ? new Error(submitError) : null}
            >
              {mutation.error && (
                <div className={styles.errorAlert}>
                  <AlertCircle className={styles.errorIcon} />
                  <span>{mutation.error.message}</span>
                </div>
              )}

              <formData.AppField name="password">
                {(field) => (
                  <div className={styles.fieldContainer}>
                    <label htmlFor={field.name} className={styles.label}>
                      Nueva contraseña 🔒
                    </label>
                    <field.PasswordField label="" />
                  </div>
                )}
              </formData.AppField>

              <formData.AppField name="confirmPassword">
                {(field) => (
                  <div className={styles.fieldContainer}>
                    <label htmlFor={field.name} className={styles.label}>
                      Confirmar contraseña 🔐
                    </label>
                    <field.PasswordField label="" />
                  </div>
                )}
              </formData.AppField>

              <button
                type="submit"
                className={styles.submitButton}
                disabled={mutation.isPending}
              >
                {mutation.isPending ? "Guardando..." : "Guardar nueva contraseña"}
              </button>

              <div className={styles.linkRow}>
                <span>¿Recordaste tu contraseña? </span>
                <a href="/login" className={styles.link}>Inicia sesión</a>
              </div>
            </formData.FormContainer>
          </formData.AppForm>
        </div>
      </div>
    </div>
  );
}
