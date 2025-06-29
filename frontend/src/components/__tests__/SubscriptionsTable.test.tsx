import { render, screen, fireEvent } from "@testing-library/react";
import { vi, describe, it, expect, beforeEach } from "vitest";
import SubscriptionsTable from "../SubscriptionsTable";
import type { EventWithSubscription } from "../../types";

// Mock data
const mockEvents: EventWithSubscription[] = [
  {
    id: 1,
    systemName: "User Service",
    eventName: "User Created",
    description: "Triggered when a new user is created",
    kafkaTopic: "user.created",
    createdAt: "2024-01-01T00:00:00Z",
    updatedAt: "2024-01-01T00:00:00Z",
    subscribed: false,
  },
  {
    id: 2,
    systemName: "Order Service",
    eventName: "Order Placed",
    description: "Triggered when an order is placed",
    kafkaTopic: "order.placed",
    createdAt: "2024-01-01T00:00:00Z",
    updatedAt: "2024-01-01T00:00:00Z",
    subscribed: true,
  },
  {
    id: 3,
    systemName: "Payment Service",
    eventName: "Payment Processed",
    description: "Triggered when a payment is processed",
    kafkaTopic: "payment.processed",
    createdAt: "2024-01-01T00:00:00Z",
    updatedAt: "2024-01-01T00:00:00Z",
    subscribed: false,
  },
];

