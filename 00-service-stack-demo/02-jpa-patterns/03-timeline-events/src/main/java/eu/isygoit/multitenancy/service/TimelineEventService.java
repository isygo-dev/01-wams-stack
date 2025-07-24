package eu.isygoit.multitenancy.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.isygoit.helper.JsonHelper;
import eu.isygoit.model.IIdAssignable;
import eu.isygoit.model.ITenantAssignable;
import eu.isygoit.model.jakarta.AuditableEntity;
import eu.isygoit.multitenancy.dto.TimelineEventMessage;
import eu.isygoit.multitenancy.model.EventType;
import eu.isygoit.multitenancy.repository.TimelineEventRepository;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TimelineEventService {

    private final TimelineEventRepository timelineEventRepository;
    private final ObjectMapper objectMapper;
    private final ProducerTemplate producerTemplate;

    @Autowired
    public TimelineEventService(TimelineEventRepository repository, ObjectMapper mapper,
                                ProducerTemplate producerTemplate) {
        this.timelineEventRepository = repository;
        this.objectMapper = mapper;
        this.producerTemplate = producerTemplate;
    }

    @Transactional
    public void recordEvent(Object entity, EventType eventType) {
        recordEvent(entity, eventType, null);
    }

    @Transactional
    public void recordEvent(Object entity, EventType eventType, Object previousState) {
        TimelineEventMessage message = new TimelineEventMessage();
        message.setEventType(eventType);
        message.setElementType(entity.getClass().getSimpleName());
        message.setElementId(getElementId(entity));
        message.setTimestamp(LocalDateTime.now());
        message.setTenant(getTenant(entity));
        message.setModifiedBy(getModifiedBy(entity));
        message.setAttributes(createAttributes(entity, previousState, eventType));

        sendToQueue(message);
    }

    private void sendToQueue(TimelineEventMessage message) {
        try {
            producerTemplate.sendBody("seda:timelineEvents", JsonHelper.toJson(message));
        } catch (Exception e) {
            throw new RuntimeException("Failed to send timeline event to queue", e);
        }
    }

    private JsonNode createAttributes(Object newEntity, Object oldEntity, EventType eventType) {
        switch (eventType) {
            case CREATED:
                return objectMapper.createObjectNode().set("data", JsonHelper.objectToNode(newEntity));
            case UPDATED:
                return objectMapper.createObjectNode().set("data", JsonHelper.computeDiff(oldEntity, newEntity));
            case DELETED:
                return objectMapper.createObjectNode().set("data", null);
            default:
                return objectMapper.createObjectNode().set("data", null);
        }
    }

    private String getElementId(Object entity) {
        if (entity instanceof IIdAssignable abstractEntity) {
            return abstractEntity.getId().toString();
        }
        throw new IllegalArgumentException("Entity must extend AbstractEntity");
    }

    private String getTenant(Object entity) {
        if (entity instanceof ITenantAssignable tenantAssignable) {
            return tenantAssignable.getTenant();
        }
        throw new IllegalArgumentException("Entity must implement ITenantAssignable");
    }

    private String getModifiedBy(Object entity) {
        if (entity instanceof AuditableEntity auditableEntity) {
            return auditableEntity.getUpdatedBy() != null
                    ? auditableEntity.getUpdatedBy()
                    : (auditableEntity.getCreatedBy() != null ? auditableEntity.getCreatedBy() : "test_user");
        }
        throw new IllegalArgumentException("Entity must implement ITenantAssignable");
    }
}