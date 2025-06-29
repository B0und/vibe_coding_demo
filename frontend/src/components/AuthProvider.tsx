import { useEffect } from "react";
import { useAuth } from "../store/authStore";

interface AuthProviderProps {
  children: React.ReactNode;
}

export default function AuthProvider({ children }: AuthProviderProps) {
  const { fetchUser } = useAuth();

  useEffect(() => {
    // Check for existing token and fetch user data on app load
    const token = localStorage.getItem("authToken");
    if (token) {
      fetchUser();
    }
  }, [fetchUser]);

  return <>{children}</>;
}
