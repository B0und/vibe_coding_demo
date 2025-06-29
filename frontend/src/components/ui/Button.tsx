import React from "react";
import { classNames } from "../../utils/helpers";

export interface ButtonProps
  extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: "primary" | "secondary" | "tertiary";
  size?: "sm" | "md" | "lg";
  isLoading?: boolean;
  leftIcon?: React.ReactNode;
  rightIcon?: React.ReactNode;
  fullWidth?: boolean;
  children: React.ReactNode;
}

const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  (
    {
      variant = "primary",
      size = "md",
      isLoading = false,
      leftIcon,
      rightIcon,
      fullWidth = false,
      children,
      className,
      disabled,
      ...props
    },
    ref
  ) => {
    const baseClasses =
      "inline-flex items-center justify-center font-medium transition-colors focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed";

    const variantClasses = {
      primary:
        "bg-primary-600 text-white hover:bg-primary-700 focus:ring-primary-500 active:bg-primary-800",
      secondary:
        "bg-secondary-100 text-secondary-900 hover:bg-secondary-200 focus:ring-secondary-500 active:bg-secondary-300 border border-secondary-300",
      tertiary:
        "bg-transparent text-primary-600 hover:bg-primary-50 focus:ring-primary-500 active:bg-primary-100",
    };

    const sizeClasses = {
      sm: "px-3 py-1.5 text-sm rounded-md gap-1.5",
      md: "px-4 py-2 text-sm rounded-md gap-2",
      lg: "px-6 py-3 text-base rounded-lg gap-2",
    };

    const iconSizeClasses = {
      sm: "w-4 h-4",
      md: "w-4 h-4",
      lg: "w-5 h-5",
    };

    const isDisabled = disabled || isLoading;

    return (
      <button
        ref={ref}
        className={classNames(
          baseClasses,
          variantClasses[variant],
          sizeClasses[size],
          {
            "w-full": fullWidth,
          },
          className
        )}
        disabled={isDisabled}
        {...props}
      >
        {isLoading && (
          <svg
            className={classNames("animate-spin", iconSizeClasses[size])}
            fill="none"
            viewBox="0 0 24 24"
          >
            <circle
              className="opacity-25"
              cx="12"
              cy="12"
              r="10"
              stroke="currentColor"
              strokeWidth="4"
            />
            <path
              className="opacity-75"
              fill="currentColor"
              d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
            />
          </svg>
        )}
        {!isLoading && leftIcon && (
          <span className={iconSizeClasses[size]}>{leftIcon}</span>
        )}
        {children}
        {!isLoading && rightIcon && (
          <span className={iconSizeClasses[size]}>{rightIcon}</span>
        )}
      </button>
    );
  }
);

Button.displayName = "Button";

export default Button;
