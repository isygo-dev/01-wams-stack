package eu.isygoit.multitenancy.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.isygoit.model.IIdAssignable;
import eu.isygoit.model.ITenantAssignable;
import eu.isygoit.multitenancy.model.EventType;
import eu.isygoit.multitenancy.model.TimeLineEvent;
import eu.isygoit.multitenancy.repository.TimelineEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
class TimelineEventService {
    private final TimelineEventRepository timelineEventRepository;
    private final ObjectMapper objectMapper;
    private final DiffService diffService;

    @Autowired
    public TimelineEventService(TimelineEventRepository repository, ObjectMapper mapper, DiffService diffService) {
        this.timelineEventRepository = repository;
        this.objectMapper = mapper;
        this.diffService = diffService;
        TimelineEventListener.setTimelineEventService(this); // Inject into listener
    }

    @Transactional
    public void recordEvent(Object entity, EventType eventType) {
        recordEvent(entity, eventType, null);
    }

    @Transactional
    public void recordEvent(Object entity, EventType eventType, Object previousState) {
        TimeLineEvent event = TimeLineEvent.builder()
                .eventType(eventType)
                .elementType(entity.getClass().getSimpleName())
                .elementId(getElementId(entity))
                .timestamp(LocalDateTime.now())
                .tenant(getTenant(entity))
                .build();

        ObjectNode attributes = objectMapper.createObjectNode();
        String modifiedBy = getCurrentUser();
        if (modifiedBy != null) {
            attributes.put("modifiedBy", modifiedBy);
        }

        switch (eventType) {
            case CREATED -> attributes.set("data", objectMapper.valueToTree(entity));
            case UPDATED -> attributes.set("data", diffService.computeDiff(previousState, entity));
            case DELETED -> attributes.set("data", objectMapper.createObjectNode()); // Empty object for minimal data
        }

        event.setAttributes(attributes);
        timelineEventRepository.save(event);
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

    private String getCurrentUser() {
        return SecurityContextHolder.getContext().getAuthentication() != null ?
                SecurityContextHolder.getContext().getAuthentication().getName() : "system";
    }
}
