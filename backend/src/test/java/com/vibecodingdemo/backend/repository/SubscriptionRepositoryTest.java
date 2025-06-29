package com.vibecodingdemo.backend.repository;

import com.vibecodingdemo.backend.entity.Event;
import com.vibecodingdemo.backend.entity.Subscription;
import com.vibecodingdemo.backend.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class SubscriptionRepositoryTest {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    private User user1, user2;
    private Event event1, event2, event3;

    @BeforeEach
    void setUp() {
        // Create test users
        user1 = new User("user1");
        user1.setTelegramRecipients("recipient1");
        user1 = userRepository.save(user1);

        user2 = new User("user2");
        user2.setTelegramRecipients("recipient2");
        user2 = userRepository.save(user2);

        // Create test events
        event1 = new Event("user-service", "user-created", "user.events.created", "User created event");
        event1 = eventRepository.save(event1);

        event2 = new Event("user-service", "user-updated", "user.events.updated", "User updated event");
        event2 = eventRepository.save(event2);

        event3 = new Event("order-service", "order-placed", "order.events.placed", "Order placed event");
        event3 = eventRepository.save(event3);
    }

    @Test
    void shouldFindSubscriptionByUserIdAndEventId() {
        // Given
        Subscription subscription = new Subscription(user1, event1);
        subscriptionRepository.save(subscription);

        // When
        Optional<Subscription> found = subscriptionRepository.findByUserIdAndEventId(user1.getId(), event1.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUser()).isEqualTo(user1);
        assertThat(found.get().getEvent()).isEqualTo(event1);
    }

    @Test
    void shouldReturnEmptyWhenSubscriptionNotFound() {
        // When
        Optional<Subscription> found = subscriptionRepository.findByUserIdAndEventId(999L, 999L);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void shouldFindAllSubscriptionsForUser() {
        // Given
        Subscription sub1 = new Subscription(user1, event1);
        Subscription sub2 = new Subscription(user1, event2);
        Subscription sub3 = new Subscription(user2, event1); // Different user
        
        subscriptionRepository.save(sub1);
        subscriptionRepository.save(sub2);
        subscriptionRepository.save(sub3);

        // When
        List<Subscription> user1Subscriptions = subscriptionRepository.findByUserId(user1.getId());
        List<Subscription> user2Subscriptions = subscriptionRepository.findByUserId(user2.getId());

        // Then
        assertThat(user1Subscriptions).hasSize(2);
        assertThat(user1Subscriptions).extracting(s -> s.getEvent().getEventName())
                .containsExactlyInAnyOrder("user-created", "user-updated");

        assertThat(user2Subscriptions).hasSize(1);
        assertThat(user2Subscriptions.get(0).getEvent().getEventName()).isEqualTo("user-created");
    }

    @Test
    void shouldFindAllSubscriptionsForEvent() {
        // Given
        Subscription sub1 = new Subscription(user1, event1);
        Subscription sub2 = new Subscription(user2, event1);
        Subscription sub3 = new Subscription(user1, event2); // Different event
        
        subscriptionRepository.save(sub1);
        subscriptionRepository.save(sub2);
        subscriptionRepository.save(sub3);

        // When
        List<Subscription> event1Subscriptions = subscriptionRepository.findByEventId(event1.getId());
        List<Subscription> event2Subscriptions = subscriptionRepository.findByEventId(event2.getId());

        // Then
        assertThat(event1Subscriptions).hasSize(2);
        assertThat(event1Subscriptions).extracting(s -> s.getUser().getUsername())
                .containsExactlyInAnyOrder("user1", "user2");

        assertThat(event2Subscriptions).hasSize(1);
        assertThat(event2Subscriptions.get(0).getUser().getUsername()).isEqualTo("user1");
    }

    @Test
    void shouldFindSubscriptionsByUsername() {
        // Given
        Subscription sub1 = new Subscription(user1, event1);
        Subscription sub2 = new Subscription(user1, event2);
        Subscription sub3 = new Subscription(user2, event3);
        
        subscriptionRepository.save(sub1);
        subscriptionRepository.save(sub2);
        subscriptionRepository.save(sub3);

        // When
        List<Subscription> user1Subscriptions = subscriptionRepository.findByUserUsername("user1");
        List<Subscription> user2Subscriptions = subscriptionRepository.findByUserUsername("user2");

        // Then
        assertThat(user1Subscriptions).hasSize(2);
        assertThat(user1Subscriptions).extracting(s -> s.getEvent().getEventName())
                .containsExactlyInAnyOrder("user-created", "user-updated");

        assertThat(user2Subscriptions).hasSize(1);
        assertThat(user2Subscriptions.get(0).getEvent().getEventName()).isEqualTo("order-placed");
    }

    @Test
    void shouldFindSubscriptionsByEventSystemName() {
        // Given
        Subscription sub1 = new Subscription(user1, event1); // user-service
        Subscription sub2 = new Subscription(user2, event2); // user-service
        Subscription sub3 = new Subscription(user1, event3); // order-service
        
        subscriptionRepository.save(sub1);
        subscriptionRepository.save(sub2);
        subscriptionRepository.save(sub3);

        // When
        List<Subscription> userServiceSubscriptions = subscriptionRepository.findByEventSystemName("user-service");
        List<Subscription> orderServiceSubscriptions = subscriptionRepository.findByEventSystemName("order-service");

        // Then
        assertThat(userServiceSubscriptions).hasSize(2);
        assertThat(userServiceSubscriptions).extracting(s -> s.getEvent().getEventName())
                .containsExactlyInAnyOrder("user-created", "user-updated");

        assertThat(orderServiceSubscriptions).hasSize(1);
        assertThat(orderServiceSubscriptions.get(0).getEvent().getEventName()).isEqualTo("order-placed");
    }

    @Test
    void shouldCheckIfSubscriptionExists() {
        // Given
        Subscription subscription = new Subscription(user1, event1);
        subscriptionRepository.save(subscription);

        // When & Then
        assertThat(subscriptionRepository.existsByUserIdAndEventId(user1.getId(), event1.getId())).isTrue();
        assertThat(subscriptionRepository.existsByUserIdAndEventId(user1.getId(), event2.getId())).isFalse();
        assertThat(subscriptionRepository.existsByUserIdAndEventId(user2.getId(), event1.getId())).isFalse();
    }

    @Test
    void shouldCountSubscriptionsForUser() {
        // Given
        Subscription sub1 = new Subscription(user1, event1);
        Subscription sub2 = new Subscription(user1, event2);
        Subscription sub3 = new Subscription(user2, event1);
        
        subscriptionRepository.save(sub1);
        subscriptionRepository.save(sub2);
        subscriptionRepository.save(sub3);

        // When & Then
        assertThat(subscriptionRepository.countByUserId(user1.getId())).isEqualTo(2);
        assertThat(subscriptionRepository.countByUserId(user2.getId())).isEqualTo(1);
    }

    @Test
    void shouldCountSubscriptionsForEvent() {
        // Given
        Subscription sub1 = new Subscription(user1, event1);
        Subscription sub2 = new Subscription(user2, event1);
        Subscription sub3 = new Subscription(user1, event2);
        
        subscriptionRepository.save(sub1);
        subscriptionRepository.save(sub2);
        subscriptionRepository.save(sub3);

        // When & Then
        assertThat(subscriptionRepository.countByEventId(event1.getId())).isEqualTo(2);
        assertThat(subscriptionRepository.countByEventId(event2.getId())).isEqualTo(1);
        assertThat(subscriptionRepository.countByEventId(event3.getId())).isEqualTo(0);
    }

    @Test
    void shouldHandleEmptyResults() {
        // When & Then
        assertThat(subscriptionRepository.findByUserId(999L)).isEmpty();
        assertThat(subscriptionRepository.findByEventId(999L)).isEmpty();
        assertThat(subscriptionRepository.findByUserUsername("nonexistent")).isEmpty();
        assertThat(subscriptionRepository.findByEventSystemName("nonexistent-service")).isEmpty();
        assertThat(subscriptionRepository.countByUserId(999L)).isEqualTo(0);
        assertThat(subscriptionRepository.countByEventId(999L)).isEqualTo(0);
    }

    @Test
    void shouldEnforceUniqueConstraintViaRepository() {
        // Given
        Subscription subscription1 = new Subscription(user1, event1);
        subscriptionRepository.save(subscription1);

        // When & Then
        Subscription subscription2 = new Subscription(user1, event1);
        try {
            subscriptionRepository.saveAndFlush(subscription2);
            assertThat(false).as("Expected constraint violation").isTrue();
        } catch (Exception e) {
            // Expected behavior - unique constraint violation
            assertThat(e.getMessage()).containsIgnoringCase("constraint");
        }
    }

    @Test
    void shouldWorkWithBidirectionalRelationships() {
        // Given
        Subscription subscription = new Subscription(user1, event1);
        
        // Use utility methods to establish bidirectional relationships
        user1.addSubscription(subscription);
        event1.addSubscription(subscription);
        
        subscriptionRepository.save(subscription);

        // When - Load user and check subscriptions
        User loadedUser = userRepository.findById(user1.getId()).orElseThrow();
        Event loadedEvent = eventRepository.findById(event1.getId()).orElseThrow();

        // Then
        assertThat(loadedUser.getSubscriptions()).hasSize(1);
        assertThat(loadedUser.getSubscriptions().iterator().next().getEvent()).isEqualTo(event1);
        
        assertThat(loadedEvent.getSubscriptions()).hasSize(1);
        assertThat(loadedEvent.getSubscriptions().iterator().next().getUser()).isEqualTo(user1);
    }
} 