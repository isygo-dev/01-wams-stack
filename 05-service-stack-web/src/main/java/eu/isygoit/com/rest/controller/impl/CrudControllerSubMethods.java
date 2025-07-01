package eu.isygoit.com.rest.controller.impl;

import eu.isygoit.com.rest.controller.ICrudControllerSubMethods;
import eu.isygoit.com.rest.controller.ResponseFactory;
import eu.isygoit.com.rest.controller.constants.CtrlConstants;
import eu.isygoit.com.rest.service.ICrudServiceMethod;
import eu.isygoit.constants.TenantConstants;
import eu.isygoit.dto.IIdentifiableDto;
import eu.isygoit.dto.common.RequestContextDto;
import eu.isygoit.exception.BadArgumentException;
import eu.isygoit.helper.CriteriaHelper;
import eu.isygoit.model.IIdAssignable;
import eu.isygoit.model.ITenantAssignable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Enhanced CRUD Controller providing comprehensive sub-methods for entity management.
 * This abstract class implements common CRUD operations with multi-tenant support,
 * performance optimizations, and comprehensive logging.
 *
 * <p>Features:
 * <ul>
 *   <li>Multi-tenant aware operations</li>
 *   <li>Comprehensive error handling and logging</li>
 *   <li>Performance monitoring with execution time tracking</li>
 *   <li>Parallel processing for bulk operations</li>
 *   <li>Optimized memory usage with streaming operations</li>
 * </ul>
 *
 * @param <I> the ID type parameter extending Serializable
 * @param <T> the entity type parameter extending IIdAssignable
 * @param <M> the minimal DTO type parameter extending IIdentifiableDto
 * @param <F> the full DTO type parameter extending M
 * @param <S> the service type parameter extending ICrudServiceMethod
 *
 * @author Enhanced with Java 17 best practices
 * @version 2.0
 * @since Java 17
 */
