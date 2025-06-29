package com.vibecodingdemo.backend.repository;

import com.vibecodingdemo.backend.entity.Event;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class EventRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    @Test
    void shouldSaveAndRetrieveEvent() {
        // Given
        Event event = new Event();
        event.setSystemName("user-service");
        event.setEventName("user-created");
        event.setKafkaTopic("user.events.created");
        event.setDescription("Event triggered when a new user is created");

        // When
        Event savedEvent = eventRepository.save(event);

        // Then
        assertThat(savedEvent).isNotNull();
        assertThat(savedEvent.getId()).isNotNull();
        assertThat(savedEvent.getSystemName()).isEqualTo("user-service");
        assertThat(savedEvent.getEventName()).isEqualTo("user-created");
        assertThat(savedEvent.getKafkaTopic()).isEqualTo("user.events.created");
        assertThat(savedEvent.getDescription()).isEqualTo("Event triggered when a new user is created");
        assertThat(savedEvent.getCreatedAt()).isNotNull();
        assertThat(savedEvent.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldFindEventById() {
        // Given
        Event event = new Event("order-service", "order-placed", "order.events.placed", "Order placed event");
        Event savedEvent = eventRepository.save(event);

        // When
        Optional<Event> foundEvent = eventRepository.findById(savedEvent.getId());

        // Then
        assertThat(foundEvent).isPresent();
        assertThat(foundEvent.get().getSystemName()).isEqualTo("order-service");
        assertThat(foundEvent.get().getEventName()).isEqualTo("order-placed");
        assertThat(foundEvent.get().getKafkaTopic()).isEqualTo("order.events.placed");
    }

    @Test
    void shouldFindEventsBySystemName() {
        // Given
        Event event1 = new Event("payment-service", "payment-processed", "payment.events.processed", "Payment processed");
        Event event2 = new Event("payment-service", "payment-failed", "payment.events.failed", "Payment failed");
        Event event3 = new Event("user-service", "user-created", "user.events.created", "User created");
        
        eventRepository.save(event1);
        eventRepository.save(event2);
        eventRepository.save(event3);

        // When
        List<Event> paymentEvents = eventRepository.findBySystemName("payment-service");
        List<Event> userEvents = eventRepository.findBySystemName("user-service");

        // Then
        assertThat(paymentEvents).hasSize(2);
        assertThat(paymentEvents).extracting(Event::getEventName)
                .containsExactlyInAnyOrder("payment-processed", "payment-failed");
        
        assertThat(userEvents).hasSize(1);
        assertThat(userEvents.get(0).getEventName()).isEqualTo("user-created");
    }

    @Test
    void shouldFindEventByKafkaTopic() {
        // Given
        Event event = new Event("notification-service", "email-sent", "notification.email.sent", "Email sent event");
        eventRepository.save(event);

        // When
        Optional<Event> foundEvent = eventRepository.findByKafkaTopic("notification.email.sent");

        // Then
        assertThat(foundEvent).isPresent();
        assertThat(foundEvent.get().getSystemName()).isEqualTo("notification-service");
        assertThat(foundEvent.get().getEventName()).isEqualTo("email-sent");
    }

    @Test
    void shouldReturnEmptyWhenEventNotFoundByKafkaTopic() {
        // When
        Optional<Event> foundEvent = eventRepository.findByKafkaTopic("nonexistent.topic");

        // Then
        assertThat(foundEvent).isEmpty();
    }

    @Test
    void shouldFindEventsBySystemNameAndEventName() {
        // Given
        Event event1 = new Event("inventory-service", "stock-updated", "inventory.stock.updated", "Stock updated");
        Event event2 = new Event("inventory-service", "stock-depleted", "inventory.stock.depleted", "Stock depleted");
        Event event3 = new Event("order-service", "stock-updated", "order.stock.updated", "Different stock updated");
        
        eventRepository.save(event1);
        eventRepository.save(event2);
        eventRepository.save(event3);

        // When
        List<Event> inventoryStockEvents = eventRepository.findBySystemNameAndEventName("inventory-service", "stock-updated");
        List<Event> orderStockEvents = eventRepository.findBySystemNameAndEventName("order-service", "stock-updated");

        // Then
        assertThat(inventoryStockEvents).hasSize(1);
        assertThat(inventoryStockEvents.get(0).getKafkaTopic()).isEqualTo("inventory.stock.updated");
        
        assertThat(orderStockEvents).hasSize(1);
        assertThat(orderStockEvents.get(0).getKafkaTopic()).isEqualTo("order.stock.updated");
    }

    @Test
    void shouldFindEventsByDescriptionContainingIgnoreCase() {
        // Given
        Event event1 = new Event("user-service", "user-login", "user.events.login", "User login successful");
        Event event2 = new Event("user-service", "user-logout", "user.events.logout", "User logout successful");
        Event event3 = new Event("auth-service", "login-failed", "auth.events.failed", "Failed login attempt");
        
        eventRepository.save(event1);
        eventRepository.save(event2);
        eventRepository.save(event3);

        // When
        List<Event> loginEvents = eventRepository.findByDescriptionContainingIgnoreCase("login");
        List<Event> logoutEvents = eventRepository.findByDescriptionContainingIgnoreCase("logout");
        
        // Then
        assertThat(loginEvents).hasSize(2);
        assertThat(loginEvents).extracting(Event::getEventName)
                .containsExactlyInAnyOrder("user-login", "login-failed");
        
        assertThat(logoutEvents).hasSize(1);
        assertThat(logoutEvents.get(0).getEventName()).isEqualTo("user-logout");
    }

    @Test
    void shouldHandleNullDescription() {
        // Given
        Event event = new Event();
        event.setSystemName("test-service");
        event.setEventName("test-event");
        event.setKafkaTopic("test.events.test");
        // description is intentionally left null

        // When
        Event savedEvent = eventRepository.save(event);

        // Then
        assertThat(savedEvent.getDescription()).isNull();
        
        // Verify we can retrieve it
        Optional<Event> foundEvent = eventRepository.findById(savedEvent.getId());
        assertThat(foundEvent).isPresent();
        assertThat(foundEvent.get().getDescription()).isNull();
    }

    @Test
    void shouldUpdateEventTimestamp() throws InterruptedException {
        // Given
        Event event = new Event("timestamp-service", "timestamp-test", "timestamp.test", "Timestamp test");
        Event savedEvent = eventRepository.save(event);
        
        // Wait a bit to ensure timestamp difference
        Thread.sleep(10);
        
        // When
        savedEvent.setDescription("Updated description");
        Event updatedEvent = eventRepository.save(savedEvent);

        // Then
        assertThat(updatedEvent.getUpdatedAt()).isAfter(updatedEvent.getCreatedAt());
    }

    @Test
    void shouldHandleEmptyResults() {
        // When
        List<Event> nonExistentSystemEvents = eventRepository.findBySystemName("non-existent-system");
        List<Event> nonExistentEventNameEvents = eventRepository.findBySystemNameAndEventName("system", "non-existent");
        List<Event> nonExistentDescriptionEvents = eventRepository.findByDescriptionContainingIgnoreCase("non-existent-text");

        // Then
        assertThat(nonExistentSystemEvents).isEmpty();
        assertThat(nonExistentEventNameEvents).isEmpty();
        assertThat(nonExistentDescriptionEvents).isEmpty();
    }
} 