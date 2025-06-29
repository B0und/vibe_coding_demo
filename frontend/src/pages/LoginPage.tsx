import { useState, useEffect } from "react";
import { useNavigate } from "react-router";
import { useAuth } from "../hooks/useAuth";
import { Button, Input } from "../components/ui";

export default function LoginPage() {
  const [username, setUsername] = useState("");
  const { login, isLoginLoading, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  // Redirect if already authenticated
  useEffect(() => {
    if (isAuthenticated) {
      navigate("/subscriptions");
    }
  }, [isAuthenticated, navigate]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!username.trim()) {
      return;
    }

    try {
      await login(username.trim());
      // Navigation will happen automatically via useEffect when isAuthenticated changes
    } catch (error) {
      // Error is automatically handled by the global error context
      console.error("Login failed:", error);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-secondary-50">
      <div className="max-w-md w-full space-y-8">
        <div>
          <h2 className="mt-6 text-center text-heading-2 text-secondary-900">
            Sign in to your account
          </h2>
          <p className="mt-2 text-center text-body-small text-secondary-600">
            Enter your username to sign in or create an account
          </p>
        </div>
        <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
          <div className="space-y-4">
            <Input
              label="Username"
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="Enter your username"
              required
              fullWidth
              disabled={isLoginLoading}
            />
          </div>

          <Button
            type="submit"
            variant="primary"
            size="lg"
            fullWidth
            disabled={isLoginLoading || !username.trim()}
          >
            {isLoginLoading ? "Signing in..." : "Sign in"}
          </Button>
        </form>
      </div>
    </div>
  );
}
