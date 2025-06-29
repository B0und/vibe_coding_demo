package com.vibecodingdemo.backend.config;

import com.vibecodingdemo.backend.service.TelegramBotUpdateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import jakarta.annotation.PostConstruct;

@Configuration
public class TelegramBotConfig {

    private static final Logger logger = LoggerFactory.getLogger(TelegramBotConfig.class);

    @Value("${telegram.bot.token:your-bot-token-here}")
    private String botToken;

    @Value("${telegram.bot.username:your-bot-username-here}")
    private String botUsername;

    private final TelegramBotUpdateListener telegramBotUpdateListener;

    public TelegramBotConfig(TelegramBotUpdateListener telegramBotUpdateListener) {
        this.telegramBotUpdateListener = telegramBotUpdateListener;
        logger.info("TelegramBotConfig constructor called");
    }

    @PostConstruct
    public void registerBot() {
        logger.info("registerBot() called with token: '{}' and username: '{}'", botToken, botUsername);
        // Only register the bot if valid credentials are provided
        if (!"your-bot-token-here".equals(botToken) && 
            !"your-bot-username-here".equals(botUsername) && 
            !botToken.trim().isEmpty() && 
            !botUsername.trim().isEmpty()) {
            
            try {
                logger.info("Registering Telegram bot: {}", botUsername);
                TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
                botsApi.registerBot(telegramBotUpdateListener);
                logger.info("Telegram bot '{}' registered successfully", botUsername);
            } catch (TelegramApiException e) {
                logger.error("Failed to register Telegram bot '{}': {}", botUsername, e.getMessage(), e);
            }
        } else {
            logger.warn("Telegram bot not registered - invalid or missing configuration. " +
                       "Please set TELEGRAM_BOT_TOKEN and TELEGRAM_BOT_USERNAME environment variables.");
        }
    }

    /**
     * Create TelegramBotsApi bean only if bot is properly configured
     */
    @Bean
    @ConditionalOnProperty(name = "telegram.bot.enabled", havingValue = "true", matchIfMissing = false)
    public TelegramBotsApi telegramBotsApi() throws TelegramApiException {
        return new TelegramBotsApi(DefaultBotSession.class);
    }
} 