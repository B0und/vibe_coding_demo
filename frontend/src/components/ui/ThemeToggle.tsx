import React from "react";
import { useTheme } from "../../contexts/ThemeContext";
import Button from "./Button";

export interface ThemeToggleProps {
  size?: "sm" | "md" | "lg";
  variant?: "primary" | "secondary" | "tertiary";
  showLabel?: boolean;
  className?: string;
}

const ThemeToggle: React.FC<ThemeToggleProps> = ({
  size = "md",
  variant = "tertiary",
  showLabel = false,
  className,
}) => {
  const { theme, actualTheme, toggleTheme } = useTheme();

  const getIcon = () => {
    if (theme === "system") {
      return (
        <svg
          className="w-5 h-5"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"
          />
        </svg>
      );
    }

    if (actualTheme === "dark") {
      return (
        <svg
          className="w-5 h-5"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z"
          />
        </svg>
      );
    }

    return (
      <svg
        className="w-5 h-5"
        fill="none"
        stroke="currentColor"
        viewBox="0 0 24 24"
      >
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth={2}
          d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z"
        />
      </svg>
    );
  };

  const getLabel = () => {
    if (theme === "system") return "System";
    return actualTheme === "dark" ? "Dark" : "Light";
  };

  const getAriaLabel = () => {
    if (theme === "system") return "Switch to light mode";
    if (actualTheme === "dark") return "Switch to system mode";
    return "Switch to dark mode";
  };

  return (
    <Button
      variant={variant}
      size={size}
      onClick={toggleTheme}
      aria-label={getAriaLabel()}
      leftIcon={getIcon()}
      className={className}
    >
      {showLabel && getLabel()}
    </Button>
  );
};

export default ThemeToggle;
