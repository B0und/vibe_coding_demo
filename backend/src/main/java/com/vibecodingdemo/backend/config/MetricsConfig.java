package com.vibecodingdemo.backend.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;

import com.vibecodingdemo.backend.repository.EventRepository;
import com.vibecodingdemo.backend.repository.SubscriptionRepository;
import com.vibecodingdemo.backend.repository.UserRepository;

import java.util.concurrent.atomic.AtomicLong;

@Configuration
public class MetricsConfig {

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private UserRepository userRepository;

    // Counters for business events
    private final AtomicLong userRegistrations = new AtomicLong(0);
    private final AtomicLong subscriptionCreations = new AtomicLong(0);
    private final AtomicLong subscriptionCancellations = new AtomicLong(0);
    private final AtomicLong eventCreations = new AtomicLong(0);
    private final AtomicLong clientErrors = new AtomicLong(0);
    private final AtomicLong criticalClientErrors = new AtomicLong(0);

    @Bean
    public Counter userRegistrationCounter() {
        return Counter.builder("app.users.registrations")
                .description("Number of user registrations")
                .register(meterRegistry);
    }

    @Bean
    public Counter subscriptionCreationCounter() {
        return Counter.builder("app.subscriptions.created")
                .description("Number of subscriptions created")
                .register(meterRegistry);
    }

    @Bean
    public Counter subscriptionCancellationCounter() {
        return Counter.builder("app.subscriptions.cancelled")
                .description("Number of subscriptions cancelled")
                .register(meterRegistry);
    }

    @Bean
    public Counter eventCreationCounter() {
        return Counter.builder("app.events.created")
                .description("Number of events created")
                .register(meterRegistry);
    }

    @Bean
    public Counter clientErrorCounter() {
        return Counter.builder("app.client.errors")
                .description("Number of client-side errors reported")
                .register(meterRegistry);
    }

    @Bean
    public Counter criticalClientErrorCounter() {
        return Counter.builder("app.client.errors.critical")
                .description("Number of critical client-side errors reported")
                .register(meterRegistry);
    }

    @Bean
    public Timer apiRequestTimer() {
        return Timer.builder("app.api.requests")
                .description("API request duration")
                .register(meterRegistry);
    }

    @Bean
    public Timer databaseQueryTimer() {
        return Timer.builder("app.database.queries")
                .description("Database query duration")
                .register(meterRegistry);
    }

    // Gauges for current state metrics
    @Bean
    public Gauge totalUsersGauge() {
        return Gauge.builder("app.users.total")
                .description("Total number of users")
                .register(meterRegistry, this, MetricsConfig::getTotalUsers);
    }

    @Bean
    public Gauge totalEventsGauge() {
        return Gauge.builder("app.events.total")
                .description("Total number of events")
                .register(meterRegistry, this, MetricsConfig::getTotalEvents);
    }

    @Bean
    public Gauge totalSubscriptionsGauge() {
        return Gauge.builder("app.subscriptions.total")
                .description("Total number of active subscriptions")
                .register(meterRegistry, this, MetricsConfig::getTotalSubscriptions);
    }

    @Bean
    public Gauge activeUsersGauge() {
        return Gauge.builder("app.users.active")
                .description("Number of users with active subscriptions")
                .register(meterRegistry, this, MetricsConfig::getActiveUsers);
    }

    // Methods to get current values for gauges
    private double getTotalUsers() {
        try {
            return userRepository.count();
        } catch (Exception e) {
            return -1; // Indicate error in metric collection
        }
    }

    private double getTotalEvents() {
        try {
            return eventRepository.count();
        } catch (Exception e) {
            return -1;
        }
    }

    private double getTotalSubscriptions() {
        try {
            return subscriptionRepository.count();
        } catch (Exception e) {
            return -1;
        }
    }

    private double getActiveUsers() {
        try {
            return subscriptionRepository.countDistinctUsers();
        } catch (Exception e) {
            return -1;
        }
    }

    // Utility methods to increment counters (to be called by services)
    public void incrementUserRegistrations() {
        userRegistrations.incrementAndGet();
        userRegistrationCounter().increment();
    }

    public void incrementSubscriptionCreations() {
        subscriptionCreations.incrementAndGet();
        subscriptionCreationCounter().increment();
    }

    public void incrementSubscriptionCancellations() {
        subscriptionCancellations.incrementAndGet();
        subscriptionCancellationCounter().increment();
    }

    public void incrementEventCreations() {
        eventCreations.incrementAndGet();
        eventCreationCounter().increment();
    }

    public void incrementClientErrors() {
        clientErrors.incrementAndGet();
        clientErrorCounter().increment();
    }

    public void incrementCriticalClientErrors() {
        criticalClientErrors.incrementAndGet();
        criticalClientErrorCounter().increment();
    }
} 