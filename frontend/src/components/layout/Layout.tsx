import { Link, useLocation, Outlet } from "react-router";
import { useAuth } from "../../store/authStore";
import { ThemeToggle } from "../ui";

export default function Layout() {
  const { user, logout } = useAuth();
  const location = useLocation();

  const navigation = [
    { name: "Subscriptions", href: "/subscriptions" },
    { name: "Profile", href: "/profile" },
    ...(user?.role === "ADMIN" ? [{ name: "Admin", href: "/admin" }] : []),
  ];

  return (
    <div className="min-h-screen bg-secondary-50">
      <nav className="bg-white shadow-sm border-b border-secondary-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between h-16">
            <div className="flex items-center">
              <Link
                to="/subscriptions"
                className="text-heading-4 text-primary-600 font-bold"
              >
                Vibe Events
              </Link>
            </div>

            <div className="flex items-center space-x-4">
              {/* Navigation Links */}
              <div className="hidden md:flex items-center space-x-4">
                {navigation.map((item) => (
                  <Link
                    key={item.name}
                    to={item.href}
                    className={`px-3 py-2 rounded-md text-sm font-medium transition-colors ${
                      location.pathname === item.href
                        ? "bg-primary-50 text-primary-700"
                        : "text-secondary-600 hover:text-secondary-900 hover:bg-secondary-50"
                    }`}
                  >
                    {item.name}
                  </Link>
                ))}
              </div>

              {/* Theme Toggle */}
              <ThemeToggle size="sm" />

              {/* User Menu */}
              <div className="flex items-center space-x-3">
                <span className="text-sm text-secondary-600">
                  {user?.username || "User"}
                </span>
                <button
                  onClick={logout}
                  className="text-sm text-secondary-600 hover:text-secondary-900 transition-colors"
                >
                  Logout
                </button>
              </div>
            </div>
          </div>
        </div>
      </nav>

      <main className="flex-1">
        <Outlet />
      </main>
    </div>
  );
}
