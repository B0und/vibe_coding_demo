package com.vibecodingdemo.backend.dto;

import java.time.LocalDateTime;

public class EventResponseDTO {
    
    private Long id;
    private String systemName;
    private String eventName;
    private String kafkaTopic;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Default constructor
    public EventResponseDTO() {}
    
    // Constructor with all fields
    public EventResponseDTO(Long id, String systemName, String eventName, String kafkaTopic, 
                           String description, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.systemName = systemName;
        this.eventName = eventName;
        this.kafkaTopic = kafkaTopic;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
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
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return "EventResponseDTO{" +
                "id=" + id +
                ", systemName='" + systemName + '\'' +
                ", eventName='" + eventName + '\'' +
                ", kafkaTopic='" + kafkaTopic + '\'' +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
} 