import { Navigate } from "react-router";
import { useAuth } from "../store/authStore";

interface ProtectedRouteProps {
  children: React.ReactNode;
  requireAdmin?: boolean;
}

export default function ProtectedRoute({
  children,
  requireAdmin = false,
}: ProtectedRouteProps) {
  const { isAuthenticated, isLoading } = useAuth();

  // Show loading state while checking authentication
  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-lg">Loading...</div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  // For now, since we don't have role-based auth in the backend,
  // we'll disable admin-only routes
  if (requireAdmin) {
    return <Navigate to="/subscriptions" replace />;
  }

  return <>{children}</>;
}
