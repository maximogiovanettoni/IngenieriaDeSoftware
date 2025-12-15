import { AlertCircle, CheckCircle2, Upload } from "lucide-react";
import { useEffect, useState } from "react";
import { useLocation } from "wouter";

import { DatePicker } from "@/components/form-components/InputFields/DatePicker";
import { BASE_API_URL } from "@/config/app-query-client";
import { useToken } from "@/services/TokenContext";

import styles from "../components/Profile/Profile.module.css";

type UserProfile = {
  firstName: string;
  lastName: string;
  age: number;
  gender: string;
  address: string;
  birthDate?: string;
  email?: string;
  profileImage?: string | null;
};

export function EditProfileScreen() {
  const [tokenState] = useToken();
  const [, setLocation] = useLocation();

  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);

  const getBackRoute = () => {
    if (tokenState.state === 'LOGGED_IN' && tokenState.tokens.role === 'staff') {
      return '/staff';
    }
    return '/profile';
  };

  useEffect(() => {
    const fetchProfile = async () => {
      if (tokenState.state !== "LOGGED_IN") {
        setLocation("/login");
        return;
      }

      try {
        const response = await fetch(`${BASE_API_URL}/profile/me`, {
          headers: {
            Authorization: `Bearer ${tokenState.tokens.accessToken}`,
          },
        });

        if (!response.ok) throw new Error("No se pudieron cargar los datos.");

        const data = await response.json();
        setProfile(data);
      } catch (err) {
        setError(err instanceof Error ? err.message : "Error desconocido");
      } finally {
        setLoading(false);
      }
    };

    fetchProfile();
  }, [tokenState, setLocation]);

  const handleChange = (field: keyof UserProfile, value: string | number) => {
    if (!profile) return;
    setProfile({ ...profile, [field]: value });
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const maxSizeMB = 5;
    const maxSizeBytes = maxSizeMB * 1024 * 1024;
    if (file.size > maxSizeBytes) {
      setError(`La imagen no puede superar los ${maxSizeMB} MB.`);
      setSelectedFile(null);
      setPreviewUrl(null);
      e.target.value = "";
      return;
    }

    setSelectedFile(file);
    setPreviewUrl(URL.createObjectURL(file));
    setError(null);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setMessage(null);

    if (tokenState.state !== "LOGGED_IN" || !profile) {
      setError("No autorizado");
      return;
    }

    try {
      const updateData = {
        firstName: profile.firstName,
        lastName: profile.lastName,
        email: profile.email || "", // El email se envía pero no se actualiza en el servicio
        birthDate: profile.birthDate,
        gender: profile.gender,
        address: profile.address,
      };

      const response = await fetch(`${BASE_API_URL}/profile/me`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${tokenState.tokens.accessToken}`,
        },
        body: JSON.stringify(updateData),
      });

      const data = await response.json();
      if (!response.ok) {
        throw new Error(data.message || "Error al actualizar el perfil");
      }

      if (selectedFile) {
        const formData = new FormData();
        formData.append("file", selectedFile);

        const uploadResponse = await fetch(`${BASE_API_URL}/profile/me/photo`, {
          method: "POST",
          headers: {
            Authorization: `Bearer ${tokenState.tokens.accessToken}`,
          },
          body: formData,
        });

        if (!uploadResponse.ok) {
          const uploadData = await uploadResponse.json();
          throw new Error(uploadData.message || "Error al subir la imagen");
        }

        const uploadData = await uploadResponse.json();
        if (uploadData.profileImage) {
          setProfile({ ...profile, profileImage: uploadData.profileImage });
        }
      }

      setMessage("Perfil actualizado correctamente 🎉");
      setTimeout(() => setLocation(getBackRoute()), 2000);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Error desconocido");
    }
  };

  if (loading) {
    return (
      <div className={styles.profileBg}>
        <div className={styles.card}>
          <p>Cargando datos...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className={styles.profileBg}>
        <div className={styles.card}>
          <div className={styles.errorAlert}>
            <AlertCircle className={styles.errorIcon} />
            <span>{error}</span>
          </div>
          <button className={styles.mainButton} onClick={() => setLocation(getBackRoute())}>
            Volver al perfil
          </button>
        </div>
      </div>
    );
  }

  if (!profile) return null;

  return (
    <div className={styles.profileBg}>
      <div className={styles.profileCard}>
        <h2 className={styles.userName}>Editar perfil</h2>

        {message && (
          <div className={styles.successAlert}>
            <CheckCircle2 className={styles.successIcon} />
            <span>{message}</span>
          </div>
        )}

        {/* 📸 Imagen de perfil */}
        <div className={styles.avatarContainer}>
          <img
            src={previewUrl || profile.profileImage || "/assets/images/profile_pic_placeholder.png"}
            alt="Foto de perfil"
            className={styles.avatar}
          />
          <label htmlFor="file-upload" className={styles.uploadButton}>
            <Upload className={styles.uploadIcon} /> Cambiar foto
          </label>
          <input
            id="file-upload"
            type="file"
            accept="image/*"
            style={{ display: "none" }}
            onChange={handleFileChange}
          />
        </div>

        <form onSubmit={handleSubmit} className={styles.editForm}>
          <div className={styles.fieldContainer}>
            <label className={styles.label}>Nombre</label>
            <input
              type="text"
              className={styles.textInput}
              value={profile.firstName}
              onChange={(e) => handleChange("firstName", e.target.value)}
              required
            />
          </div>

          <div className={styles.fieldContainer}>
            <label className={styles.label}>Apellido</label>
            <input
              type="text"
              className={styles.textInput}
              value={profile.lastName}
              onChange={(e) => handleChange("lastName", e.target.value)}
              required
            />
          </div>

          <div className={styles.fieldContainer}>
            <label className={styles.label}>Fecha de nacimiento</label>
            <DatePicker
              name="birthDate"
              value={profile.birthDate || ""}
              onChange={(e) => handleChange("birthDate", e.target.value)}
              placeholder="DD/MM/YYYY"
              className={styles.textInput}
            />
          </div>

          <div className={styles.fieldContainer}>
            <label className={styles.label}>Género</label>
            <select
              className={styles.textInput}
              value={profile.gender}
              onChange={(e) => handleChange("gender", e.target.value)}
            >
              <option value="male">Masculino</option>
              <option value="female">Femenino</option>
              <option value="other">Otro</option>
            </select>
          </div>

          <div className={styles.fieldContainer}>
            <label className={styles.label}>Domicilio</label>
            <input
              type="text"
              className={styles.textInput}
              value={profile.address}
              onChange={(e) => handleChange("address", e.target.value)}
              required
            />
          </div>

          <div className={styles.buttonRow}>
            <button type="button" className={styles.mainButton} onClick={() => setLocation(getBackRoute())}>
              Cancelar
            </button>
            <button type="submit" className={styles.mainButton}>
              Guardar cambios
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
