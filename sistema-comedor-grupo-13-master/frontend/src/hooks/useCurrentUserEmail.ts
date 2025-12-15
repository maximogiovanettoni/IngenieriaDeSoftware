import { useEffect, useState } from "react";
import { useToken } from "@/services/TokenContext";

export function useCurrentUserEmail() {
  const [tokenState] = useToken();
  const [email, setEmail] = useState<string | null>(null);

  useEffect(() => {
    if (tokenState.state !== "LOGGED_IN") {
      setEmail(null);
      return;
    }

    try {
      const token = tokenState.tokens.accessToken;
      const payload = JSON.parse(window.atob(token.split(".")[1]));

      setEmail(typeof payload.sub === "string" ? payload.sub : null);
    } catch {
      console.error("Failed to decode JWT payload");
      setEmail(null);
    }
  }, [tokenState]);

  return email;
}
