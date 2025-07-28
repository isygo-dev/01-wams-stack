package eu.isygoit.service.timeline;

import eu.isygoit.helper.JsonHelper;
import eu.isygoit.model.IIdAssignable;
import eu.isygoit.model.ITenantAssignable;
import eu.isygoit.model.jakarta.AuditableEntity;
import eu.isygoit.model.timeline.TimelineEventMessage;
import eu.isygoit.model.timeline.TimelineEventType;
import org.apache.camel.ProducerTemplate;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * Record event.
     *
     * @param entity    the entity
     * @param timelineEventType the event type
     */
    @Transactional
    public void recordEvent(IIdAssignable entity, TimelineEventType timelineEventType) {
        TimelineEventMessage message = TimelineEventMessage.builder()
                .tenant(getTenant(entity))
                .timelineEventType(timelineEventType)
                .elementType(entity.getClass().getSimpleName())
                .elementId(getElementId(entity))
                .timestamp(LocalDateTime.now())
                .modifiedBy(getModifiedBy(entity))
                .attributes(JsonHelper.objectToNode(entity))
                .build();

        sendToQueue(message);
    }

    private void sendToQueue(TimelineEventMessage message) {
        try {
            producerTemplate.sendBody("seda:timelineEvents", JsonHelper.toJson(message));
        } catch (Exception e) {
            throw new RuntimeException("Failed to send timeline event to queue", e);
        }
    }

    private String getElementId(IIdAssignable entity) {
        return entity.getId() != null ? entity.getId().toString() : null;
    }

    private String getTenant(IIdAssignable entity) {
        if (entity instanceof ITenantAssignable tenantAssignable) {
            return tenantAssignable.getTenant();
        }
        throw new IllegalArgumentException("Entity must implement ITenantAssignable");
    }

    private String getModifiedBy(IIdAssignable entity) {
        if (entity instanceof AuditableEntity auditableEntity) {
            return auditableEntity.getUpdatedBy() != null
                    ? auditableEntity.getUpdatedBy()
                    : (auditableEntity.getCreatedBy() != null ? auditableEntity.getCreatedBy() : "test_user");
        }
        throw new IllegalArgumentException("Entity must implement ITenantAssignable");
    }
}