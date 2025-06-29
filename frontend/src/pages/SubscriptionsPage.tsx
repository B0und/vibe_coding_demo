import { useState } from "react";
import { Button, SearchInput, Card } from "../components/ui";
import {
  useSubscriptionData,
  useSubscriptionMutations,
} from "../hooks/useSubscriptions";
import SubscriptionsTable from "../components/SubscriptionsTable";

export default function SubscriptionsPage() {
  const [searchTerm, setSearchTerm] = useState("");

  // Fetch events and subscription data using the custom hook
  const { data: events, isLoading, isError, error } = useSubscriptionData();

  // Get mutation hooks for subscription toggling
  const { subscribe, unsubscribe, loadingEventIds } =
    useSubscriptionMutations();

  // Filter events based on search term
  const filteredEvents = events.filter(
    (event) =>
      event.systemName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      event.eventName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      event.description.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const handleSearch = (value: string) => {
    setSearchTerm(value);
  };

  const handleClear = () => {
    setSearchTerm("");
  };

  // Handle subscription toggle
  const handleSubscriptionToggle = (
    eventId: number,
    shouldSubscribe: boolean
  ) => {
    if (shouldSubscribe) {
      subscribe.mutate(eventId);
    } else {
      unsubscribe.mutate(eventId);
    }
  };

  // Handle loading state
  if (isLoading) {
    return (
      <div className="max-w-6xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
        <div className="mb-6">
          <h1 className="text-heading-2 text-secondary-900">
            Event Subscriptions
          </h1>
          <p className="mt-2 text-body text-secondary-600">
            Manage your event subscriptions and notification preferences.
          </p>
        </div>
        <Card>
          <SubscriptionsTable
            data={[]}
            isLoading={true}
            emptyText="Loading events..."
          />
        </Card>
      </div>
    );
  }

  // Handle error state
  if (isError) {
    return (
      <div className="max-w-6xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
        <div className="mb-6">
          <h1 className="text-heading-2 text-secondary-900">
            Event Subscriptions
          </h1>
          <p className="mt-2 text-body text-secondary-600">
            Manage your event subscriptions and notification preferences.
          </p>
        </div>
        <Card>
          <div className="text-center py-12">
            <div className="text-error-600 mb-4">
              <svg
                className="mx-auto h-12 w-12"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.732-.833-2.5 0L4.268 18.5c-.77.833.192 2.5 1.732 2.5z"
                />
              </svg>
            </div>
            <h3 className="text-lg font-medium text-secondary-900 mb-2">
              Failed to load events
            </h3>
            <p className="text-secondary-600 mb-4">
              {error?.message || "An error occurred while loading events."}
            </p>
            <Button variant="primary" onClick={() => window.location.reload()}>
              Try Again
            </Button>
          </div>
        </Card>
      </div>
    );
  }

  return (
    <div className="max-w-6xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
      <div className="mb-6">
        <h1 className="text-heading-2 text-secondary-900">
          Event Subscriptions
        </h1>
        <p className="mt-2 text-body text-secondary-600">
          Manage your event subscriptions and notification preferences.
        </p>
      </div>

      <Card>
        <div className="space-y-4">
          {/* Search and Actions */}
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
            <SearchInput
              value={searchTerm}
              onSearch={handleSearch}
              onClear={handleClear}
              placeholder="Search by system, event name, or description..."
              className="sm:max-w-xs"
            />
            <div className="text-body-small text-secondary-600">
              {filteredEvents.length} events available
            </div>
          </div>

          {/* Subscription Statistics */}
          <div className="flex gap-6 text-sm text-secondary-600 bg-secondary-50 px-4 py-3 rounded-lg">
            <div>
              <span className="font-medium text-secondary-900">
                {events.filter((e) => e.subscribed).length}
              </span>{" "}
              subscribed
            </div>
            <div>
              <span className="font-medium text-secondary-900">
                {events.filter((e) => !e.subscribed).length}
              </span>{" "}
              available
            </div>
            <div>
              <span className="font-medium text-secondary-900">
                {events.length}
              </span>{" "}
              total events
            </div>
          </div>

          {/* Subscriptions Table */}
          <SubscriptionsTable
            data={filteredEvents}
            emptyText={
              searchTerm
                ? "No events match your search criteria. Try adjusting your search terms."
                : "No events available at this time."
            }
            interactive={true}
            onSubscriptionToggle={handleSubscriptionToggle}
            loadingEventIds={loadingEventIds}
          />
        </div>
      </Card>
    </div>
  );
}
