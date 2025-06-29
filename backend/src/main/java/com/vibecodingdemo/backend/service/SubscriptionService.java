package com.vibecodingdemo.backend.service;

import com.vibecodingdemo.backend.dto.SubscriptionDTO;
import com.vibecodingdemo.backend.entity.Event;
import com.vibecodingdemo.backend.entity.Subscription;
import com.vibecodingdemo.backend.entity.User;
import com.vibecodingdemo.backend.exception.AlreadySubscribedException;
import com.vibecodingdemo.backend.exception.EventNotFoundException;
import com.vibecodingdemo.backend.exception.NotSubscribedException;
import com.vibecodingdemo.backend.repository.EventRepository;
import com.vibecodingdemo.backend.repository.SubscriptionRepository;
import com.vibecodingdemo.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Autowired
    public SubscriptionService(SubscriptionRepository subscriptionRepository, UserRepository userRepository, EventRepository eventRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    /**
     * Get all subscriptions for a user by username
     * @param username the username to get subscriptions for
     * @return list of SubscriptionDTOs for the user
     * @throws IllegalArgumentException if user is not found
     */
    public List<SubscriptionDTO> getUserSubscriptions(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }

        // Verify user exists
        User user = userRepository.findByUsername(username.trim())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        // Get all subscriptions for the user
        List<Subscription> subscriptions = subscriptionRepository.findByUserUsername(username.trim());

        // Convert to DTOs
        return subscriptions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Check if a user is subscribed to a specific event
     * @param username the username to check
     * @param eventId the event ID to check subscription for
     * @return true if user is subscribed to the event, false otherwise
     * @throws IllegalArgumentException if user is not found
     */
    public boolean isUserSubscribed(String username, Long eventId) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        
        if (eventId == null) {
            throw new IllegalArgumentException("Event ID cannot be null");
        }

        // Verify user exists
        User user = userRepository.findByUsername(username.trim())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        // Check if subscription exists
        return subscriptionRepository.existsByUserIdAndEventId(user.getId(), eventId);
    }

    /**
     * Subscribe a user to an event
     * @param username the username of the user to subscribe
     * @param eventId the ID of the event to subscribe to
     * @return the created SubscriptionDTO
     * @throws IllegalArgumentException if username is null/empty or eventId is null
     * @throws IllegalArgumentException if user is not found
     * @throws EventNotFoundException if event is not found
     * @throws AlreadySubscribedException if user is already subscribed to the event
     */
    @Transactional
    public SubscriptionDTO subscribeToEvent(String username, Long eventId) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        
        if (eventId == null) {
            throw new IllegalArgumentException("Event ID cannot be null");
        }

        // Verify user exists
        User user = userRepository.findByUsername(username.trim())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        // Verify event exists
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with ID: " + eventId));

        // Check if user is already subscribed
        if (subscriptionRepository.existsByUserIdAndEventId(user.getId(), eventId)) {
            throw new AlreadySubscribedException("User " + username + " is already subscribed to event ID: " + eventId);
        }

        // Create and save subscription
        Subscription subscription = new Subscription(user, event);
        subscription = subscriptionRepository.save(subscription);

        return convertToDTO(subscription);
    }

    /**
     * Unsubscribe a user from an event
     * @param username the username of the user to unsubscribe
     * @param eventId the ID of the event to unsubscribe from
     * @throws IllegalArgumentException if username is null/empty or eventId is null
     * @throws IllegalArgumentException if user is not found
     * @throws NotSubscribedException if user is not subscribed to the event
     */
    @Transactional
    public void unsubscribeFromEvent(String username, Long eventId) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        
        if (eventId == null) {
            throw new IllegalArgumentException("Event ID cannot be null");
        }

        // Verify user exists
        User user = userRepository.findByUsername(username.trim())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        // Find the subscription
        Subscription subscription = subscriptionRepository.findByUserIdAndEventId(user.getId(), eventId)
                .orElseThrow(() -> new NotSubscribedException("User " + username + " is not subscribed to event ID: " + eventId));

        // Delete the subscription
        subscriptionRepository.delete(subscription);
    }

    /**
     * Convert a Subscription entity to SubscriptionDTO
     * @param subscription the subscription entity to convert
     * @return the converted SubscriptionDTO
     */
    private SubscriptionDTO convertToDTO(Subscription subscription) {
        return new SubscriptionDTO(
                subscription.getId(),
                subscription.getEvent().getId(),
                subscription.getEvent().getEventName(),
                subscription.getEvent().getSystemName(),
                true // subscribed is always true for existing subscriptions
        );
    }
} 