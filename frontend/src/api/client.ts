import type { Event, Subscription } from '../types';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

// Type definitions for the API client
export interface RequestConfig {
  headers?: Record<string, string>;
  signal?: AbortSignal;
}

export interface ApiError {
  message: string;
  status?: number;
  code?: string;
  details?: unknown;
}

// Custom error class for API errors
export class ApiClientError extends Error implements ApiError {
  status?: number;
  code?: string;
  details?: unknown;

  constructor(message: string, status?: number, code?: string, details?: unknown) {
    super(message);
    this.name = 'ApiClientError';
    this.status = status;
    this.code = code;
    this.details = details;
  }
}

class ApiClient {
  private baseURL: string;

  constructor(baseURL: string) {
    this.baseURL = baseURL;
  }

  private async request<T>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<T> {
    const url = `${this.baseURL}${endpoint}`;
    
    const config: RequestInit = {
      headers: {
        'Content-Type': 'application/json',
        ...options.headers,
      },
      ...options,
    };

    // Add auth token if available
    const token = localStorage.getItem('authToken');
    if (token) {
      config.headers = {
        ...config.headers,
        Authorization: `Bearer ${token}`,
      };
    }

    try {
      const response = await fetch(url, config);
      
      if (!response.ok) {
        await this.handleErrorResponse(response);
      }
      
      // Handle empty responses
      const contentType = response.headers.get('content-type');
      if (contentType && contentType.includes('application/json')) {
        return await response.json();
      } else {
        return {} as T;
      }
    } catch (error) {
      if (error instanceof ApiClientError) {
        throw error;
      }
      
      // Handle network errors
      if (error instanceof TypeError && error.message.includes('fetch')) {
        throw new ApiClientError(
          'Network error. Please check your connection and try again.',
          undefined,
          'NETWORK_ERROR'
        );
      }
      
      throw new ApiClientError(
        'An unexpected error occurred',
        undefined,
        'UNKNOWN_ERROR',
        error
      );
    }
  }

  private async handleErrorResponse(response: Response): Promise<never> {
    let details: unknown;
    let message = 'An unexpected error occurred';
    const status = response.status;
    let code: string;

    try {
      const contentType = response.headers.get('content-type');
      if (contentType && contentType.includes('application/json')) {
        details = await response.json();
      } else {
        details = await response.text();
      }
    } catch {
      details = null;
    }

    switch (status) {
      case 400:
        message = 'Bad request. Please check your input.';
        code = 'BAD_REQUEST';
        break;
      case 401:
        message = 'Authentication required. Please log in.';
        code = 'UNAUTHORIZED';
        // Clear invalid token
        localStorage.removeItem('authToken');
        break;
      case 403:
        message = 'Access denied. You do not have permission to perform this action.';
        code = 'FORBIDDEN';
        break;
      case 404:
        message = 'Resource not found.';
        code = 'NOT_FOUND';
        break;
      case 409:
        message = 'Conflict. The resource already exists or cannot be modified.';
        code = 'CONFLICT';
        break;
      case 422:
        message = 'Invalid data provided.';
        code = 'VALIDATION_ERROR';
        break;
      case 429:
        message = 'Too many requests. Please try again later.';
        code = 'RATE_LIMITED';
        break;
      case 500:
        message = 'Internal server error. Please try again later.';
        code = 'INTERNAL_ERROR';
        break;
      case 502:
        message = 'Service temporarily unavailable.';
        code = 'BAD_GATEWAY';
        break;
      case 503:
        message = 'Service unavailable. Please try again later.';
        code = 'SERVICE_UNAVAILABLE';
        break;
      default:
        message = `Server error (${status}). Please try again later.`;
        code = 'SERVER_ERROR';
    }

    // Try to extract a more specific error message from the response
    if (details && typeof details === 'object' && details !== null) {
      const errorObj = details as Record<string, unknown>;
      if (typeof errorObj.message === 'string') {
        message = errorObj.message;
      } else if (typeof errorObj.error === 'string') {
        message = errorObj.error;
      }
    }

    throw new ApiClientError(message, status, code, details);
  }

  async get<T>(endpoint: string, config?: RequestConfig): Promise<T> {
    return this.request<T>(endpoint, { 
      method: 'GET',
      ...config
    });
  }

  async post<T>(endpoint: string, data?: unknown, config?: RequestConfig): Promise<T> {
    return this.request<T>(endpoint, {
      method: 'POST',
      body: data ? JSON.stringify(data) : undefined,
      ...config
    });
  }

  async put<T>(endpoint: string, data?: unknown, config?: RequestConfig): Promise<T> {
    return this.request<T>(endpoint, {
      method: 'PUT',
      body: data ? JSON.stringify(data) : undefined,
      ...config
    });
  }

  async delete<T>(endpoint: string, config?: RequestConfig): Promise<T> {
    return this.request<T>(endpoint, { 
      method: 'DELETE',
      ...config
    });
  }

  async patch<T>(endpoint: string, data?: unknown, config?: RequestConfig): Promise<T> {
    return this.request<T>(endpoint, {
      method: 'PATCH',
      body: data ? JSON.stringify(data) : undefined,
      ...config
    });
  }

  logout(): void {
    localStorage.removeItem('authToken');
  }

  // Subscription Management API Methods
  
  /**
   * Get all available events
   * @returns Promise<Event[]>
   */
  async getEvents(): Promise<Event[]> {
    return this.get<Event[]>('/api/events');
  }

  /**
   * Get current user's subscriptions
   * @returns Promise<Subscription[]>
   */
  async getUserSubscriptions(): Promise<Subscription[]> {
    return this.get<Subscription[]>('/api/subscriptions');
  }

  /**
   * Subscribe to an event
   * @param eventId - The ID of the event to subscribe to
   * @returns Promise<Subscription>
   */
  async subscribeToEvent(eventId: number): Promise<Subscription> {
    return this.post<Subscription>(`/api/subscriptions/${eventId}`);
  }

  /**
   * Unsubscribe from an event
   * @param eventId - The ID of the event to unsubscribe from
   * @returns Promise<void>
   */
  async unsubscribeFromEvent(eventId: number): Promise<void> {
    return this.delete<void>(`/api/subscriptions/${eventId}`);
  }

  /**
   * Check subscription status for a specific event
   * @param eventId - The ID of the event to check
   * @returns Promise<{eventId: number, subscribed: boolean}>
   */
  async getSubscriptionStatus(eventId: number): Promise<{eventId: number, subscribed: boolean}> {
    return this.get<{eventId: number, subscribed: boolean}>(`/api/subscriptions/${eventId}/status`);
  }
}

// Create and export the singleton API client instance
export const apiClient = new ApiClient(API_BASE_URL);

// Authentication API functions
export interface LoginResponse {
  user: {
    id: string;
    username: string;
    createdAt: string;
  };
  token: string;
}

export interface AuthUser {
  id: string;
  username: string;
  createdAt: string;
  updatedAt?: string;
}

export const authApi = {
  async login(username: string): Promise<LoginResponse> {
    const response = await apiClient.post<LoginResponse>('/api/users/register', {
      username: username.trim()
    });
    
    // Store the token in localStorage
    if (response.token) {
      localStorage.setItem('authToken', response.token);
    }
    
    return response;
  },

  async getCurrentUser(): Promise<AuthUser> {
    return apiClient.get<AuthUser>('/api/users/me');
  },

  logout(): void {
    localStorage.removeItem('authToken');
  }
}; 