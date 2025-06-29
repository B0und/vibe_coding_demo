package com.vibecodingdemo.backend.entity;

import com.vibecodingdemo.backend.repository.EventRepository;
import com.vibecodingdemo.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class SubscriptionTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Test
    void shouldCreateSubscriptionWithUserAndEvent() {
        // Given
        User user = new User("testuser");
        user.setTelegramRecipients("recipient1");
        User savedUser = userRepository.save(user);

        Event event = new Event("test-service", "test-event", "test.events.test", "Test event");
        Event savedEvent = eventRepository.save(event);

        // When
        Subscription subscription = new Subscription(savedUser, savedEvent);
        entityManager.persistAndFlush(subscription);

        // Then
        assertThat(subscription.getId()).isNotNull();
        assertThat(subscription.getUser()).isEqualTo(savedUser);
        assertThat(subscription.getEvent()).isEqualTo(savedEvent);
        assertThat(subscription.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldEnforceUniqueConstraintOnUserIdAndEventId() {
        // Given
        User user = new User("uniqueuser");
        User savedUser = userRepository.save(user);

        Event event = new Event("unique-service", "unique-event", "unique.events.test", "Unique event");
        Event savedEvent = eventRepository.save(event);

        // First subscription
        Subscription subscription1 = new Subscription(savedUser, savedEvent);
        entityManager.persistAndFlush(subscription1);

        // Second subscription with same user and event
        Subscription subscription2 = new Subscription(savedUser, savedEvent);

        // When & Then
        try {
            entityManager.persistAndFlush(subscription2);
            assertThat(false).as("Expected constraint violation").isTrue();
        } catch (Exception e) {
            // Expected behavior - unique constraint violation
            assertThat(e.getMessage()).containsIgnoringCase("constraint");
        }
    }

    @Test
    void shouldAllowSameUserToSubscribeToMultipleEvents() {
        // Given
        User user = new User("multiuser");
        User savedUser = userRepository.save(user);

        Event event1 = new Event("service1", "event1", "service1.events.event1", "Event 1");
        Event event2 = new Event("service2", "event2", "service2.events.event2", "Event 2");
        Event savedEvent1 = eventRepository.save(event1);
        Event savedEvent2 = eventRepository.save(event2);

        // When
        Subscription subscription1 = new Subscription(savedUser, savedEvent1);
        Subscription subscription2 = new Subscription(savedUser, savedEvent2);
        
        entityManager.persistAndFlush(subscription1);
        entityManager.persistAndFlush(subscription2);

        // Then
        assertThat(subscription1.getId()).isNotNull();
        assertThat(subscription2.getId()).isNotNull();
        assertThat(subscription1.getUser()).isEqualTo(savedUser);
        assertThat(subscription2.getUser()).isEqualTo(savedUser);
        assertThat(subscription1.getEvent()).isEqualTo(savedEvent1);
        assertThat(subscription2.getEvent()).isEqualTo(savedEvent2);
    }

    @Test
    void shouldAllowMultipleUsersToSubscribeToSameEvent() {
        // Given
        User user1 = new User("user1");
        User user2 = new User("user2");
        User savedUser1 = userRepository.save(user1);
        User savedUser2 = userRepository.save(user2);

        Event event = new Event("shared-service", "shared-event", "shared.events.test", "Shared event");
        Event savedEvent = eventRepository.save(event);

        // When
        Subscription subscription1 = new Subscription(savedUser1, savedEvent);
        Subscription subscription2 = new Subscription(savedUser2, savedEvent);
        
        entityManager.persistAndFlush(subscription1);
        entityManager.persistAndFlush(subscription2);

        // Then
        assertThat(subscription1.getId()).isNotNull();
        assertThat(subscription2.getId()).isNotNull();
        assertThat(subscription1.getUser()).isEqualTo(savedUser1);
        assertThat(subscription2.getUser()).isEqualTo(savedUser2);
        assertThat(subscription1.getEvent()).isEqualTo(savedEvent);
        assertThat(subscription2.getEvent()).isEqualTo(savedEvent);
    }

    @Test
    void shouldMaintainReferentialIntegrity() {
        // Given
        User user = new User("refuser");
        User savedUser = userRepository.save(user);

        Event event = new Event("ref-service", "ref-event", "ref.events.test", "Reference event");
        Event savedEvent = eventRepository.save(event);

        Subscription subscription = new Subscription(savedUser, savedEvent);
        Subscription savedSubscription = entityManager.persistAndFlush(subscription);

        // When - retrieve the subscription
        Subscription foundSubscription = entityManager.find(Subscription.class, savedSubscription.getId());

        // Then
        assertThat(foundSubscription).isNotNull();
        assertThat(foundSubscription.getUser().getUsername()).isEqualTo("refuser");
        assertThat(foundSubscription.getEvent().getSystemName()).isEqualTo("ref-service");
        assertThat(foundSubscription.getEvent().getEventName()).isEqualTo("ref-event");
    }
} 