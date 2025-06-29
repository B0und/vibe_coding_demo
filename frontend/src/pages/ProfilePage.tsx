import { useState } from "react";
import { Button, Input, Card, Modal } from "../components/ui";

export default function ProfilePage() {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [telegramUsername, setTelegramUsername] = useState("");

  const handleSave = () => {
    // Mock save functionality
    setIsModalOpen(true);
  };

  return (
    <div className="max-w-4xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
      <div className="mb-6">
        <h1 className="text-heading-2 text-secondary-900">User Profile</h1>
        <p className="mt-2 text-body text-secondary-600">
          Manage your account settings and preferences.
        </p>
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        {/* Personal Information Card */}
        <Card
          header={
            <h2 className="text-heading-4 text-secondary-900">
              Personal Information
            </h2>
          }
          footer={
            <div className="flex justify-end">
              <Button variant="primary" onClick={handleSave}>
                Save Changes
              </Button>
            </div>
          }
        >
          <div className="space-y-4">
            <Input
              label="Full Name"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="Enter your full name"
              fullWidth
            />
            <Input
              label="Email Address"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="Enter your email"
              fullWidth
            />
            <Input
              label="Telegram Username"
              value={telegramUsername}
              onChange={(e) => setTelegramUsername(e.target.value)}
              placeholder="@username"
              helperText="Used for receiving event notifications"
              fullWidth
            />
          </div>
        </Card>

        {/* Account Settings Card */}
        <Card
          header={
            <h2 className="text-heading-4 text-secondary-900">
              Account Settings
            </h2>
          }
        >
          <div className="space-y-4">
            <div className="flex items-center justify-between py-2">
              <div>
                <p className="text-body font-medium text-secondary-900">
                  Email Notifications
                </p>
                <p className="text-body-small text-secondary-500">
                  Receive email updates about your subscriptions
                </p>
              </div>
              <Button variant="secondary" size="sm">
                Configure
              </Button>
            </div>

            <div className="flex items-center justify-between py-2">
              <div>
                <p className="text-body font-medium text-secondary-900">
                  Telegram Notifications
                </p>
                <p className="text-body-small text-secondary-500">
                  Get instant notifications via Telegram
                </p>
              </div>
              <Button variant="secondary" size="sm">
                Setup
              </Button>
            </div>

            <div className="flex items-center justify-between py-2">
              <div>
                <p className="text-body font-medium text-secondary-900">
                  Privacy Settings
                </p>
                <p className="text-body-small text-secondary-500">
                  Manage your data and privacy preferences
                </p>
              </div>
              <Button variant="secondary" size="sm">
                Manage
              </Button>
            </div>
          </div>
        </Card>
      </div>

      {/* Success Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title="Profile Updated"
        size="sm"
        footer={
          <div className="flex justify-end space-x-3">
            <Button variant="secondary" onClick={() => setIsModalOpen(false)}>
              Close
            </Button>
          </div>
        }
      >
        <p className="text-body text-secondary-600">
          Your profile has been successfully updated. Changes will take effect
          immediately.
        </p>
      </Modal>
    </div>
  );
}
