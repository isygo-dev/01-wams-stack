package eu.isygoit.repository;

import eu.isygoit.model.TimeLineEvent;
import eu.isygoit.repository.timeline.TimelineEventRepository;

public interface AppTimelineEventRepository extends TimelineEventRepository<TimeLineEvent, Long> {
}
