package com.vibecodingdemo.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "events")
public class Event {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "system_name", nullable = false)
    private String systemName;
    
    @Column(name = "event_name", nullable = false)
    private String eventName;
    
    @Column(name = "kafka_topic", nullable = false)
    private String kafkaTopic;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Subscription> subscriptions = new HashSet<>();
    
    // Default constructor
    public Event() {}
    
    // Constructor with required fields
    public Event(String systemName, String eventName, String kafkaTopic, String description) {
        this.systemName = systemName;
        this.eventName = eventName;
        this.kafkaTopic = kafkaTopic;
        this.description = description;
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
    
    public Set<Subscription> getSubscriptions() {
        return subscriptions;
    }
    
    public void setSubscriptions(Set<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
    }
    
    // Utility methods for managing subscriptions
    public void addSubscription(Subscription subscription) {
        subscriptions.add(subscription);
        subscription.setEvent(this);
    }
    
    public void removeSubscription(Subscription subscription) {
        subscriptions.remove(subscription);
        subscription.setEvent(null);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Event)) return false;
        Event event = (Event) o;
        return id != null && id.equals(event.getId());
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    @Override
    public String toString() {
        return "Event{" +
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