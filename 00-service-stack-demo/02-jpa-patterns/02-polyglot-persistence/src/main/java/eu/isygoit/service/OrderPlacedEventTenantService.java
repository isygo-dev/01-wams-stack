package eu.isygoit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.isygoit.annotation.InjectRepository;
import eu.isygoit.jpa.json.JsonBasedTenantService;
import eu.isygoit.model.EventEntity;
import eu.isygoit.model.OrderPlacedEntity;
import eu.isygoit.repository.EventTenantAssignableRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@InjectRepository(value = EventTenantAssignableRepository.class)
public class OrderPlacedEventTenantService extends JsonBasedTenantService<OrderPlacedEntity, Long, EventEntity, EventTenantAssignableRepository> {

    /**
     * Constructor that initializes class types and element type.
     * Using constructor injection for better testability.
     *
     * @param objectMapper
     */
    public OrderPlacedEventTenantService(ObjectMapper objectMapper) {
        super(objectMapper);
    }
}