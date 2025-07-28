package eu.isygoit.service;

import eu.isygoit.model.TimeLineEvent;
import eu.isygoit.repository.timeline.TimelineEventRepository;
import eu.isygoit.route.timeline.AbstractTimelineEventRoute;
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