package eu.isygoit.timeline.service;

import eu.isygoit.model.IIdAssignable;
import eu.isygoit.timeline.schema.EventType;

/**
 * The type Timeline event service.
 */
public interface ITimelineEventService {

    void recordEvent(IIdAssignable entity, EventType eventType);
}