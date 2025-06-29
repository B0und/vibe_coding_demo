package com.vibecodingdemo.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class EventDTO {
    
    @NotBlank(message = "System name is required")
    private String systemName;
    
    @NotBlank(message = "Event name is required")
    private String eventName;
    
    @NotBlank(message = "Kafka topic is required")
    private String kafkaTopic;
    
    @NotNull(message = "Description is required")
    private String description;
    
    // Default constructor
    public EventDTO() {}
    
    // Constructor with all fields
    public EventDTO(String systemName, String eventName, String kafkaTopic, String description) {
        this.systemName = systemName;
        this.eventName = eventName;
        this.kafkaTopic = kafkaTopic;
        this.description = description;
    }
    
    // Getters and Setters
    public String getSystemName() {
        return systemName;
    }
    
    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }
    
    public String getEventName() {
        return eventName;
    }
    
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }
    
    public String getKafkaTopic() {
        return kafkaTopic;
    }
    
    public void setKafkaTopic(String kafkaTopic) {
        this.kafkaTopic = kafkaTopic;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    @Override
    public String toString() {
        return "EventDTO{" +
                "systemName='" + systemName + '\'' +
                ", eventName='" + eventName + '\'' +
                ", kafkaTopic='" + kafkaTopic + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
} 