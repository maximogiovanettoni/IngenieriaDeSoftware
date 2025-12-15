import { AlertCircle } from "lucide-react";
import { useEffect, useState } from "react";
import { useLocation } from "wouter";

import { BASE_API_URL } from "@/config/app-query-client";
import { useToken } from "@/services/TokenContext";
import { formatDateForDisplay } from "@/utils/dateUtils";

import styles from "../components/Profile/Profile.module.css";

type UserProfile = {
  firstName: string;
  lastName: string;
  email: string;
  emailVerified: boolean;
  birthDate: string;
  gender: string;
  address: string;
  profileImage?: string | null;
};

const genderTranslations: Record<string, string> = {
  male: "Masculino",
  female: "Femenino",
  other: "Otro",
};

function translateGender(gender: string): string {
  return genderTranslations[gender] || gender;
}

export function ProfileScreen() {
  const [tokenState] = useToken();
  const [, setLocation] = useLocation();
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

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

        if (!response.ok) {
          throw new Error(`Error ${response.status}: ${response.statusText}`);
        }

        const data = await response.json();
        setProfile(data);
      } catch (err) {
        if (err instanceof Error) {
          setError(err.message);
        } else {
          setError("No se pudo cargar el perfil");
        }
      } finally {
        setLoading(false);
      }
    };

    fetchProfile();
  }, [tokenState, setLocation]);

  if (loading) {
    return (
      <div className={styles.profileBg}>
        <div className={styles.card}>
          <p>Cargando perfil...</p>
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
          <button className={styles.mainButton} onClick={() => setLocation("/login")}>
            Ir al inicio de sesión
          </button>
        </div>
      </div>
    );
  }

  if (!profile) return null;

  return (
    <div className={styles.profileBg}>
      <div className={styles.profileCard}>
        <div className={styles.avatarContainer}>
          <img
            src={profile.profileImage || "https://cdn-icons-png.flaticon.com/512/149/149071.png"}
            alt="Foto de perfil"
            className={styles.avatar}
          />
          <h2 className={styles.userName}>
            {profile.firstName} {profile.lastName}
          </h2>
          <p className={styles.email}>
            {profile.email}{" "}
            <span className={profile.emailVerified ? styles.verified : styles.notVerified}>
              {profile.emailVerified ? "✔ Verificado" : "✖ No verificado"}
            </span>
          </p>
        </div>

        <div className={styles.infoSection}>
          <h3>Información personal</h3>
          <div className={styles.infoRow}>
            <span className={styles.label}>Fecha de Nacimiento:</span>
            <span className={styles.value}>{formatDateForDisplay(profile.birthDate)}</span>
          </div>
          <div className={styles.infoRow}>
            <span className={styles.label}>Género:</span>
            <span className={styles.value}>{translateGender(profile.gender)}</span>
          </div>
          <div className={styles.infoRow}>
            <span className={styles.label}>Domicilio:</span>
            <span className={styles.value}>{profile.address}</span>
          </div>
        </div>

        <div className={styles.buttonRow}>
          <button className={styles.mainButton} onClick={() => setLocation("/profile/edit")}>
            Editar perfil
          </button>

          <button
            className={styles.mainButton}
            onClick={() => {
              setLocation("/");
            }}
          >
            Volver
          </button>
        </div>
      </div>
    </div>
  );
}
