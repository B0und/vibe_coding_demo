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
        logger.info("TelegramBotUpdateListener constructor called");
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
                        "👋 Hello! I'm the VibeCodeDemo Bot.\n\n" +
                        "🆔 <b>Your Chat ID:</b> <code>" + chatId + "</code>\n\n" +
                        "To activate notifications:\n" +
                        "• Use: <code>/start YOUR_ACTIVATION_CODE</code>\n" +
                        "• Get your activation code from the web application\n\n" +
                        "Commands: /start, /help");
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
            "🆔 <b>Your Chat ID:</b> <code>" + chatId + "</code>\n\n" +
            "<b>Commands:</b>\n" +
            "• <code>/start</code> - Show your Chat ID and activation instructions\n" +
            "• <code>/start &lt;code&gt;</code> - Activate notifications with your activation code\n" +
            "• <code>/help</code> - Show this help message\n\n" +
            "<b>How to get started:</b>\n" +
            "1. Log in to the web application\n" +
            "2. Go to your Profile page\n" +
            "3. Click \"Setup Bot\" to generate an activation code\n" +
            "4. Send <code>/start YOUR_CODE</code> to this bot\n" +
            "5. You'll receive event notifications here!\n\n" +
            "<b>Your Chat ID is:</b> <code>" + chatId + "</code>\n" +
            "Save this ID - you might need it for manual setup.\n\n" +
            "Need support? Contact the application administrator.";
        
        telegramBotService.sendMessage(chatId, helpMessage);
    }
} 