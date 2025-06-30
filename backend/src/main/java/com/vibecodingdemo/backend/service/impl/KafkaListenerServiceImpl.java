package com.vibecodingdemo.backend.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibecodingdemo.backend.dto.KafkaMessageDTO;
import com.vibecodingdemo.backend.entity.Event;
import com.vibecodingdemo.backend.entity.Subscription;
import com.vibecodingdemo.backend.entity.User;
import com.vibecodingdemo.backend.repository.EventRepository;
import com.vibecodingdemo.backend.repository.SubscriptionRepository;
import com.vibecodingdemo.backend.service.KafkaListenerService;
import com.vibecodingdemo.backend.service.TelegramBotService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.TopicPartitionOffset;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class KafkaListenerServiceImpl implements KafkaListenerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaListenerServiceImpl.class);

    private final KafkaListenerEndpointRegistry endpointRegistry;
    private final ConcurrentKafkaListenerContainerFactory<String, String> containerFactory;
    private final EventRepository eventRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final TelegramBotService telegramBotService;
    private final ObjectMapper objectMapper;
    
    // Keep track of active containers by topic
    private final ConcurrentMap<String, ConcurrentMessageListenerContainer<String, String>> topicToContainerMap = new ConcurrentHashMap<>();

    /**
     * Simple data holder for user notification information
     * to avoid Hibernate lazy loading issues
     */
    private static class UserNotificationData {
        private final String username;
        private final String telegramChatId;
        private final String telegramRecipients;
        
        public UserNotificationData(String username, String telegramChatId, String telegramRecipients) {
            this.username = username;
            this.telegramChatId = telegramChatId;
            this.telegramRecipients = telegramRecipients;
        }
        
        public String getUsername() { return username; }
        public String getTelegramChatId() { return telegramChatId; }
        public String getTelegramRecipients() { return telegramRecipients; }
    }

    @Autowired
    public KafkaListenerServiceImpl(
            KafkaListenerEndpointRegistry endpointRegistry,
            ConcurrentKafkaListenerContainerFactory<String, String> containerFactory,
            EventRepository eventRepository,
            SubscriptionRepository subscriptionRepository,
            TelegramBotService telegramBotService) {
        this.endpointRegistry = endpointRegistry;
        this.containerFactory = containerFactory;
        this.eventRepository = eventRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.telegramBotService = telegramBotService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public boolean startListeningToTopic(String topic) {
        if (topic == null || topic.trim().isEmpty()) {
            logger.warn("Cannot start listening to null or empty topic");
            return false;
        }

        try {
            // Create a new message listener container
            ConcurrentMessageListenerContainer<String, String> container = 
                containerFactory.createContainer(topic);
            
            // Set the container ID for tracking
            container.setBeanName(generateContainerId(topic));
            
            // Set up the message listener
            container.setupMessageListener(new MessageListener<String, String>() {
                @Override
                public void onMessage(ConsumerRecord<String, String> record) {
                    try {
                        logger.debug("Received message from topic {}: {}", record.topic(), record.value());
                        processMessage(record.topic(), record.value());
                    } catch (Exception e) {
                        logger.error("Error processing message from topic {}: {}", record.topic(), e.getMessage(), e);
                        throw e; // Re-throw to trigger error handler
                    }
                }
            });

            // Start the container directly
            container.start();
            
            // Track the container
            topicToContainerMap.put(topic, container);
            
            logger.info("Successfully started listening to topic: {}", topic);
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to start listening to topic {}: {}", topic, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean stopListeningToTopic(String topic) {
        if (topic == null || topic.trim().isEmpty()) {
            logger.warn("Cannot stop listening to null or empty topic");
            return false;
        }

        ConcurrentMessageListenerContainer<String, String> container = topicToContainerMap.get(topic);
        if (container == null) {
            logger.info("Not currently listening to topic: {}", topic);
            return false;
        }

        try {
            // Stop the container
            container.stop();
            
            logger.info("Successfully stopped listening to topic: {}", topic);
            
            // Remove from tracking map
            topicToContainerMap.remove(topic);
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to stop listening to topic {}: {}", topic, e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processMessage(String topic, String message) {
        logger.info("Processing message from topic '{}': {}", topic, message);
        
        try {
            // 1. Parse the JSON message
            KafkaMessageDTO kafkaMessage = parseMessage(message);
            logger.debug("Parsed Kafka message: {}", kafkaMessage);
            
            // 2. Find the event associated with this topic
            Optional<Event> eventOpt = eventRepository.findByKafkaTopic(topic);
            if (eventOpt.isEmpty()) {
                logger.warn("No event found for topic '{}'. Skipping message processing.", topic);
                return;
            }
            
            Event event = eventOpt.get();
            logger.debug("Found event: {} for topic: {}", event.getId(), topic);
            
            // 3. Find all subscribed users for this event (with users eagerly fetched)
            List<Subscription> subscriptions = subscriptionRepository.findByEventIdWithUsers(event.getId());
            if (subscriptions.isEmpty()) {
                logger.info("No subscribers found for event '{}' (topic: {}). Skipping notifications.", 
                    event.getEventName(), topic);
                return;
            }
            
            logger.info("Found {} subscribers for event '{}' (topic: {})", 
                subscriptions.size(), event.getEventName(), topic);
            
            // 4. Format the message for Telegram
            String formattedMessage = formatMessageForTelegram(kafkaMessage, event);
            
            // 5. Extract user data and send notifications (to avoid Hibernate lazy loading issues)
            int successCount = 0;
            int failureCount = 0;
            
            // Extract user data within transaction to avoid lazy loading issues
            List<UserNotificationData> userDataList = new ArrayList<>();
            for (Subscription subscription : subscriptions) {
                User user = subscription.getUser();
                
                // Extract user data into simple objects within transaction
                String username = user.getUsername();
                String telegramChatId = user.getTelegramChatId();
                String telegramRecipients = user.getTelegramRecipients();
                
                logger.debug("Extracted notification data for user: {} (chatId: {}, recipients: {})", 
                    username, telegramChatId, telegramRecipients);
                
                userDataList.add(new UserNotificationData(username, telegramChatId, telegramRecipients));
            }
            
            // Send notifications using extracted data (no Hibernate entities involved)
            for (UserNotificationData userData : userDataList) {
                boolean notificationSent = sendNotificationToUser(userData, formattedMessage);
                
                if (notificationSent) {
                    successCount++;
                } else {
                    failureCount++;
                }
            }
            
            logger.info("Notification processing completed for topic '{}'. Success: {}, Failures: {}", 
                topic, successCount, failureCount);
            
        } catch (Exception e) {
            logger.error("Error processing message from topic '{}': {}", topic, e.getMessage(), e);
            throw new RuntimeException("Failed to process Kafka message", e);
        }
    }

    @Override
    public boolean isListeningToTopic(String topic) {
        return topicToContainerMap.containsKey(topic);
    }

    /**
     * Parse the incoming message as JSON.
     * If parsing fails, create a basic message object with the raw content.
     * 
     * @param message the raw message string
     * @return parsed KafkaMessageDTO
     */
    private KafkaMessageDTO parseMessage(String message) {
        try {
            return objectMapper.readValue(message, KafkaMessageDTO.class);
        } catch (Exception e) {
            logger.warn("Failed to parse message as JSON, creating basic message object: {}", e.getMessage());
            
            // Create a basic message object with the raw content
            KafkaMessageDTO basicMessage = new KafkaMessageDTO();
            basicMessage.setMessage(message);
            basicMessage.setTimestamp(LocalDateTime.now());
            return basicMessage;
        }
    }
    
    /**
     * Format the Kafka message for Telegram notification.
     * 
     * @param kafkaMessage the parsed Kafka message
     * @param event the event associated with the message
     * @return formatted message for Telegram
     */
    private String formatMessageForTelegram(KafkaMessageDTO kafkaMessage, Event event) {
        StringBuilder formatted = new StringBuilder();
        
        // Add notification header with event information
        formatted.append("🔔 <b>Event Notification</b>\n\n");
        formatted.append("📊 <b>System:</b> ").append(event.getSystemName()).append("\n");
        formatted.append("📋 <b>Event:</b> ").append(event.getEventName()).append("\n");
        
        // Add timestamp if available
        if (kafkaMessage.getTimestamp() != null) {
            formatted.append("⏰ <b>Time:</b> ").append(kafkaMessage.getTimestamp()).append("\n");
        }
        
        formatted.append("\n");
        
        // Add message content
        if (kafkaMessage.getTitle() != null && !kafkaMessage.getTitle().trim().isEmpty()) {
            formatted.append("📌 <b>Title:</b> ").append(kafkaMessage.getTitle()).append("\n");
        }
        
        if (kafkaMessage.getDescription() != null && !kafkaMessage.getDescription().trim().isEmpty()) {
            formatted.append("📝 <b>Description:</b> ").append(kafkaMessage.getDescription()).append("\n");
        }
        
        if (kafkaMessage.getMessage() != null && !kafkaMessage.getMessage().trim().isEmpty()) {
            formatted.append("💬 <b>Message:</b> ").append(kafkaMessage.getMessage()).append("\n");
        }
        
        // Add severity if available
        if (kafkaMessage.getSeverity() != null && !kafkaMessage.getSeverity().trim().isEmpty()) {
            String severityEmoji = getSeverityEmoji(kafkaMessage.getSeverity());
            formatted.append("⚠️ <b>Severity:</b> ").append(severityEmoji).append(" ").append(kafkaMessage.getSeverity()).append("\n");
        }
        
        // Add additional data if available
        if (kafkaMessage.getData() != null && !kafkaMessage.getData().isEmpty()) {
            formatted.append("\n📊 <b>Additional Data:</b>\n");
            kafkaMessage.getData().forEach((key, value) -> {
                formatted.append("  • <b>").append(key).append(":</b> ").append(value).append("\n");
            });
        }
        
        return formatted.toString();
    }
    
    /**
     * Get emoji for severity level.
     * 
     * @param severity the severity level
     * @return appropriate emoji
     */
    private String getSeverityEmoji(String severity) {
        if (severity == null) return "ℹ️";
        
        switch (severity.toLowerCase()) {
            case "critical":
            case "error":
                return "🔴";
            case "warning":
            case "warn":
                return "🟡";
            case "info":
            case "information":
                return "🔵";
            case "success":
                return "🟢";
            default:
                return "ℹ️";
        }
    }
    
    /**
     * Send notification to a specific user via their configured Telegram recipients.
     * 
     * @param userData the user data to send notification to
     * @param message the formatted message
     * @return true if at least one notification was sent successfully, false otherwise
     */
    private boolean sendNotificationToUser(UserNotificationData userData, String message) {
        boolean atLeastOneSuccess = false;
        
        try {
            // Check if user has telegram chat ID (bot activation)
            if (userData.getTelegramChatId() != null && !userData.getTelegramChatId().trim().isEmpty()) {
                boolean sent = telegramBotService.sendMessage(userData.getTelegramChatId(), message);
                if (sent) {
                    atLeastOneSuccess = true;
                    logger.debug("Notification sent to user '{}' via bot chat ID: {}", 
                        userData.getUsername(), userData.getTelegramChatId());
                } else {
                    logger.warn("Failed to send notification to user '{}' via bot chat ID: {}", 
                        userData.getUsername(), userData.getTelegramChatId());
                }
            }
            
            // Also send to additional telegram recipients if configured
            if (userData.getTelegramRecipients() != null && !userData.getTelegramRecipients().trim().isEmpty()) {
                String[] recipients = userData.getTelegramRecipients().split(";");
                for (String recipient : recipients) {
                    recipient = recipient.trim();
                    if (!recipient.isEmpty()) {
                        boolean sent = telegramBotService.sendMessage(recipient, message);
                        if (sent) {
                            atLeastOneSuccess = true;
                            logger.debug("Notification sent to user '{}' via recipient: {}", 
                                userData.getUsername(), recipient);
                        } else {
                            logger.warn("Failed to send notification to user '{}' via recipient: {}", 
                                userData.getUsername(), recipient);
                        }
                    }
                }
            }
            
            if (!atLeastOneSuccess) {
                logger.warn("No telegram recipients configured for user '{}' or all sends failed", 
                    userData.getUsername());
            }
            
        } catch (Exception e) {
            logger.error("Error sending notification to user '{}': {}", userData.getUsername(), e.getMessage(), e);
        }
        
        return atLeastOneSuccess;
    }

    /**
     * Generate a unique container ID for a topic.
     * 
     * @param topic the topic name
     * @return a unique container ID
     */
    private String generateContainerId(String topic) {
        return "dynamic-listener-" + topic + "-" + System.currentTimeMillis();
    }
} 