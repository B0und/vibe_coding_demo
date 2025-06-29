import { useState } from "react";
import { useNavigate } from "react-router";
import { useAuth } from "../store/authStore";
import { Button, Input, Checkbox } from "../components/ui";

export default function LoginPage() {
  const [isAdmin, setIsAdmin] = useState(false);
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    // Mock login - in real app, this would make an API call
    login(isAdmin ? "admin" : "user");
    navigate("/subscriptions");
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-secondary-50">
      <div className="max-w-md w-full space-y-8">
        <div>
          <h2 className="mt-6 text-center text-heading-2 text-secondary-900">
            Sign in to your account
          </h2>
          <p className="mt-2 text-center text-body-small text-secondary-600">
            Demo app - use any email/password
          </p>
        </div>
        <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
          <div className="space-y-4">
            <Input
              label="Email address"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="Email address"
              required
              fullWidth
            />
            <Input
              label="Password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Password"
              required
              fullWidth
            />
          </div>

          <Checkbox
            checked={isAdmin}
            onChange={(e) => setIsAdmin(e.target.checked)}
            label="Login as Admin"
            description="Check this to access admin features"
          />

          <Button type="submit" variant="primary" size="lg" fullWidth>
            Sign in
          </Button>
        </form>
      </div>
    </div>
  );
}
