package com.vibecodingdemo.backend.service;

/**
 * Service interface for managing dynamic Kafka listeners.
 * Provides methods to start and stop listeners for specific topics programmatically.
 */
public interface KafkaListenerService {

    /**
     * Start a Kafka consumer for the specified topic.
     * Creates and registers a new listener container that will process messages from the topic.
     *
     * @param topic the Kafka topic to start listening to
     * @return true if the listener was successfully started, false if already listening to this topic
     */
    boolean startListeningToTopic(String topic);

    /**
     * Stop the Kafka consumer for the specified topic.
     * Stops and unregisters the listener container for the topic.
     *
     * @param topic the Kafka topic to stop listening to
     * @return true if the listener was successfully stopped, false if not listening to this topic
     */
    boolean stopListeningToTopic(String topic);

    /**
     * Process a message received from a Kafka topic.
     * This method is called by the dynamic listeners when a message is received.
     *
     * @param topic the topic the message was received from
     * @param message the message content
     */
    void processMessage(String topic, String message);

    /**
     * Check if the service is currently listening to a specific topic.
     *
     * @param topic the topic to check
     * @return true if actively listening to the topic, false otherwise
     */
    boolean isListeningToTopic(String topic);
} 