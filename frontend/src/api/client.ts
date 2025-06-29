const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

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
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      
      return await response.json();
    } catch (error) {
      console.error('API request failed:', error);
      throw error;
    }
  }

  async get<T>(endpoint: string): Promise<T> {
    return this.request<T>(endpoint, { method: 'GET' });
  }

  async post<T>(endpoint: string, data?: any): Promise<T> {
    return this.request<T>(endpoint, {
      method: 'POST',
      body: data ? JSON.stringify(data) : undefined,
    });
  }

  async put<T>(endpoint: string, data?: any): Promise<T> {
    return this.request<T>(endpoint, {
      method: 'PUT',
      body: data ? JSON.stringify(data) : undefined,
    });
  }

  async delete<T>(endpoint: string): Promise<T> {
    return this.request<T>(endpoint, { method: 'DELETE' });
  }
}

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
    try {
      const response = await apiClient.post<LoginResponse>('/users/register', {
        username: username.trim()
      });
      
      // Store the token in localStorage
      if (response.token) {
        localStorage.setItem('authToken', response.token);
      }
      
      return response;
    } catch (error) {
      console.error('Login failed:', error);
      throw new Error('Login failed. Please try again.');
    }
  },

  async getCurrentUser(): Promise<AuthUser> {
    try {
      const user = await apiClient.get<AuthUser>('/users/me');
      return user;
    } catch (error) {
      console.error('Failed to fetch current user:', error);
      // Clear invalid token
      localStorage.removeItem('authToken');
      throw new Error('Failed to fetch user information.');
    }
  },

  logout(): void {
    localStorage.removeItem('authToken');
  }
}; 