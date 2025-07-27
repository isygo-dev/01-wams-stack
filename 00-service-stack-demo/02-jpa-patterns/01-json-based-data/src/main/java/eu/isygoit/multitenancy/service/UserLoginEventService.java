package eu.isygoit.multitenancy.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.isygoit.annotation.InjectRepository;
import eu.isygoit.jpa.json.JsonBasedService;
import eu.isygoit.multitenancy.model.EventEntity;
import eu.isygoit.multitenancy.model.UserLoginEntity;
import eu.isygoit.multitenancy.repository.EventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@InjectRepository(value = EventRepository.class)
public class UserLoginEventService extends JsonBasedService<UserLoginEntity, Long, EventEntity, EventRepository> {
    /**
     * Constructor that initializes class types and element type.
     * Using constructor injection for better testability.
     *
     * @param objectMapper
     */
    public UserLoginEventService(ObjectMapper objectMapper) {
        super(objectMapper);
    }
}