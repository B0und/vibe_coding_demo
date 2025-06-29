import React from "react";
import { classNames } from "../../utils/helpers";

export interface CheckboxProps
  extends Omit<React.InputHTMLAttributes<HTMLInputElement>, "type"> {
  label?: string;
  description?: string;
  error?: string;
  indeterminate?: boolean;
}

const Checkbox = React.forwardRef<HTMLInputElement, CheckboxProps>(
  (
    {
      label,
      description,
      error,
      indeterminate = false,
      className,
      id,
      ...props
    },
    ref
  ) => {
    const checkboxId =
      id || `checkbox-${Math.random().toString(36).substr(2, 9)}`;
    const hasError = !!error;

    const checkboxRef = React.useRef<HTMLInputElement>(null);

    React.useImperativeHandle(ref, () => checkboxRef.current!);

    React.useEffect(() => {
      if (checkboxRef.current) {
        checkboxRef.current.indeterminate = indeterminate;
      }
    }, [indeterminate]);

    const baseCheckboxClasses =
      "h-4 w-4 rounded border-2 text-primary-600 focus:ring-2 focus:ring-primary-500 focus:ring-offset-0 transition-colors";

    const checkboxStateClasses = hasError
      ? "border-error-300 text-error-600 focus:ring-error-500"
      : "border-secondary-300 focus:ring-primary-500";

    const checkboxClasses = classNames(
      baseCheckboxClasses,
      checkboxStateClasses,
      className
    );

    return (
      <div className="space-y-1">
        <div className="flex items-start">
          <div className="flex items-center h-5">
            <input
              ref={checkboxRef}
              id={checkboxId}
              type="checkbox"
              className={checkboxClasses}
              {...props}
            />
          </div>

          {(label || description) && (
            <div className="ml-3 text-sm">
              {label && (
                <label
                  htmlFor={checkboxId}
                  className={classNames(
                    "font-medium cursor-pointer",
                    hasError ? "text-error-900" : "text-secondary-900"
                  )}
                >
                  {label}
                </label>
              )}
              {description && (
                <p
                  className={classNames(
                    "text-sm",
                    hasError ? "text-error-600" : "text-secondary-500"
                  )}
                >
                  {description}
                </p>
              )}
            </div>
          )}
        </div>

        {error && <p className="text-sm text-error-600 ml-7">{error}</p>}
      </div>
    );
  }
);

Checkbox.displayName = "Checkbox";

export default Checkbox;
