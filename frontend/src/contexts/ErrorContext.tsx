import { createContext, useContext, useState, useCallback } from "react";
import type { ReactNode } from "react";
import { ApiClientError } from "../api/client";

export interface ErrorInfo {
  id: string;
  message: string;
  type: "error" | "warning" | "info";
  status?: number;
  code?: string;
  timestamp: Date;
}

interface ErrorContextType {
  errors: ErrorInfo[];
  addError: (
    error: ApiClientError | Error | string,
    type?: ErrorInfo["type"]
  ) => void;
  removeError: (id: string) => void;
  clearErrors: () => void;
}

const ErrorContext = createContext<ErrorContextType | undefined>(undefined);

interface ErrorProviderProps {
  children: ReactNode;
}

export function ErrorProvider({ children }: ErrorProviderProps) {
  const [errors, setErrors] = useState<ErrorInfo[]>([]);

  const addError = useCallback(
    (
      error: ApiClientError | Error | string,
      type: ErrorInfo["type"] = "error"
    ) => {
      const errorInfo: ErrorInfo = {
        id: `error-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
        type,
        timestamp: new Date(),
        message: "",
        status: undefined,
        code: undefined,
      };

      if (error instanceof ApiClientError) {
        errorInfo.message = error.message;
        errorInfo.status = error.status;
        errorInfo.code = error.code;

        // Handle specific error types
        if (error.code === "UNAUTHORIZED") {
          // Redirect to login or trigger re-authentication
          // This could be handled by the auth store or router
          window.dispatchEvent(new CustomEvent("auth:unauthorized"));
        }
      } else if (error instanceof Error) {
        errorInfo.message = error.message;
      } else {
        errorInfo.message = error;
      }

      setErrors((prev) => [...prev, errorInfo]);

      // Auto-remove error after 5 seconds for non-critical errors
      if (type !== "error" || (errorInfo.status && errorInfo.status < 500)) {
        setTimeout(() => {
          setErrors((prev) => prev.filter((e) => e.id !== errorInfo.id));
        }, 5000);
      }
    },
    []
  );

  const removeError = useCallback((id: string) => {
    setErrors((prev) => prev.filter((error) => error.id !== id));
  }, []);

  const clearErrors = useCallback(() => {
    setErrors([]);
  }, []);

  return (
    <ErrorContext.Provider
      value={{ errors, addError, removeError, clearErrors }}
    >
      {children}
    </ErrorContext.Provider>
  );
}

export function useError() {
  const context = useContext(ErrorContext);
  if (context === undefined) {
    throw new Error("useError must be used within an ErrorProvider");
  }
  return context;
}

// Error Toast Component
export function ErrorToast() {
  const { errors, removeError } = useError();

  if (errors.length === 0) return null;

  return (
    <div className="fixed top-4 right-4 z-50 space-y-2">
      {errors.map((error) => (
        <div
          key={error.id}
          className={`
            max-w-md p-4 rounded-lg shadow-lg border-l-4 bg-white dark:bg-gray-800
            ${error.type === "error" ? "border-red-500" : ""}
            ${error.type === "warning" ? "border-yellow-500" : ""}
            ${error.type === "info" ? "border-blue-500" : ""}
          `}
        >
          <div className="flex items-start justify-between">
            <div className="flex-1">
              <div
                className={`
                text-sm font-medium
                ${
                  error.type === "error" ? "text-red-800 dark:text-red-200" : ""
                }
                ${
                  error.type === "warning"
                    ? "text-yellow-800 dark:text-yellow-200"
                    : ""
                }
                ${
                  error.type === "info"
                    ? "text-blue-800 dark:text-blue-200"
                    : ""
                }
              `}
              >
                {error.type === "error" && "❌ Error"}
                {error.type === "warning" && "⚠️ Warning"}
                {error.type === "info" && "ℹ️ Info"}
              </div>
              <div className="mt-1 text-sm text-gray-700 dark:text-gray-300">
                {error.message}
              </div>
              {error.code && (
                <div className="mt-1 text-xs text-gray-500 dark:text-gray-400">
                  Code: {error.code}
                </div>
              )}
            </div>
            <button
              onClick={() => removeError(error.id)}
              className="ml-4 text-gray-400 hover:text-gray-600 dark:hover:text-gray-200"
            >
              ✕
            </button>
          </div>
        </div>
      ))}
    </div>
  );
}
