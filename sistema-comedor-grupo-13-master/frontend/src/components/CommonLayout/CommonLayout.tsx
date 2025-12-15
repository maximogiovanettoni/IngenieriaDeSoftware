import React from "react";
import { Link } from "wouter";

import { ErrorBoundary } from "@/components/ErrorBoundary/ErrorBoundary";
import { useCart } from "@/hooks/useCart";
import { useToken } from "@/services/TokenContext";
import { useLogout } from "@/services/UserServices";
import { CartBadge } from "./CartBadge";
import styles from "./CommonLayout.module.css";

export const CommonLayout = ({ children }: React.PropsWithChildren) => {
  const [tokenState] = useToken();

  return (
    <div className={styles.mainLayout}>
      <ul className={styles.topBar}>{tokenState.state === "LOGGED_OUT" ? <LoggedOutLinks /> : <LoggedInLinks />}</ul>
      <div className={styles.body}>
        <ErrorBoundary>{children}</ErrorBoundary>
      </div>
    </div>
  );
};

const LoggedOutLinks = () => {
  return (
    <>
      <li>
        <Link href="/login">Log in</Link>
      </li>
      <li>
        <Link href="/signup">Sign Up</Link>
      </li>
    </>
  );
};

const LoggedInLinks = () => {
  const logout = useLogout();
  const { getItemCount } = useCart();
  const itemCount = getItemCount();

  const handleLogout = () => {
    logout.mutate();
  };

  return (
    <>
      <li>
        <Link href="/dashboard">Panel</Link>
      </li>
      <li>
        <Link href="/orders">Mis Pedidos</Link>
      </li>
      <li>
        <Link href="/profile">Perfil</Link>
      </li>
      <li>
        <CartBadge itemCount={itemCount} />
      </li>
      <li>
        <button onClick={handleLogout} disabled={logout.isPending}>
          {logout.isPending ? "Cerrando sesión..." : "Cerrar Sesión"}
        </button>
      </li>
    </>
  );
};
