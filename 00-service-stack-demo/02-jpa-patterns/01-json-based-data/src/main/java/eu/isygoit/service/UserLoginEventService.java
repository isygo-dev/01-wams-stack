package eu.isygoit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.isygoit.annotation.InjectRepository;
import eu.isygoit.jpa.json.JsonBasedService;
import eu.isygoit.model.EventEntity;
import eu.isygoit.model.UserLoginEntity;
import eu.isygoit.repository.EventRepository;
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