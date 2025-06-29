package com.vibecodingdemo.backend.service.impl;

import com.vibecodingdemo.backend.service.KafkaListenerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class KafkaListenerServiceStub implements KafkaListenerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaListenerServiceStub.class);

    @Override
    public boolean startListeningToTopic(String topic) {
        logger.info("KafkaListenerService stub: startListeningToTopic called for topic: {}", topic);
        return true;
    }

    @Override
    public boolean stopListeningToTopic(String topic) {
        logger.info("KafkaListenerService stub: stopListeningToTopic called for topic: {}", topic);
        return true;
    }

    @Override
    public void processMessage(String topic, String message) {
        logger.info("KafkaListenerService stub: processMessage called for topic: {} with message: {}", topic, message);
    }

    @Override
    public boolean isListeningToTopic(String topic) {
        logger.info("KafkaListenerService stub: isListeningToTopic called for topic: {}", topic);
        return false;
    }
} 