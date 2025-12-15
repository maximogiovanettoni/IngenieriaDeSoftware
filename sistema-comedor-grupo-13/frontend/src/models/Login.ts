import { z } from "zod";

import { useValidationConfig } from "@/services/UserServices";

export const SignupRequestSchema = z.object({
  firstName: z.string(),
  lastName: z.string(),
  email: z.string(),
  birthDate: z.string(),
  gender: z.string(),
  address: z.string(),
  password: z.string(),
});

export function useSignupSchema() {
  const { config } = useValidationConfig();

  const fallback = {
    email: {
      allowedEmailDomain: "@example.com",
      domainPattern: "^[a-zA-Z0-9._%+-]+@example\\.com$",
      messages: {
        required: "El email es requerido",
        invalidFormat: "Formato de email inválido",
        invalidDomain: "Debe usar un correo institucional",
      },
    },
    user: {
      birthDate: {
        minAge: 18,
        maxAge: 99,
        messages: {
          required: "Fecha de nacimiento requerida",
          invalidFormat: "La fecha de nacimiento debe tener formato válido (dd-mm-aaaa)",
          invalidRange: "La edad debe estar entre 18 y 99 años",
        },
      },
      firstName: {
        minLength: 2,
        maxLength: 50,
        pattern: "^[A-Za-z ]+$",
        messages: { required: "Nombre requerido", invalidFormat: "Solo letras", invalidLength: "Longitud inválida" },
      },
      lastName: {
        minLength: 2,
        maxLength: 50,
        pattern: "^[A-Za-z ]+$",
        messages: { required: "Apellido requerido", invalidFormat: "Solo letras", invalidLength: "Longitud inválida" },
      },
      password: {
        minLength: 8,
        maxLength: 64,
        pattern: "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).+$",
        messages: {
          required: "Contraseña requerida",
          invalidFormat: "Debe tener mayúscula, minúscula y número",
          invalidLength: "Longitud inválida",
        },
      },
      address: {
        maxLength: 100,
        pattern: "^[A-Za-z0-9\\s,.-]+$",
        messages: {
          required: "Dirección requerida",
          invalidFormat: "Formato inválido",
          invalidLength: "Demasiado larga",
        },
      },
      gender: {
        pattern: "^(male|female|other)$",
        messages: { required: "El género es requerido", invalidFormat: "Debe ser masculino, femenino u otro" },
      },
    },
  };

  const cfg = config ?? fallback;

  return SignupRequestSchema.extend({
    firstName: SignupRequestSchema.shape.firstName
      .min(cfg.user.firstName.minLength, cfg.user.firstName.messages.required)
      .max(cfg.user.firstName.maxLength, cfg.user.firstName.messages.invalidLength)
      .regex(new RegExp(cfg.user.firstName.pattern), cfg.user.firstName.messages.invalidFormat),

    lastName: SignupRequestSchema.shape.lastName
      .min(cfg.user.lastName.minLength, cfg.user.lastName.messages.required)
      .max(cfg.user.lastName.maxLength, cfg.user.lastName.messages.invalidLength)
      .regex(new RegExp(cfg.user.lastName.pattern), cfg.user.lastName.messages.invalidFormat),

    email: SignupRequestSchema.shape.email
      .email(cfg.email.messages.invalidFormat)
      .regex(new RegExp(cfg.email.domainPattern), cfg.email.messages.invalidDomain),

    birthDate: z
      .string()
      .min(1, cfg.user.birthDate.messages.required)
      .refine((dateStr) => {
        // Validar formato YYYY-MM-DD
        const dateRegex = /^\d{4}-\d{2}-\d{2}$/;
        if (!dateRegex.test(dateStr)) {
          return false;
        }

        const date = new Date(dateStr);

        // Validar que sea una fecha válida
        if (isNaN(date.getTime())) {
          return false;
        }

        const today = new Date();
        today.setHours(0, 0, 0, 0);

        // La fecha no puede ser futura
        if (date > today) {
          return false;
        }

        // Calcular edad exacta
        const age = today.getFullYear() - date.getFullYear();
        const monthDiff = today.getMonth() - date.getMonth();
        const dayDiff = today.getDate() - date.getDate();
        const exactAge = monthDiff < 0 || (monthDiff === 0 && dayDiff < 0) ? age - 1 : age;

        return exactAge >= cfg.user.birthDate.minAge && exactAge <= cfg.user.birthDate.maxAge;
      }, cfg.user.birthDate.messages.invalidRange),

    address: z
      .string()
      .min(1, cfg.user.address.messages.required)
      .max(cfg.user.address.maxLength, cfg.user.address.messages.invalidLength)
      .regex(new RegExp(cfg.user.address.pattern), cfg.user.address.messages.invalidFormat),

    gender: z
      .string()
      .min(1, cfg.user.gender.messages.required)
      .refine((val) => ["male", "female", "other"].includes(val), cfg.user.gender.messages.invalidFormat),

    password: z
      .string()
      .min(cfg.user.password.minLength, cfg.user.password.messages.invalidLength)
      .max(cfg.user.password.maxLength, cfg.user.password.messages.invalidLength)
      .regex(new RegExp(cfg.user.password.pattern), cfg.user.password.messages.invalidFormat),
  });
}

export type SignupRequest = z.infer<typeof SignupRequestSchema>;

export const LoginRequestSchema = z.object({
  username: z.string().min(1, "El usuario no puede quedar vacío."),
  password: z.string().min(1, "La contraseña no puede quedar vacía."),
});

export type LoginRequest = z.infer<typeof LoginRequestSchema>;

export const AuthResponseSchema = z.object({
  accessToken: z.string().min(1),
  refreshToken: z.string().min(1),
  role: z.string().optional(),
});

export type AuthResponse = z.infer<typeof AuthResponseSchema>;
