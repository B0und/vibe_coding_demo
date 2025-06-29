import { useState } from "react";
import { Button, Card, Modal, Table, Input } from "../components/ui";
import {
  useEvents,
  useCreateEvent,
  useUpdateEvent,
  useDeleteEvent,
} from "../hooks/useEvents";
import type { Event } from "../types";
import type { EventInput } from "../api/client";
import type { Column } from "../components/ui/Table";

interface EventFormData extends EventInput {
  id?: number;
}

export default function AdminPage() {
  // React Query hooks
  const { data: events = [], isLoading, error } = useEvents();
  const createEventMutation = useCreateEvent();
  const updateEventMutation = useUpdateEvent();
  const deleteEventMutation = useDeleteEvent();

  // Modal and form state
  const [isEventModalOpen, setIsEventModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [editingEvent, setEditingEvent] = useState<Event | null>(null);
  const [eventToDelete, setEventToDelete] = useState<Event | null>(null);

  // Form data state
  const [formData, setFormData] = useState<EventFormData>({
    systemName: "",
    eventName: "",
    kafkaTopic: "",
    description: "",
  });

  // Form validation errors
  const [formErrors, setFormErrors] = useState<Partial<EventFormData>>({});

  // Reset form to initial state
  const resetForm = () => {
    setFormData({
      systemName: "",
      eventName: "",
      kafkaTopic: "",
      description: "",
    });
    setFormErrors({});
    setEditingEvent(null);
  };

  // Open modal for creating new event
  const handleAddEvent = () => {
    resetForm();
    setIsEventModalOpen(true);
  };

  // Open modal for editing existing event
  const handleEditEvent = (event: Event) => {
    setEditingEvent(event);
    setFormData({
      id: event.id,
      systemName: event.systemName,
      eventName: event.eventName,
      kafkaTopic: event.kafkaTopic,
      description: event.description,
    });
    setFormErrors({});
    setIsEventModalOpen(true);
  };

  // Open delete confirmation modal
  const handleDeleteEvent = (event: Event) => {
    setEventToDelete(event);
    setIsDeleteModalOpen(true);
  };

  // Validate form data
  const validateForm = (): boolean => {
    const errors: Partial<EventFormData> = {};

    if (!formData.systemName.trim()) {
      errors.systemName = "System name is required";
    }

    if (!formData.eventName.trim()) {
      errors.eventName = "Event name is required";
    }

    if (!formData.kafkaTopic.trim()) {
      errors.kafkaTopic = "Kafka topic is required";
    }

    if (!formData.description.trim()) {
      errors.description = "Description is required";
    }

    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  // Handle form submission
  const handleSubmit = async () => {
    if (!validateForm()) return;

    try {
      if (editingEvent) {
        // Update existing event
        await updateEventMutation.mutateAsync({
          id: editingEvent.id,
          event: {
            systemName: formData.systemName.trim(),
            eventName: formData.eventName.trim(),
            kafkaTopic: formData.kafkaTopic.trim(),
            description: formData.description.trim(),
          },
        });
      } else {
        // Create new event
        await createEventMutation.mutateAsync({
          systemName: formData.systemName.trim(),
          eventName: formData.eventName.trim(),
          kafkaTopic: formData.kafkaTopic.trim(),
          description: formData.description.trim(),
        });
      }

      setIsEventModalOpen(false);
      resetForm();
    } catch (error) {
      console.error("Failed to save event:", error);
    }
  };

  // Handle delete confirmation
  const handleConfirmDelete = async () => {
    if (!eventToDelete) return;

    try {
      await deleteEventMutation.mutateAsync(eventToDelete.id);
      setIsDeleteModalOpen(false);
      setEventToDelete(null);
    } catch (error) {
      console.error("Failed to delete event:", error);
    }
  };

  // Handle form input changes
  const handleInputChange = (field: keyof EventFormData, value: string) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
    // Clear error when user starts typing
    if (formErrors[field]) {
      setFormErrors((prev) => ({ ...prev, [field]: undefined }));
    }
  };

  // Table columns configuration
  const columns: Column<Event>[] = [
    {
      key: "systemName",
      title: "System Name",
      width: "20%",
    },
    {
      key: "eventName",
      title: "Event Name",
      width: "25%",
    },
    {
      key: "kafkaTopic",
      title: "Kafka Topic",
      width: "20%",
    },
    {
      key: "description",
      title: "Description",
      width: "25%",
    },
    {
      key: "actions",
      title: "Actions",
      width: "10%",
      align: "center",
      render: (_, record) => (
        <div className="flex space-x-2 justify-center">
          <Button
            variant="secondary"
            size="sm"
            onClick={(e) => {
              e.stopPropagation();
              handleEditEvent(record);
            }}
          >
            Edit
          </Button>
          <Button
            variant="secondary"
            size="sm"
            onClick={(e) => {
              e.stopPropagation();
              handleDeleteEvent(record);
            }}
            className="text-red-600 hover:text-red-700 hover:bg-red-50"
          >
            Delete
          </Button>
        </div>
      ),
    },
  ];

  return (
    <div className="max-w-7xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
      <div className="mb-6">
        <h1 className="text-heading-2 text-secondary-900">Admin Panel</h1>
        <p className="mt-2 text-body text-secondary-600">
          Manage event definitions and system configuration.
        </p>
      </div>

      {/* Event Management Card */}
      <Card
        header={
          <div className="flex justify-between items-center">
            <h2 className="text-heading-4 text-secondary-900">
              Event Management
            </h2>
            <Button variant="primary" onClick={handleAddEvent}>
              Add Event
            </Button>
          </div>
        }
      >
        {error && (
          <div className="mb-4 p-4 bg-red-50 border border-red-200 rounded-md">
            <p className="text-sm text-red-600">
              Failed to load events:{" "}
              {error instanceof Error ? error.message : "Unknown error"}
            </p>
          </div>
        )}

        <Table
          data={events}
          columns={columns}
          loading={isLoading}
          emptyText="No events found. Click 'Add Event' to create your first event."
          size="md"
          bordered
        />
      </Card>

      {/* Event Form Modal */}
      <Modal
        isOpen={isEventModalOpen}
        onClose={() => setIsEventModalOpen(false)}
        title={editingEvent ? "Edit Event" : "Add New Event"}
        size="lg"
        footer={
          <div className="flex justify-end space-x-3">
            <Button
              variant="secondary"
              onClick={() => setIsEventModalOpen(false)}
              disabled={
                createEventMutation.isPending || updateEventMutation.isPending
              }
            >
              Cancel
            </Button>
            <Button
              variant="primary"
              onClick={handleSubmit}
              disabled={
                createEventMutation.isPending || updateEventMutation.isPending
              }
            >
              {createEventMutation.isPending || updateEventMutation.isPending
                ? "Saving..."
                : editingEvent
                ? "Update Event"
                : "Create Event"}
            </Button>
          </div>
        }
      >
        <div className="space-y-4">
          <Input
            label="System Name"
            value={formData.systemName}
            onChange={(e) => handleInputChange("systemName", e.target.value)}
            placeholder="e.g., user-service"
            error={formErrors.systemName}
            fullWidth
            required
          />

          <Input
            label="Event Name"
            value={formData.eventName}
            onChange={(e) => handleInputChange("eventName", e.target.value)}
            placeholder="e.g., user-registered"
            error={formErrors.eventName}
            fullWidth
            required
          />

          <Input
            label="Kafka Topic"
            value={formData.kafkaTopic}
            onChange={(e) => handleInputChange("kafkaTopic", e.target.value)}
            placeholder="e.g., user.events.registered"
            error={formErrors.kafkaTopic}
            fullWidth
            required
          />

          <div className="space-y-1">
            <label className="block text-sm font-medium text-secondary-700">
              Description <span className="text-red-500">*</span>
            </label>
            <textarea
              value={formData.description}
              onChange={(e) => handleInputChange("description", e.target.value)}
              placeholder="Describe what this event represents..."
              rows={3}
              className={`block w-full px-3 py-2 border rounded-md shadow-sm placeholder-secondary-400 focus:outline-none focus:ring-2 focus:ring-offset-0 transition-colors sm:text-sm ${
                formErrors.description
                  ? "border-error-300 text-error-900 focus:ring-error-500 focus:border-error-500"
                  : "border-secondary-300 text-secondary-900 focus:ring-primary-500 focus:border-primary-500"
              }`}
            />
            {formErrors.description && (
              <p className="text-sm text-error-600">{formErrors.description}</p>
            )}
          </div>
        </div>
      </Modal>

      {/* Delete Confirmation Modal */}
      <Modal
        isOpen={isDeleteModalOpen}
        onClose={() => setIsDeleteModalOpen(false)}
        title="Delete Event"
        size="md"
        footer={
          <div className="flex justify-end space-x-3">
            <Button
              variant="secondary"
              onClick={() => setIsDeleteModalOpen(false)}
              disabled={deleteEventMutation.isPending}
            >
              Cancel
            </Button>
            <Button
              variant="secondary"
              onClick={handleConfirmDelete}
              disabled={deleteEventMutation.isPending}
              className="text-red-600 hover:text-red-700 hover:bg-red-50 border-red-300"
            >
              {deleteEventMutation.isPending ? "Deleting..." : "Delete Event"}
            </Button>
          </div>
        }
      >
        {eventToDelete && (
          <div className="space-y-3">
            <p className="text-body text-secondary-600">
              Are you sure you want to delete this event? This action cannot be
              undone.
            </p>
            <div className="p-4 bg-secondary-50 rounded-md">
              <div className="space-y-1 text-sm">
                <p>
                  <strong>System:</strong> {eventToDelete.systemName}
                </p>
                <p>
                  <strong>Event:</strong> {eventToDelete.eventName}
                </p>
                <p>
                  <strong>Topic:</strong> {eventToDelete.kafkaTopic}
                </p>
              </div>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
}
