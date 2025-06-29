import React from "react";
import { classNames } from "../../utils/helpers";

export interface InputProps
  extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  helperText?: string;
  error?: string;
  leftIcon?: React.ReactNode;
  rightIcon?: React.ReactNode;
  fullWidth?: boolean;
}

const Input = React.forwardRef<HTMLInputElement, InputProps>(
  (
    {
      label,
      helperText,
      error,
      leftIcon,
      rightIcon,
      fullWidth = false,
      className,
      id,
      ...props
    },
    ref
  ) => {
    const inputId = id || `input-${Math.random().toString(36).substr(2, 9)}`;
    const hasError = !!error;

    const baseInputClasses =
      "block px-3 py-2 border rounded-md shadow-sm placeholder-secondary-400 focus:outline-none focus:ring-2 focus:ring-offset-0 transition-colors sm:text-sm";

    const inputStateClasses = hasError
      ? "border-error-300 text-error-900 focus:ring-error-500 focus:border-error-500"
      : "border-secondary-300 text-secondary-900 focus:ring-primary-500 focus:border-primary-500";

    const inputClasses = classNames(
      baseInputClasses,
      inputStateClasses,
      {
        "pl-10": !!leftIcon,
        "pr-10": !!rightIcon,
        "w-full": fullWidth,
      },
      className
    );

    const iconClasses =
      "absolute inset-y-0 flex items-center pointer-events-none";
    const iconSizeClasses = "w-5 h-5 text-secondary-400";

    return (
      <div className={classNames("space-y-1", { "w-full": fullWidth })}>
        {label && (
          <label
            htmlFor={inputId}
            className="block text-sm font-medium text-secondary-700"
          >
            {label}
          </label>
        )}

        <div className="relative">
          {leftIcon && (
            <div className={classNames(iconClasses, "left-0 pl-3")}>
              <span className={iconSizeClasses}>{leftIcon}</span>
            </div>
          )}

          <input ref={ref} id={inputId} className={inputClasses} {...props} />

          {rightIcon && (
            <div className={classNames(iconClasses, "right-0 pr-3")}>
              <span className={iconSizeClasses}>{rightIcon}</span>
            </div>
          )}
        </div>

        {(helperText || error) && (
          <p
            className={classNames(
              "text-sm",
              hasError ? "text-error-600" : "text-secondary-500"
            )}
          >
            {error || helperText}
          </p>
        )}
      </div>
    );
  }
);

Input.displayName = "Input";

export default Input;
