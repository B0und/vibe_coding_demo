import React from "react";
import type { ReactNode } from "react";
import { QueryErrorResetBoundary } from "@tanstack/react-query";
import { ErrorBoundary } from "react-error-boundary";
import { useError } from "../contexts/ErrorContext";
import { ApiClientError } from "../api/client";
import { errorReporter } from "../utils/errorReporting";

interface QueryErrorBoundaryProps {
  children: ReactNode;
  fallback?: ReactNode;
}

function ErrorFallback({
  error,
  resetErrorBoundary,
}: {
  error: Error;
  resetErrorBoundary: () => void;
}) {
  const { addError } = useError();

  React.useEffect(() => {
    // Add the error to our global error context
    addError(error);

    // Report the error to the backend for logging
    errorReporter.reportReactError(
      error,
      { componentStack: error.stack },
      {
        component: "QueryErrorBoundary",
        errorBoundary: true,
        userAction: "component_render",
      }
    );
  }, [error, addError]);

  const handleRetry = () => {
    // Log the retry attempt
    errorReporter.reportCustomError(
      "User retried after error boundary",
      {
        originalError: error.message,
        component: "QueryErrorBoundary",
        userAction: "retry_button_click",
      },
      false
    );

    resetErrorBoundary();
  };

  // Determine if this is a critical error that should show more details
  const isCritical =
    !(error instanceof ApiClientError) || (error.status && error.status >= 500);

  return (
    <div className="min-h-[200px] flex items-center justify-center p-8">
      <div className="text-center max-w-md">
        <div className="mb-4">
          {isCritical ? (
            <div className="text-red-500 text-4xl mb-2">⚠️</div>
          ) : (
            <div className="text-yellow-500 text-4xl mb-2">⚡</div>
          )}
        </div>

        <h2 className="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-2">
          {isCritical ? "Something went wrong" : "Temporary issue"}
        </h2>

        <p className="text-gray-600 dark:text-gray-400 mb-4">
          {error instanceof ApiClientError
            ? error.message
            : isCritical
            ? "An unexpected error occurred. Our team has been notified."
            : "Please try again in a moment."}
        </p>

        {/* Show error details in development */}
        {import.meta.env.DEV && (
          <details className="mb-4 text-left">
            <summary className="cursor-pointer text-sm text-gray-500 hover:text-gray-700">
              Error Details (Development)
            </summary>
            <pre className="mt-2 p-2 bg-gray-100 dark:bg-gray-800 rounded text-xs overflow-auto">
              {error.stack}
            </pre>
          </details>
        )}

        <div className="space-y-2">
          <button
            onClick={handleRetry}
            className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
          >
            Try Again
          </button>

          {isCritical && (
            <div className="text-xs text-gray-500 dark:text-gray-400">
              If this problem persists, please contact support
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export function QueryErrorBoundary({
  children,
  fallback,
}: QueryErrorBoundaryProps) {
  return (
    <QueryErrorResetBoundary>
      {({ reset }) => (
        <ErrorBoundary
          FallbackComponent={fallback ? () => <>{fallback}</> : ErrorFallback}
          onReset={reset}
          onError={(error, errorInfo) => {
            console.error("QueryErrorBoundary caught an error:", error);

            // Report the error with full context
            errorReporter.reportReactError(
              error,
              {
                componentStack: errorInfo.componentStack || undefined,
              },
              {
                component: "QueryErrorBoundary",
                errorBoundary: true,
                hasCustomFallback: !!fallback,
              }
            );
          }}
        >
          {children}
        </ErrorBoundary>
      )}
    </QueryErrorResetBoundary>
  );
}
