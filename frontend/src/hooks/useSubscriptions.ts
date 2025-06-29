import React from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '../api/client';
import { useError } from '../contexts/ErrorContext';
import { useIsAuthenticated } from './useAuth';
import type { Event, Subscription, EventWithSubscription } from '../types';

// Query keys for React Query
export const subscriptionKeys = {
  all: ['subscriptions'] as const,
  events: ['events'] as const,
  userSubscriptions: ['subscriptions', 'user'] as const,
  subscriptionStatus: (eventId: number) => ['subscriptions', 'status', eventId] as const,
};

/**
 * Hook to fetch all available events
 */
export function useEvents() {
  const { isAuthenticated } = useIsAuthenticated();
  
  return useQuery({
    queryKey: subscriptionKeys.events,
    queryFn: () => apiClient.getEvents(),
    staleTime: 5 * 60 * 1000, // 5 minutes
    enabled: isAuthenticated, // Only fetch when authenticated
  });
}

/**
 * Hook to fetch current user's subscriptions
 */
export function useUserSubscriptions() {
  const { isAuthenticated } = useIsAuthenticated();
  
  return useQuery({
    queryKey: subscriptionKeys.userSubscriptions,
    queryFn: () => apiClient.getUserSubscriptions(),
    staleTime: 1 * 60 * 1000, // 1 minute
    enabled: isAuthenticated, // Only fetch when authenticated
  });
}

/**
 * Combined hook that fetches both events and user subscriptions,
 * and provides a merged data structure indicating subscription status for each event
 */
export function useSubscriptionData() {
  const eventsQuery = useEvents();
  const subscriptionsQuery = useUserSubscriptions();

  // Create a combined data structure
  const combinedData: EventWithSubscription[] = React.useMemo(() => {
    if (!eventsQuery.data || !subscriptionsQuery.data) {
      return [];
    }

    // Create a Set of subscribed event IDs for quick lookup
    const subscribedEventIds = new Set(
      subscriptionsQuery.data.map(sub => sub.eventId)
    );

    // Map events to include subscription status
    return eventsQuery.data.map(event => ({
      ...event,
      subscribed: subscribedEventIds.has(event.id),
    }));
  }, [eventsQuery.data, subscriptionsQuery.data]);

  return {
    data: combinedData,
    isLoading: eventsQuery.isLoading || subscriptionsQuery.isLoading,
    isError: eventsQuery.isError || subscriptionsQuery.isError,
    error: eventsQuery.error || subscriptionsQuery.error,
    refetch: () => {
      eventsQuery.refetch();
      subscriptionsQuery.refetch();
    },
  };
}

/**
 * Hook to check subscription status for a specific event
 */
export function useSubscriptionStatus(eventId: number) {
  const { isAuthenticated } = useIsAuthenticated();
  
  return useQuery({
    queryKey: subscriptionKeys.subscriptionStatus(eventId),
    queryFn: () => apiClient.getSubscriptionStatus(eventId),
    enabled: !!eventId && isAuthenticated, // Only fetch when authenticated and eventId exists
    staleTime: 30 * 1000, // 30 seconds
  });
}

/**
 * Hook for subscription mutations (subscribe/unsubscribe)
 * Enhanced with error handling via ErrorContext and loading state tracking
 */
export function useSubscriptionMutations() {
  const queryClient = useQueryClient();
  const { addError } = useError();
  const [loadingEventIds, setLoadingEventIds] = React.useState<Set<number>>(new Set());

  const subscribeMutation = useMutation({
    mutationFn: (eventId: number) => apiClient.subscribeToEvent(eventId),
    onMutate: async (eventId: number) => {
      // Add to loading state
      setLoadingEventIds(prev => new Set([...prev, eventId]));

      // Cancel any outgoing refetches
      await queryClient.cancelQueries({ queryKey: subscriptionKeys.userSubscriptions });
      await queryClient.cancelQueries({ queryKey: subscriptionKeys.events });

      // Snapshot the previous value
      const previousSubscriptions = queryClient.getQueryData<Subscription[]>(subscriptionKeys.userSubscriptions);
      const previousEvents = queryClient.getQueryData<Event[]>(subscriptionKeys.events);

      // Optimistically update user subscriptions
      if (previousSubscriptions && previousEvents) {
        const event = previousEvents.find(e => e.id === eventId);
        if (event) {
          const newSubscription: Subscription = {
            id: Date.now(), // Temporary ID
            eventId: event.id,
            eventName: event.eventName,
            systemName: event.systemName,
            subscribed: true,
          };
          queryClient.setQueryData<Subscription[]>(
            subscriptionKeys.userSubscriptions,
            [...previousSubscriptions, newSubscription]
          );
        }
      }

      // Return a context object with the snapshotted value
      return { previousSubscriptions, previousEvents };
    },
    onSuccess: (_, eventId) => {
      // Remove from loading state
      setLoadingEventIds(prev => {
        const newSet = new Set(prev);
        newSet.delete(eventId);
        return newSet;
      });
    },
    onError: (err, eventId, context) => {
      // Remove from loading state
      setLoadingEventIds(prev => {
        const newSet = new Set(prev);
        newSet.delete(eventId);
        return newSet;
      });

      // Add error to global error context for toast notification
      addError(err, 'error');

      // If the mutation fails, use the context returned from onMutate to roll back
      if (context?.previousSubscriptions) {
        queryClient.setQueryData(subscriptionKeys.userSubscriptions, context.previousSubscriptions);
      }
    },
    onSettled: () => {
      // Always refetch after error or success
      queryClient.invalidateQueries({ queryKey: subscriptionKeys.userSubscriptions });
    },
  });

  const unsubscribeMutation = useMutation({
    mutationFn: (eventId: number) => apiClient.unsubscribeFromEvent(eventId),
    onMutate: async (eventId: number) => {
      // Add to loading state
      setLoadingEventIds(prev => new Set([...prev, eventId]));

      // Cancel any outgoing refetches
      await queryClient.cancelQueries({ queryKey: subscriptionKeys.userSubscriptions });

      // Snapshot the previous value
      const previousSubscriptions = queryClient.getQueryData<Subscription[]>(subscriptionKeys.userSubscriptions);

      // Optimistically update user subscriptions
      if (previousSubscriptions) {
        queryClient.setQueryData<Subscription[]>(
          subscriptionKeys.userSubscriptions,
          previousSubscriptions.filter(sub => sub.eventId !== eventId)
        );
      }

      // Return a context object with the snapshotted value
      return { previousSubscriptions };
    },
    onSuccess: (_, eventId) => {
      // Remove from loading state
      setLoadingEventIds(prev => {
        const newSet = new Set(prev);
        newSet.delete(eventId);
        return newSet;
      });
    },
    onError: (err, eventId, context) => {
      // Remove from loading state
      setLoadingEventIds(prev => {
        const newSet = new Set(prev);
        newSet.delete(eventId);
        return newSet;
      });

      // Add error to global error context for toast notification
      addError(err, 'error');

      // If the mutation fails, use the context returned from onMutate to roll back
      if (context?.previousSubscriptions) {
        queryClient.setQueryData(subscriptionKeys.userSubscriptions, context.previousSubscriptions);
      }
    },
    onSettled: () => {
      // Always refetch after error or success
      queryClient.invalidateQueries({ queryKey: subscriptionKeys.userSubscriptions });
    },
  });

  return {
    subscribe: subscribeMutation,
    unsubscribe: unsubscribeMutation,
    loadingEventIds, // Export loading state for UI components
  };
} 