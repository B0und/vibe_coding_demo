import { useState, useEffect } from "react";
import { Button, Input, Card, Modal } from "../components/ui";
import { userProfileApi, type UserProfile } from "../api/client";
import { ApiClientError } from "../api/client";

export default function ProfilePage() {
  // State for user profile data
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // State for form inputs
  const [telegramRecipients, setTelegramRecipients] = useState("");

  // State for Telegram bot activation
  const [activationCode, setActivationCode] = useState("");
  const [chatId, setChatId] = useState("");
  const [generatedCode, setGeneratedCode] = useState("");

  // State for modals and UI
  const [isActivationModalOpen, setIsActivationModalOpen] = useState(false);
  const [isSuccessModalOpen, setIsSuccessModalOpen] = useState(false);
  const [successMessage, setSuccessMessage] = useState("");
  const [isGeneratingCode, setIsGeneratingCode] = useState(false);
  const [isActivating, setIsActivating] = useState(false);
  const [isSaving, setIsSaving] = useState(false);

  // Load user profile on component mount
  useEffect(() => {
    loadProfile();
  }, []);

  const loadProfile = async () => {
    try {
      setLoading(true);
      setError(null);
      const userProfile = await userProfileApi.getProfile();
      setProfile(userProfile);
      setTelegramRecipients(userProfile.telegramRecipients || "");
    } catch (err) {
      console.error("Failed to load profile:", err);
      setError(
        err instanceof ApiClientError ? err.message : "Failed to load profile"
      );
    } finally {
      setLoading(false);
    }
  };

  const handleSaveTelegramRecipients = async () => {
    if (!profile) return;

    try {
      setIsSaving(true);
      setError(null);

      await userProfileApi.updateTelegramRecipients(telegramRecipients);

      // Update local profile state
      setProfile({
        ...profile,
        telegramRecipients: telegramRecipients,
      });

      setSuccessMessage("Telegram recipients updated successfully!");
      setIsSuccessModalOpen(true);
    } catch (err) {
      console.error("Failed to update Telegram recipients:", err);
      setError(
        err instanceof ApiClientError
          ? err.message
          : "Failed to update Telegram recipients"
      );
    } finally {
      setIsSaving(false);
    }
  };

  const handleGenerateActivationCode = async () => {
    try {
      setIsGeneratingCode(true);
      setError(null);

      const response = await userProfileApi.generateTelegramActivationCode();
      setGeneratedCode(response.activationCode);
      setIsActivationModalOpen(true);
    } catch (err) {
      console.error("Failed to generate activation code:", err);
      setError(
        err instanceof ApiClientError
          ? err.message
          : "Failed to generate activation code"
      );
    } finally {
      setIsGeneratingCode(false);
    }
  };

  const handleActivateBot = async () => {
    if (!activationCode || !chatId) {
      setError("Please enter both activation code and chat ID");
      return;
    }

    try {
      setIsActivating(true);
      setError(null);

      await userProfileApi.activateTelegramBot(activationCode, chatId);

      // Reload profile to get updated telegram chat ID
      await loadProfile();

      setIsActivationModalOpen(false);
      setActivationCode("");
      setChatId("");
      setGeneratedCode("");

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
              <Button
                variant="secondary"
                onClick={handleGenerateActivationCode}
                disabled={isGeneratingCode}
              >
                {isGeneratingCode ? "Generating..." : "Setup Bot"}
              </Button>
              <Button
                variant="primary"
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
                <p className="text-sm text-blue-600 mt-1">
                  Click "Setup Bot" to generate an activation code and link your
                  Telegram account.
                </p>
              )}
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

      {/* Telegram Bot Activation Modal */}
      <Modal
        isOpen={isActivationModalOpen}
        onClose={() => setIsActivationModalOpen(false)}
        title="Activate Telegram Bot"
        size="md"
        footer={
          <div className="flex justify-end space-x-3">
            <Button
              variant="secondary"
              onClick={() => setIsActivationModalOpen(false)}
              disabled={isActivating}
            >
              Cancel
            </Button>
            <Button
              variant="primary"
              onClick={handleActivateBot}
              disabled={isActivating || !activationCode || !chatId}
            >
              {isActivating ? "Activating..." : "Activate"}
            </Button>
          </div>
        }
      >
        <div className="space-y-4">
          {generatedCode && (
            <div className="p-4 bg-green-50 border border-green-200 rounded-md">
              <p className="text-sm text-green-800 font-medium">
                Your activation code:{" "}
                <span className="font-mono text-lg">{generatedCode}</span>
              </p>
              <p className="text-sm text-green-600 mt-1">
                This code expires in 10 minutes. Use it in the Telegram bot.
              </p>
            </div>
          )}

          <div className="space-y-3">
            <p className="text-body text-secondary-600">
              To activate the Telegram bot:
            </p>
            <ol className="list-decimal list-inside text-sm text-secondary-600 space-y-1 ml-4">
              <li>Find and start a chat with the bot in Telegram</li>
              <li>
                Send this command to the bot:{" "}
                <code className="bg-secondary-100 px-1 rounded">
                  /start {generatedCode || "YOUR_CODE"}
                </code>
              </li>
              <li>The bot will respond with success and show your Chat ID</li>
              <li>Copy the Chat ID from the bot's response</li>
              <li>
                Enter both the activation code and Chat ID below, then click
                Activate
              </li>
            </ol>

            <div className="p-3 bg-blue-50 border border-blue-200 rounded-md">
              <p className="text-sm text-blue-800">
                <strong>💡 Tip:</strong> If you just send <code>/start</code>{" "}
                (without a code) or <code>/help</code> to the bot, it will show
                you your Chat ID and instructions.
              </p>
            </div>
          </div>

          <div className="space-y-3">
            <Input
              label="Activation Code"
              value={activationCode}
              onChange={(e) => setActivationCode(e.target.value)}
              placeholder="123456"
              maxLength={6}
              fullWidth
            />

            <Input
              label="Chat ID"
              value={chatId}
              onChange={(e) => setChatId(e.target.value)}
              placeholder="123456789"
              helperText="Copy this from the bot's response after sending /start command"
              fullWidth
            />
          </div>
        </div>
      </Modal>

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
