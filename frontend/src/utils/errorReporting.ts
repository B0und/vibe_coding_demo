import { apiClient, type ClientErrorReport } from '../api/client';

/**
 * Enhanced error reporting utility for client-side errors
 */
export class ErrorReporter {
  private static instance: ErrorReporter;
  private isEnabled: boolean = true;
  private maxReportsPerSession: number = 50;
  private reportCount: number = 0;

  private constructor() {
    this.setupGlobalErrorHandlers();
  }

  public static getInstance(): ErrorReporter {
    if (!ErrorReporter.instance) {
      ErrorReporter.instance = new ErrorReporter();
    }
    return ErrorReporter.instance;
  }

  /**
   * Set up global error handlers for unhandled errors and promise rejections
   */
  private setupGlobalErrorHandlers(): void {
    // Handle unhandled JavaScript errors
    window.addEventListener('error', (event) => {
      this.reportError({
        message: event.message,
        stack: event.error?.stack,
        url: window.location.href,
        timestamp: new Date().toISOString(),
        userAgent: navigator.userAgent,
        critical: true,
        props: {
          filename: event.filename,
          lineno: event.lineno,
          colno: event.colno,
          type: 'javascript_error'
        }
      });
    });

    // Handle unhandled promise rejections
    window.addEventListener('unhandledrejection', (event) => {
      const error = event.reason;
      this.reportError({
        message: error?.message || 'Unhandled promise rejection',
        stack: error?.stack,
        url: window.location.href,
        timestamp: new Date().toISOString(),
        userAgent: navigator.userAgent,
        critical: true,
        props: {
          type: 'unhandled_promise_rejection',
          reason: error
        }
      });
    });
  }

  /**
   * Report a React component error
   */
  public reportReactError(
    error: Error,
    errorInfo: { componentStack?: string },
    props?: Record<string, unknown>
  ): void {
    this.reportError({
      message: error.message,
      stack: error.stack,
      url: window.location.href,
      componentStack: errorInfo.componentStack,
      timestamp: new Date().toISOString(),
      userAgent: navigator.userAgent,
      critical: true,
      props: {
        ...props,
        type: 'react_error',
        errorName: error.name
      }
    });
  }

  /**
   * Report an API error
   */
  public reportApiError(
    error: Error,
    endpoint: string,
    method: string,
    requestData?: unknown
  ): void {
    this.reportError({
      message: error.message,
      stack: error.stack,
      url: window.location.href,
      timestamp: new Date().toISOString(),
      userAgent: navigator.userAgent,
      critical: false,
      props: {
        type: 'api_error',
        endpoint,
        method,
        requestData: this.sanitizeRequestData(requestData),
        errorName: error.name
      }
    });
  }

  /**
   * Report a custom error with additional context
   */
  public reportCustomError(
    message: string,
    context?: Record<string, unknown>,
    critical: boolean = false
  ): void {
    this.reportError({
      message,
      url: window.location.href,
      timestamp: new Date().toISOString(),
      userAgent: navigator.userAgent,
      critical,
      props: {
        ...context,
        type: 'custom_error'
      }
    });
  }

  /**
   * Core error reporting method
   */
  private async reportError(errorReport: ClientErrorReport): Promise<void> {
    if (!this.isEnabled || this.reportCount >= this.maxReportsPerSession) {
      return;
    }

    this.reportCount++;

    try {
      // Add additional browser context
      const enhancedReport: ClientErrorReport = {
        ...errorReport,
        props: {
          ...errorReport.props,
          browserInfo: this.getBrowserInfo(),
          viewportSize: {
            width: window.innerWidth,
            height: window.innerHeight
          },
          sessionId: this.getSessionId()
        }
      };

      await apiClient.reportError(enhancedReport);
    } catch (reportingError) {
      console.warn('Failed to report error:', reportingError);
    }
  }

  /**
   * Get browser information for error context
   */
  private getBrowserInfo(): Record<string, unknown> {
    return {
      userAgent: navigator.userAgent,
      language: navigator.language,
      platform: navigator.platform,
      cookieEnabled: navigator.cookieEnabled,
      onLine: navigator.onLine,
      url: window.location.href,
      referrer: document.referrer
    };
  }

  /**
   * Get or create a session ID for tracking errors within a session
   */
  private getSessionId(): string {
    let sessionId = sessionStorage.getItem('errorReportingSessionId');
    if (!sessionId) {
      sessionId = `session_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
      sessionStorage.setItem('errorReportingSessionId', sessionId);
    }
    return sessionId;
  }

  /**
   * Sanitize request data to remove sensitive information
   */
  private sanitizeRequestData(data: unknown): unknown {
    if (!data || typeof data !== 'object') {
      return data;
    }

    const sensitiveKeys = ['password', 'token', 'secret', 'key', 'authorization'];
    const sanitized = { ...data as Record<string, unknown> };

    for (const key of Object.keys(sanitized)) {
      if (sensitiveKeys.some(sensitive => key.toLowerCase().includes(sensitive))) {
        sanitized[key] = '[REDACTED]';
      }
    }

    return sanitized;
  }

  /**
   * Enable or disable error reporting
   */
  public setEnabled(enabled: boolean): void {
    this.isEnabled = enabled;
  }

  /**
   * Set maximum number of reports per session to prevent spam
   */
  public setMaxReportsPerSession(max: number): void {
    this.maxReportsPerSession = max;
  }

  /**
   * Reset the report count (useful for testing)
   */
  public resetReportCount(): void {
    this.reportCount = 0;
  }
}

// Export singleton instance
export const errorReporter = ErrorReporter.getInstance(); 