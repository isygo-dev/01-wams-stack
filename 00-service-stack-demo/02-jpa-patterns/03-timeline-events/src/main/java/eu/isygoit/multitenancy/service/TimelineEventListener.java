package eu.isygoit.multitenancy.service;

import eu.isygoit.model.IIdAssignable;
import eu.isygoit.multitenancy.model.EventType;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * The type Timeline event listener.
 */
@Component
public class TimelineEventListener implements ApplicationContextAware {

    private static ApplicationContext applicationContext;
    private static TimelineEventService timelineEventService;

    /**
     * Instantiates a new Timeline event listener.
     */
// No-arg constructor required by Hibernate
    public TimelineEventListener() {
    }

    @Override
    public void setApplicationContext(ApplicationContext context) {
        TimelineEventListener.applicationContext = context;
    }

    /**
     * On create.
     *
     * @param entity the entity
     */
    @PostPersist
    public void onCreate(IIdAssignable entity) {
        getTimelineEventService().recordEvent(entity, EventType.CREATED);
    }

    /**
     * On update.
     *
     * @param entity the entity
     */
    @PostUpdate
    public void onUpdate(IIdAssignable entity) {
        getTimelineEventService().recordEvent(entity, EventType.UPDATED);
    }

    /**
     * On delete.
     *
     * @param entity the entity
     */
    @PostRemove
    public void onDelete(IIdAssignable entity) {
        getTimelineEventService().recordEvent(entity, EventType.DELETED);
    }

    private TimelineEventService getTimelineEventService() {
        if (timelineEventService == null) {
            timelineEventService = applicationContext.getBean(TimelineEventService.class);
        }
        return timelineEventService;
    }
}