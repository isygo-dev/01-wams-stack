package eu.isygoit.multitenancy.service;

import eu.isygoit.multitenancy.model.TimeLineEvent;
import eu.isygoit.timeline.repository.TimelineEventRepository;
import eu.isygoit.timeline.route.AbstractTimelineEventRoute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The type Timeline event route.
 */
@Component
public class TimelineEventRoute extends AbstractTimelineEventRoute<TimeLineEvent> {

    @Autowired
    protected TimelineEventRoute(TimelineEventRepository timelineEventRepository) {
        super(timelineEventRepository);
    }
}