package com.vibecodingdemo.backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.vibecodingdemo.backend.service.UserService;
import com.vibecodingdemo.backend.service.EventService;
import com.vibecodingdemo.backend.service.SubscriptionService;
import com.vibecodingdemo.backend.service.KafkaListenerService;
import com.vibecodingdemo.backend.entity.User;
import com.vibecodingdemo.backend.dto.EventDTO;
import com.vibecodingdemo.backend.dto.EventResponseDTO;
import com.vibecodingdemo.backend.dto.SubscriptionDTO;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    private static final Logger logger = LoggerFactory.getLogger(DebugController.class);

    @Value("${telegram.bot.token:NOT_SET}")
    private String botToken;

    @Value("${telegram.bot.username:NOT_SET}")
    private String botUsername;

    private final UserService userService;
    private final EventService eventService;
    private final SubscriptionService subscriptionService;
    private final KafkaListenerService kafkaListenerService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public DebugController(UserService userService, EventService eventService, 
                          SubscriptionService subscriptionService, KafkaListenerService kafkaListenerService,
                          KafkaTemplate<String, String> kafkaTemplate) {
        this.userService = userService;
        this.eventService = eventService;
        this.subscriptionService = subscriptionService;
        this.kafkaListenerService = kafkaListenerService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @GetMapping("/telegram-config")
    public Map<String, Object> getTelegramConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("botTokenSet", !botToken.equals("NOT_SET") && !botToken.trim().isEmpty());
        config.put("botUsernameSet", !botUsername.equals("NOT_SET") && !botUsername.trim().isEmpty());
        config.put("botUsername", botUsername.equals("NOT_SET") ? null : botUsername);
        return config;
    }

    /**
     * Test endpoint to trigger a Telegram notification via the full Kafka pipeline
     * This creates a test event, subscribes the current user, sends a message to Kafka,
     * and lets the Kafka consumer process it and send the Telegram notification
     */
    @PostMapping("/test-notification")
    public ResponseEntity<Map<String, Object>> testNotification(Authentication authentication) {
        try {
            // For debug endpoint, use authentication if available, otherwise use default test user
            String username;
            if (authentication != null && authentication.isAuthenticated()) {
                username = authentication.getName();
            } else {
                // Use default test user for debugging - find any user with Telegram configured
                List<User> allUsers = userService.getAllUsers();
                Optional<User> testUser = allUsers.stream()
                    .filter(u -> u.getTelegramChatId() != null && !u.getTelegramChatId().trim().isEmpty())
                    .findFirst();
                
                if (testUser.isEmpty()) {
                    return ResponseEntity.badRequest()
                        .body(Map.of("error", "No users found with Telegram configured. Please activate Telegram for at least one user."));
                }
                username = testUser.get().getUsername();
                logger.info("Using test user {} for debug notification (no authentication provided)", username);
            }
            logger.info("Testing notification via Kafka pipeline for user: {}", username);

            // Get the current user
            Optional<User> userOpt = userService.findByUsername(username);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "User not found: " + username));
            }
            User user = userOpt.get();

            // Check if user has Telegram configured
            if (user.getTelegramChatId() == null || user.getTelegramChatId().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "error", "Telegram bot not activated", 
                        "message", "Please activate your Telegram bot first by sending '/start <activation-code>' to the bot"
                    ));
            }

            // Create or get test event
            EventResponseDTO testEvent = createTestEvent();
            
            // Subscribe user to test event if not already subscribed
            ensureUserSubscribed(user, testEvent);

            // Ensure Kafka is listening to the test topic
            ensureKafkaListening(testEvent.getKafkaTopic());

            // Create and send test message to Kafka
            String testMessage = createKafkaTestMessage();
            
            logger.info("Sending test message to Kafka topic: {}", testEvent.getKafkaTopic());
            kafkaTemplate.send(testEvent.getKafkaTopic(), testMessage);

            logger.info("Test message sent to Kafka successfully for user: {}", username);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Test message sent to Kafka! Check your Telegram in a few seconds.",
                "event", testEvent.getEventName(),
                "kafkaTopic", testEvent.getKafkaTopic(),
                "telegramChatId", user.getTelegramChatId(),
                "flow", "Message sent to Kafka → Consumer processes → Telegram notification"
            ));

        } catch (Exception e) {
            logger.error("Error in test notification endpoint: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "error", "Internal server error: " + e.getMessage()
                ));
        }
    }

    /**
     * Create or get the test event
     */
    private EventResponseDTO createTestEvent() {
        String testEventName = "test-notification";
        String testSystemName = "vibe-demo";
        String testKafkaTopic = "test.notifications";

        // Try to find existing test event
        try {
            List<EventResponseDTO> events = eventService.getAllEvents();
            for (EventResponseDTO eventDTO : events) {
                if (testEventName.equals(eventDTO.getEventName()) && 
                    testSystemName.equals(eventDTO.getSystemName())) {
                    return eventDTO;
                }
            }
        } catch (Exception e) {
            logger.warn("Error checking for existing test event: {}", e.getMessage());
        }

        // Create new test event
        try {
            EventDTO newEventDTO = new EventDTO(
                testSystemName,
                testEventName,
                testKafkaTopic,
                "Test event for Kafka notification testing - created automatically"
            );
            
            EventResponseDTO createdEvent = eventService.createEvent(newEventDTO);
            logger.info("Created new test event with ID: {}", createdEvent.getId());
            return createdEvent;
        } catch (Exception e) {
            logger.error("Failed to create test event: {}", e.getMessage(), e);
            throw new RuntimeException("Could not create test event", e);
        }
    }

    /**
     * Ensure user is subscribed to the test event
     */
    private void ensureUserSubscribed(User user, EventResponseDTO testEvent) {
        try {
            // Check if already subscribed
            List<SubscriptionDTO> userSubscriptions = subscriptionService.getUserSubscriptions(user.getUsername());
            boolean alreadySubscribed = userSubscriptions.stream()
                .anyMatch(sub -> sub.getEventId().equals(testEvent.getId()));

            if (!alreadySubscribed) {
                subscriptionService.subscribeToEvent(user.getUsername(), testEvent.getId());
                logger.info("Subscribed user {} to test event {}", user.getUsername(), testEvent.getEventName());
            } else {
                logger.info("User {} already subscribed to test event {}", user.getUsername(), testEvent.getEventName());
            }
        } catch (Exception e) {
            logger.error("Error ensuring user subscription: {}", e.getMessage(), e);
            // Don't throw here, just log - we can still send the notification
        }
    }

    /**
     * Ensure Kafka is listening to the test topic
     */
    private void ensureKafkaListening(String topic) {
        try {
            if (!kafkaListenerService.isListeningToTopic(topic)) {
                boolean started = kafkaListenerService.startListeningToTopic(topic);
                if (started) {
                    logger.info("Started Kafka listener for topic: {}", topic);
                } else {
                    logger.warn("Failed to start Kafka listener for topic: {}", topic);
                }
            } else {
                logger.info("Kafka already listening to topic: {}", topic);
            }
        } catch (Exception e) {
            logger.error("Error ensuring Kafka listener: {}", e.getMessage(), e);
        }
    }

    /**
     * Create a test message in JSON format for Kafka
     */
    private String createKafkaTestMessage() {
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        
        return "{\n" +
               "  \"event\": \"test-notification\",\n" +
               "  \"system\": \"vibe-demo\",\n" +
               "  \"timestamp\": \"" + timestamp + "\",\n" +
               "  \"title\": \"Test Notification\",\n" +
               "  \"description\": \"This is a test notification from VibeCodeDemo!\",\n" +
               "  \"message\": \"Testing the complete Kafka → Consumer → Telegram notification flow\",\n" +
               "  \"severity\": \"info\",\n" +
               "  \"data\": {\n" +
               "    \"test\": true,\n" +
               "    \"source\": \"debug-controller\",\n" +
               "    \"purpose\": \"End-to-end testing\"\n" +
               "  }\n" +
               "}";
    }
} 