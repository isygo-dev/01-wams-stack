package eu.isygoit.service.timeline;

import eu.isygoit.model.IIdAssignable;
import eu.isygoit.model.timeline.TimelineEventType;

/**
 * The type Timeline event service.
 */
public interface ITimelineEventService {

    void recordEvent(IIdAssignable entity, TimelineEventType timelineEventType);
}