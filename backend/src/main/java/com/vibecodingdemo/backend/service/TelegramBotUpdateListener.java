package com.vibecodingdemo.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class TelegramBotUpdateListener extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(TelegramBotUpdateListener.class);
    
    @Value("${telegram.bot.token}")
    private String botToken;
    
    @Value("${telegram.bot.username}")
    private String botUsername;
    
    private final TelegramBotService telegramBotService;
    
    public TelegramBotUpdateListener(TelegramBotService telegramBotService) {
        this.telegramBotService = telegramBotService;
    }
    
    @Override
    public String getBotToken() {
        return botToken;
    }
    
    @Override
    public String getBotUsername() {
        return botUsername;
    }
    
    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage()) {
                Message message = update.getMessage();
                String chatId = message.getChatId().toString();
                String text = message.getText();
                
                logger.info("Received message from chat ID {}: {}", chatId, text);
                
                // Handle different types of messages
                if (text != null && text.startsWith("/start")) {
                    telegramBotService.handleStartCommand(chatId, text);
                } else if (text != null && text.equals("/help")) {
                    handleHelpCommand(chatId);
                } else {
                    // For any other message, provide guidance
                    telegramBotService.sendMessage(chatId, 
                        "👋 Hello! To activate notifications, use: /start <activation_code>\n" +
                        "Get your activation code from the web application.\n\n" +
                        "Need help? Use /help for more information.");
                }
            }
        } catch (Exception e) {
            logger.error("Error processing update: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Handle the /help command
     * @param chatId the chat ID to send help message to
     */
    private void handleHelpCommand(String chatId) {
        String helpMessage = 
            "🤖 <b>VibeCodeDemo Bot Help</b>\n\n" +
            "<b>Commands:</b>\n" +
            "• /start &lt;code&gt; - Activate notifications with your activation code\n" +
            "• /help - Show this help message\n\n" +
            "<b>How to get started:</b>\n" +
            "1. Log in to the web application\n" +
            "2. Generate an activation code\n" +
            "3. Send /start &lt;your_code&gt; to this bot\n" +
            "4. You'll receive event notifications here!\n\n" +
            "Need support? Contact the application administrator.";
        
        telegramBotService.sendMessage(chatId, helpMessage);
    }
} 