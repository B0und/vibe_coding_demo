package com.vibecodingdemo.backend.repository;

import com.vibecodingdemo.backend.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    
    /**
     * Find events by system name
     * @param systemName the system name to search for
     * @return list of events for the given system
     */
    List<Event> findBySystemName(String systemName);
    
    /**
     * Find event by Kafka topic
     * @param kafkaTopic the Kafka topic to search for
     * @return Optional containing the event if found, empty otherwise
     */
    Optional<Event> findByKafkaTopic(String kafkaTopic);
    
    /**
     * Find events by system name and event name
     * @param systemName the system name
     * @param eventName the event name
     * @return list of matching events
     */
    List<Event> findBySystemNameAndEventName(String systemName, String eventName);
    
    /**
     * Search events by description containing the given text (case insensitive)
     * @param searchText the text to search for in descriptions
     * @return list of events with descriptions containing the search text
     */
    @Query("SELECT e FROM Event e WHERE LOWER(e.description) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    List<Event> findByDescriptionContainingIgnoreCase(@Param("searchText") String searchText);
} 