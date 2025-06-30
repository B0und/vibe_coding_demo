package com.vibecodingdemo.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

@Service
public class TelegramBotService {

    private static final Logger logger = LoggerFactory.getLogger(TelegramBotService.class);
    
    @Value("${telegram.bot.token}")
    private String botToken;
    
    @Value("${telegram.bot.username}")
    private String botUsername;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final UserService userService;
    
    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot";
    
    public TelegramBotService(UserService userService) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.userService = userService;
        logger.info("TelegramBotService constructor called");
    }
    
    /**
     * Send a message to a Telegram chat
     * @param chatId the chat ID to send the message to
     * @param message the message content (can be JSON string or plain text)
     * @return true if message was sent successfully, false otherwise
     */
    @Retryable(
        value = {Exception.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2.0)
    )
    public boolean sendMessage(String chatId, String message) {
        try {
            logger.info("Attempting to send message to chat ID: {}", chatId);
            
            // Format message if it's JSON
            String formattedMessage = formatMessage(message);
            
            // Prepare the request
            String url = TELEGRAM_API_URL + botToken + "/sendMessage";
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("chat_id", chatId);
            requestBody.put("text", formattedMessage);
            requestBody.put("parse_mode", "HTML");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            // Send the request
            ResponseEntity<String> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                request, 
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Message sent successfully to chat ID: {}", chatId);
                return true;
            } else {
                logger.error("Failed to send message. Status: {}, Response: {}", 
                    response.getStatusCode(), response.getBody());
                return false;
            }
            
        } catch (Exception e) {
            logger.error("Error sending message to chat ID {}: {}", chatId, e.getMessage(), e);
            throw e; // Re-throw to trigger retry mechanism
        }
    }
    
    /**
     * Handle the /start command - show chat ID and simple activation instructions
     * @param chatId the chat ID of the user
     * @param text the full command text (e.g., "/start" or "/start 123456")
     */
    public void handleStartCommand(String chatId, String text) {
        try {
            logger.info("Handling start command from chat ID: {}", chatId);
            
            // Always show the welcome message with chat ID
            sendMessage(chatId, 
                "👋 <b>Welcome to VibeCodeDemo Bot!</b>\n\n" +
                "🆔 <b>Your Chat ID:</b> <code>" + chatId + "</code>\n\n" +
                "📝 <b>To activate notifications:</b>\n" +
                "1. Copy your Chat ID above: <code>" + chatId + "</code>\n" +
                "2. Go to the web app Profile page\n" +
                "3. Paste your Chat ID in the activation field\n" +
                "4. Click 'Activate Bot'\n\n" +
                "🔔 Once activated, you'll receive event notifications here!\n\n" +
                "ℹ️ Need help? Use /help");
            
            logger.info("Start command handled successfully for chat ID: {}", chatId);
            
        } catch (Exception e) {
            logger.error("Error handling start command for chat ID {}: {}", chatId, e.getMessage(), e);
            sendMessage(chatId, 
                "❌ An error occurred while processing your request.\n\n" +
                "🆔 <b>Your Chat ID:</b> <code>" + chatId + "</code>\n\n" +
                "Please try again or contact support.");
        }
    }
    
    /**
     * Format a message - if it's JSON, convert to human-readable format
     * @param message the original message
     * @return formatted message
     */
    private String formatMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return message;
        }
        
        try {
            // Try to parse as JSON
            JsonNode jsonNode = objectMapper.readTree(message);
            
            // If successful, format as human-readable
            StringBuilder formatted = new StringBuilder();
            formatted.append("🔔 <b>Notification</b>\n\n");
            
            formatJsonNode(jsonNode, formatted, "");
            
            return formatted.toString();
            
        } catch (Exception e) {
            // Not JSON or parsing failed, return as-is
            return message;
        }
    }
    
    /**
     * Recursively format JSON node to human-readable format
     * @param node the JSON node to format
     * @param sb the StringBuilder to append to
     * @param prefix the prefix for indentation
     */
    private void formatJsonNode(JsonNode node, StringBuilder sb, String prefix) {
        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                JsonNode value = entry.getValue();
                
                // Use safe HTML formatting - don't create HTML tags from field names
                sb.append(prefix).append("<b>").append(escapeHtml(key)).append(":</b> ");
                
                if (value.isValueNode()) {
                    sb.append(escapeHtml(value.asText())).append("\n");
                } else {
                    sb.append("\n");
                    formatJsonNode(value, sb, prefix + "  ");
                }
            });
        } else if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                sb.append(prefix).append("• ");
                JsonNode item = node.get(i);
                if (item.isValueNode()) {
                    sb.append(escapeHtml(item.asText())).append("\n");
                } else {
                    sb.append("\n");
                    formatJsonNode(item, sb, prefix + "  ");
                }
            }
        }
    }
    
    /**
     * Escape HTML characters to prevent issues with Telegram's HTML parser
     * @param text the text to escape
     * @return escaped text
     */
    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#x27;");
    }
    
    /**
     * Get the bot username
     * @return the bot username
     */
    public String getBotUsername() {
        return botUsername;
    }
    
    /**
     * Get the bot token (for internal use only)
     * @return the bot token
     */
    protected String getBotToken() {
        return botToken;
    }
} 