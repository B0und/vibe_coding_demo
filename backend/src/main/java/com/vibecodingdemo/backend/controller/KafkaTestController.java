package com.vibecodingdemo.backend.controller;

import com.vibecodingdemo.backend.service.KafkaListenerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Temporary test controller for testing Kafka dynamic listener functionality.
 * This will be removed once the Kafka consumer implementation is complete.
 */
@RestController
@RequestMapping("/api/kafka-test")
public class KafkaTestController {

    private final KafkaListenerService kafkaListenerService;

    @Autowired
    public KafkaTestController(KafkaListenerService kafkaListenerService) {
        this.kafkaListenerService = kafkaListenerService;
    }

    @PostMapping("/start-listener/{topic}")
    public ResponseEntity<Map<String, Object>> startListener(@PathVariable String topic) {
        boolean started = kafkaListenerService.startListeningToTopic(topic);
        return ResponseEntity.ok(Map.of(
            "success", started,
            "message", started ? "Started listening to topic: " + topic : "Failed to start or already listening to topic: " + topic,
            "topic", topic
        ));
    }

    @PostMapping("/stop-listener/{topic}")
    public ResponseEntity<Map<String, Object>> stopListener(@PathVariable String topic) {
        boolean stopped = kafkaListenerService.stopListeningToTopic(topic);
        return ResponseEntity.ok(Map.of(
            "success", stopped,
            "message", stopped ? "Stopped listening to topic: " + topic : "Failed to stop or not listening to topic: " + topic,
            "topic", topic
        ));
    }

    @GetMapping("/is-listening/{topic}")
    public ResponseEntity<Map<String, Object>> isListening(@PathVariable String topic) {
        boolean listening = kafkaListenerService.isListeningToTopic(topic);
        return ResponseEntity.ok(Map.of(
            "listening", listening,
            "topic", topic
        ));
    }
} 