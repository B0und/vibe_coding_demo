import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import type { UseQueryOptions, UseMutationOptions } from '@tanstack/react-query';
import { apiClient, ApiClientError } from '../api/client';

// Default query options with retry logic and error handling
const defaultQueryOptions = {
  retry: (failureCount: number, error: unknown) => {
    // Don't retry on client errors (4xx) except for 408 (timeout) and 429 (rate limit)
    if (error instanceof ApiClientError) {
      const status = error.status;
      if (status && status >= 400 && status < 500 && status !== 408 && status !== 429) {
        return false;
      }
    }
    // Retry up to 3 times for other errors
    return failureCount < 3;
  },
  retryDelay: (attemptIndex: number) => Math.min(1000 * 2 ** attemptIndex, 30000), // Exponential backoff
  staleTime: 5 * 60 * 1000, // 5 minutes
  gcTime: 10 * 60 * 1000, // 10 minutes (formerly cacheTime)
};

// Default mutation options
const defaultMutationOptions = {
  retry: (failureCount: number, error: unknown) => {
    // Don't retry mutations on client errors
    if (error instanceof ApiClientError) {
      const status = error.status;
      if (status && status >= 400 && status < 500) {
        return false;
      }
    }
    // Retry up to 2 times for server errors
    return failureCount < 2;
  },
  retryDelay: (attemptIndex: number) => Math.min(1000 * 2 ** attemptIndex, 10000),
};

// Generic API query hook
export function useApiQuery<T>(
  queryKey: (string | number)[],
  queryFn: () => Promise<T>,
  options?: Omit<UseQueryOptions<T, ApiClientError>, 'queryKey' | 'queryFn'>
) {
  return useQuery({
    queryKey,
    queryFn,
    ...defaultQueryOptions,
    ...options,
  });
}

// Generic API mutation hook
export function useApiMutation<TData, TVariables = void>(
  mutationFn: (variables: TVariables) => Promise<TData>,
  options?: UseMutationOptions<TData, ApiClientError, TVariables>
) {
  return useMutation({
    mutationFn,
    ...defaultMutationOptions,
    ...options,
  });
}

// Hook for GET requests
export function useGet<T>(
  endpoint: string,
  options?: Omit<UseQueryOptions<T, ApiClientError>, 'queryKey' | 'queryFn'>
) {
  return useApiQuery(
    ['GET', endpoint],
    () => apiClient.get<T>(endpoint),
    options
  );
}

// Hook for POST mutations
export function usePost<TData, TVariables = unknown>(
  endpoint: string,
  options?: UseMutationOptions<TData, ApiClientError, TVariables>
) {
  const queryClient = useQueryClient();
  
  return useApiMutation(
    (data: TVariables) => apiClient.post<TData>(endpoint, data),
    {
      ...options,
      onSuccess: (data, variables, context) => {
        // Invalidate related queries on successful POST
        queryClient.invalidateQueries({ queryKey: ['GET'] });
        options?.onSuccess?.(data, variables, context);
      },
    }
  );
}

// Hook for PUT mutations
export function usePut<TData, TVariables = unknown>(
  endpoint: string,
  options?: UseMutationOptions<TData, ApiClientError, TVariables>
) {
  const queryClient = useQueryClient();
  
  return useApiMutation(
    (data: TVariables) => apiClient.put<TData>(endpoint, data),
    {
      ...options,
      onSuccess: (data, variables, context) => {
        // Invalidate related queries on successful PUT
        queryClient.invalidateQueries({ queryKey: ['GET'] });
        options?.onSuccess?.(data, variables, context);
      },
    }
  );
}

// Hook for PATCH mutations
export function usePatch<TData, TVariables = unknown>(
  endpoint: string,
  options?: UseMutationOptions<TData, ApiClientError, TVariables>
) {
  const queryClient = useQueryClient();
  
  return useApiMutation(
    (data: TVariables) => apiClient.patch<TData>(endpoint, data),
    {
      ...options,
      onSuccess: (data, variables, context) => {
        // Invalidate related queries on successful PATCH
        queryClient.invalidateQueries({ queryKey: ['GET'] });
        options?.onSuccess?.(data, variables, context);
      },
    }
  );
}

// Hook for DELETE mutations
export function useDelete<TData = void>(
  endpoint: string,
  options?: UseMutationOptions<TData, ApiClientError, void>
) {
  const queryClient = useQueryClient();
  
  return useApiMutation(
    () => apiClient.delete<TData>(endpoint),
    {
      ...options,
      onSuccess: (data, variables, context) => {
        // Invalidate related queries on successful DELETE
        queryClient.invalidateQueries({ queryKey: ['GET'] });
        options?.onSuccess?.(data, variables, context);
      },
    }
  );
}

// Utility hook to get the query client for manual cache management
export function useApiClient() {
  return useQueryClient();
} 