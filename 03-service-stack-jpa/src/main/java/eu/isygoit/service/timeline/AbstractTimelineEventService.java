package eu.isygoit.service.timeline;

import eu.isygoit.exception.TimelineEventDispatchException;
import eu.isygoit.helper.JsonHelper;
import eu.isygoit.model.timeline.ITimelineEventSource;
import eu.isygoit.model.timeline.TimelineEventMessage;
import eu.isygoit.model.timeline.TimelineEventType;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;

/**
 * The type Timeline event service.
 */
@Slf4j
public abstract class AbstractTimelineEventService implements ITimelineEventService {

    private final ProducerTemplate producerTemplate;
    @Value("${timeline.queueName}")
    private String queueName;

    /**
     * Instantiates a new Timeline event service.
     *
     * @param producerTemplate the producer template
     */
    public AbstractTimelineEventService(ProducerTemplate producerTemplate) {
        this.producerTemplate = producerTemplate;
    }

    public void recordEvent(ITimelineEventSource entity, TimelineEventType eventType) {
        TimelineEventMessage message = buildQueuedMessage(entity, eventType);
        // sends ONLY after the DB transaction commits — no ghost events on rollback
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        sendAfterCommit(message);
                    }
                }
        );
    }

    private TimelineEventMessage buildQueuedMessage(ITimelineEventSource entity, TimelineEventType timelineEventType) {
        TimelineEventMessage message = TimelineEventMessage.builder()
                .tenant(entity.resolveTenant())
                .timelineEventType(timelineEventType)
                .elementType(entity.getClass().getSimpleName())
                .elementId(entity.resolveElementId())
                .timestamp(LocalDateTime.now())
                .modifiedBy(entity.resolveModifiedBy())
                .attributes(TrackChangesExtractor.extract(entity))
                .build();

        return message;
    }

    private void sendAfterCommit(TimelineEventMessage message) {
        try {
            log.info("starting write route: " + (queueName != null ? queueName : "timelineEvents"));
            producerTemplate.sendBody((queueName != null ? queueName : "timelineEvents"), JsonHelper.toJson(message));
        } catch (Exception e) {
            throw new TimelineEventDispatchException("Failed to dispatch event for element: " + message.getElementId(), e);
        }
    }
}