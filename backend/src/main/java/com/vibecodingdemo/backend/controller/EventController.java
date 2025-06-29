package com.vibecodingdemo.backend.controller;

import com.vibecodingdemo.backend.dto.EventDTO;
import com.vibecodingdemo.backend.dto.EventResponseDTO;
import com.vibecodingdemo.backend.exception.ResourceNotFoundException;
import com.vibecodingdemo.backend.service.EventService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    @Autowired
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    /**
     * Get all events
     * @return List of all events
     */
    @GetMapping
    public ResponseEntity<List<EventResponseDTO>> getAllEvents() {
        List<EventResponseDTO> events = eventService.getAllEvents();
        return ResponseEntity.ok(events);
    }

    /**
     * Get event by ID
     * @param id the event ID
     * @return the event if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDTO> getEventById(@PathVariable Long id) {
        return eventService.getEventById(id)
                .map(event -> ResponseEntity.ok(event))
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
    }

    /**
     * Create a new event
     * @param eventDTO the event data
     * @return the created event
     */
    @PostMapping
    public ResponseEntity<EventResponseDTO> createEvent(@Valid @RequestBody EventDTO eventDTO) {
        EventResponseDTO createdEvent = eventService.createEvent(eventDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }

    /**
     * Update an existing event
     * @param id the event ID
     * @param eventDTO the updated event data
     * @return the updated event
     */
    @PutMapping("/{id}")
    public ResponseEntity<EventResponseDTO> updateEvent(@PathVariable Long id, 
                                                       @Valid @RequestBody EventDTO eventDTO) {
        return eventService.updateEvent(id, eventDTO)
                .map(updatedEvent -> ResponseEntity.ok(updatedEvent))
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
    }

    /**
     * Delete an event by ID
     * @param id the event ID
     * @return success message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteEvent(@PathVariable Long id) {
        if (eventService.deleteEvent(id)) {
            return ResponseEntity.ok(Map.of("message", "Event deleted successfully"));
        } else {
            throw new ResourceNotFoundException("Event not found with id: " + id);
        }
    }

    /**
     * Get events by system name
     * @param systemName the system name
     * @return List of events for the given system
     */
    @GetMapping("/system/{systemName}")
    public ResponseEntity<List<EventResponseDTO>> getEventsBySystemName(@PathVariable String systemName) {
        List<EventResponseDTO> events = eventService.getEventsBySystemName(systemName);
        return ResponseEntity.ok(events);
    }

    /**
     * Get event by Kafka topic
     * @param kafkaTopic the Kafka topic
     * @return the event if found
     */
    @GetMapping("/topic/{kafkaTopic}")
    public ResponseEntity<EventResponseDTO> getEventByKafkaTopic(@PathVariable String kafkaTopic) {
        return eventService.getEventByKafkaTopic(kafkaTopic)
                .map(event -> ResponseEntity.ok(event))
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with Kafka topic: " + kafkaTopic));
    }
} 