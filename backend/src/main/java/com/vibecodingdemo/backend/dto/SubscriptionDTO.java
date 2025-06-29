package com.vibecodingdemo.backend.dto;

import jakarta.validation.constraints.NotNull;

public class SubscriptionDTO {
    
    private Long id;
    
    @NotNull(message = "Event ID is required")
    private Long eventId;
    
    @NotNull(message = "Event name is required")
    private String eventName;
    
    @NotNull(message = "System name is required")
    private String systemName;
    
    @NotNull(message = "Subscribed status is required")
    private Boolean subscribed;
    
    // Default constructor
    public SubscriptionDTO() {}
    
    // Constructor with all fields
    public SubscriptionDTO(Long id, Long eventId, String eventName, String systemName, Boolean subscribed) {
        this.id = id;
        this.eventId = eventId;
        this.eventName = eventName;
        this.systemName = systemName;
        this.subscribed = subscribed;
    }
    
    // Constructor without id (for new subscriptions)
    public SubscriptionDTO(Long eventId, String eventName, String systemName, Boolean subscribed) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.systemName = systemName;
        this.subscribed = subscribed;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getEventId() {
        return eventId;
    }
    
    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }
    
    public String getEventName() {
        return eventName;
    }
    
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }
    
    public String getSystemName() {
        return systemName;
    }
    
    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }
    
    public Boolean getSubscribed() {
        return subscribed;
    }
    
    public void setSubscribed(Boolean subscribed) {
        this.subscribed = subscribed;
    }
    
    @Override
    public String toString() {
        return "SubscriptionDTO{" +
                "id=" + id +
                ", eventId=" + eventId +
                ", eventName='" + eventName + '\'' +
                ", systemName='" + systemName + '\'' +
                ", subscribed=" + subscribed +
                '}';
    }
} 