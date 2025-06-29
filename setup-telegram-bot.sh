#!/bin/bash

echo "🤖 Telegram Bot Setup for VibeCodeDemo"
echo "======================================"
echo ""

# Check if .env file exists
if [ ! -f ".env" ]; then
    echo "❌ .env file not found!"
    exit 1
fi

echo "📝 Current .env file contents:"
echo "------------------------------"
cat .env
echo ""
echo "------------------------------"
echo ""

echo "🔧 To fix the Telegram bot, you need to:"
echo ""
echo "1. Create a Telegram Bot (if you haven't already):"
echo "   • Message @BotFather on Telegram: https://t.me/BotFather"
echo "   • Use the /newbot command"
echo "   • Choose a name and username for your bot"
echo "   • Copy the API token"
echo ""
echo "2. Add these lines to your .env file:"
echo ""
echo "# Backend Environment Variables"
echo "# Telegram Bot Configuration"
echo "TELEGRAM_BOT_TOKEN=your-actual-bot-token-from-botfather"
echo "TELEGRAM_BOT_USERNAME=your-actual-bot-username"
echo ""
echo "# JWT Configuration"
echo "JWT_SECRET=your-super-secret-jwt-key-change-this-in-production"
echo "JWT_EXPIRATION=86400000"
echo ""
echo "3. Restart the backend container:"
echo "   docker-compose restart backend"
echo ""
echo "4. Check the logs to verify the bot is registered:"
echo "   docker logs vibe_coding_demo-backend-1"
echo ""
echo "5. Test the bot by messaging it on Telegram!"
echo ""

# Check if bot credentials are configured
if grep -q "TELEGRAM_BOT_TOKEN=your-bot-token-here" .env 2>/dev/null || ! grep -q "TELEGRAM_BOT_TOKEN=" .env 2>/dev/null; then
    echo "⚠️  WARNING: Telegram bot token not configured or using default value"
    echo "   The bot will not work until you set a real token from @BotFather"
    echo ""
fi

if grep -q "TELEGRAM_BOT_USERNAME=your-bot-username-here" .env 2>/dev/null || ! grep -q "TELEGRAM_BOT_USERNAME=" .env 2>/dev/null; then
    echo "⚠️  WARNING: Telegram bot username not configured or using default value"
    echo "   The bot will not work until you set a real username from @BotFather"
    echo ""
fi

echo "💡 Need help? Check the backend/environment-template.txt file for examples" 