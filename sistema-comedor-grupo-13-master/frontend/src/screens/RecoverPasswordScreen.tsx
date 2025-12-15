import { useAppForm } from "@/config/use-app-form";
import styles from "../components/RecoverPassword/RecoverPassword.module.css";
import { AlertCircle} from "lucide-react";
import { useLocation } from "wouter";
import { useRecoverPassword } from "@/services/UserServices";
import { useValidationConfig } from "@/services/UserServices";
import { useState } from "react";

export function RecoverPasswordScreen() {
  const mutation = useRecoverPassword();
  const [ ,setLocation] = useLocation();
  const { config } = useValidationConfig();
  const [ changeError, setChangeError] = useState<string | null>(null);


  const formData = useAppForm({
    defaultValues: {
      email: "",
    },
    validators: {
    onChange: ({ value }) => {
        const email = value.email.trim();
        const allowedDomain = config?.email.allowedEmailDomain;

        if(allowedDomain){
          if (!email.endsWith(allowedDomain)) {
            setChangeError(config?.email.messages.invalidDomain || "Debe usar un correo institucional");
          }
          else{
            setChangeError(null);
          }
        }
      },
    },
    onSubmit: async ({ value }) => {

      if(changeError){
        return;
      }

      const res = await mutation.mutateAsync({ email: value.email });
      if (res?.success) {
        setLocation("/reset-request-sent");
      }
  
    },
  });

  return (
    <div className={styles.recoverBg}>
      <div className={styles.logoCircle}>
        <span role="img" aria-label="logo" className={styles.logoIcon}>🎓</span>
      </div>

      <h1 className={styles.title}>Comedor Universitario</h1>
      <p className={styles.subtitle}>Recupera el acceso a tu cuenta</p>

      <div className={styles.recoverWrapper}>
        <div className={styles.recoverCard}>
          <h2 className={styles.recoverTitle}>Recuperar Contraseña</h2>
          <p className={styles.recoverDesc}>Ingresa tu correo electrónico registrado</p>

          <formData.AppForm>
            <formData.FormContainer extraError={changeError ? new Error(changeError) : null}>
              {mutation.error && (
                <div className={styles.errorAlert}>
                  <AlertCircle className={styles.errorIcon} />
                  <span>{mutation.error.message}</span>
                </div>
              )}

              <formData.AppField name="email">
                {(field) => (
                  <div className={styles.fieldContainer}>
                    <label htmlFor={field.name} className={styles.label}>
                      Correo electrónico 📧
                    </label>
                      <field.TextField label=""/>
                  </div>
                )}
              </formData.AppField>

              <button
                type="submit"
                className={styles.submitButton}
                disabled={mutation.isPending}
              >
                {mutation.isPending ? "Enviando..." : "Enviar enlace de recuperación"}
              </button>

              <div className={styles.linkRow}>
                <span>¿Recordaste tu contraseña? </span>
                <a href="/login" className={styles.link}>Volver al inicio de sesión</a>
              </div>
            </formData.FormContainer>
          </formData.AppForm>
        </div>
      </div>
    </div>
  );
}
