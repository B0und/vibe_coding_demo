package com.vibecodingdemo.backend.service;

import com.vibecodingdemo.backend.entity.Event;
import com.vibecodingdemo.backend.repository.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Component that initializes Kafka consumers for all existing event topics
 * when the application starts up.
 */
@Component
public class KafkaStartupInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger logger = LoggerFactory.getLogger(KafkaStartupInitializer.class);

    private final EventRepository eventRepository;
    private final KafkaListenerService kafkaListenerService;

    @Autowired
    public KafkaStartupInitializer(EventRepository eventRepository, KafkaListenerService kafkaListenerService) {
        this.eventRepository = eventRepository;
        this.kafkaListenerService = kafkaListenerService;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        logger.info("Application ready - initializing Kafka consumers for existing event topics");
        
        try {
            // Fetch all active events from the database
            List<Event> events = eventRepository.findAll();
            
            if (events.isEmpty()) {
                logger.info("No events found in database - no Kafka consumers to initialize");
                return;
            }
            
            logger.info("Found {} events in database, starting Kafka consumers...", events.size());
            
            int successCount = 0;
            int failureCount = 0;
            
            for (Event eventEntity : events) {
                String kafkaTopic = eventEntity.getKafkaTopic();
                
                if (kafkaTopic == null || kafkaTopic.trim().isEmpty()) {
                    logger.warn("Event '{}' (ID: {}) has no Kafka topic configured - skipping", 
                        eventEntity.getEventName(), eventEntity.getId());
                    failureCount++;
                    continue;
                }
                
                try {
                    boolean started = kafkaListenerService.startListeningToTopic(kafkaTopic);
                    
                    if (started) {
                        logger.info("✅ Started Kafka consumer for event '{}' (topic: '{}')", 
                            eventEntity.getEventName(), kafkaTopic);
                        successCount++;
                    } else {
                        logger.warn("⚠️ Failed to start or already listening to topic '{}' for event '{}'", 
                            kafkaTopic, eventEntity.getEventName());
                        failureCount++;
                    }
                    
                } catch (Exception e) {
                    logger.error("❌ Error starting Kafka consumer for event '{}' (topic: '{}'): {}", 
                        eventEntity.getEventName(), kafkaTopic, e.getMessage(), e);
                    failureCount++;
                }
            }
            
            logger.info("Kafka consumer initialization completed - Success: {}, Failures: {}", 
                successCount, failureCount);
            
            if (failureCount > 0) {
                logger.warn("Some Kafka consumers failed to initialize. Check logs for details.");
            } else {
                logger.info("🎉 All Kafka consumers initialized successfully!");
            }
            
        } catch (Exception e) {
            logger.error("Critical error during Kafka consumer initialization: {}", e.getMessage(), e);
        }
    }
} 