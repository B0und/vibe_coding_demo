import { useState, useEffect } from "react";
import { Button, Input, Card, Modal } from "../components/ui";
import { userProfileApi, debugApi, type UserProfile } from "../api/client";
import { ApiClientError } from "../api/client";

export default function ProfilePage() {
  // State for user profile data
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // State for form inputs
  const [telegramRecipients, setTelegramRecipients] = useState("");

  // State for Telegram bot activation
  const [chatId, setChatId] = useState("");
  const [isActivating, setIsActivating] = useState(false);

  // State for UI
  const [isSaving, setIsSaving] = useState(false);

  // State for success feedback
  const [successMessage, setSuccessMessage] = useState("");
  const [isSuccessModalOpen, setIsSuccessModalOpen] = useState(false);

  // State for test notification
  const [isTestingNotification, setIsTestingNotification] = useState(false);

  // Load user profile on component mount
  useEffect(() => {
    loadProfile();
  }, []);

  const loadProfile = async () => {
    try {
      setLoading(true);
      setError(null);

      const data = await userProfileApi.getProfile();
      setProfile(data);
      setTelegramRecipients(data.telegramRecipients || "");
    } catch (err) {
      console.error("Failed to load profile:", err);
      setError(
        err instanceof ApiClientError
          ? err.message
          : "Failed to load profile data"
      );
    } finally {
      setLoading(false);
    }
  };

  const handleSaveTelegramRecipients = async () => {
    try {
      setIsSaving(true);
      setError(null);

      await userProfileApi.updateTelegramRecipients(telegramRecipients);

      // Reload profile to get updated data
      await loadProfile();

      setSuccessMessage("Telegram recipients updated successfully!");
      setIsSuccessModalOpen(true);
    } catch (err) {
      console.error("Failed to update telegram recipients:", err);
      setError(
        err instanceof ApiClientError
          ? err.message
          : "Failed to update telegram recipients"
      );
    } finally {
      setIsSaving(false);
    }
  };

  const handleActivateBot = async () => {
    if (!chatId) {
      setError("Please enter a valid Chat ID");
      return;
    }

    try {
      setIsActivating(true);
      setError(null);

      // Call API with just the chat ID
      await userProfileApi.activateTelegramBot(chatId);

      // Reload profile to get updated telegram chat ID
      await loadProfile();

      setChatId("");

      setSuccessMessage("Telegram bot activated successfully!");
      setIsSuccessModalOpen(true);
    } catch (err) {
      console.error("Failed to activate Telegram bot:", err);
      setError(
        err instanceof ApiClientError
          ? err.message
          : "Failed to activate Telegram bot"
      );
    } finally {
      setIsActivating(false);
    }
  };

  const handleTestNotification = async () => {
    try {
      setIsTestingNotification(true);
      setError(null);

      const response = await debugApi.testNotification();

      if (response.success) {
        setSuccessMessage(response.message);
        setIsSuccessModalOpen(true);
      } else {
        setError(response.error || "Failed to send test notification");
      }
    } catch (err) {
      console.error("Failed to send test notification:", err);
      if (err instanceof ApiClientError) {
        setError(err.message);
      } else {
        setError("Failed to send test notification");
      }
    } finally {
      setIsTestingNotification(false);
    }
  };

  if (loading) {
    return (
      <div className="max-w-4xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
        <div className="flex justify-center items-center h-64">
          <div className="text-body text-secondary-600">Loading profile...</div>
        </div>
      </div>
    );
  }

  if (!profile) {
    return (
      <div className="max-w-4xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
        <div className="flex justify-center items-center h-64">
          <div className="text-body text-red-600">Failed to load profile</div>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
      <div className="mb-6">
        <h1 className="text-heading-2 text-secondary-900">User Profile</h1>
        <p className="mt-2 text-body text-secondary-600">
          Manage your account settings and Telegram notification preferences.
        </p>
      </div>

      {error && (
        <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-md">
          <p className="text-sm text-red-600">{error}</p>
        </div>
      )}

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        {/* Profile Information Card */}
        <Card
          header={
            <h2 className="text-heading-4 text-secondary-900">
              Profile Information
            </h2>
          }
        >
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-secondary-700 mb-1">
                Username
              </label>
              <div className="p-3 bg-secondary-50 border border-secondary-200 rounded-md text-body text-secondary-900">
                {profile.username}
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-secondary-700 mb-1">
                Role
              </label>
              <div className="p-3 bg-secondary-50 border border-secondary-200 rounded-md text-body text-secondary-900">
                {profile.role}
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-secondary-700 mb-1">
                Member Since
              </label>
              <div className="p-3 bg-secondary-50 border border-secondary-200 rounded-md text-body text-secondary-900">
                {new Date(profile.createdAt).toLocaleDateString()}
              </div>
            </div>
          </div>
        </Card>

        {/* Telegram Configuration Card */}
        <Card
          header={
            <h2 className="text-heading-4 text-secondary-900">
              Telegram Configuration
            </h2>
          }
          footer={
            <div className="flex justify-end space-x-3">
              {!profile.telegramChatId && (
                <Button
                  variant="primary"
                  onClick={handleActivateBot}
                  disabled={isActivating || !chatId.trim()}
                >
                  {isActivating ? "Activating..." : "Activate Bot"}
                </Button>
              )}
              <Button
                variant="secondary"
                onClick={handleSaveTelegramRecipients}
                disabled={isSaving}
              >
                {isSaving ? "Saving..." : "Save Recipients"}
              </Button>
            </div>
          }
        >
          <div className="space-y-4">
            <Input
              label="Telegram Recipients"
              value={telegramRecipients}
              onChange={(e) => setTelegramRecipients(e.target.value)}
              placeholder="user1;user2;@username"
              helperText="Semicolon-separated list of Telegram usernames or chat IDs"
              fullWidth
            />

            <div className="p-3 bg-blue-50 border border-blue-200 rounded-md">
              <p className="text-sm text-blue-800">
                <strong>Bot Status:</strong>{" "}
                {profile.telegramChatId
                  ? `✅ Activated (Chat ID: ${profile.telegramChatId})`
                  : "❌ Not activated"}
              </p>
              {!profile.telegramChatId && (
                <div className="text-sm text-blue-600 mt-2 space-y-2">
                  <p>
                    <strong>Simple Setup:</strong>
                  </p>
                  <p>
                    1. Go to Telegram and message the bot: <code>/start</code>
                  </p>
                  <p>2. Copy your Chat ID from the bot's response</p>
                  <p>3. Paste it below and click "Activate Bot"</p>
                </div>
              )}
            </div>

            {/* Simple Chat ID Input - Only show if not activated */}
            {!profile.telegramChatId && (
              <div className="p-3 bg-yellow-50 border border-yellow-200 rounded-md">
                <Input
                  label="Telegram Chat ID"
                  value={chatId}
                  onChange={(e) => setChatId(e.target.value)}
                  placeholder="Paste your Chat ID here (e.g., 123456789)"
                  helperText="Get this by messaging the bot with /start"
                  fullWidth
                />
              </div>
            )}

            {/* Test Notification Section - Always visible now */}
            <div className="p-3 bg-green-50 border border-green-200 rounded-md">
              <p className="text-sm text-green-800 font-medium mb-2">
                🧪 Test Telegram Integration (Full Kafka Pipeline)
              </p>
              <p className="text-sm text-green-600 mb-3">
                {profile.telegramChatId
                  ? "Send a test message through: Frontend → Backend → Kafka → Consumer → Telegram"
                  : "⚠️ Telegram not activated yet, but you can still test the Kafka pipeline (no Telegram message will be sent)"}
              </p>
              <Button
                variant="secondary"
                size="sm"
                onClick={handleTestNotification}
                disabled={isTestingNotification}
                className={
                  profile.telegramChatId
                    ? "bg-green-100 text-green-700 border-green-300 hover:bg-green-200"
                    : "bg-yellow-100 text-yellow-700 border-yellow-300 hover:bg-yellow-200"
                }
              >
                {isTestingNotification
                  ? "🚀 Sending via Kafka..."
                  : `🚀 Test Kafka Pipeline${
                      !profile.telegramChatId ? " (No Telegram)" : ""
                    }`}
              </Button>
            </div>
          </div>
        </Card>
      </div>

      {/* Account Statistics Card */}
      <div className="mt-6">
        <Card
          header={
            <h2 className="text-heading-4 text-secondary-900">
              Account Statistics
            </h2>
          }
        >
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="text-center p-4 bg-secondary-50 rounded-lg">
              <div className="text-2xl font-bold text-primary-600">
                {profile.role === "ADMIN" ? "∞" : "?"}
              </div>
              <div className="text-sm text-secondary-600">Events Created</div>
            </div>

            <div className="text-center p-4 bg-secondary-50 rounded-lg">
              <div className="text-2xl font-bold text-green-600">?</div>
              <div className="text-sm text-secondary-600">
                Active Subscriptions
              </div>
            </div>

            <div className="text-center p-4 bg-secondary-50 rounded-lg">
              <div className="text-2xl font-bold text-blue-600">?</div>
              <div className="text-sm text-secondary-600">
                Notifications Sent
              </div>
            </div>
          </div>
        </Card>
      </div>

      {/* Success Modal */}
      <Modal
        isOpen={isSuccessModalOpen}
        onClose={() => setIsSuccessModalOpen(false)}
        title="Success"
        size="sm"
        footer={
          <div className="flex justify-end">
            <Button
              variant="primary"
              onClick={() => setIsSuccessModalOpen(false)}
            >
              Close
            </Button>
          </div>
        }
      >
        <p className="text-body text-secondary-600">{successMessage}</p>
      </Modal>
    </div>
  );
}
