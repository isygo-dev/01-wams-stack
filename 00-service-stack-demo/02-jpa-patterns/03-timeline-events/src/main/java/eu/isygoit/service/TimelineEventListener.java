package eu.isygoit.service;

import eu.isygoit.model.IIdAssignable;
import eu.isygoit.model.timeline.TimelineEventType;
import eu.isygoit.service.timeline.ITimelineEventService;
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
    private static ITimelineEventService timelineEventService;

    /**
     * Instantiates a new Timeline event listener.
     */

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
        getTimelineEventService().recordEvent(entity, TimelineEventType.CREATED);
    }

    /**
     * On update.
     *
     * @param entity the entity
     */
    @PostUpdate
    public void onUpdate(IIdAssignable entity) {
        getTimelineEventService().recordEvent(entity, TimelineEventType.UPDATED);
    }

    /**
     * On delete.
     *
     * @param entity the entity
     */
    @PostRemove
    public void onDelete(IIdAssignable entity) {
        getTimelineEventService().recordEvent(entity, TimelineEventType.DELETED);
    }

    private ITimelineEventService getTimelineEventService() {
        if (timelineEventService == null) {
            timelineEventService = applicationContext.getBean(ITimelineEventService.class);
        }
        return timelineEventService;
    }
}