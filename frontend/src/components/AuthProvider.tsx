import { useEffect } from "react";
import { useCurrentUser } from "../hooks/useAuth";

interface AuthProviderProps {
  children: React.ReactNode;
}

export default function AuthProvider({ children }: AuthProviderProps) {
  // The useCurrentUser hook automatically handles fetching user data
  // when a token exists, so we don't need to manually trigger it
  const userQuery = useCurrentUser();

  useEffect(() => {
    // Listen for unauthorized events to handle token expiration
    const handleUnauthorized = () => {
      // The auth hook already handles token cleanup, just refetch to update state
      userQuery.refetch();
    };

    window.addEventListener("auth:unauthorized", handleUnauthorized);

    return () => {
      window.removeEventListener("auth:unauthorized", handleUnauthorized);
    };
  }, [userQuery]);

  return <>{children}</>;
}
