package com.vibecodingdemo.backend.config;

import com.fasterxml.jackson.core.JsonParseException;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerConfig.class);

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value("${spring.kafka.consumer.auto-offset-reset}")
    private String autoOffsetReset;

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        
        // Additional consumer configurations for reliability
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        configProps.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 1000);
        configProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        configProps.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 10000);
        
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        
        // Producer configurations for reliability
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        
        // Configure advanced error handling with Dead Letter Topic
        DefaultErrorHandler errorHandler = createAdvancedErrorHandler();
        factory.setCommonErrorHandler(errorHandler);
        
        // Set concurrency level (number of consumer threads per container)
        factory.setConcurrency(1);
        
        return factory;
    }

    /**
     * Create an advanced error handler with Dead Letter Topic support and retry logic.
     */
    private DefaultErrorHandler createAdvancedErrorHandler() {
        // Create Dead Letter Publishing Recoverer
        DeadLetterPublishingRecoverer deadLetterRecoverer = new DeadLetterPublishingRecoverer(
            kafkaTemplate(),
            (record, exception) -> {
                // Generate DLT topic name: original-topic.DLT
                String dltTopicName = record.topic() + ".DLT";
                logger.error("Publishing message to DLT topic '{}' due to exception: {}", 
                    dltTopicName, exception.getMessage());
                return new org.apache.kafka.common.TopicPartition(dltTopicName, 0);
            }
        );

        // Create exponential backoff with max retries
        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(3);
        backOff.setInitialInterval(1000L); // Start with 1 second
        backOff.setMultiplier(2.0); // Double the interval each time
        backOff.setMaxInterval(10000L); // Max 10 seconds between retries

        // Create the error handler
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(deadLetterRecoverer, backOff);

        // Configure which exceptions should not be retried (go directly to DLT)
        errorHandler.addNotRetryableExceptions(
            JsonParseException.class,
            IllegalArgumentException.class,
            NullPointerException.class
        );

        // Add logging for retry attempts
        errorHandler.setRetryListeners((record, ex, deliveryAttempt) -> {
            logger.warn("Retry attempt {} for message from topic '{}' due to: {}", 
                deliveryAttempt, record.topic(), ex.getMessage());
        });

        logger.info("Configured Kafka error handler with exponential backoff and DLT support");
        return errorHandler;
    }
} 