describe("SubscriptionsTable", () => {
  const defaultProps = {
    data: mockEvents,
    interactive: true,
    onSubscriptionToggle: vi.fn(),
    loadingEventIds: new Set<number>(),
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe("basic rendering", () => {
    it("should render all events with correct data", () => {
      render(<SubscriptionsTable {...defaultProps} />);

      // Check that all events are rendered
      expect(screen.getByText("User Created")).toBeInTheDocument();
      expect(screen.getByText("Order Placed")).toBeInTheDocument();
      expect(screen.getByText("Payment Processed")).toBeInTheDocument();

      // Check system names
      expect(screen.getByText("User Service")).toBeInTheDocument();
      expect(screen.getByText("Order Service")).toBeInTheDocument();
      expect(screen.getByText("Payment Service")).toBeInTheDocument();

      // Check descriptions
      expect(
        screen.getByText("Triggered when a new user is created")
      ).toBeInTheDocument();
      expect(
        screen.getByText("Triggered when an order is placed")
      ).toBeInTheDocument();
      expect(
        screen.getByText("Triggered when a payment is processed")
      ).toBeInTheDocument();
    });

    it("should render checkboxes with correct subscription states", () => {
      render(<SubscriptionsTable {...defaultProps} />);

      const checkboxes = screen.getAllByRole("checkbox");
      expect(checkboxes).toHaveLength(3);

      // Event 1: not subscribed
      expect(checkboxes[0]).not.toBeChecked();
      // Event 2: subscribed
      expect(checkboxes[1]).toBeChecked();
      // Event 3: not subscribed
      expect(checkboxes[2]).not.toBeChecked();
    });

    it("should render empty state when no data provided", () => {
      const emptyText = "No events available";
      render(
        <SubscriptionsTable {...defaultProps} data={[]} emptyText={emptyText} />
      );

      expect(screen.getByText(emptyText)).toBeInTheDocument();
    });
  });

  describe("interactive behavior", () => {
    it("should call onSubscriptionToggle when checkbox is clicked", () => {
      const mockToggle = vi.fn();
      render(
        <SubscriptionsTable
          {...defaultProps}
          onSubscriptionToggle={mockToggle}
        />
      );

      const checkboxes = screen.getAllByRole("checkbox");

      // Click first checkbox (unsubscribed -> subscribed)
      fireEvent.click(checkboxes[0]);
      expect(mockToggle).toHaveBeenCalledWith(1, true);

      // Click second checkbox (subscribed -> unsubscribed)
      fireEvent.click(checkboxes[1]);
      expect(mockToggle).toHaveBeenCalledWith(2, false);
    });

    it("should not call onSubscriptionToggle when interactive is false", () => {
      const mockToggle = vi.fn();
      render(
        <SubscriptionsTable
          {...defaultProps}
          interactive={false}
          onSubscriptionToggle={mockToggle}
        />
      );

      const checkboxes = screen.getAllByRole("checkbox");
      fireEvent.click(checkboxes[0]);

      expect(mockToggle).not.toHaveBeenCalled();
    });

    it("should disable checkboxes when interactive is false", () => {
      render(<SubscriptionsTable {...defaultProps} interactive={false} />);

      const checkboxes = screen.getAllByRole("checkbox");
      checkboxes.forEach((checkbox) => {
        expect(checkbox).toBeDisabled();
      });
    });
  });

  describe("loading states", () => {
    it("should show loading spinner for events in loading state", () => {
      const loadingEventIds = new Set([1, 3]);
      render(
        <SubscriptionsTable
          {...defaultProps}
          loadingEventIds={loadingEventIds}
        />
      );

      // Check for loading spinners (they have the animate-spin class)
      const spinners = document.querySelectorAll(".animate-spin");
      expect(spinners).toHaveLength(2);

      // Check that loading events have disabled checkboxes
      const checkboxes = screen.getAllByRole("checkbox");
      expect(checkboxes[0]).toBeDisabled(); // Event 1 is loading
      expect(checkboxes[1]).not.toBeDisabled(); // Event 2 is not loading
      expect(checkboxes[2]).toBeDisabled(); // Event 3 is loading
    });

    it("should apply opacity styles to loading rows", () => {
      const loadingEventIds = new Set([2]);
      render(
        <SubscriptionsTable
          {...defaultProps}
          loadingEventIds={loadingEventIds}
        />
      );

      // The loading row should have reduced opacity on text elements
      const orderServiceText = screen.getByText("Order Service");
      const orderPlacedText = screen.getByText("Order Placed");
      const orderDescText = screen.getByText(
        "Triggered when an order is placed"
      );

      expect(orderServiceText).toHaveClass("opacity-60");
      expect(orderPlacedText).toHaveClass("opacity-60");
      expect(orderDescText).toHaveClass("opacity-60");
    });

    it("should show correct aria-labels for loading checkboxes", () => {
      const loadingEventIds = new Set([1]);
      render(
        <SubscriptionsTable
          {...defaultProps}
          loadingEventIds={loadingEventIds}
        />
      );

      const loadingCheckbox = screen.getByLabelText(
        "Updating subscription for User Created"
      );
      expect(loadingCheckbox).toBeInTheDocument();
      expect(loadingCheckbox).toBeDisabled();
    });

    it("should not show loading spinner for non-loading events", () => {
      const loadingEventIds = new Set([1]);
      render(
        <SubscriptionsTable
          {...defaultProps}
          loadingEventIds={loadingEventIds}
        />
      );

      // Only one spinner should be present
      const spinners = document.querySelectorAll(".animate-spin");
      expect(spinners).toHaveLength(1);

      // Non-loading checkboxes should have normal aria-labels
      expect(
        screen.getByLabelText("Subscribe to Order Placed")
      ).toBeInTheDocument();
      expect(
        screen.getByLabelText("Subscribe to Payment Processed")
      ).toBeInTheDocument();
    });
  });

  describe("loading state behavior", () => {
    it("should handle mixed loading and non-loading states correctly", () => {
      const loadingEventIds = new Set([1, 3]);
      render(
        <SubscriptionsTable
          {...defaultProps}
          loadingEventIds={loadingEventIds}
        />
      );

      const checkboxes = screen.getAllByRole("checkbox");

      // Loading events should be disabled
      expect(checkboxes[0]).toBeDisabled();
      expect(checkboxes[2]).toBeDisabled();

      // Non-loading event should be enabled
      expect(checkboxes[1]).not.toBeDisabled();

      // Should have correct number of spinners
      const spinners = document.querySelectorAll(".animate-spin");
      expect(spinners).toHaveLength(2);
    });

    it("should handle empty loading state set", () => {
      render(
        <SubscriptionsTable {...defaultProps} loadingEventIds={new Set()} />
      );

      const checkboxes = screen.getAllByRole("checkbox");
      checkboxes.forEach((checkbox) => {
        expect(checkbox).not.toBeDisabled();
      });

      const spinners = document.querySelectorAll(".animate-spin");
      expect(spinners).toHaveLength(0);
    });

    it("should handle all events in loading state", () => {
      const loadingEventIds = new Set([1, 2, 3]);
      render(
        <SubscriptionsTable
          {...defaultProps}
          loadingEventIds={loadingEventIds}
        />
      );

      const checkboxes = screen.getAllByRole("checkbox");
      checkboxes.forEach((checkbox) => {
        expect(checkbox).toBeDisabled();
      });

      const spinners = document.querySelectorAll(".animate-spin");
      expect(spinners).toHaveLength(3);
    });
  });

  describe("accessibility", () => {
    it("should have proper aria-labels for checkboxes", () => {
      render(<SubscriptionsTable {...defaultProps} />);

      expect(
        screen.getByLabelText("Subscribe to User Created")
      ).toBeInTheDocument();
      expect(
        screen.getByLabelText("Subscribe to Order Placed")
      ).toBeInTheDocument();
      expect(
        screen.getByLabelText("Subscribe to Payment Processed")
      ).toBeInTheDocument();
    });

    it("should have proper table structure", () => {
      render(<SubscriptionsTable {...defaultProps} />);

      expect(screen.getByRole("table")).toBeInTheDocument();
      expect(screen.getAllByRole("columnheader")).toHaveLength(4);
      expect(screen.getAllByRole("row")).toHaveLength(4); // 1 header + 3 data rows
    });
  });
});
