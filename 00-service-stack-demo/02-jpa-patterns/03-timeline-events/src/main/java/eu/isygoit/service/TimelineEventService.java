package eu.isygoit.service;

import eu.isygoit.service.timeline.AbstractTimelineEventService;
import org.apache.camel.ProducerTemplate;
import org.springframework.stereotype.Service;

/**
 * The type Timeline event service.
 */
@Service
public class TimelineEventService extends AbstractTimelineEventService {

    /**
     * Instantiates a new Timeline event service.
     *
     * @param producerTemplate the producer template
     */
    public TimelineEventService(ProducerTemplate producerTemplate) {
        super(producerTemplate);
    }
}