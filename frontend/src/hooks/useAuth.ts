import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { authApi, type AuthUser, type LoginResponse } from '../api/client';
import { useCallback } from 'react';

export interface User {
  id: string;
  username: string;
  role: 'USER' | 'ADMIN';
  createdAt: string;
  updatedAt?: string;
}

// Query key constants
const AUTH_QUERY_KEYS = {
  user: ['auth', 'user'] as const,
} as const;

// Hook to get current user
export function useCurrentUser() {
  return useQuery({
    queryKey: AUTH_QUERY_KEYS.user,
    queryFn: async (): Promise<User | null> => {
      const token = localStorage.getItem('authToken');
      if (!token) {
        return null;
      }
      
      try {
        const authUser: AuthUser = await authApi.getCurrentUser();
        return {
          id: authUser.id,
          username: authUser.username,
          role: authUser.role,
          createdAt: authUser.createdAt,
          updatedAt: authUser.updatedAt,
        };
      } catch (error) {
        // Only remove token for actual authentication errors (401)
        // Don't remove for network errors or other issues
        if (error && typeof error === 'object' && 'status' in error && error.status === 401) {
          localStorage.removeItem('authToken');
        }
        return null;
      }
    },
    staleTime: 5 * 60 * 1000, // 5 minutes
    gcTime: 10 * 60 * 1000, // 10 minutes
    retry: false, // Don't retry auth failures
  });
}

// Hook for login mutation
export function useLogin() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: async (username: string): Promise<User> => {
      const response: LoginResponse = await authApi.login(username);
      return {
        id: response.user.id,
        username: response.user.username,
        role: response.user.role,
        createdAt: response.user.createdAt,
      };
    },
    onSuccess: (user) => {
      // Update the user query cache
      queryClient.setQueryData(AUTH_QUERY_KEYS.user, user);
      // Invalidate all queries to refresh data for the new user
      queryClient.invalidateQueries();
    },
    onError: () => {
      // Error will be automatically shown by the global error handler
      // Just ensure token is cleared on auth failure
      localStorage.removeItem('authToken');
      queryClient.setQueryData(AUTH_QUERY_KEYS.user, null);
    },
    retry: false, // Don't retry login failures
  });
}

// Hook for logout
export function useLogout() {
  const queryClient = useQueryClient();
  
  return useCallback(() => {
    authApi.logout();
    queryClient.setQueryData(AUTH_QUERY_KEYS.user, null);
    queryClient.clear(); // Clear all cached data
    
    // Dispatch custom event for components that need to react to logout
    window.dispatchEvent(new CustomEvent('auth:logout'));
  }, [queryClient]);
}

// Main auth hook that combines everything
export function useAuth() {
  const userQuery = useCurrentUser();
  const loginMutation = useLogin();
  const logout = useLogout();
  
  return {
    // User data
    user: userQuery.data || null,
    isAuthenticated: !!userQuery.data,
    
    // Loading states
    isLoading: userQuery.isLoading,
    isLoginLoading: loginMutation.isPending,
    
    // Error states
    error: userQuery.error || loginMutation.error,
    
    // Actions
    login: loginMutation.mutateAsync,
    logout,
    
    // Utilities
    refetch: userQuery.refetch,
  };
}

// Hook to check if user is authenticated (useful for route guards)
export function useIsAuthenticated() {
  const { data: user, isLoading } = useCurrentUser();
  
  return {
    isAuthenticated: !!user,
    isLoading,
    user,
  };
} 