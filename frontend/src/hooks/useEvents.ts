import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { apiClient, eventManagementApi, type EventInput } from '../api/client';
import type { Event } from '../types';

// Query keys for React Query cache management
export const eventQueryKeys = {
  all: ['events'] as const,
  lists: () => [...eventQueryKeys.all, 'list'] as const,
  list: (filters?: Record<string, unknown>) => [...eventQueryKeys.lists(), { filters }] as const,
  details: () => [...eventQueryKeys.all, 'detail'] as const,
  detail: (id: number) => [...eventQueryKeys.details(), id] as const,
};

/**
 * Hook to fetch all events
 * @returns React Query result with events data
 */
export function useEvents() {
  return useQuery({
    queryKey: eventQueryKeys.list(),
    queryFn: () => apiClient.getEvents(),
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
}

/**
 * Hook to create a new event
 * @returns React Query mutation for creating events
 */
export function useCreateEvent() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (event: EventInput) => eventManagementApi.createEvent(event),
    onSuccess: (newEvent) => {
      // Add the new event to the cache
      queryClient.setQueryData<Event[]>(eventQueryKeys.list(), (oldData) => {
        return oldData ? [...oldData, newEvent] : [newEvent];
      });
      
      // Invalidate and refetch events list to ensure consistency
      queryClient.invalidateQueries({ queryKey: eventQueryKeys.lists() });
    },
    onError: (error) => {
      console.error('Failed to create event:', error);
    },
  });
}

/**
 * Hook to update an existing event
 * @returns React Query mutation for updating events
 */
export function useUpdateEvent() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, event }: { id: number; event: EventInput }) => 
      eventManagementApi.updateEvent(id, event),
    onSuccess: (updatedEvent) => {
      // Update the event in the cache
      queryClient.setQueryData<Event[]>(eventQueryKeys.list(), (oldData) => {
        return oldData?.map((event) => 
          event.id === updatedEvent.id ? updatedEvent : event
        ) ?? [];
      });
      
      // Update individual event cache if it exists
      queryClient.setQueryData(eventQueryKeys.detail(updatedEvent.id), updatedEvent);
      
      // Invalidate related queries
      queryClient.invalidateQueries({ queryKey: eventQueryKeys.lists() });
    },
    onError: (error) => {
      console.error('Failed to update event:', error);
    },
  });
}

/**
 * Hook to delete an event
 * @returns React Query mutation for deleting events
 */
export function useDeleteEvent() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: number) => eventManagementApi.deleteEvent(id),
    onSuccess: (_, deletedId) => {
      // Remove the event from the cache
      queryClient.setQueryData<Event[]>(eventQueryKeys.list(), (oldData) => {
        return oldData?.filter((event) => event.id !== deletedId) ?? [];
      });
      
      // Remove individual event cache
      queryClient.removeQueries({ queryKey: eventQueryKeys.detail(deletedId) });
      
      // Invalidate related queries
      queryClient.invalidateQueries({ queryKey: eventQueryKeys.lists() });
    },
    onError: (error) => {
      console.error('Failed to delete event:', error);
    },
  });
}

/**
 * Hook to fetch a single event by ID
 * @param id - Event ID
 * @returns React Query result with event data
 */
export function useEvent(id: number) {
  return useQuery({
    queryKey: eventQueryKeys.detail(id),
    queryFn: () => apiClient.get<Event>(`/api/events/${id}`),
    enabled: !!id,
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
} 