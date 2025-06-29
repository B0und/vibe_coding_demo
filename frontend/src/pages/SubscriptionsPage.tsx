import { useState } from "react";
import {
  Button,
  SearchInput,
  Table,
  Card,
  type Column,
} from "../components/ui";

// Mock data for demonstration
interface Subscription {
  id: string;
  eventTitle: string;
  eventDate: string;
  location: string;
  status: "active" | "cancelled";
  subscribedAt: string;
}

const mockSubscriptions: Subscription[] = [
  {
    id: "1",
    eventTitle: "React Conference 2024",
    eventDate: "2024-03-15",
    location: "San Francisco, CA",
    status: "active",
    subscribedAt: "2024-01-10",
  },
  {
    id: "2",
    eventTitle: "TypeScript Workshop",
    eventDate: "2024-02-20",
    location: "Online",
    status: "active",
    subscribedAt: "2024-01-15",
  },
  {
    id: "3",
    eventTitle: "JavaScript Meetup",
    eventDate: "2024-01-25",
    location: "New York, NY",
    status: "cancelled",
    subscribedAt: "2024-01-05",
  },
];

export default function SubscriptionsPage() {
  const [searchTerm, setSearchTerm] = useState("");
  const [filteredData, setFilteredData] = useState(mockSubscriptions);

  const handleSearch = (value: string) => {
    setSearchTerm(value);
    const filtered = mockSubscriptions.filter(
      (subscription) =>
        subscription.eventTitle.toLowerCase().includes(value.toLowerCase()) ||
        subscription.location.toLowerCase().includes(value.toLowerCase())
    );
    setFilteredData(filtered);
  };

  const handleClear = () => {
    setSearchTerm("");
    setFilteredData(mockSubscriptions);
  };

  const columns: Column<Subscription>[] = [
    {
      key: "eventTitle",
      title: "Event",
      render: (value, record) => (
        <div>
          <div className="font-medium text-secondary-900">{value}</div>
          <div className="text-sm text-secondary-500">{record.location}</div>
        </div>
      ),
    },
    {
      key: "eventDate",
      title: "Date",
      render: (value) => new Date(value).toLocaleDateString(),
    },
    {
      key: "status",
      title: "Status",
      align: "center",
      render: (value) => (
        <span
          className={`inline-flex px-2 py-1 text-xs font-medium rounded-full ${
            value === "active"
              ? "bg-success-100 text-success-800"
              : "bg-error-100 text-error-800"
          }`}
        >
          {value.charAt(0).toUpperCase() + value.slice(1)}
        </span>
      ),
    },
    {
      key: "subscribedAt",
      title: "Subscribed",
      render: (value) => new Date(value).toLocaleDateString(),
    },
    {
      key: "actions",
      title: "Actions",
      align: "right",
      render: (_, record) => (
        <div className="flex space-x-2">
          <Button variant="secondary" size="sm">
            View
          </Button>
          {record.status === "active" && (
            <Button variant="tertiary" size="sm">
              Unsubscribe
            </Button>
          )}
        </div>
      ),
    },
  ];

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
              placeholder="Search events or locations..."
              className="sm:max-w-xs"
            />
            <Button variant="primary">Browse Events</Button>
          </div>

          {/* Results count */}
          <div className="text-body-small text-secondary-600">
            Showing {filteredData.length} of {mockSubscriptions.length}{" "}
            subscriptions
          </div>

          {/* Table */}
          <Table
            data={filteredData}
            columns={columns}
            emptyText="No subscriptions found. Try adjusting your search or browse available events."
            onRowClick={(record) => console.log("Clicked:", record)}
          />
        </div>
      </Card>
    </div>
  );
}
