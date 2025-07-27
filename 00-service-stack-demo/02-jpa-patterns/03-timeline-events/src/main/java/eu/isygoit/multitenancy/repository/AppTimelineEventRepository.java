package eu.isygoit.multitenancy.repository;

import eu.isygoit.multitenancy.model.TimeLineEvent;
import eu.isygoit.timeline.repository.TimelineEventRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppTimelineEventRepository extends TimelineEventRepository<TimeLineEvent, Long> {
}
