package eu.isygoit.multitenancy.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.isygoit.multitenancy.model.EventType;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;

// JPA Entity Listener
public class TimelineEventListener {
    private static final ThreadLocal<Object> previousState = new ThreadLocal<>();
    private static TimelineEventService timelineEventService;

    // Static setter for dependency injection
    public static void setTimelineEventService(TimelineEventService service) {
        TimelineEventListener.timelineEventService = service;
    }

    @PostPersist
    public void onPostPersist(Object entity) {
        timelineEventService.recordEvent(entity, EventType.CREATED);
    }

    @PostLoad
    public void onPostLoad(Object entity) {
        // Store deep copy of entity state for diffing
        previousState.set(deepCopy(entity));
    }

    @PreUpdate
    public void onPreUpdate(Object entity) {
        Object prev = previousState.get();
        if (prev != null) {
            timelineEventService.recordEvent(entity, EventType.UPDATED, prev);
            previousState.remove();
        }
    }

    @PreRemove
    public void onPreRemove(Object entity) {
        timelineEventService.recordEvent(entity, EventType.DELETED);
    }

    private Object deepCopy(Object entity) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(mapper.writeValueAsString(entity), entity.getClass());
        } catch (Exception e) {
            throw new RuntimeException("Failed to deep copy entity", e);
        }
    }
}