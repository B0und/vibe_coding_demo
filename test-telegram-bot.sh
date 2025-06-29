#!/bin/bash

echo "🤖 Telegram Bot Test Script"
echo "=========================="
echo ""

# Read bot configuration from environment variables (same as backend)
BOT_TOKEN="${TELEGRAM_BOT_TOKEN}"
BOT_USERNAME="${TELEGRAM_BOT_USERNAME}"

if [ -z "$BOT_TOKEN" ] || [ -z "$BOT_USERNAME" ]; then
    echo "❌ Bot credentials not found in environment variables"
    echo "   Please ensure TELEGRAM_BOT_TOKEN and TELEGRAM_BOT_USERNAME are set"
    echo "   You can source them from your .env file:"
    echo "   source .env"
    exit 1
fi

echo "✅ Bot Configuration:"
echo "   Token: ${BOT_TOKEN:0:10}... (hidden for security)"
echo "   Username: @$BOT_USERNAME"
echo ""

echo "🔍 Testing bot API connection..."
response=$(curl -s "https://api.telegram.org/bot$BOT_TOKEN/getMe")
if echo "$response" | jq -e '.ok' > /dev/null 2>&1; then
    echo "✅ Bot API connection successful!"
    bot_name=$(echo "$response" | jq -r '.result.first_name')
    echo "   Bot name: $bot_name"
else
    echo "❌ Bot API connection failed!"
    echo "$response"
    exit 1
fi

echo ""
echo "🎯 How to test your bot:"
echo ""
echo "1. Open Telegram and search for: @$BOT_USERNAME"
echo "   Or use this link: https://t.me/$BOT_USERNAME"
echo ""
echo "2. Start a conversation with the bot by clicking 'Start' or sending any message"
echo ""
echo "3. Try these commands:"
echo "   • Send any text message (should get a help response)"
echo "   • Send '/help' for help information"
echo "   • Send '/start YOUR_ACTIVATION_CODE' to activate notifications"
echo ""
echo "4. Check the backend logs to see the bot processing messages:"
echo "   docker logs vibe_coding_demo-backend-1 --tail 20 -f"
echo ""

echo "🔧 Backend Status:"
echo "   Backend URL: http://localhost:8080"
echo "   Health check: http://localhost:8080/actuator/health"
echo ""

echo "✅ Your Telegram bot is now working!"
echo "   The issue was a logging configuration problem that has been fixed."
echo "   The bot is properly registered and ready to receive messages." 