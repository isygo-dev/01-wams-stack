package eu.isygoit.multitenancy.service;

import eu.isygoit.model.ITenantAssignable;
import eu.isygoit.multitenancy.model.EventType;
import jakarta.persistence.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class TimelineEventListener implements ApplicationContextAware {

    private static ApplicationContext applicationContext;
    private static TimelineEventService timelineEventService;

    @Override
    public void setApplicationContext(ApplicationContext context) {
        TimelineEventListener.applicationContext = context;
    }

    // No-arg constructor required by Hibernate
    public TimelineEventListener() {
    }

    @PrePersist
    public void onCreate(Object entity) {
        getTimelineEventService().recordEvent(entity, EventType.CREATED);
    }

    @PreUpdate
    public void onUpdate(Object entity) {
        getTimelineEventService().recordEvent(entity, EventType.UPDATED);
    }

    @PreRemove
    public void onDelete(Object entity) {
        getTimelineEventService().recordEvent(entity, EventType.DELETED);
    }

    private TimelineEventService getTimelineEventService() {
        if (timelineEventService == null) {
            timelineEventService = applicationContext.getBean(TimelineEventService.class);
        }
        return timelineEventService;
    }
}