package eu.isygoit.multitenancy.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.isygoit.dto.ITenantAssignableDto;
import eu.isygoit.exception.ObjectNotFoundException;
import eu.isygoit.helper.JsonHelper;
import eu.isygoit.model.IIdAssignable;
import eu.isygoit.model.ITenantAssignable;
import eu.isygoit.model.jakarta.AuditableEntity;
import eu.isygoit.multitenancy.dto.TimelineEventMessage;
import eu.isygoit.multitenancy.model.EventType;
import eu.isygoit.multitenancy.repository.TimelineEventRepository;
import eu.isygoit.repository.GenericRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class TimelineEventService {

    private final TimelineEventRepository timelineEventRepository;
    private final ProducerTemplate producerTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public TimelineEventService(TimelineEventRepository repository,
                                ProducerTemplate producerTemplate) {
        this.timelineEventRepository = repository;
        this.producerTemplate = producerTemplate;
    }

    @Transactional
    public void recordEvent(IIdAssignable entity, EventType eventType) {
        TimelineEventMessage message= TimelineEventMessage.builder()
                .tenant(getTenant(entity))
                .eventType(eventType)
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
        return entity.getId()!=null?entity.getId().toString():null;
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