package eu.isygoit.com.rest.controller.impl;

import eu.isygoit.com.rest.controller.ICrudControllerSubMethods;
import eu.isygoit.com.rest.controller.ResponseFactory;
import eu.isygoit.com.rest.service.ICrudServiceEvents;
import eu.isygoit.com.rest.service.ICrudServiceMethods;
import eu.isygoit.com.rest.service.ICrudServiceUtils;
import eu.isygoit.dto.IIdAssignableDto;
import eu.isygoit.dto.common.RequestContextDto;
import eu.isygoit.exception.BadArgumentException;
import eu.isygoit.helper.CriteriaHelper;
import eu.isygoit.jwt.filter.QueryCriteria;
import eu.isygoit.model.IIdAssignable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Abstract base class for CRUD controller sub-methods without tenant-specific operations.
 * Provides implementations for create, read, update, and delete operations.
 *
 * @param <I> the type of the identifier (must be Serializable)
 * @param <T> the entity type (must implement IIdAssignable)
 * @param <M> the minimal DTO type (must implement IIdAssignableDto)
 * @param <F> the full DTO type (extends M)
 * @param <S> the service type (must implement ICrudServiceMethods, ICrudServiceEvents, ICrudServiceUtils)
 */
@Slf4j
public abstract class CrudControllerSubMethods<
        I extends Serializable,
        T extends IIdAssignable<I>,
        M extends IIdAssignableDto<I>,
        F extends M,
        S extends ICrudServiceMethods<I, T> & ICrudServiceEvents<I, T> & ICrudServiceUtils<I, T>>
        extends CrudControllerUtils<I, T, M, F, S>
        implements ICrudControllerSubMethods<I, T, M, F, S> {

    private final Class<T> entityClass;

    @SuppressWarnings("unchecked")
    protected CrudControllerSubMethods() {
        this.entityClass = (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[1];
    }

    // region CRUD Operations

    /**
     * Creates a single entity.
     *
     * @param context Request context
     * @param dto     DTO to create the entity from
     * @return ResponseEntity containing the created DTO
     * @throws BadArgumentException if the DTO is null
     */
    @Override
    public ResponseEntity<F> subCreate(RequestContextDto context, F dto) {
        return executeWithMonitoring("subCreate", () -> {
            log.info("Creating {} for tenant: {}", entityClass.getSimpleName(), context.getSenderTenant());
            validateCreateRequest(dto);

            F processedDto = beforeCreate(dto);
            T entity = mapper().dtoToEntity(processedDto);
            T createdEntity = crudService().create(entity);
            T postProcessedEntity = afterCreate(createdEntity);
            F resultDto = mapper().entityToDto(postProcessedEntity);

            return ResponseFactory.responseCreated(resultDto);
        });
    }

    /**
     * Creates multiple entities in bulk.
     *
     * @param context Request context
     * @param dtos    List of DTOs to create entities from
     * @return ResponseEntity containing the list of created DTOs
     * @throws BadArgumentException if the DTO list is empty or exceeds max size
     */
    @Override
    public ResponseEntity<List<F>> subCreate(RequestContextDto context, List<F> dtos) {
        return executeWithMonitoring("subCreateBulk", () -> {
            log.info("Bulk creating {} entities for tenant: {}", dtos.size(), context.getSenderTenant());
            validateBulkOperation(dtos);

            List<T> entities = dtos.stream()
                    .map(this::beforeCreate)
                    .map(mapper()::dtoToEntity)
                    .toList();
            List<T> createdEntities = crudService().createBatch(entities);
            List<T> postProcessedEntities = createdEntities.stream()
                    .map(this::afterCreate)
                    .toList();
            List<F> resultDtos = mapper().listEntityToDto(postProcessedEntities);

            return ResponseFactory.responseOk(List.copyOf(resultDtos));
        });
    }

    /**
     * Updates multiple entities in bulk.
     *
     * @param context Request context
     * @param dtos    List of DTOs to update entities from
     * @return ResponseEntity containing the list of updated DTOs
     * @throws BadArgumentException if the DTO list is empty or exceeds max size
     */
    @Override
    public ResponseEntity<List<F>> subUpdate(RequestContextDto context, List<F> dtos) {
        return executeWithMonitoring("subUpdateBulk", () -> {
            log.info("Bulk updating {} entities for tenant: {}", dtos.size(), context.getSenderTenant());
            validateBulkOperation(dtos);

            List<T> entities = dtos.stream()
                    .map(dto -> beforeUpdate(dto.getId(), dto))
                    .map(mapper()::dtoToEntity)
                    .toList();
            List<T> updatedEntities = crudService().updateBatch(entities);
            List<T> postProcessedEntities = updatedEntities.stream()
                    .map(this::afterUpdate)
                    .toList();
            List<F> resultDtos = mapper().listEntityToDto(postProcessedEntities);

            return ResponseFactory.responseOk(List.copyOf(resultDtos));
        });
    }

    /**
     * Updates a single entity by ID.
     *
     * @param context Request context
     * @param id      ID of the entity to update
     * @param dto     DTO containing updated data
     * @return ResponseEntity containing the updated DTO
     *

     * @throws BadArgumentException if ID or DTO is null
     */
    @Override
    public ResponseEntity<F> subUpdate(RequestContextDto context, I id, F dto) {
        return executeWithMonitoring("subUpdateById", () -> {
            log.info("Updating {} with ID: {} for tenant: {}", entityClass.getSimpleName(), id, context.getSenderTenant());
            validateNotNull(id, "ID cannot be null");
            validateNotNull(dto, "DTO cannot be null");
            dto.setId(id);

            F processedDto = beforeUpdate(id, dto);
            T entity = mapper().dtoToEntity(processedDto);
            T updatedEntity = crudService().update(entity);
            T postProcessedEntity = afterUpdate(updatedEntity);
            F resultDto = mapper().entityToDto(postProcessedEntity);

            return ResponseFactory.responseOk(resultDto);
        });
    }

    /**
     * Deletes a single entity by ID.
     *
     * @param context Request context
     * @param id      ID of the entity to delete
     * @return ResponseEntity indicating success or failure
     * @throws BadArgumentException if ID is null
     */
    @Override
    public ResponseEntity<?> subDelete(RequestContextDto context, I id) {
        return executeWithMonitoring("subDelete", () -> {
            log.info("Deleting {} with ID: {} for tenant: {}", entityClass.getSimpleName(), id, context.getSenderTenant());
            validateNotNull(id, "ID cannot be null");

            if (!beforeDelete(id)) {
                log.warn("Pre-delete validation failed for ID: {}", id);
                return ResponseFactory.responseBadRequest();
            }

            crudService().delete(id);
            afterDelete(id);
            return ResponseFactory.responseNoContent();
        });
    }

    /**
     * Deletes multiple entities in bulk.
     *
     * @param context Request context
     * @param dtos    List of DTOs to delete
     * @return ResponseEntity indicating success or failure
     * @throws BadArgumentException if the DTO list is empty or exceeds max size
     */
    @Override
    public ResponseEntity<?> subDelete(RequestContextDto context, List<F> dtos) {
        return executeWithMonitoring("subDeleteBulk", () -> {
            log.info("Bulk deleting {} entities for tenant: {}", dtos.size(), context.getSenderTenant());
            validateBulkOperation(dtos);

            if (!beforeDelete(dtos)) {
                log.warn("Pre-delete validation failed for bulk operation");
                return ResponseFactory.responseBadRequest();
            }

            List<T> entities = mapper().listDtoToEntity(dtos);
            crudService().deleteBatch(entities);
            afterDelete(dtos);
            return ResponseFactory.responseOk(exceptionHandler().handleMessage("object.deleted.successfully"));
        });
    }

    // endregion

    // region Query Operations

    /**
     * Retrieves all entities for the default tenant.
     *
     * @param context Request context
     * @return ResponseEntity containing the list of minimal DTOs
     */
    @Override
    public ResponseEntity<List<M>> subFindAllDefault(RequestContextDto context) {
        return executeWithMonitoring("subFindAllDefault", () -> {
            log.info("Finding all default {}s", entityClass.getSimpleName());

            List<T> entities = crudService().findAll();
            List<M> resultDtos = minDtoMapper().listEntityToDto(entities);
            List<M> postProcessedDtos = afterFindAll(context, resultDtos);

            return createListResponse(postProcessedDtos);
        });
    }

    /**
     * Retrieves all entities.
     *
     * @param context Request context
     * @return ResponseEntity containing the list of minimal DTOs
     */
    @Override
    public ResponseEntity<List<M>> subFindAll(RequestContextDto context) {
        return executeWithMonitoring("subFindAll", () -> {
            log.info("Finding all {}s for tenant: {}", entityClass.getSimpleName(), context.getSenderTenant());

            List<T> entities = crudService().findAll();
            List<M> resultDtos = minDtoMapper().listEntityToDto(entities);
            List<M> postProcessedDtos = afterFindAll(context, resultDtos);

            return createListResponse(postProcessedDtos);
        });
    }

    /**
     * Retrieves paginated entities.
     *
     * @param context Request context
     * @param page    Page number (0-based)
     * @param size    Page size
     * @return ResponseEntity containing the list of minimal DTOs
     * @throws BadArgumentException if page or size is invalid
     */
    @Override
    public ResponseEntity<List<M>> subFindAll(RequestContextDto context, Integer page, Integer size) {
        return executeWithMonitoring("subFindAllPaginated", () -> {
            log.info("Finding paginated {}s (page: {}, size: {}) for tenant: {}",
                    entityClass.getSimpleName(), page, size, context.getSenderTenant());
            validateNotNull(page, "Page cannot be null");
            validateNotNull(size, "Size cannot be null");

            int validatedSize = validatePageSize(size);
            PageRequest pageRequest = PageRequest.of(page, validatedSize, Sort.by(Sort.Direction.DESC, CREATE_DATE_FIELD));

            List<T> entities = crudService().findAll(pageRequest);
            List<M> resultDtos = minDtoMapper().listEntityToDto(entities);
            List<M> postProcessedDtos = afterFindAll(context, resultDtos);

            return createListResponse(postProcessedDtos);
        });
    }

    /**
     * Retrieves all entities with full details.
     *
     * @param context Request context
     * @return ResponseEntity containing the list of full DTOs
     */
    @Override
    public ResponseEntity<List<F>> subFindAllFull(RequestContextDto context) {
        return executeWithMonitoring("subFindAllFull", () -> {
            log.info("Finding all full {}s for tenant: {}", entityClass.getSimpleName(), context.getSenderTenant());

            List<T> entities = crudService().findAll();
            List<F> resultDtos = mapper().listEntityToDto(entities);
            List<F> postProcessedDtos = afterFindAllFull(context, resultDtos);

            return createListResponse(postProcessedDtos);
        });
    }

    /**
     * Retrieves paginated entities with full details.
     *
     * @param context Request context
     * @param page    Page number (0-based)
     * @param size    Page size
     * @return ResponseEntity containing the list of full DTOs
     * @throws BadArgumentException if page or size is invalid
     */
    @Override
    public ResponseEntity<List<F>> subFindAllFull(RequestContextDto context, Integer page, Integer size) {
        return executeWithMonitoring("subFindAllFullPaginated", () -> {
            log.info("Finding paginated full {}s (page: {}, size: {}) for tenant: {}",
                    entityClass.getSimpleName(), page, size, context.getSenderTenant());
            validateNotNull(page, "Page cannot be null");
            validateNotNull(size, "Size cannot be null");

            int validatedSize = validatePageSize(size);
            PageRequest pageRequest = PageRequest.of(page, validatedSize, Sort.by(Sort.Direction.DESC, CREATE_DATE_FIELD));

            List<T> entities = crudService().findAll(pageRequest);
            List<F> resultDtos = mapper().listEntityToDto(entities);
            List<F> postProcessedDtos = afterFindAllFull(context, resultDtos);

            return createListResponse(postProcessedDtos);
        });
    }

    /**
     * Retrieves a single entity by ID.
     *
     * @param context Request context
     * @param id      ID of the entity to retrieve
     * @return ResponseEntity containing the DTO or not found response
     * @throws BadArgumentException if ID is null
     */
    @Override
    public ResponseEntity<F> subFindById(RequestContextDto context, I id) {
        return executeWithMonitoring("subFindById", () -> {
            log.info("Finding {} by ID: {} for tenant: {}", entityClass.getSimpleName(), id, context.getSenderTenant());
            validateNotNull(id, "ID cannot be null");

            Optional<T> entity = crudService().findById(id);
            return entity
                    .map(mapper()::entityToDto)
                    .map(this::afterFindById)
                    .map(ResponseFactory::responseOk)
                    .orElseGet(() -> ResponseFactory.responseNotFound());
        });
    }

    /**
     * Retrieves the count of entities.
     *
     * @param context Request context
     * @return ResponseEntity containing the count
     */
    @Override
    public ResponseEntity<Long> subGetCount(RequestContextDto context) {
        return executeWithMonitoring("subGetCount", () -> {
            log.info("Counting {}s for tenant: {}", entityClass.getSimpleName(), context.getSenderTenant());
            Long count = crudService().count();
            return ResponseFactory.responseOk(count);
        });
    }

    /**
     * Retrieves filtered entities based on criteria.
     *
     * @param context  Request context
     * @param criteria Filter criteria as a string
     * @return ResponseEntity containing the list of filtered DTOs
     */
    @Override
    public ResponseEntity<List<F>> subFindAllFilteredByCriteria(RequestContextDto context, String criteria) {
        return executeWithMonitoring("subFindAllFilteredByCriteria", () -> {
            log.info("Finding filtered {}s for tenant: {}", entityClass.getSimpleName(), context.getSenderTenant());
            log.debug("Filter criteria: {}", criteria);

            List<QueryCriteria> criteriaList = CriteriaHelper.convertSqlWhereToCriteria(criteria);
            List<T> entities = crudService().findAllByCriteriaFilter(criteriaList);
            List<F> resultDtos = mapper().listEntityToDto(entities);

            return createListResponse(resultDtos);
        });
    }

    /**
     * Retrieves paginated filtered entities based on criteria.
     *
     * @param context  Request context
     * @param criteria Filter criteria as a string
     * @param page     Page number (0-based)
     * @param size     Page size
     * @return ResponseEntity containing the list of filtered DTOs
     * @throws BadArgumentException if page or size is invalid
     */
    @Override
    public ResponseEntity<List<F>> subFindAllFilteredByCriteria(RequestContextDto context, String criteria, Integer page, Integer size) {
        return executeWithMonitoring("subFindAllFilteredByCriteriaPaginated", () -> {
            log.info("Finding paginated filtered {}s (page: {}, size: {}) for tenant: {}",
                    entityClass.getSimpleName(), page, size, context.getSenderTenant());
            log.debug("Filter criteria: {}", criteria);
            validateNotNull(page, "Page cannot be null");
            validateNotNull(size, "Size cannot be null");

            List<QueryCriteria> criteriaList = CriteriaHelper.convertSqlWhereToCriteria(criteria);
            int validatedSize = validatePageSize(size);
            PageRequest pageRequest = PageRequest.of(page, validatedSize, Sort.by(Sort.Direction.DESC, CREATE_DATE_FIELD));

            List<T> entities = crudService().findAllByCriteriaFilter(criteriaList, pageRequest);
            List<F> resultDtos = mapper().listEntityToDto(entities);

            return createListResponse(resultDtos);
        });
    }

    /**
     * Retrieves available filter criteria for the entity type.
     *
     * @return ResponseEntity containing the map of filter criteria
     */
    @Override
    public ResponseEntity<Map<String, String>> subfindAllFilterCriterias() {
        return executeWithMonitoring("subfindAllFilterCriterias", () -> {
            log.info("Retrieving filter criteria for {}", entityClass.getSimpleName());
            Map<String, String> criteriaMap = CriteriaHelper.getCriteriaData(entityClass);
            return createMapResponse(criteriaMap);
        });
    }

    // endregion

    // region Lifecycle Hooks

    /**
     * Hook called after entity creation.
     *
     * @param entity Created entity
     * @return Processed entity
     */
    @Override
    public T afterCreate(T entity) {
        log.debug("Post-create hook for {}", entityClass.getSimpleName());
        return entity;
    }

    /**
     * Hook called before entity update.
     *
     * @param id   ID of the entity to update
     * @param dto  DTO containing update data
     * @return Processed DTO
     */
    @Override
    public F beforeUpdate(I id, F dto) {
        log.debug("Pre-update hook for {} with ID: {}", entityClass.getSimpleName(), id);
        return dto;
    }

    /**
     * Hook called before entity creation.
     *
     * @param dto DTO to create
     * @return Processed DTO
     */
    @Override
    public F beforeCreate(F dto) {
        log.debug("Pre-create hook for {}", entityClass.getSimpleName());
        return dto;
    }

    /**
     * Hook called after entity update.
     *
     * @param entity Updated entity
     * @return Processed entity
     */
    @Override
    public T afterUpdate(T entity) {
        log.debug("Post-update hook for {}", entityClass.getSimpleName());
        return entity;
    }

    /**
     * Hook called before deleting an entity.
     *
     * @param id ID of the entity to delete
     * @return true if deletion is allowed, false otherwise
     */
    @Override
    public boolean beforeDelete(I id) {
        log.debug("Pre-delete hook for {} with ID: {}", entityClass.getSimpleName(), id);
        return true;
    }

    /**
     * Hook called after deleting an entity.
     *
     * @param id ID of the deleted entity
     * @return true if post-processing is successful
     */
    @Override
    public boolean afterDelete(I id) {
        log.debug("Post-delete hook for {} with ID: {}", entityClass.getSimpleName(), id);
        return true;
    }

    /**
     * Hook called before bulk deletion.
     *
     * @param dtos List of DTOs to delete
     * @return true if deletion is allowed, false otherwise
     */
    @Override
    public boolean beforeDelete(List<F> dtos) {
        log.debug("Pre-delete bulk hook for {} entities", dtos.size());
        return true;
    }

    /**
     * Hook called after bulk deletion.
     *
     * @param dtos List of deleted DTOs
     * @return true if post-processing is successful
     */
    @Override
    public boolean afterDelete(List<F> dtos) {
        log.debug("Post-delete bulk hook for {} entities", dtos.size());
        return true;
    }

    /**
     * Hook called after finding an entity by ID.
     *
     * @param dto Retrieved DTO
     * @return Processed DTO
     */
    @Override
    public F afterFindById(F dto) {
        log.debug("Post-find-by-id hook for {}", entityClass.getSimpleName());
        return dto;
    }

    /**
     * Hook called after retrieving all full DTOs.
     *
     * @param context Request context
     * @param dtos    List of retrieved DTOs
     * @return Processed list of DTOs
     */
    @Override
    public List<F> afterFindAllFull(RequestContextDto context, List<F> dtos) {
        log.debug("Post-find-all-full hook for {} entities", dtos.size());
        return dtos;
    }

    /**
     * Hook called after retrieving all minimal DTOs.
     *
     * @param context Request context
     * @param dtos    List of retrieved DTOs
     * @return Processed list of DTOs
     */
    @Override
    public List<M> afterFindAll(RequestContextDto context, List<M> dtos) {
        log.debug("Post-find-all hook for {} entities", dtos.size());
        return dtos;
    }

    // endregion

    // region Validation and Utility Methods

    /**
     * Validates a create request DTO.
     *
     * @param dto DTO to validate
     * @throws BadArgumentException if DTO is null
     */
    private void validateCreateRequest(F dto) {
        validateNotNull(dto, "Create request DTO cannot be null");
    }

    /**
     * Validates and adjusts page size.
     *
     * @param size Requested page size
     * @return Validated page size
     */
    private int validatePageSize(Integer size) {
        if (size == null || size <= 0) {
            log.debug("Invalid page size {}, using default: {}", size, DEFAULT_PAGE_SIZE);
            return DEFAULT_PAGE_SIZE;
        }
        if (size > MAX_PAGE_SIZE) {
            log.warn("Requested page size {} exceeds maximum {}, adjusting to maximum", size, MAX_PAGE_SIZE);
            return MAX_PAGE_SIZE;
        }
        return size;
    }

    /**
     * Validates that an object is not null.
     *
     * @param obj     Object to validate
     * @param message Error message if null
     * @throws BadArgumentException if object is null
     */
    private void validateNotNull(Object obj, String message) {
        if (obj == null) {
            throw new BadArgumentException(message);
        }
    }

    /**
     * Creates a response for a list result.
     *
     * @param results List of results
     * @return ResponseEntity with results or no content
     */
    private <R> ResponseEntity<List<R>> createListResponse(List<R> results) {
        return CollectionUtils.isEmpty(results) ?
                ResponseFactory.responseNoContent() :
                ResponseFactory.responseOk(List.copyOf(results));
    }

    /**
     * Creates a response for a map result.
     *
     * @param results Map of results
     * @return ResponseEntity with results or no content
     */
    private ResponseEntity<Map<String, String>> createMapResponse(Map<String, String> results) {
        return CollectionUtils.isEmpty(results) ?
                ResponseFactory.responseNoContent() :
                ResponseFactory.responseOk(results);
    }

    /**
     * Executes an operation with performance monitoring.
     *
     * @param operation Operation name
     * @param supplier  Operation to execute
     * @return ResponseEntity with operation result
     */
    private <R> ResponseEntity<R> executeWithMonitoring(String operation, Supplier<ResponseEntity<R>> supplier) {
        StopWatch stopWatch = new StopWatch(operation);
        stopWatch.start();
        try {
            ResponseEntity<R> result = supplier.get();
            stopWatch.stop();
            log.info("Completed {} in {}ms", operation, stopWatch.getTotalTimeMillis());
            return result;
        } catch (Exception e) {
            stopWatch.stop();
            log.error("Failed {} after {}ms: {}", operation, stopWatch.getTotalTimeMillis(), e.getMessage(), e);
            return getBackExceptionResponse(e);
        }
    }

    // endregion
}