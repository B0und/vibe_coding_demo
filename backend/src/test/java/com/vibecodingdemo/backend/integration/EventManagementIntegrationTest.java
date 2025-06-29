package com.vibecodingdemo.backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibecodingdemo.backend.dto.EventDTO;
import com.vibecodingdemo.backend.entity.Event;
import com.vibecodingdemo.backend.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class EventManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Event testEvent;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
        
        testEvent = new Event("TestSystem", "TestEvent", "test-topic", "Test event description");
        testEvent = eventRepository.save(testEvent);
    }

    @Test
    @WithMockUser
    void testGetAllEvents() throws Exception {
        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].systemName", is("TestSystem")))
                .andExpect(jsonPath("$[0].eventName", is("TestEvent")))
                .andExpect(jsonPath("$[0].kafkaTopic", is("test-topic")));
    }

    @Test
    @WithMockUser
    void testGetEventById() throws Exception {
        mockMvc.perform(get("/api/events/{id}", testEvent.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.systemName", is("TestSystem")))
                .andExpect(jsonPath("$.eventName", is("TestEvent")))
                .andExpect(jsonPath("$.kafkaTopic", is("test-topic")))
                .andExpect(jsonPath("$.description", is("Test event description")));
    }

    @Test
    @WithMockUser
    void testGetEventByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/events/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("Event not found with id: 999")));
    }

    @Test
    @WithMockUser
    void testCreateEvent() throws Exception {
        EventDTO newEventDTO = new EventDTO("NewSystem", "NewEvent", "new-topic", "New event description");

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.systemName", is("NewSystem")))
                .andExpect(jsonPath("$.eventName", is("NewEvent")))
                .andExpect(jsonPath("$.kafkaTopic", is("new-topic")))
                .andExpect(jsonPath("$.description", is("New event description")))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.createdAt", notNullValue()))
                .andExpect(jsonPath("$.updatedAt", notNullValue()));
    }

    @Test
    @WithMockUser
    void testCreateEventValidationFailure() throws Exception {
        EventDTO invalidEventDTO = new EventDTO("", "", "", ""); // All fields empty

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEventDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation failed")))
                .andExpect(jsonPath("$.fieldErrors", notNullValue()));
    }

    @Test
    @WithMockUser
    void testUpdateEvent() throws Exception {
        EventDTO updateEventDTO = new EventDTO("UpdatedSystem", "UpdatedEvent", "updated-topic", "Updated description");

        mockMvc.perform(put("/api/events/{id}", testEvent.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEventDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.systemName", is("UpdatedSystem")))
                .andExpect(jsonPath("$.eventName", is("UpdatedEvent")))
                .andExpect(jsonPath("$.kafkaTopic", is("updated-topic")))
                .andExpect(jsonPath("$.description", is("Updated description")));
    }

    @Test
    @WithMockUser
    void testUpdateEventNotFound() throws Exception {
        EventDTO updateEventDTO = new EventDTO("UpdatedSystem", "UpdatedEvent", "updated-topic", "Updated description");

        mockMvc.perform(put("/api/events/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEventDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("Event not found with id: 999")));
    }

    @Test
    @WithMockUser
    void testDeleteEvent() throws Exception {
        mockMvc.perform(delete("/api/events/{id}", testEvent.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("Event deleted successfully")));

        // Verify the event is actually deleted
        mockMvc.perform(get("/api/events/{id}", testEvent.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testDeleteEventNotFound() throws Exception {
        mockMvc.perform(delete("/api/events/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("Event not found with id: 999")));
    }

    @Test
    @WithMockUser
    void testGetEventsBySystemName() throws Exception {
        // Create another event with different system name
        Event anotherEvent = new Event("AnotherSystem", "AnotherEvent", "another-topic", "Another description");
        eventRepository.save(anotherEvent);

        mockMvc.perform(get("/api/events/system/{systemName}", "TestSystem"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].systemName", is("TestSystem")));
    }

    @Test
    @WithMockUser
    void testGetEventByKafkaTopic() throws Exception {
        mockMvc.perform(get("/api/events/topic/{kafkaTopic}", "test-topic"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.kafkaTopic", is("test-topic")))
                .andExpect(jsonPath("$.systemName", is("TestSystem")));
    }

    @Test
    @WithMockUser
    void testGetEventByKafkaTopicNotFound() throws Exception {
        mockMvc.perform(get("/api/events/topic/{kafkaTopic}", "non-existent-topic"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("Event not found with Kafka topic: non-existent-topic")));
    }
} 