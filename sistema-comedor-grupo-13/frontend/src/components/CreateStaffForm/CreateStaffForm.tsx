import { useState } from "react";

import { useCreateStaff } from "@/services/AdminServices";
import { ApiError, StaffFormData } from "@/types/staff.types";

import styles from "./CreateStaffForm.module.css";

export const CreateStaffForm = () => {
  const [form, setForm] = useState<StaffFormData>({
    firstName: "",
    lastName: "",
    email: "",
    temporaryPassword: "",
    birthDate: "",
    gender: "male",
    address: "",
  });

  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const { mutate, isPending } = useCreateStaff();

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    mutate(form, {
      onSuccess: (response) => {
        setMessage(`Staff creado: ${response.email}`);
        setError(null);
        setForm({ 
          firstName: "", 
          lastName: "", 
          email: "", 
          temporaryPassword: "",
          birthDate: "",
          gender: "male",
          address: "",
        });
      },
      onError: (err: ApiError) => {
        const msg = err.response?.data?.error || err.response?.data?.message || err.message || "Error al crear staff";
        setError(msg);
        setMessage(null);
      },
    });
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setForm((prev: StaffFormData) => ({
      ...prev,
      [name]: value,
    }));
  };

  return (
    <div className={styles.container}>
      <h2>Crear Usuario Staff</h2>
      <form onSubmit={handleSubmit} className={styles.form}>
        <div>
          <label htmlFor="firstName">Nombre:</label>
          <input
            type="text"
            id="firstName"
            name="firstName"
            value={form.firstName}
            onChange={handleInputChange}
            required
          />
        </div>

        <div>
          <label htmlFor="lastName">Apellido:</label>
          <input
            type="text"
            id="lastName"
            name="lastName"
            value={form.lastName}
            onChange={handleInputChange}
            required
          />
        </div>

        <div>
          <label htmlFor="email">Email:</label>
          <input type="email" id="email" name="email" value={form.email} onChange={handleInputChange} required />
        </div>

        <div>
          <label htmlFor="birthDate">Fecha de Nacimiento:</label>
          <input
            type="date"
            id="birthDate"
            name="birthDate"
            value={form.birthDate}
            onChange={handleInputChange}
            required
          />
        </div>

        <div>
          <label htmlFor="gender">Género:</label>
          <select
            id="gender"
            name="gender"
            value={form.gender}
            onChange={handleInputChange}
            required
          >
            <option value="male">Masculino</option>
            <option value="female">Femenino</option>
            <option value="other">Otro</option>
          </select>
        </div>

        <div>
          <label htmlFor="address">Dirección:</label>
          <input
            type="text"
            id="address"
            name="address"
            value={form.address}
            onChange={handleInputChange}
            required
          />
        </div>

        <div>
          <label htmlFor="temporaryPassword">Contraseña Temporal:</label>
          <input
            type="password"
            id="temporaryPassword"
            name="temporaryPassword"
            value={form.temporaryPassword}
            onChange={handleInputChange}
            required
          />
        </div>

        <button type="submit" disabled={isPending}>
          {isPending ? "Creando..." : "Crear Staff"}
        </button>
      </form>

      {message && <p className={styles.success}>{message}</p>}
      {error && <p className={styles.error}>{error}</p>}
    </div>
  );
};