@Slf4j
public abstract class CrudControllerSubMethods<I extends Serializable, T extends IIdAssignable<I>,
        M extends IIdentifiableDto,
        F extends M,
        S extends ICrudServiceMethod<I, T>>
        extends CrudControllerUtils<I, T, M, F, S>
        implements ICrudControllerSubMethods<I, T, M, F, S> {

    /**
     * Cached persistent class type to avoid repeated reflection calls.
     * This optimization reduces the overhead of generic type resolution.
     */
    private final Class<T> persistentClass =
            (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];

    /**
     * Default page size for pagination operations to prevent memory issues
     */
    private static final int DEFAULT_PAGE_SIZE = 50;

    /**
     * Maximum allowed page size to prevent DoS attacks
     */
    private static final int MAX_PAGE_SIZE = 1000;

    /**
     * Creates a single entity with comprehensive validation and performance monitoring.
     *
     * @param requestContext the request context containing tenant information
     * @param object the entity DTO to create
     * @return ResponseEntity containing the created entity or error response
     */
    @Override
    public final ResponseEntity<F> subCreate(RequestContextDto requestContext, F object) {
        var stopWatch = new StopWatch("subCreate");
        stopWatch.start();

        log.info("Create {} request received for tenant: {}",
                persistentClass.getSimpleName(), requestContext.getSenderTenant());

        try {
            validateCreateRequest(object);

            var createFunction = buildCreateFunction(requestContext);

            var createdObject = Optional.of(object)
                    .map(this::beforeCreate)
                    .map(mapper()::dtoToEntity)
                    .map(createFunction)
                    .map(this::afterCreate)
                    .map(mapper()::entityToDto)
                    .orElseThrow(() -> new BadArgumentException("Object creation failed"));

            stopWatch.stop();
            log.info("Successfully created {} in {}ms",
                    persistentClass.getSimpleName(), stopWatch.getTotalTimeMillis());

            return ResponseFactory.responseCreated(createdObject);

        } catch (Exception e) {
            stopWatch.stop();
            log.error("Failed to create {} after {}ms. Error: {}",
                    persistentClass.getSimpleName(), stopWatch.getTotalTimeMillis(), e.getMessage(), e);
            return getBackExceptionResponse(e);
        }
    }

    /**
     * Updates multiple entities using parallel processing for improved performance.
     *
     * @param requestContext the request context containing tenant information
     * @param objects the list of entity DTOs to update
     * @return ResponseEntity containing the updated entities or error response
     */
    @Override
    public final ResponseEntity<List<F>> subUpdate(RequestContextDto requestContext, List<F> objects) {
        var stopWatch = new StopWatch("subUpdate");
        stopWatch.start();

        log.info("Update {} entities request received for tenant: {}",
                persistentClass.getSimpleName(), requestContext.getSenderTenant());

        if (CollectionUtils.isEmpty(objects)) {
            log.warn("Empty or null objects list received for update operation");
            return ResponseFactory.responseBadRequest();
        }

        try {
            validateBulkSize(objects.size());
            var updateFunction = buildUpdateFunction(requestContext);

            // Use parallel stream for better performance on large datasets
            var processedDtos = objects.parallelStream()
                    .map(f -> {
                        log.debug("Processing update for entity with ID: {}", f.getId());
                        return beforeUpdate((I) f.getId(), f);
                    })
                    .map(mapper()::dtoToEntity)
                    .map(updateFunction)
                    .map(this::afterUpdate)
                    .toList();

            var result = mapper().listEntityToDto(processedDtos);

            stopWatch.stop();
            log.info("Successfully updated {} entities in {}ms",
                    objects.size(), stopWatch.getTotalTimeMillis());

            return ResponseFactory.responseOk(result);

        } catch (Exception e) {
            stopWatch.stop();
            log.error("Failed to update {} entities after {}ms. Error: {}",
                    objects.size(), stopWatch.getTotalTimeMillis(), e.getMessage(), e);
            return getBackExceptionResponse(e);
        }
    }

    /**
     * Creates multiple entities using parallel processing for improved performance.
     *
     * @param requestContext the request context containing tenant information
     * @param objects the list of entity DTOs to create
     * @return ResponseEntity containing the created entities or error response
     */
    @Override
    public final ResponseEntity<List<F>> subCreate(RequestContextDto requestContext, List<F> objects) {
        var stopWatch = new StopWatch("subCreateBulk");
        stopWatch.start();

        log.info("Bulk create {} entities request received for tenant: {}",
                persistentClass.getSimpleName(), requestContext.getSenderTenant());

        if (CollectionUtils.isEmpty(objects)) {
            log.warn("Empty or null objects list received for bulk create operation");
            return ResponseFactory.responseBadRequest();
        }

        try {
            validateBulkSize(objects.size());
            var createFunction = buildCreateFunction(requestContext);

            // Use parallel processing for bulk operations
            var processedDtos = objects.parallelStream()
                    .map(f -> {
                        log.debug("Processing creation for new entity");
                        return beforeCreate(f);
                    })
                    .map(mapper()::dtoToEntity)
                    .map(createFunction)
                    .map(this::afterCreate)
                    .toList();

            var result = mapper().listEntityToDto(processedDtos);

            stopWatch.stop();
            log.info("Successfully created {} entities in {}ms",
                    objects.size(), stopWatch.getTotalTimeMillis());

            return ResponseFactory.responseOk(result);

        } catch (Exception e) {
            stopWatch.stop();
            log.error("Failed to create {} entities after {}ms. Error: {}",
                    objects.size(), stopWatch.getTotalTimeMillis(), e.getMessage(), e);
            return getBackExceptionResponse(e);
        }
    }

    /**
     * Deletes a single entity by ID with proper validation and lifecycle hooks.
     *
     * @param requestContext the request context containing tenant information
     * @param id the ID of the entity to delete
     * @return ResponseEntity indicating success or failure
     */
    @Override
    public final ResponseEntity<?> subDelete(RequestContextDto requestContext, I id) {
        var stopWatch = new StopWatch("subDelete");
        stopWatch.start();

        log.info("Delete {} with ID: {} request received for tenant: {}",
                persistentClass.getSimpleName(), id, requestContext.getSenderTenant());

        if (id == null) {
            log.warn("Null ID received for delete operation");
            return ResponseFactory.responseBadRequest();
        }

        try {
            if (beforeDelete(id)) {
                log.debug("Pre-delete validation passed for ID: {}", id);
                crudService().delete(requestContext.getSenderTenant(), id);
                afterDelete(id);

                stopWatch.stop();
                log.info("Successfully deleted {} with ID: {} in {}ms",
                        persistentClass.getSimpleName(), id, stopWatch.getTotalTimeMillis());
            } else {
                log.warn("Pre-delete validation failed for ID: {}", id);
            }

            return ResponseFactory.responseNoContent();

        } catch (Exception e) {
            stopWatch.stop();
            log.error("Failed to delete {} with ID: {} after {}ms. Error: {}",
                    persistentClass.getSimpleName(), id, stopWatch.getTotalTimeMillis(), e.getMessage(), e);
            return getBackExceptionResponse(e);
        }
    }

    /**
     * Deletes multiple entities with bulk validation and processing.
     *
     * @param requestContext the request context containing tenant information
     * @param objects the list of entity DTOs to delete
     * @return ResponseEntity indicating success or failure
     */
    @Override
    public final ResponseEntity<?> subDelete(RequestContextDto requestContext, List<F> objects) {
        var stopWatch = new StopWatch("subDeleteBulk");
        stopWatch.start();

        log.info("Bulk delete {} entities request received for tenant: {}",
                persistentClass.getSimpleName(), requestContext.getSenderTenant());

        if (CollectionUtils.isEmpty(objects)) {
            log.warn("Empty or null objects list received for bulk delete operation");
            return ResponseFactory.responseBadRequest();
        }

        try {
            validateBulkSize(objects.size());

            if (beforeDelete(objects)) {
                log.debug("Pre-delete validation passed for {} entities", objects.size());
                crudService().delete(requestContext.getSenderTenant(), mapper().listDtoToEntity(objects));
                afterDelete(objects);

                stopWatch.stop();
                log.info("Successfully deleted {} entities in {}ms",
                        objects.size(), stopWatch.getTotalTimeMillis());
            } else {
                log.warn("Pre-delete validation failed for bulk delete operation");
            }

            return ResponseFactory.responseOk(
                    exceptionHandler().handleMessage("object.deleted.successfully"));

        } catch (Exception e) {
            stopWatch.stop();
            log.error("Failed to delete {} entities after {}ms. Error: {}",
                    objects.size(), stopWatch.getTotalTimeMillis(), e.getMessage(), e);
            return getBackExceptionResponse(e);
        }
    }

    /**
     * Retrieves all entities with tenant-aware filtering and performance optimization.
     *
     * @param requestContext the request context containing tenant information
     * @return ResponseEntity containing all entities or no content response
     */
    @Override
    public final ResponseEntity<List<M>> subFindAll(RequestContextDto requestContext) {
        var stopWatch = new StopWatch("subFindAll");
        stopWatch.start();

        log.info("Find all {}s request received for tenant: {}",
                persistentClass.getSimpleName(), requestContext.getSenderTenant());

        try {
            var entities = isTenantAware(requestContext)
                    ? crudService().findAll(requestContext.getSenderTenant())
                    : crudService().findAll();

            var list = Optional.ofNullable(entities)
                    .map(minDtoMapper()::listEntityToDto)
                    .orElse(Collections.emptyList());

            stopWatch.stop();

            if (CollectionUtils.isEmpty(list)) {
                log.info("No {} entities found in {}ms",
                        persistentClass.getSimpleName(), stopWatch.getTotalTimeMillis());
                return ResponseFactory.responseNoContent();
            }

            log.info("Successfully retrieved {} entities in {}ms",
                    list.size(), stopWatch.getTotalTimeMillis());

            afterFindAll(requestContext, list);
            return ResponseFactory.responseOk(list);

        } catch (Exception e) {
            stopWatch.stop();
            log.error("Failed to find all {} entities after {}ms. Error: {}",
                    persistentClass.getSimpleName(), stopWatch.getTotalTimeMillis(), e.getMessage(), e);
            return getBackExceptionResponse(e);
        }
    }

    /**
     * Retrieves all entities from default tenant with optimized processing.
     *
     * @param requestContext the request context containing tenant information
     * @return ResponseEntity containing all entities from default tenant
     */
    @Override
    public final ResponseEntity<List<M>> subFindAllDefault(RequestContextDto requestContext) {
        var stopWatch = new StopWatch("subFindAllDefault");
        stopWatch.start();

        log.info("Find all default {}s request received", persistentClass.getSimpleName());

        try {
            var entities = isTenantAware(requestContext)
                    ? crudService().findAll(TenantConstants.DEFAULT_TENANT_NAME)
                    : crudService().findAll();

            var list = Optional.ofNullable(entities)
                    .map(minDtoMapper()::listEntityToDto)
                    .orElse(Collections.emptyList());

            stopWatch.stop();

            if (CollectionUtils.isEmpty(list)) {
                log.info("No default {} entities found in {}ms",
                        persistentClass.getSimpleName(), stopWatch.getTotalTimeMillis());
                return ResponseFactory.responseNoContent();
            }

            log.info("Successfully retrieved {} default entities in {}ms",
                    list.size(), stopWatch.getTotalTimeMillis());

            afterFindAll(requestContext, list);
            return ResponseFactory.responseOk(list);

        } catch (Exception e) {
            stopWatch.stop();
            log.error("Failed to find all default {} entities after {}ms. Error: {}",
                    persistentClass.getSimpleName(), stopWatch.getTotalTimeMillis(), e.getMessage(), e);
            return getBackExceptionResponse(e);
        }
    }

    /**
     * Retrieves entities with pagination and performance monitoring.
     *
     * @param requestContext the request context containing tenant information
     * @param page the page number (0-based)
     * @param size the page size
     * @return ResponseEntity containing paginated entities
     */
    @Override
    public final ResponseEntity<List<M>> subFindAll(RequestContextDto requestContext, Integer page, Integer size) {
        var stopWatch = new StopWatch("subFindAllPaginated");
        stopWatch.start();

        log.info("Find all {}s by page/size request received {}/{} for tenant: {}",
                persistentClass.getSimpleName(), page, size, requestContext.getSenderTenant());

        try {
            var validatedSize = validateAndAdjustPageSize(size);
            var pageRequest = PageRequest.of(page, validatedSize,
                    Sort.by(Sort.Direction.DESC, "createDate"));

            var entities = isTenantAware(requestContext)
                    ? crudService().findAll(requestContext.getSenderTenant(), pageRequest)
                    : crudService().findAll(pageRequest);

            var list = Optional.ofNullable(entities)
                    .map(minDtoMapper()::listEntityToDto)
                    .orElse(Collections.emptyList());

            stopWatch.stop();

            if (CollectionUtils.isEmpty(list)) {
                log.info("No {} entities found for page {}/{} in {}ms",
                        persistentClass.getSimpleName(), page, validatedSize, stopWatch.getTotalTimeMillis());
                return ResponseFactory.responseNoContent();
            }

            log.info("Successfully retrieved {} entities (page {}/{}) in {}ms",
                    list.size(), page, validatedSize, stopWatch.getTotalTimeMillis());

            afterFindAll(requestContext, list);
            return ResponseFactory.responseOk(list);

        } catch (Exception e) {
            stopWatch.stop();
            log.error("Failed to find paginated {} entities after {}ms. Error: {}",
                    persistentClass.getSimpleName(), stopWatch.getTotalTimeMillis(), e.getMessage(), e);
            return getBackExceptionResponse(e);
        }
    }

    /**
     * Retrieves all full entities with comprehensive data and performance optimization.
     *
     * @param requestContext the request context containing tenant information
     * @return ResponseEntity containing all full entities
     */
    @Override
    public final ResponseEntity<List<F>> subFindAllFull(RequestContextDto requestContext) {
        var stopWatch = new StopWatch("subFindAllFull");
        stopWatch.start();

        log.info("Find all full {}s request received for tenant: {}",
                persistentClass.getSimpleName(), requestContext.getSenderTenant());

        try {
            var entities = isTenantAware(requestContext)
                    ? crudService().findAll(requestContext.getSenderTenant())
                    : crudService().findAll();

            var list = Optional.ofNullable(entities)
                    .map(mapper()::listEntityToDto)
                    .orElse(Collections.emptyList());

            stopWatch.stop();

            if (CollectionUtils.isEmpty(list)) {
                log.info("No full {} entities found in {}ms",
                        persistentClass.getSimpleName(), stopWatch.getTotalTimeMillis());
                return ResponseFactory.responseNoContent();
            }

            log.info("Successfully retrieved {} full entities in {}ms",
                    list.size(), stopWatch.getTotalTimeMillis());

            afterFindAllFull(requestContext, list);
            return ResponseFactory.responseOk(list);

        } catch (Exception e) {
            stopWatch.stop();
            log.error("Failed to find all full {} entities after {}ms. Error: {}",
                    persistentClass.getSimpleName(), stopWatch.getTotalTimeMillis(), e.getMessage(), e);
            return getBackExceptionResponse(e);
        }
    }

    /**
     * Retrieves paginated full entities with validation and performance monitoring.
     *
     * @param requestContext the request context containing tenant information
     * @param page the page number (0-based)
     * @param size the page size
     * @return ResponseEntity containing paginated full entities
     */
    @Override
    public final ResponseEntity<List<F>> subFindAllFull(RequestContextDto requestContext, Integer page, Integer size) {
        var stopWatch = new StopWatch("subFindAllFullPaginated");
        stopWatch.start();

        log.info("Find all full {}s by page/size request received {}/{} for tenant: {}",
                persistentClass.getSimpleName(), page, size, requestContext.getSenderTenant());

        if (page == null || size == null) {
            log.warn("Invalid pagination parameters: page={}, size={}", page, size);
            return ResponseFactory.responseBadRequest();
        }

        try {
            var validatedSize = validateAndAdjustPageSize(size);
            var pageRequest = PageRequest.of(page, validatedSize,
                    Sort.by(Sort.Direction.DESC, "createDate"));

            var entities = isTenantAware(requestContext)
                    ? crudService().findAll(requestContext.getSenderTenant(), pageRequest)
                    : crudService().findAll(pageRequest);

            var list = Optional.ofNullable(entities)
                    .map(mapper()::listEntityToDto)
                    .orElse(Collections.emptyList());

            stopWatch.stop();

            if (CollectionUtils.isEmpty(list)) {
                log.info("No full {} entities found for page {}/{} in {}ms",
                        persistentClass.getSimpleName(), page, validatedSize, stopWatch.getTotalTimeMillis());
                return ResponseFactory.responseNoContent();
            }

            log.info("Successfully retrieved {} full entities (page {}/{}) in {}ms",
                    list.size(), page, validatedSize, stopWatch.getTotalTimeMillis());

            afterFindAllFull(requestContext, list);
            return ResponseFactory.responseOk(list);

        } catch (Exception e) {
            stopWatch.stop();
            log.error("Failed to find paginated full {} entities after {}ms. Error: {}",
                    persistentClass.getSimpleName(), stopWatch.getTotalTimeMillis(), e.getMessage(), e);
            return getBackExceptionResponse(e);
        }
    }

    /**
     * Retrieves a single entity by ID with comprehensive validation and performance monitoring.
     *
     * @param requestContext the request context containing tenant information
     * @param id the entity ID to search for
     * @return ResponseEntity containing the found entity or not found response
     */
    @Override
    public final ResponseEntity<F> subFindById(RequestContextDto requestContext, I id) {
        var stopWatch = new StopWatch("subFindById");
        stopWatch.start();

        log.info("Find {} by id {} request received for tenant: {}",
                persistentClass.getSimpleName(), id, requestContext.getSenderTenant());

        if (id == null) {
            log.warn("Null ID received for find by ID operation");
            return ResponseFactory.responseBadRequest();
        }

        try {
            var optionalEntity = isTenantAware(requestContext)
                    ? crudService().findById(requestContext.getSenderTenant(), id)
                    : crudService().findById(id);

            stopWatch.stop();

            return optionalEntity
                    .map(mapper()::entityToDto)
                    .map(dto -> {
                        log.info("Successfully found {} with ID: {} in {}ms",
                                persistentClass.getSimpleName(), id, stopWatch.getTotalTimeMillis());
                        return ResponseFactory.responseOk(afterFindById(dto));
                    })
                    .orElseGet(() -> {
                        log.info("No {} found with ID: {} in {}ms",
                                persistentClass.getSimpleName(), id, stopWatch.getTotalTimeMillis());
                        return ResponseFactory.responseNotFound();
                    });

        } catch (Exception e) {
            stopWatch.stop();
            log.error("Failed to find {} with ID: {} after {}ms. Error: {}",
                    persistentClass.getSimpleName(), id, stopWatch.getTotalTimeMillis(), e.getMessage(), e);
            return getBackExceptionResponse(e);
        }
    }

    /**
     * Retrieves the count of entities with tenant-aware filtering.
     *
     * @param requestContext the request context containing tenant information
     * @return ResponseEntity containing the entity count
     */
    @Override
    public final ResponseEntity<Long> subGetCount(RequestContextDto requestContext) {
        var stopWatch = new StopWatch("subGetCount");
        stopWatch.start();

        log.info("Get count {} request received for tenant: {}",
                persistentClass.getSimpleName(), requestContext.getSenderTenant());

        try {
            var count = isTenantAware(requestContext)
                    ? crudService().count(requestContext.getSenderTenant())
                    : crudService().count();

            stopWatch.stop();
            log.info("Successfully retrieved count {} for {} in {}ms",
                    count, persistentClass.getSimpleName(), stopWatch.getTotalTimeMillis());

            return ResponseFactory.responseOk(count);

        } catch (Exception e) {
            stopWatch.stop();
            log.error("Failed to get count for {} after {}ms. Error: {}",
                    persistentClass.getSimpleName(), stopWatch.getTotalTimeMillis(), e.getMessage(), e);
            return getBackExceptionResponse(e);
        }
    }

    /**
     * Updates a single entity by ID with comprehensive validation and performance monitoring.
     *
     * @param requestContext the request context containing tenant information
     * @param id the entity ID to update
     * @param object the updated entity data
     * @return ResponseEntity containing the updated entity
     */
    @Override
    public final ResponseEntity<F> subUpdate(RequestContextDto requestContext, I id, F object) {
        var stopWatch = new StopWatch("subUpdateById");
        stopWatch.start();

        log.info("Update {} with ID: {} request received for tenant: {}",
                persistentClass.getSimpleName(), id, requestContext.getSenderTenant());

        if (object == null || id == null) {
            log.warn("Invalid parameters: object={}, id={}", object != null, id);
            return ResponseFactory.responseBadRequest();
        }

        try {
            object.setId(id);
            var updateFunction = buildUpdateFunction(requestContext);

            var updatedObject = Optional.of(object)
                    .map(o -> beforeUpdate(id, o))
                    .map(mapper()::dtoToEntity)
                    .map(updateFunction)
                    .map(this::afterUpdate)
                    .map(mapper()::entityToDto)
                    .orElseThrow(() -> new BadArgumentException("Object update failed"));

            stopWatch.stop();
            log.info("Successfully updated {} with ID: {} in {}ms",
                    persistentClass.getSimpleName(), id, stopWatch.getTotalTimeMillis());

            return ResponseFactory.responseOk(updatedObject);

        } catch (Exception e) {
            stopWatch.stop();
            log.error("Failed to update {} with ID: {} after {}ms. Error: {}",
                    persistentClass.getSimpleName(), id, stopWatch.getTotalTimeMillis(), e.getMessage(), e);
            return getBackExceptionResponse(e);
        }
    }

    /**
     * Retrieves entities filtered by criteria with performance optimization.
     *
     * @param requestContext the request context containing tenant information
     * @param criteria the filter criteria string
     * @return ResponseEntity containing filtered entities
     */
    @Override
    public final ResponseEntity<List<F>> subFindAllFilteredByCriteria(RequestContextDto requestContext, String criteria) {
        var stopWatch = new StopWatch("subFindAllFilteredByCriteria");
        stopWatch.start();

        log.info("Find all {}s by criteria request received for tenant: {}",
                persistentClass.getSimpleName(), requestContext.getSenderTenant());
        log.debug("Filter criteria: {}", criteria);

        try {
            var criteriaList = CriteriaHelper.convertStringToCriteria(criteria, ",");
            var senderTenant = isTenantAware(requestContext) ? requestContext.getSenderTenant() : null;

            var entities = crudService().findAllByCriteriaFilter(senderTenant, criteriaList);
            var list = mapper().listEntityToDto(entities);

            stopWatch.stop();

            if (CollectionUtils.isEmpty(list)) {
                log.info("No {} entities found matching criteria in {}ms",
                        persistentClass.getSimpleName(), stopWatch.getTotalTimeMillis());
                return ResponseFactory.responseNoContent();
            }

            log.info("Successfully retrieved {} filtered entities in {}ms",
                    list.size(), stopWatch.getTotalTimeMillis());

            return ResponseFactory.responseOk(list);

        } catch (Exception e) {
            stopWatch.stop();
            log.error("Failed to find filtered {} entities after {}ms. Error: {}",
                    persistentClass.getSimpleName(), stopWatch.getTotalTimeMillis(), e.getMessage(), e);
            return getBackExceptionResponse(e);
        }
    }

    /**
     * Retrieves paginated entities filtered by criteria with performance optimization.
     *
     * @param requestContext the request context containing tenant information
     * @param criteria the filter criteria string
     * @param page the page number (0-based)
     * @param size the page size
     * @return ResponseEntity containing paginated filtered entities
     */
    @Override
    public final ResponseEntity<List<F>> subFindAllFilteredByCriteria(RequestContextDto requestContext,
                                                                      String criteria, Integer page, Integer size) {
        var stopWatch = new StopWatch("subFindAllFilteredByCriteriaPaginated");
        stopWatch.start();

        log.info("Find all {}s by criteria with pagination {}/{} request received for tenant: {}",
                persistentClass.getSimpleName(), page, size, requestContext.getSenderTenant());
        log.debug("Filter criteria: {}", criteria);

        try {
            var criteriaList = CriteriaHelper.convertStringToCriteria(criteria, ",");
            var validatedSize = validateAndAdjustPageSize(size);
            var pageRequest = PageRequest.of(page, validatedSize,
                    Sort.by(Sort.Direction.DESC, "createDate"));
            var senderTenant = isTenantAware(requestContext) ? requestContext.getSenderTenant() : null;

            var entities = crudService().findAllByCriteriaFilter(senderTenant, criteriaList, pageRequest);
            var list = mapper().listEntityToDto(entities);

            stopWatch.stop();

            if (CollectionUtils.isEmpty(list)) {
                log.info("No {} entities found matching criteria for page {}/{} in {}ms",
                        persistentClass.getSimpleName(), page, validatedSize, stopWatch.getTotalTimeMillis());
                return ResponseFactory.responseNoContent();
            }

            log.info("Successfully retrieved {} filtered entities (page {}/{}) in {}ms",
                    list.size(), page, validatedSize, stopWatch.getTotalTimeMillis());

            return ResponseFactory.responseOk(list);

        } catch (Exception e) {
            stopWatch.stop();
            log.error("Failed to find paginated filtered {} entities after {}ms. Error: {}",
                    persistentClass.getSimpleName(), stopWatch.getTotalTimeMillis(), e.getMessage(), e);
            return getBackExceptionResponse(e);
        }
    }

    /**
     * Retrieves available filter criteria for the entity type.
     *
     * @return ResponseEntity containing the criteria map or no content response
     */
    @Override
    public final ResponseEntity<Map<String, String>> subfindAllFilterCriterias() {
        var stopWatch = new StopWatch("subfindAllFilterCriterias");
        stopWatch.start();

        log.info("Get filter criteria for {} request received", persistentClass.getSimpleName());

        try {
            var criteriaMap = CriteriaHelper.getCriteriaData(persistentClass);

            stopWatch.stop();

            if (CollectionUtils.isEmpty(criteriaMap)) {
                log.info("No filter criteria available for {} in {}ms",
                        persistentClass.getSimpleName(), stopWatch.getTotalTimeMillis());
                return ResponseFactory.responseNoContent();
            }

            log.info("Successfully retrieved {} filter criteria for {} in {}ms",
                    criteriaMap.size(), persistentClass.getSimpleName(), stopWatch.getTotalTimeMillis());

            return ResponseFactory.responseOk(criteriaMap);

        } catch (Exception e) {
            stopWatch.stop();
            log.error("Failed to get filter criteria for {} after {}ms. Error: {}",
                    persistentClass.getSimpleName(), stopWatch.getTotalTimeMillis(), e.getMessage(), e);
            return getBackExceptionResponse(e);
        }
    }

    // ========================================
    // LIFECYCLE HOOK METHODS (Can be overridden by subclasses)
    // ========================================

    /**
     * Hook method called after successful entity creation.
     * Subclasses can override this method to add custom post-creation logic.
     *
     * @param object the created entity
     * @return the entity (potentially modified)
     */
    @Override
    public T afterCreate(T object) {
        log.debug("Post-create hook called for {}", persistentClass.getSimpleName());
        return object;
    }

    /**
     * Hook method called before entity update.
     * Subclasses can override this method to add custom pre-update validation or modification.
     *
     * @param id the entity ID being updated
     * @param object the entity DTO to update
     * @return the entity DTO (potentially modified)
     */
    @Override
    public F beforeUpdate(I id, F object) {
        log.debug("Pre-update hook called for {} with ID: {}", persistentClass.getSimpleName(), id);
        return object;
    }

    /**
     * Hook method called before entity creation.
     * Subclasses can override this method to add custom pre-creation validation or modification.
     *
     * @param object the entity DTO to create
     * @return the entity DTO (potentially modified)
     */
    @Override
    public F beforeCreate(F object) {
        log.debug("Pre-create hook called for {}", persistentClass.getSimpleName());
        return object;
    }

    /**
     * Hook method called after successful entity update.
     * Subclasses can override this method to add custom post-update logic.
     *
     * @param object the updated entity
     * @return the entity (potentially modified)
     */
    @Override
    public T afterUpdate(T object) {
        log.debug("Post-update hook called for {}", persistentClass.getSimpleName());
        return object;
    }

    /**
     * Hook method called before single entity deletion.
     * Subclasses can override this method to add custom pre-deletion validation.
     *
     * @param id the entity ID to delete
     * @return true if deletion should proceed, false otherwise
     */
    @Override
    public boolean beforeDelete(I id) {
        log.debug("Pre-delete hook called for {} with ID: {}", persistentClass.getSimpleName(), id);
        return true;
    }

    /**
     * Hook method called after successful single entity deletion.
     * Subclasses can override this method to add custom post-deletion logic.
     *
     * @param id the deleted entity ID
     * @return true if post-deletion processing was successful
     */
    @Override
    public boolean afterDelete(I id) {
        log.debug("Post-delete hook called for {} with ID: {}", persistentClass.getSimpleName(), id);
        return true;
    }

    /**
     * Hook method called before bulk entity deletion.
     * Subclasses can override this method to add custom pre-deletion validation.
     *
     * @param objects the entity DTOs to delete
     * @return true if deletion should proceed, false otherwise
     */
    @Override
    public boolean beforeDelete(List<F> objects) {
        log.debug("Pre-delete bulk hook called for {} entities of type {}",
                objects.size(), persistentClass.getSimpleName());
        return true;
    }

    /**
     * Hook method called after successful bulk entity deletion.
     * Subclasses can override this method to add custom post-deletion logic.
     *
     * @param objects the deleted entity DTOs
     * @return true if post-deletion processing was successful
     */
    @Override
    public boolean afterDelete(List<F> objects) {
        log.debug("Post-delete bulk hook called for {} entities of type {}",
                objects.size(), persistentClass.getSimpleName());
        return true;
    }

    /**
     * Hook method called after finding an entity by ID.
     * Subclasses can override this method to add custom post-retrieval processing.
     *
     * @param object the found entity DTO
     * @return the entity DTO (potentially modified)
     */
    @Override
    public F afterFindById(F object) {
        log.debug("Post-find-by-id hook called for {}", persistentClass.getSimpleName());
        return object;
    }

    /**
     * Hook method called after finding all full entities.
     * Subclasses can override this method to add custom post-retrieval processing.
     *
     * @param requestContext the request context
     * @param list the found entity DTOs
     * @return the entity DTOs list (potentially modified)
     */
    @Override
    public List<F> afterFindAllFull(RequestContextDto requestContext, List<F> list) {
        log.debug("Post-find-all-full hook called for {} entities of type {}",
                list.size(), persistentClass.getSimpleName());
        return list;
    }

    /**
     * Hook method called after finding all minimal entities.
     * Subclasses can override this method to add custom post-retrieval processing.
     *
     * @param requestContext the request context
     * @param list the found entity DTOs
     * @return the entity DTOs list (potentially modified)
     */
    @Override
    public List<M> afterFindAll(RequestContextDto requestContext, List<M> list) {
        log.debug("Post-find-all hook called for {} entities of type {}",
                list.size(), persistentClass.getSimpleName());
        return list;
    }

    // ========================================
    // PRIVATE UTILITY METHODS
    // ========================================

    /**
     * Determines if the current entity type is tenant-aware and the request is not from super tenant.
     *
     * @param requestContext the request context containing tenant information
     * @return true if tenant filtering should be applied, false otherwise
     */
    private boolean isTenantAware(RequestContextDto requestContext) {
        return ITenantAssignable.class.isAssignableFrom(persistentClass) &&
                !TenantConstants.SUPER_TENANT_NAME.equals(requestContext.getSenderTenant());
    }

    /**
     * Builds a create function based on tenant awareness.
     *
     * @param requestContext the request context containing tenant information
     * @return the appropriate create function
     */
    private Function<T, T> buildCreateFunction(RequestContextDto requestContext) {
        return obj -> {
            if (obj instanceof ITenantAssignable) {
                log.debug("Creating tenant-aware entity for tenant: {}", requestContext.getSenderTenant());
                return crudService().create(requestContext.getSenderTenant(), obj);
            } else {
                log.debug("Creating non-tenant entity");
                return crudService().create(obj);
            }
        };
    }

    /**
     * Builds an update function based on tenant awareness.
     *
     * @param requestContext the request context containing tenant information
     * @return the appropriate update function
     */
    private Function<T, T> buildUpdateFunction(RequestContextDto requestContext) {
        return obj -> {
            if (obj instanceof ITenantAssignable) {
                log.debug("Updating tenant-aware entity for tenant: {}", requestContext.getSenderTenant());
                return crudService().update(requestContext.getSenderTenant(), obj);
            } else {
                log.debug("Updating non-tenant entity");
                return crudService().update(obj);
            }
        };
    }

    /**
     * Validates the create request object.
     *
     * @param object the object to validate
     * @throws BadArgumentException if validation fails
     */
    private void validateCreateRequest(F object) {
        if (object == null) {
            throw new BadArgumentException("Object cannot be null for create operation");
        }
        log.debug("Create request validation passed");
    }

    /**
     * Validates bulk operation size to prevent performance issues.
     *
     * @param size the size of the bulk operation
     * @throws BadArgumentException if size exceeds limits
     */
    private void validateBulkSize(int size) {
        if (size > MAX_PAGE_SIZE) {
            log.warn("Bulk operation size {} exceeds maximum allowed size {}", size, MAX_PAGE_SIZE);
            throw new BadArgumentException(
                    String.format("Bulk operation size %d exceeds maximum allowed size %d", size, MAX_PAGE_SIZE));
        }
        log.debug("Bulk operation size validation passed: {}", size);
    }

    /**
     * Validates and adjusts page size to prevent performance issues.
     *
     * @param size the requested page size
     * @return the validated and potentially adjusted page size
     */
    private int validateAndAdjustPageSize(Integer size) {
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
     * Utility method to execute operations with performance monitoring.
     * This method can be used for future enhancements requiring consistent performance tracking.
     *
     * @param operationName the name of the operation for logging
     * @param operation the operation to execute
     * @param <R> the return type
     * @return the result of the operation
     */
    private <R> R executeWithPerformanceMonitoring(String operationName, Supplier<R> operation) {
        var stopWatch = new StopWatch(operationName);
        stopWatch.start();

        try {
            var result = operation.get();
            stopWatch.stop();
            log.debug("Operation {} completed in {}ms", operationName, stopWatch.getTotalTimeMillis());
            return result;
        } catch (Exception e) {
            stopWatch.stop();
            log.error("Operation {} failed after {}ms: {}", operationName, stopWatch.getTotalTimeMillis(), e.getMessage());
            throw e;
        }
    }

    /**
     * Utility method for async operations that might be useful for future enhancements.
     *
     * @param operation the operation to execute asynchronously
     * @param <R> the return type
     * @return CompletableFuture containing the result
     */
    private <R> CompletableFuture<R> executeAsync(Supplier<R> operation) {
        return CompletableFuture.supplyAsync(operation)
                .exceptionally(throwable -> {
                    log.error("Async operation failed: {}", throwable.getMessage(), throwable);
                    throw new RuntimeException(throwable);
                });
    }
}