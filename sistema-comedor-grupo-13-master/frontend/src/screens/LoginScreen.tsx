import { Login } from "@/components/Login/Login";
import { useLogin } from "@/services/UserServices";
import { useLocation } from "wouter";

export const LoginScreen = () => {
  const { mutate, error } = useLogin();
  const [, setLocation] = useLocation();

  const handleSubmit = (value: Parameters<typeof mutate>[0]) => {
    // call mutate with onSuccess to navigate immediately based on role
    mutate(value, {
      onSuccess: (tokens: unknown) => {
        try {
          const role = (tokens as { role?: string })?.role ?? null;
          if (role === "admin") {
            setLocation("/admin");
          } else {
           
            setLocation("/");
          }
        } catch {
          setLocation("/menu");
        }
      },
    });
  };

  return <Login onSubmit={handleSubmit} submitError={error} />;
};
