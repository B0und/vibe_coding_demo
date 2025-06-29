package com.vibecodingdemo.backend.mapper;

import com.vibecodingdemo.backend.dto.EventDTO;
import com.vibecodingdemo.backend.dto.EventResponseDTO;
import com.vibecodingdemo.backend.entity.Event;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {
    
    /**
     * Convert EventDTO to Event entity
     * @param eventDTO the DTO to convert
     * @return Event entity
     */
    public Event toEntity(EventDTO eventDTO) {
        if (eventDTO == null) {
            return null;
        }
        
        Event event = new Event();
        event.setSystemName(eventDTO.getSystemName());
        event.setEventName(eventDTO.getEventName());
        event.setKafkaTopic(eventDTO.getKafkaTopic());
        event.setDescription(eventDTO.getDescription());
        
        return event;
    }
    
    /**
     * Convert Event entity to EventResponseDTO
     * @param event the entity to convert
     * @return EventResponseDTO
     */
    public EventResponseDTO toResponseDTO(Event event) {
        if (event == null) {
            return null;
        }
        
        return new EventResponseDTO(
            event.getId(),
            event.getSystemName(),
            event.getEventName(),
            event.getKafkaTopic(),
            event.getDescription(),
            event.getCreatedAt(),
            event.getUpdatedAt()
        );
    }
    
    /**
     * Update an existing Event entity with data from EventDTO
     * @param event the existing entity to update
     * @param eventDTO the DTO containing new data
     */
    public void updateEntityFromDTO(Event event, EventDTO eventDTO) {
        if (event == null || eventDTO == null) {
            return;
        }
        
        event.setSystemName(eventDTO.getSystemName());
        event.setEventName(eventDTO.getEventName());
        event.setKafkaTopic(eventDTO.getKafkaTopic());
        event.setDescription(eventDTO.getDescription());
    }
} 