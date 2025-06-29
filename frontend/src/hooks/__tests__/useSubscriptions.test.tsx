import { renderHook, act, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { vi, describe, it, expect, beforeEach, afterEach } from "vitest";
import {
  useSubscriptionMutations,
  subscriptionKeys,
} from "../useSubscriptions";
import { ErrorProvider } from "../../contexts/ErrorContext";
import { apiClient } from "../../api/client";
import type { ReactNode } from "react";

// Mock the API client
vi.mock("../../api/client", () => ({
  apiClient: {
    subscribeToEvent: vi.fn(),
    unsubscribeFromEvent: vi.fn(),
  },
}));

const mockApiClient = vi.mocked(apiClient);

// Test wrapper component
function TestWrapper({ children }: { children: ReactNode }) {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  });

  return (
    <QueryClientProvider client={queryClient}>
      <ErrorProvider>{children}</ErrorProvider>
    </QueryClientProvider>
  );
}

describe("useSubscriptionMutations", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  describe("subscribe mutation", () => {
    it("should add event to loading state during subscription", async () => {
      const { result } = renderHook(() => useSubscriptionMutations(), {
        wrapper: TestWrapper,
      });

      // Mock successful API response
      mockApiClient.subscribeToEvent.mockResolvedValueOnce({
        id: 1,
        eventId: 123,
        eventName: "Test Event",
        systemName: "Test System",
        subscribed: true,
      });

      // Initially, no events should be loading
      expect(result.current.loadingEventIds.has(123)).toBe(false);

      // Start subscription
      act(() => {
        result.current.subscribe.mutate(123);
      });

      // Event should be in loading state
      expect(result.current.loadingEventIds.has(123)).toBe(true);

      // Wait for mutation to complete
      await waitFor(() => {
        expect(result.current.subscribe.isSuccess).toBe(true);
      });

      // Event should no longer be in loading state
      expect(result.current.loadingEventIds.has(123)).toBe(false);
    });

    it("should remove event from loading state on error", async () => {
      const { result } = renderHook(() => useSubscriptionMutations(), {
        wrapper: TestWrapper,
      });

      // Mock API error
      const mockError = new Error("Subscription failed");
      mockApiClient.subscribeToEvent.mockRejectedValueOnce(mockError);

      // Start subscription
      act(() => {
        result.current.subscribe.mutate(123);
      });

      // Event should be in loading state
      expect(result.current.loadingEventIds.has(123)).toBe(true);

      // Wait for mutation to fail
      await waitFor(() => {
        expect(result.current.subscribe.isError).toBe(true);
      });

      // Event should no longer be in loading state
      expect(result.current.loadingEventIds.has(123)).toBe(false);
    });

    it("should handle multiple concurrent subscriptions", async () => {
      const { result } = renderHook(() => useSubscriptionMutations(), {
        wrapper: TestWrapper,
      });

      // Mock successful API responses
      mockApiClient.subscribeToEvent.mockImplementation((eventId) =>
        Promise.resolve({
          id: eventId,
          eventId,
          eventName: `Event ${eventId}`,
          systemName: "Test System",
          subscribed: true,
        })
      );

      // Start multiple subscriptions
      act(() => {
        result.current.subscribe.mutate(123);
        result.current.subscribe.mutate(456);
        result.current.subscribe.mutate(789);
      });

      // All events should be in loading state
      expect(result.current.loadingEventIds.has(123)).toBe(true);
      expect(result.current.loadingEventIds.has(456)).toBe(true);
      expect(result.current.loadingEventIds.has(789)).toBe(true);

      // Wait for mutations to complete
      await waitFor(() => {
        expect(result.current.subscribe.isSuccess).toBe(true);
      });

      // All events should no longer be in loading state
      expect(result.current.loadingEventIds.has(123)).toBe(false);
      expect(result.current.loadingEventIds.has(456)).toBe(false);
      expect(result.current.loadingEventIds.has(789)).toBe(false);
    });
  });

  describe("unsubscribe mutation", () => {
    it("should add event to loading state during unsubscription", async () => {
      const { result } = renderHook(() => useSubscriptionMutations(), {
        wrapper: TestWrapper,
      });

      // Mock successful API response
      mockApiClient.unsubscribeFromEvent.mockResolvedValueOnce(undefined);

      // Initially, no events should be loading
      expect(result.current.loadingEventIds.has(123)).toBe(false);

      // Start unsubscription
      act(() => {
        result.current.unsubscribe.mutate(123);
      });

      // Event should be in loading state
      expect(result.current.loadingEventIds.has(123)).toBe(true);

      // Wait for mutation to complete
      await waitFor(() => {
        expect(result.current.unsubscribe.isSuccess).toBe(true);
      });

      // Event should no longer be in loading state
      expect(result.current.loadingEventIds.has(123)).toBe(false);
    });

    it("should remove event from loading state on error", async () => {
      const { result } = renderHook(() => useSubscriptionMutations(), {
        wrapper: TestWrapper,
      });

      // Mock API error
      const mockError = new Error("Unsubscription failed");
      mockApiClient.unsubscribeFromEvent.mockRejectedValueOnce(mockError);

      // Start unsubscription
      act(() => {
        result.current.unsubscribe.mutate(123);
      });

      // Event should be in loading state
      expect(result.current.loadingEventIds.has(123)).toBe(true);

      // Wait for mutation to fail
      await waitFor(() => {
        expect(result.current.unsubscribe.isError).toBe(true);
      });

      // Event should no longer be in loading state
      expect(result.current.loadingEventIds.has(123)).toBe(false);
    });
  });

  describe("loading state management", () => {
    it("should correctly track loading state for mixed operations", async () => {
      const { result } = renderHook(() => useSubscriptionMutations(), {
        wrapper: TestWrapper,
      });

      // Mock API responses with delays to test concurrent operations
      mockApiClient.subscribeToEvent.mockImplementation(
        () =>
          new Promise((resolve) =>
            setTimeout(
              () =>
                resolve({
                  id: 1,
                  eventId: 123,
                  eventName: "Test Event",
                  systemName: "Test System",
                  subscribed: true,
                }),
              100
            )
          )
      );

      mockApiClient.unsubscribeFromEvent.mockImplementation(
        () =>
          new Promise((resolve) => setTimeout(() => resolve(undefined), 150))
      );

      // Start both operations
      act(() => {
        result.current.subscribe.mutate(123);
        result.current.unsubscribe.mutate(456);
      });

      // Both events should be in loading state
      expect(result.current.loadingEventIds.has(123)).toBe(true);
      expect(result.current.loadingEventIds.has(456)).toBe(true);
      expect(result.current.loadingEventIds.size).toBe(2);

      // Wait for subscribe to complete first
      await waitFor(
        () => {
          expect(result.current.subscribe.isSuccess).toBe(true);
        },
        { timeout: 200 }
      );

      // Subscribe should be done, unsubscribe still loading
      expect(result.current.loadingEventIds.has(123)).toBe(false);
      expect(result.current.loadingEventIds.has(456)).toBe(true);
      expect(result.current.loadingEventIds.size).toBe(1);

      // Wait for unsubscribe to complete
      await waitFor(
        () => {
          expect(result.current.unsubscribe.isSuccess).toBe(true);
        },
        { timeout: 300 }
      );

      // Both should be done
      expect(result.current.loadingEventIds.has(123)).toBe(false);
      expect(result.current.loadingEventIds.has(456)).toBe(false);
      expect(result.current.loadingEventIds.size).toBe(0);
    });
  });
});
