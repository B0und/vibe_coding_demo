import { Table, Checkbox, type Column } from "./ui";
import type { EventWithSubscription } from "../types";

interface SubscriptionsTableProps {
  data: EventWithSubscription[];
  isLoading?: boolean;
  emptyText?: string;
  onSubscriptionToggle?: (eventId: number, subscribed: boolean) => void;
  interactive?: boolean;
  loadingEventIds?: Set<number>; // Track which events are currently being updated
}

export default function SubscriptionsTable({
  data,
  isLoading = false,
  emptyText = "No events available at this time.",
  onSubscriptionToggle,
  interactive = false,
  loadingEventIds = new Set(),
}: SubscriptionsTableProps) {
  // Define table columns
  const columns: Column<EventWithSubscription>[] = [
    {
      key: "subscribed",
      title: "Subscribed",
      align: "center",
      width: "100px",
      render: (subscribed: boolean, record: EventWithSubscription) => {
        const isEventLoading = loadingEventIds.has(record.id);

        return (
          <div className="flex items-center justify-center">
            {isEventLoading ? (
              <div className="flex items-center space-x-2">
                <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-primary-600"></div>
                <Checkbox
                  checked={subscribed}
                  disabled={true}
                  aria-label={`Updating subscription for ${record.eventName}`}
                  className="opacity-50"
                />
              </div>
            ) : (
              <Checkbox
                checked={subscribed}
                disabled={!interactive}
                onChange={(e) => {
                  if (interactive && onSubscriptionToggle) {
                    onSubscriptionToggle(record.id, e.target.checked);
                  }
                }}
                aria-label={`Subscribe to ${record.eventName}`}
              />
            )}
          </div>
        );
      },
    },
    {
      key: "systemName",
      title: "System",
      render: (systemName: string, record: EventWithSubscription) => {
        const isEventLoading = loadingEventIds.has(record.id);
        return (
          <span
            className={`font-medium text-secondary-900 ${
              isEventLoading ? "opacity-60" : ""
            }`}
          >
            {systemName}
          </span>
        );
      },
    },
    {
      key: "eventName",
      title: "Event Name",
      render: (eventName: string, record: EventWithSubscription) => {
        const isEventLoading = loadingEventIds.has(record.id);
        return (
          <span
            className={`font-medium text-secondary-900 ${
              isEventLoading ? "opacity-60" : ""
            }`}
          >
            {eventName}
          </span>
        );
      },
    },
    {
      key: "description",
      title: "Description",
      render: (description: string, record: EventWithSubscription) => {
        const isEventLoading = loadingEventIds.has(record.id);
        return (
          <span
            className={`text-secondary-700 ${
              isEventLoading ? "opacity-60" : ""
            }`}
          >
            {description}
          </span>
        );
      },
    },
  ];

  return (
    <Table
      data={data}
      columns={columns}
      loading={isLoading}
      emptyText={emptyText}
      size="md"
      striped={false}
      bordered={true}
    />
  );
}
