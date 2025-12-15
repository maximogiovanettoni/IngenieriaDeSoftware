import { useState } from "react";
import { useLocation } from "wouter";

import styles from "@/components/Login/Login.module.css";
import { useAppForm } from "@/config/use-app-form";
import { useSignupSchema } from "@/models/Login";
import { useSignup } from "@/services/UserServices";
import { ApiError } from "@/types/api";

export const SignupScreen = () => {
  const { mutateAsync } = useSignup();
  const schema = useSignupSchema();
  const [, setLocation] = useLocation();
  const [submitError, setSubmitError] = useState<string | null>(null);

  const formData = useAppForm({
    defaultValues: {
      firstName: "",
      lastName: "",
      email: "",
      birthDate: "",
      gender: "",
      address: "",
      password: "",
    },
    validators: {
      onSubmit: schema,
    },
    onSubmit: async ({ value }) => {
      try {
        const res = await mutateAsync(value);
        if (res?.success) {
          setLocation("/registration-success");
        } else {
          setSubmitError("Ocurrió un error al registrarse. Intente nuevamente.");
        }
      } catch (err: unknown) {
        const e = err as ApiError;
        if (e.status === 409) {
          setSubmitError("Ya existe un usuario con ese email.");
        } else {
          setSubmitError("Error inesperado. Intente nuevamente más tarde.");
        }
      }
    },
  });

  return (
    <div className={styles.loginBg}>
      <div className={styles.logoCircle}>
        <span role="img" aria-label="logo" className={styles.logoIcon}>
          🎓
        </span>
      </div>

      <h1 className={styles.title}>Comedor Universitario</h1>
      <p className={styles.subtitle}>Crea tu cuenta para empezar a usar la plataforma</p>

      <div className={styles.loginWrapper}>
        <div className={styles.card}>
          <h2 className={styles.cardTitle}>Crear cuenta</h2>
          <p className={styles.cardDesc}>Completa tus datos personales</p>

          <formData.AppForm>
            <formData.FormContainer extraError={submitError ? new Error(submitError) : null}>
              <div className={styles.inputGroup}>
                <formData.AppField name="firstName">
                  {(field) => (
                    <div className={styles.fieldContainer}>
                      <field.TextField label="Nombre" />
                    </div>
                  )}
                </formData.AppField>

                <formData.AppField name="lastName">
                  {(field) => (
                    <div className={styles.fieldContainer}>
                      <field.TextField label="Apellido" />
                    </div>
                  )}
                </formData.AppField>

                <formData.AppField name="email">
                  {(field) => (
                    <div className={styles.fieldContainer}>
                      <field.TextField label="Email Institucional" />
                    </div>
                  )}
                </formData.AppField>

                <formData.AppField name="birthDate">
                  {(field) => (
                    <div className={styles.fieldContainer}>
                      <field.DateField label="Fecha de Nacimiento" />
                    </div>
                  )}
                </formData.AppField>

                <formData.AppField name="gender">
                  {(field) => (
                    <div className={styles.fieldContainer}>
                      <field.GenderField label="Género" />
                    </div>
                  )}
                </formData.AppField>

                <formData.AppField name="address">
                  {(field) => (
                    <div className={styles.fieldContainer}>
                      <field.TextField label="Domicilio" />
                    </div>
                  )}
                </formData.AppField>

                <formData.AppField name="password">
                  {(field) => (
                    <div className={styles.fieldContainer}>
                      <field.PasswordField label="Contraseña" />
                    </div>
                  )}
                </formData.AppField>
              </div>

              <button type="submit" className={styles.mainButton}>
                Crear cuenta
              </button>

              <div className={styles.registerRow}>
                <span>¿Ya tienes cuenta? </span>
                <a href="/login" className={styles.link}>
                  Inicia sesión
                </a>
              </div>
            </formData.FormContainer>
          </formData.AppForm>
        </div>
      </div>
    </div>
  );
};
