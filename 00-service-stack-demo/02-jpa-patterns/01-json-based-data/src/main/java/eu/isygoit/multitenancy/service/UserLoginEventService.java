package eu.isygoit.multitenancy.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.isygoit.annotation.InjectRepository;
import eu.isygoit.com.rest.service.ICrudServiceEvents;
import eu.isygoit.com.rest.service.ICrudServiceMethods;
import eu.isygoit.com.rest.service.ICrudServiceUtils;
import eu.isygoit.exception.JpaRepositoryNotDefinedException;
import eu.isygoit.exception.ObjectNotFoundException;
import eu.isygoit.jwt.filter.QueryCriteria;
import eu.isygoit.multitenancy.common.UserLoginEntity;
import eu.isygoit.multitenancy.model.EventEntity;
import eu.isygoit.multitenancy.repository.EventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@Slf4j
@InjectRepository(value = EventRepository.class)
public class UserLoginEventService implements ICrudServiceMethods<UUID, UserLoginEntity>,
        ICrudServiceEvents<UUID, UserLoginEntity>,
        ICrudServiceUtils<UUID, UserLoginEntity> {

    private static final String USER_LOGIN_TYPE = "UserLogin";
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Long count() {
        return eventRepository.countByElementType(USER_LOGIN_TYPE);
    }

    @Override
    public boolean existsById(UUID id) {
        return eventRepository.existsByElementTypeAndJsonId(USER_LOGIN_TYPE, id.toString());
    }

    @Override
    public UserLoginEntity create(UserLoginEntity object) {
        if (object.getId() == null) {
            object.setId(UUID.randomUUID());
        }
        UserLoginEntity beforeCreateResult = beforeCreate(object);
        EventEntity entity = toEventEntity(beforeCreateResult, "tenant1");
        EventEntity saved = eventRepository.save(entity);
        UserLoginEntity result = toUserLoginEntity(saved);
        return afterCreate(result);
    }

    @Override
    public UserLoginEntity createAndFlush(UserLoginEntity object) {
        if (object.getId() == null) {
            object.setId(UUID.randomUUID());
        }
        UserLoginEntity beforeCreateResult = beforeCreate(object);
        EventEntity entity = toEventEntity(beforeCreateResult, "tenant1");
        EventEntity saved = eventRepository.saveAndFlush(entity);
        UserLoginEntity result = toUserLoginEntity(saved);
        return afterCreate(result);
    }

    @Override
    public List<UserLoginEntity> createBatch(List<UserLoginEntity> objects) {
        objects.forEach(obj -> {
            if (obj.getId() == null) {
                obj.setId(UUID.randomUUID());
            }
        });
        List<UserLoginEntity> beforeCreateResults = objects.stream()
                .map(this::beforeCreate)
                .toList();
        List<EventEntity> entities = beforeCreateResults.stream()
                .map(obj -> toEventEntity(obj, "tenant1"))
                .toList();
        List<UserLoginEntity> results = eventRepository.saveAll(entities)
                .stream()
                .map(this::toUserLoginEntity)
                .toList();
        return results.stream()
                .map(this::afterCreate)
                .toList();
    }

    @Override
    public void delete(UUID id) {
        beforeDelete(id);
        eventRepository.deleteByElementTypeAndJsonId(USER_LOGIN_TYPE, id.toString());
        afterDelete(id);
    }

    @Override
    public void deleteBatch(List<UserLoginEntity> objects) {
        beforeDelete(objects);
        List<String> ids = objects.stream().map(e -> e.getId().toString()).toList();
        eventRepository.deleteByElementTypeAndJsonIdIn(USER_LOGIN_TYPE, ids);
        afterDelete(objects);
    }

    @Override
    public List<UserLoginEntity> findAll() {
        List<UserLoginEntity> results = eventRepository.findAllByElementType(USER_LOGIN_TYPE)
                .stream().map(this::toUserLoginEntity).toList();
        return afterFindAll(results);
    }

    @Override
    public List<UserLoginEntity> findAll(Pageable pageable) {
        List<UserLoginEntity> results = eventRepository.findAllByElementType(USER_LOGIN_TYPE, pageable)
                .stream().map(this::toUserLoginEntity).toList();
        return afterFindAll(results);
    }

    @Override
    public Optional<UserLoginEntity> findById(UUID id) throws ObjectNotFoundException {
        Optional<UserLoginEntity> result = eventRepository.findByElementTypeAndJsonId(USER_LOGIN_TYPE, id.toString())
                .map(this::toUserLoginEntity);
        return result.map(this::afterFindById);
    }

    @Override
    public UserLoginEntity saveOrUpdate(UserLoginEntity object) {
        if (object.getId() == null) {
            return create(object);
        }
        return update(object);
    }

    @Override
    public List<UserLoginEntity> saveOrUpdate(List<UserLoginEntity> objects) {
        return objects.stream().map(this::saveOrUpdate).toList();
    }

    @Override
    public UserLoginEntity update(UserLoginEntity object) {
        UserLoginEntity beforeUpdateResult = beforeUpdate(object);
        Optional<EventEntity> optionalEntity =
                eventRepository.findByElementTypeAndJsonId(USER_LOGIN_TYPE, beforeUpdateResult.getId().toString());
        if (optionalEntity.isEmpty()) {
            throw new ObjectNotFoundException("UserLogin not found for id: " + beforeUpdateResult.getId());
        }
        EventEntity entity = optionalEntity.get();
        entity.setAttributes(objectMapper.valueToTree(beforeUpdateResult));
        UserLoginEntity result = toUserLoginEntity(eventRepository.save(entity));
        return afterUpdate(result);
    }

    @Override
    public UserLoginEntity updateAndFlush(UserLoginEntity object) {
        UserLoginEntity beforeUpdateResult = beforeUpdate(object);
        Optional<EventEntity> optionalEntity =
                eventRepository.findByElementTypeAndJsonId(USER_LOGIN_TYPE, beforeUpdateResult.getId().toString());
        if (optionalEntity.isEmpty()) {
            throw new ObjectNotFoundException("UserLogin not found for id: " + beforeUpdateResult.getId());
        }
        EventEntity entity = optionalEntity.get();
        entity.setAttributes(objectMapper.valueToTree(beforeUpdateResult));
        UserLoginEntity result = toUserLoginEntity(eventRepository.saveAndFlush(entity));
        return afterUpdate(result);
    }

    @Override
    public List<UserLoginEntity> updateBatch(List<UserLoginEntity> objects) {
        return objects.stream()
                .map(this::beforeUpdate)
                .map(this::update)
                .toList();
    }

    @Override
    public List<UserLoginEntity> findAllByCriteriaFilter(List<QueryCriteria> criteria) {
        // TODO: Implement dynamic filtering using JSON criteria
        return findAll();
    }

    @Override
    public List<UserLoginEntity> findAllByCriteriaFilter(List<QueryCriteria> criteria, PageRequest pageRequest) {
        // TODO: Implement dynamic filtering using JSON criteria + pagination
        return findAll(pageRequest);
    }

    // Event lifecycle methods
    @Override
    public UserLoginEntity beforeUpdate(UserLoginEntity object) {
        log.debug("Before update UserLogin: {}", object.getId());
        // Add custom validation or business logic here
        return object;
    }

    @Override
    public UserLoginEntity afterUpdate(UserLoginEntity object) {
        log.debug("After update UserLogin: {}", object.getId());
        // Add custom post-update logic here (e.g., notifications, audit logs)
        return object;
    }

    @Override
    public void beforeDelete(UUID id) {
        log.debug("Before delete UserLogin: {}", id);
        // Add custom pre-delete logic here (e.g., validation, cleanup)
    }

    @Override
    public void afterDelete(UUID id) {
        log.debug("After delete UserLogin: {}", id);
        // Add custom post-delete logic here (e.g., notifications, cleanup)
    }

    @Override
    public void beforeDelete(List<UserLoginEntity> objects) {
        log.debug("Before delete UserLogin batch: {} items", objects.size());
        // Add custom pre-delete logic for batch operations
    }

    @Override
    public void afterDelete(List<UserLoginEntity> objects) {
        log.debug("After delete UserLogin batch: {} items", objects.size());
        // Add custom post-delete logic for batch operations
    }

    @Override
    public UserLoginEntity beforeCreate(UserLoginEntity object) {
        log.debug("Before create UserLogin: {}", object.getUserId());
        // Add custom validation or business logic here
        // Example: validate IP format, set default values, etc.
        return object;
    }

    @Override
    public List<UserLoginEntity> afterFindAll(List<UserLoginEntity> list) {
        log.debug("After find all UserLogin: {} items", list.size());
        // Add custom post-find logic here (e.g., data enrichment, filtering)
        return list;
    }

    @Override
    public UserLoginEntity afterFindById(UserLoginEntity object) {
        log.debug("After find by id UserLogin: {}", object.getId());
        // Add custom post-find logic here (e.g., data enrichment)
        return object;
    }

    @Override
    public UserLoginEntity afterCreate(UserLoginEntity object) {
        log.debug("After create UserLogin: {}", object.getId());
        // Add custom post-create logic here (e.g., notifications, audit logs)
        return object;
    }

    @Override
    public Repository repository() throws JpaRepositoryNotDefinedException {
        return eventRepository;
    }

    // Helper methods
    private EventEntity toEventEntity(UserLoginEntity entity, String tenant) {
        JsonNode json = objectMapper.valueToTree(entity);
        return EventEntity.builder()
                .tenant(tenant)
                .elementType(USER_LOGIN_TYPE)
                .attributes(json)
                .build();
    }

    private UserLoginEntity toUserLoginEntity(EventEntity eventEntity) {
        return objectMapper.convertValue(eventEntity.getAttributes(), UserLoginEntity.class);
    }
}