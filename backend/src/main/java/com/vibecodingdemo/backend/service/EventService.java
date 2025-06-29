package com.vibecodingdemo.backend.service;

import com.vibecodingdemo.backend.dto.EventDTO;
import com.vibecodingdemo.backend.dto.EventResponseDTO;
import com.vibecodingdemo.backend.entity.Event;
import com.vibecodingdemo.backend.mapper.EventMapper;
import com.vibecodingdemo.backend.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    @Autowired
    public EventService(EventRepository eventRepository, EventMapper eventMapper) {
        this.eventRepository = eventRepository;
        this.eventMapper = eventMapper;
    }

    /**
     * Get all events
     * @return list of all events as EventResponseDTO
     */
    public List<EventResponseDTO> getAllEvents() {
        return eventRepository.findAll()
                .stream()
                .map(eventMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get event by ID
     * @param id the event ID
     * @return Optional containing EventResponseDTO if found, empty otherwise
     */
    public Optional<EventResponseDTO> getEventById(Long id) {
        return eventRepository.findById(id)
                .map(eventMapper::toResponseDTO);
    }

    /**
     * Create a new event
     * @param eventDTO the event data
     * @return the created event as EventResponseDTO
     */
    public EventResponseDTO createEvent(EventDTO eventDTO) {
        Event event = eventMapper.toEntity(eventDTO);
        Event savedEvent = eventRepository.save(event);
        return eventMapper.toResponseDTO(savedEvent);
    }

    /**
     * Update an existing event
     * @param id the event ID
     * @param eventDTO the updated event data
     * @return Optional containing the updated event as EventResponseDTO if found, empty otherwise
     */
    public Optional<EventResponseDTO> updateEvent(Long id, EventDTO eventDTO) {
        return eventRepository.findById(id)
                .map(existingEvent -> {
                    eventMapper.updateEntityFromDTO(existingEvent, eventDTO);
                    Event updatedEvent = eventRepository.save(existingEvent);
                    return eventMapper.toResponseDTO(updatedEvent);
                });
    }

    /**
     * Delete an event by ID
     * @param id the event ID
     * @return true if the event was deleted, false if not found
     */
    public boolean deleteEvent(Long id) {
        if (eventRepository.existsById(id)) {
            eventRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Check if an event exists by ID
     * @param id the event ID
     * @return true if the event exists, false otherwise
     */
    public boolean existsById(Long id) {
        return eventRepository.existsById(id);
    }

    /**
     * Find events by system name
     * @param systemName the system name
     * @return list of events for the given system
     */
    public List<EventResponseDTO> getEventsBySystemName(String systemName) {
        return eventRepository.findBySystemName(systemName)
                .stream()
                .map(eventMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Find event by Kafka topic
     * @param kafkaTopic the Kafka topic
     * @return Optional containing EventResponseDTO if found, empty otherwise
     */
    public Optional<EventResponseDTO> getEventByKafkaTopic(String kafkaTopic) {
        return eventRepository.findByKafkaTopic(kafkaTopic)
                .map(eventMapper::toResponseDTO);
    }
} 