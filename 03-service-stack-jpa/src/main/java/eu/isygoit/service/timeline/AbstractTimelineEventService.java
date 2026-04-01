package eu.isygoit.service.timeline;

import eu.isygoit.constants.ErrorCodeConstants;
import eu.isygoit.exception.TimelineEventDispatchException;
import eu.isygoit.helper.JsonHelper;
import eu.isygoit.model.IIdAssignable;
import eu.isygoit.model.ITenantAssignable;
import eu.isygoit.model.jakarta.AuditableEntity;
import eu.isygoit.model.timeline.ITimelineEventSource;
import eu.isygoit.model.timeline.TimelineEventMessage;
import eu.isygoit.model.timeline.TimelineEventType;
import org.apache.camel.ProducerTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;

/**
 * The type Timeline event service.
 */
public abstract class AbstractTimelineEventService implements ITimelineEventService {

    private final ProducerTemplate producerTemplate;

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
                .timelineEventType(timelineEventType)
                .elementType(entity.getClass().getSimpleName())
                .elementId(entity.resolveElementId())
                .timestamp(LocalDateTime.now())
                .modifiedBy(entity.resolveModifiedBy())
                .attributes(TrackChangesExtractor.extract(entity))
                .build();

        if(message instanceof ITenantAssignable messageTenantAssignable){
            messageTenantAssignable.setTenant(entity.resolveTenant());
        }
        return message;
    }

    private void sendAfterCommit(TimelineEventMessage message) {
        try {
            producerTemplate.sendBody("seda:timelineEvents", JsonHelper.toJson(message));
        } catch (Exception e) {
            throw new TimelineEventDispatchException("Failed to dispatch event for element: " + message.getElementId(), e);
        }
    }
}