package com.vibecodingdemo.backend.repository;

import com.vibecodingdemo.backend.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    
    /**
     * Find a subscription by user ID and event ID
     * This leverages the unique constraint on (user_id, event_id)
     * @param userId the user ID
     * @param eventId the event ID
     * @return Optional containing the subscription if found, empty otherwise
     */
    Optional<Subscription> findByUserIdAndEventId(Long userId, Long eventId);
    
    /**
     * Find all subscriptions for a specific user
     * @param userId the user ID
     * @return list of subscriptions for the user
     */
    List<Subscription> findByUserId(Long userId);
    
    /**
     * Find all subscriptions for a specific event
     * @param eventId the event ID
     * @return list of subscriptions for the event
     */
    List<Subscription> findByEventId(Long eventId);
    
    /**
     * Find all subscriptions for a specific event with users eagerly fetched
     * @param eventId the event ID
     * @return list of subscriptions for the event with users loaded
     */
    @Query("SELECT s FROM Subscription s JOIN FETCH s.user WHERE s.event.id = :eventId")
    List<Subscription> findByEventIdWithUsers(@Param("eventId") Long eventId);
    
    /**
     * Find all subscriptions for a user by username
     * @param username the username
     * @return list of subscriptions for the user
     */
    @Query("SELECT s FROM Subscription s WHERE s.user.username = :username")
    List<Subscription> findByUserUsername(@Param("username") String username);
    
    /**
     * Find all subscriptions for an event by system name
     * @param systemName the system name
     * @return list of subscriptions for events of the given system
     */
    @Query("SELECT s FROM Subscription s WHERE s.event.systemName = :systemName")
    List<Subscription> findByEventSystemName(@Param("systemName") String systemName);
    
    /**
     * Check if a subscription exists for a given user and event
     * @param userId the user ID
     * @param eventId the event ID
     * @return true if subscription exists, false otherwise
     */
    boolean existsByUserIdAndEventId(Long userId, Long eventId);
    
    /**
     * Count total subscriptions for a specific user
     * @param userId the user ID
     * @return number of subscriptions for the user
     */
    long countByUserId(Long userId);
    
    /**
     * Count total subscriptions for a specific event
     * @param eventId the event ID
     * @return number of subscriptions for the event
     */
    long countByEventId(Long eventId);
    
    /**
     * Count distinct users with active subscriptions
     * @return number of unique users with at least one subscription
     */
    @Query("SELECT COUNT(DISTINCT s.user.id) FROM Subscription s")
    long countDistinctUsers();
} 