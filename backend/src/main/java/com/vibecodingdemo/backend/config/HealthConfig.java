package com.vibecodingdemo.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuator.health.Health;
import org.springframework.boot.actuator.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;

@Configuration
public class HealthConfig {

    private static final Logger logger = LoggerFactory.getLogger(HealthConfig.class);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${telegram.bot.token:your-bot-token-here}")
    private String telegramBotToken;

    @Bean
    public HealthIndicator databaseHealthIndicator() {
        return new DatabaseHealthIndicator(dataSource);
    }

    @Bean
    public HealthIndicator kafkaHealthIndicator() {
        return new KafkaHealthIndicator(kafkaTemplate);
    }

    @Bean
    public HealthIndicator telegramHealthIndicator() {
        return new TelegramHealthIndicator(telegramBotToken);
    }

    /**
     * Custom health indicator for database connectivity
     */
    public static class DatabaseHealthIndicator implements HealthIndicator {
        
        private final DataSource dataSource;
        private static final Logger logger = LoggerFactory.getLogger(DatabaseHealthIndicator.class);

        public DatabaseHealthIndicator(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        @Override
        public Health health() {
            try {
                Instant start = Instant.now();
                
                try (Connection connection = dataSource.getConnection()) {
                    // Simple query to test connectivity
                    connection.createStatement().execute("SELECT 1");
                    
                    Duration responseTime = Duration.between(start, Instant.now());
                    
                    return Health.up()
                            .withDetail("database", "PostgreSQL")
                            .withDetail("responseTime", responseTime.toMillis() + "ms")
                            .withDetail("status", "Connected")
                            .build();
                }
                
            } catch (SQLException e) {
                logger.error("Database health check failed", e);
                return Health.down()
                        .withDetail("database", "PostgreSQL")
                        .withDetail("error", e.getMessage())
                        .withDetail("status", "Connection failed")
                        .build();
            }
        }
    }

    /**
     * Custom health indicator for Kafka connectivity
     */
    public static class KafkaHealthIndicator implements HealthIndicator {
        
        private final KafkaTemplate<String, String> kafkaTemplate;
        private static final Logger logger = LoggerFactory.getLogger(KafkaHealthIndicator.class);

        public KafkaHealthIndicator(KafkaTemplate<String, String> kafkaTemplate) {
            this.kafkaTemplate = kafkaTemplate;
        }

        @Override
        public Health health() {
            try {
                Instant start = Instant.now();
                
                // Test Kafka connectivity by getting metadata
                var metadata = kafkaTemplate.getProducerFactory()
                        .createProducer()
                        .partitionsFor("health-check-topic");
                
                Duration responseTime = Duration.between(start, Instant.now());
                
                return Health.up()
                        .withDetail("kafka", "Apache Kafka")
                        .withDetail("responseTime", responseTime.toMillis() + "ms")
                        .withDetail("status", "Connected")
                        .withDetail("brokers", kafkaTemplate.getProducerFactory().getConfigurationProperties().get("bootstrap.servers"))
                        .build();
                        
            } catch (Exception e) {
                logger.error("Kafka health check failed", e);
                return Health.down()
                        .withDetail("kafka", "Apache Kafka")
                        .withDetail("error", e.getMessage())
                        .withDetail("status", "Connection failed")
                        .build();
            }
        }
    }

    /**
     * Custom health indicator for Telegram Bot API
     */
    public static class TelegramHealthIndicator implements HealthIndicator {
        
        private final String telegramBotToken;
        private static final Logger logger = LoggerFactory.getLogger(TelegramHealthIndicator.class);

        public TelegramHealthIndicator(String telegramBotToken) {
            this.telegramBotToken = telegramBotToken;
        }

        @Override
        public Health health() {
            // Skip check if token is not configured
            if ("your-bot-token-here".equals(telegramBotToken) || telegramBotToken.isEmpty()) {
                return Health.up()
                        .withDetail("telegram", "Telegram Bot API")
                        .withDetail("status", "Not configured")
                        .withDetail("note", "Bot token not set")
                        .build();
            }

            try {
                Instant start = Instant.now();
                
                // Simple HTTP call to Telegram API to check connectivity
                RestTemplate restTemplate = new RestTemplate();
                String url = "https://api.telegram.org/bot" + telegramBotToken + "/getMe";
                
                var response = restTemplate.getForEntity(url, String.class);
                Duration responseTime = Duration.between(start, Instant.now());
                
                if (response.getStatusCode().is2xxSuccessful()) {
                    return Health.up()
                            .withDetail("telegram", "Telegram Bot API")
                            .withDetail("responseTime", responseTime.toMillis() + "ms")
                            .withDetail("status", "Connected")
                            .withDetail("httpStatus", response.getStatusCode().value())
                            .build();
                } else {
                    return Health.down()
                            .withDetail("telegram", "Telegram Bot API")
                            .withDetail("status", "API Error")
                            .withDetail("httpStatus", response.getStatusCode().value())
                            .build();
                }
                
            } catch (Exception e) {
                logger.error("Telegram health check failed", e);
                return Health.down()
                        .withDetail("telegram", "Telegram Bot API")
                        .withDetail("error", e.getMessage())
                        .withDetail("status", "Connection failed")
                        .build();
            }
        }
    }
} 