package com.vibecodingdemo.backend.controller;

import com.vibecodingdemo.backend.dto.SubscriptionDTO;
import com.vibecodingdemo.backend.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @Autowired
    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    /**
     * Get all subscriptions for the current authenticated user
     * GET /api/subscriptions
     */
    @GetMapping
    public ResponseEntity<?> getUserSubscriptions() {
        try {
            // Get the currently authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Not authenticated"));
            }

            String username = authentication.getName();
            List<SubscriptionDTO> subscriptions = subscriptionService.getUserSubscriptions(username);

            return ResponseEntity.ok(subscriptions);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to retrieve subscriptions"));
        }
    }

    /**
     * Subscribe the current authenticated user to an event
     * POST /api/subscriptions/{eventId}
     */
    @PostMapping("/{eventId}")
    public ResponseEntity<?> subscribeToEvent(@PathVariable Long eventId) {
        try {
            // Get the currently authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Not authenticated"));
            }

            String username = authentication.getName();
            SubscriptionDTO subscription = subscriptionService.subscribeToEvent(username, eventId);

            return ResponseEntity.status(HttpStatus.CREATED).body(subscription);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
        // Custom exceptions (EventNotFoundException, AlreadySubscribedException) 
        // are handled by GlobalExceptionHandler
    }

    /**
     * Unsubscribe the current authenticated user from an event
     * DELETE /api/subscriptions/{eventId}
     */
    @DeleteMapping("/{eventId}")
    public ResponseEntity<?> unsubscribeFromEvent(@PathVariable Long eventId) {
        try {
            // Get the currently authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Not authenticated"));
            }

            String username = authentication.getName();
            subscriptionService.unsubscribeFromEvent(username, eventId);

            return ResponseEntity.noContent().build(); // 204 No Content

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
        // Custom exceptions (NotSubscribedException) are handled by GlobalExceptionHandler
    }

    /**
     * Check if the current authenticated user is subscribed to an event
     * GET /api/subscriptions/{eventId}/status
     */
    @GetMapping("/{eventId}/status")
    public ResponseEntity<?> getSubscriptionStatus(@PathVariable Long eventId) {
        try {
            // Get the currently authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Not authenticated"));
            }

            String username = authentication.getName();
            boolean isSubscribed = subscriptionService.isUserSubscribed(username, eventId);

            return ResponseEntity.ok(Map.of(
                "eventId", eventId,
                "subscribed", isSubscribed
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to check subscription status"));
        }
    }
} 