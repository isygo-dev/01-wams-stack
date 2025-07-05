package eu.isygoit.com.rest.controller.impl.tenancy;

import eu.isygoit.com.rest.controller.ICrudControllerSubMethods;
import eu.isygoit.com.rest.controller.ResponseFactory;
import eu.isygoit.com.rest.controller.impl.CrudControllerUtils;
import eu.isygoit.com.rest.service.ICrudServiceEvents;
import eu.isygoit.com.rest.service.ICrudServiceUtils;
import eu.isygoit.com.rest.service.ICrudTenantServiceMethods;
import eu.isygoit.constants.TenantConstants;
import eu.isygoit.dto.IIdAssignableDto;
import eu.isygoit.dto.common.RequestContextDto;
import eu.isygoit.exception.BadArgumentException;
import eu.isygoit.helper.CriteriaHelper;
import eu.isygoit.jwt.filter.QueryCriteria;
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
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Abstract base class for CRUD controller sub-methods with tenant-aware operations.
 * Provides implementations for create, read, update, and delete operations using functional interfaces.
 *
 * @param <I> the type of the identifier (must be Serializable)
 * @param <T> the entity type (must implement IIdAssignable)
 * @param <M> the minimal DTO type (must implement IIdentifiableDto)
 * @param <F> the full DTO type (extends M)
 * @param <S> the service type (must implement ICrudServiceMethod)
 */
@Slf4j
public abstract class CrudTenantControllerSubMethods<I extends Serializable,
        T extends IIdAssignable<I> & ITenantAssignable,
        M extends IIdAssignableDto,
        F extends M,
        S extends ICrudTenantServiceMethods<I, T> & ICrudServiceEvents<I, T> & ICrudServiceUtils<I, T>>
        extends CrudControllerUtils<I, T, M, F, S>
        implements ICrudControllerSubMethods<I, T, M, F, S> {

    private static final int DEFAULT_PAGE_SIZE = 50;
    private static final int MAX_PAGE_SIZE = 1000;
    private final Class<T> persistentClass =
            (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];

    /**
     * Creates a single entity for the specified tenant.
     *
     * @param requestContext the request context containing tenant information
     * @param object         the DTO to create
     * @return ResponseEntity containing the created DTO
     */
    @Override
    public final ResponseEntity<F> subCreate(RequestContextDto requestContext, F object) {
        return executeWithPerformanceMonitoring("subCreate", () -> {
            log.info("Create {} request received for tenant: {}", persistentClass.getSimpleName(), requestContext.getSenderTenant());
            log.debug("Input DTO: {}", object);

            // Validate input and prepare creation function
            validateCreateRequest(object);
            var createFunction = buildCreateFunction(requestContext);

            // Process creation pipeline
            return Optional.of(object)
                    .map(this::beforeCreate)
                    .map(dto -> {
                        log.debug("After pre-create hook: {}", dto);
                        return mapper().dtoToEntity(dto);
                    })
                    .map(createFunction)
                    .map(this::afterCreate)
                    .map(entity -> {
                        log.debug("After post-create hook: {}", entity);
                        return mapper().entityToDto(entity);
                    })
                    .map(dto -> {
                        log.debug("Final created DTO: {}", dto);
                        return ResponseFactory.responseCreated(dto);
                    })
                    .orElseThrow(() -> new BadArgumentException("Object creation failed"));
        });
    }

    /**
     * Creates multiple entities in bulk for the specified tenant.
     *
     * @param requestContext the request context containing tenant information
     * @param objects        the list of DTOs to create
     * @return ResponseEntity containing the list of created DTOs
     */
    @Override
    public final ResponseEntity<List<F>> subCreate(RequestContextDto requestContext, List<F> objects) {
        return executeWithPerformanceMonitoring("subCreateBulk", () -> {
            log.info("Bulk create {} entities request received for tenant: {}", persistentClass.getSimpleName(), requestContext.getSenderTenant());
            log.debug("Number of entities to create: {}", objects.size());

            // Validate input list
            if (CollectionUtils.isEmpty(objects)) {
                log.warn("Empty or null objects list received for bulk create operation");
                return ResponseFactory.responseBadRequest();
            }

            validateBulkSize(objects.size());
            var createFunction = buildCreateFunction(requestContext);

            // Process bulk creation in parallel
            var processedDtos = objects.parallelStream()
                    .map(dto -> {
                        log.debug("Processing creation for DTO: {}", dto);
                        return beforeCreate(dto);
                    })
                    .map(dto -> {
                        log.debug("Converting DTO to entity: {}", dto);
                        return mapper().dtoToEntity(dto);
                    })
                    .map(createFunction)
                    .map(entity -> {
                        log.debug("Applying post-create hook to entity: {}", entity);
                        return afterCreate(entity);
                    })
                    .toList();

            // Convert entities back to DTOs
            var result = mapper().listEntityToDto(processedDtos);
            log.debug("Created {} entities successfully", result.size());
            return ResponseFactory.responseOk(result);
        });
    }

    /**
     * Updates multiple entities in bulk for the specified tenant.
     *
     * @param requestContext the request context containing tenant information
     * @param objects        the list of DTOs to update
     * @return ResponseEntity containing the list of updated DTOs
     */
    @Override
    public final ResponseEntity<List<F>> subUpdate(RequestContextDto requestContext, List<F> objects) {
        return executeWithPerformanceMonitoring("subUpdate", () -> {
            log.info("Update {} entities request received for tenant: {}", persistentClass.getSimpleName(), requestContext.getSenderTenant());
            log.debug("Number of entities to update: {}", objects.size());

            // Validate input list
            if (CollectionUtils.isEmpty(objects)) {
                log.warn("Empty or null objects list received for update operation");
                return ResponseFactory.responseBadRequest();
            }

            validateBulkSize(objects.size());
            var updateFunction = buildUpdateFunction(requestContext);

            // Process bulk update in parallel
            var processedDtos = objects.parallelStream()
                    .map(dto -> {
                        log.debug("Processing update for DTO with ID: {}", dto.getId());
                        return beforeUpdate((I) dto.getId(), dto);
                    })
                    .map(dto -> {
                        log.debug("Converting DTO to entity: {}", dto);
                        return mapper().dtoToEntity(dto);
                    })
                    .map(updateFunction)
                    .map(entity -> {
                        log.debug("Applying post-update hook to entity: {}", entity);
                        return afterUpdate(entity);
                    })
                    .toList();

            // Convert entities back to DTOs
            var result = mapper().listEntityToDto(processedDtos);
            log.debug("Updated {} entities successfully", result.size());
            return ResponseFactory.responseOk(result);
        });
    }

    /**
     * Updates a single entity by ID for the specified tenant.
     *
     * @param requestContext the request context containing tenant information
     * @param id             the ID of the entity to update
     * @param object         the DTO containing updated data
     * @return ResponseEntity containing the updated DTO
     */
    @Override
    public final ResponseEntity<F> subUpdate(RequestContextDto requestContext, I id, F object) {
        return executeWithPerformanceMonitoring("subUpdateById", () -> {
            log.info("Update {} with ID: {} request received for tenant: {}", persistentClass.getSimpleName(), id, requestContext.getSenderTenant());
            log.debug("Input DTO: {}", object);

            // Validate input parameters
            if (object == null || id == null) {
                log.warn("Invalid parameters: object={}, id={}", object != null, id);
                return ResponseFactory.responseBadRequest();
            }

            object.setId(id);
            var updateFunction = buildUpdateFunction(requestContext);

            // Process update pipeline
            return Optional.of(object)
                    .map(dto -> {
                        log.debug("Applying pre-update hook for ID: {}", id);
                        return beforeUpdate(id, dto);
                    })
                    .map(dto -> {
                        log.debug("Converting DTO to entity: {}", dto);
                        return mapper().dtoToEntity(dto);
                    })
                    .map(updateFunction)
                    .map(entity -> {
                        log.debug("Applying post-update hook: {}", entity);
                        return afterUpdate(entity);
                    })
                    .map(entity -> {
                        log.debug("Converting entity to DTO: {}", entity);
                        return mapper().entityToDto(entity);
                    })
                    .map(dto -> {
                        log.debug("Final updated DTO: {}", dto);
                        return ResponseFactory.responseOk(dto);
                    })
                    .orElseThrow(() -> new BadArgumentException("Object update failed"));
        });
    }

    /**
     * Deletes a single entity by ID for the specified tenant.
     *
     * @param requestContext the request context containing tenant information
     * @param id             the ID of the entity to delete
     * @return ResponseEntity with no content on success
     */
    @Override
    public final ResponseEntity<?> subDelete(RequestContextDto requestContext, I id) {
        return executeWithPerformanceMonitoring("subDelete", () -> {
            log.info("Delete {} with ID: {} request received for tenant: {}", persistentClass.getSimpleName(), id, requestContext.getSenderTenant());

            // Validate ID
            if (id == null) {
                log.warn("Null ID received for delete operation");
                return ResponseFactory.responseBadRequest();
            }

            // Check pre-delete validation
            if (!beforeDelete(id)) {
                log.warn("Pre-delete validation failed for ID: {}", id);
                return ResponseFactory.responseBadRequest();
            }

            // Execute deletion
            var deleteFunction = buildDeleteFunction(requestContext);
            log.debug("Executing delete operation for ID: {}", id);
            deleteFunction.apply(id);
            log.debug("Applying post-delete hook for ID: {}", id);
            afterDelete(id);
            return ResponseFactory.responseNoContent();
        });
    }

    /**
     * Deletes multiple entities in bulk for the specified tenant.
     *
     * @param requestContext the request context containing tenant information
     * @param objects        the list of DTOs to delete
     * @return ResponseEntity with success message on completion
     */
    @Override
    public final ResponseEntity<?> subDelete(RequestContextDto requestContext, List<F> objects) {
        return executeWithPerformanceMonitoring("subDeleteBulk", () -> {
            log.info("Bulk delete {} entities request received for tenant: {}", persistentClass.getSimpleName(), requestContext.getSenderTenant());
            log.debug("Number of entities to delete: {}", objects.size());

            // Validate input list
            if (CollectionUtils.isEmpty(objects)) {
                log.warn("Empty or null objects list received for bulk delete operation");
                return ResponseFactory.responseBadRequest();
            }

            validateBulkSize(objects.size());

            // Check pre-delete validation
            if (!beforeDelete(objects)) {
                log.warn("Pre-delete validation failed for bulk delete operation");
                return ResponseFactory.responseBadRequest();
            }

            // Execute bulk deletion
            var deleteBulkFunction = buildDeleteBulkFunction(requestContext);
            log.debug("Executing bulk delete operation");
            deleteBulkFunction.apply(mapper().listDtoToEntity(objects));
            log.debug("Applying post-delete hook for bulk operation");
            afterDelete(objects);
            return ResponseFactory.responseOk(exceptionHandler().handleMessage("object.deleted.successfully"));
        });
    }

    /**
     * Retrieves all entities for the specified tenant.
     *
     * @param requestContext the request context containing tenant information
     * @return ResponseEntity containing the list of minimal DTOs
     * ^H
     */
    @Override
    public final ResponseEntity<List<M>> subFindAll(RequestContextDto requestContext) {
        return executeWithPerformanceMonitoring("subFindAll", () -> {
            log.info("Find all {}s request received for tenant: {}", persistentClass.getSimpleName(), requestContext.getSenderTenant());

            // Execute find all operation
            var findAllFunction = buildFindAllFunction(requestContext);
            log.debug("Executing find all operation");
            var list = Optional.ofNullable(findAllFunction.get())
                    .map(entities -> {
                        log.debug("Converting {} entities to minimal DTOs", entities.size());
                        return minDtoMapper().listEntityToDto(entities);
                    })
                    .map(l -> {
                        log.debug("Applying post-find-all hook");
                        return afterFindAll(requestContext, l);
                    })
                    .orElse(Collections.emptyList());

            // Return appropriate response based on result
            return CollectionUtils.isEmpty(list)
                    ? ResponseFactory.responseNoContent()
                    : ResponseFactory.responseOk(list);
        });
    }

    /**
     * Retrieves all entities for the default tenant.
     *
     * @param requestContext the request context containing tenant information
     * @return ResponseEntity containing the list of minimal DTOs
     */
    @Override
    public final ResponseEntity<List<M>> subFindAllDefault(RequestContextDto requestContext) {
        return executeWithPerformanceMonitoring("subFindAllDefault", () -> {
            log.info("Find all default {}s request received", persistentClass.getSimpleName());

            // Execute find all default operation
            var findAllDefaultFunction = buildFindAllDefaultFunction();
            log.debug("Executing find all default operation");
            var list = Optional.ofNullable(findAllDefaultFunction.get())
                    .map(entities -> {
                        log.debug("Converting {} entities to minimal DTOs", entities.size());
                        return minDtoMapper().listEntityToDto(entities);
                    })
                    .map(l -> {
                        log.debug("Applying post-find-all hook");
                        return afterFindAll(requestContext, l);
                    })
                    .orElse(Collections.emptyList());

            // Return appropriate response based on result
            return CollectionUtils.isEmpty(list)
                    ? ResponseFactory.responseNoContent()
                    : ResponseFactory.responseOk(list);
        });
    }

    /**
     * Retrieves paginated entities for the specified tenant.
     *
     * @param requestContext the request context containing tenant information
     * @param page           the page number
     * @param size           the page size
     * @return ResponseEntity containing the list of minimal DTOs
     */
    @Override
    public final ResponseEntity<List<M>> subFindAll(RequestContextDto requestContext, Integer page, Integer size) {
        return executeWithPerformanceMonitoring("subFindAllPaginated", () -> {
            log.info("Find all {}s by page/size request received {}/{} for tenant: {}", persistentClass.getSimpleName(), page, size, requestContext.getSenderTenant());

            // Validate and prepare pagination
            var validatedSize = validateAndAdjustPageSize(size);
            var pageRequest = PageRequest.of(page, validatedSize, Sort.by(Sort.Direction.DESC, "createDate"));
            log.debug("Pagination parameters: page={}, size={}", page, validatedSize);

            // Execute paginated find operation
            var findAllPaginatedFunction = buildFindAllPaginatedFunction(requestContext, pageRequest);
            log.debug("Executing paginated find operation");
            var list = Optional.ofNullable(findAllPaginatedFunction.get())
                    .map(entities -> {
                        log.debug("Converting {} entities to minimal DTOs", entities.size());
                        return minDtoMapper().listEntityToDto(entities);
                    })
                    .map(l -> {
                        log.debug("Applying post-find-all hook");
                        return afterFindAll(requestContext, l);
                    })
                    .orElse(Collections.emptyList());

            // Return appropriate response based on result
            return CollectionUtils.isEmpty(list)
                    ? ResponseFactory.responseNoContent()
                    : ResponseFactory.responseOk(list);
        });
    }

    /**
     * Retrieves all entities with full details for the specified tenant.
     *
     * @param requestContext the request context containing tenant information
     * @return ResponseEntity containing the list of full DTOs
     */
    @Override
    public final ResponseEntity<List<F>> subFindAllFull(RequestContextDto requestContext) {
        return executeWithPerformanceMonitoring("subFindAllFull", () -> {
            log.info("Find all full {}s request received for tenant: {}", persistentClass.getSimpleName(), requestContext.getSenderTenant());

            // Execute find all operation
            var findAllFunction = buildFindAllFunction(requestContext);
            log.debug("Executing find all full operation");
            var list = Optional.ofNullable(findAllFunction.get())
                    .map(entities -> {
                        log.debug("Converting {} entities to full DTOS", entities.size());
                        return mapper().listEntityToDto(entities);
                    })
                    .map(l -> {
                        log.debug("Applying post-find-all-full hook");
                        return afterFindAllFull(requestContext, l);
                    })
                    .orElse(Collections.emptyList());

            // Return appropriate response based on result
            return CollectionUtils.isEmpty(list)
                    ? ResponseFactory.responseNoContent()
                    : ResponseFactory.responseOk(list);
        });
    }

    /**
     * Retrieves paginated entities with full details for the specified tenant.
     *
     * @param requestContext the request context containing tenant information
     * @param page           the page number
     * @param size           the page size
     * @return ResponseEntity containing the list of full DTOs
     */
    @Override
    public final ResponseEntity<List<F>> subFindAllFull(RequestContextDto requestContext, Integer page, Integer size) {
        return executeWithPerformanceMonitoring("subFindAllFullPaginated", () -> {
            log.info("Find all full {}s by page/size request received {}/{} for tenant: {}", persistentClass.getSimpleName(), page, size, requestContext.getSenderTenant());

            // Validate pagination parameters
            if (page == null || size == null) {
                log.warn("Invalid pagination parameters: page={}, size={}", page, size);
                return ResponseFactory.responseBadRequest();
            }

            var validatedSize = validateAndAdjustPageSize(size);
            var pageRequest = PageRequest.of(page, validatedSize, Sort.by(Sort.Direction.DESC, "createDate"));
            log.debug("Pagination parameters: page={}, size={}", page, validatedSize);

            // Execute paginated find operation
            var findAllPaginatedFunction = buildFindAllPaginatedFunction(requestContext, pageRequest);
            log.debug("Executing paginated find full operation");
            var list = Optional.ofNullable(findAllPaginatedFunction.get())
                    .map(entities -> {
                        log.debug("Converting {} entities to full DTOs", entities.size());
                        return mapper().listEntityToDto(entities);
                    })
                    .map(l -> {
                        log.debug("Applying post-find-all-full hook");
                        return afterFindAllFull(requestContext, l);
                    })
                    .orElse(Collections.emptyList());

            // Return appropriate response based on result
            return CollectionUtils.isEmpty(list)
                    ? ResponseFactory.responseNoContent()
                    : ResponseFactory.responseOk(list);
        });
    }

    /**
     * Retrieves a single entity by ID for the specified tenant.
     *
     * @param requestContext the request context containing tenant information
     * @param id             the ID of the entity to retrieve
     * @return ResponseEntity containing the full DTO or not found response
     */
    @Override
    public final ResponseEntity<F> subFindById(RequestContextDto requestContext, I id) {
        return executeWithPerformanceMonitoring("subFindById", () -> {
            log.info("Find {} by id {} request received for tenant: {}", persistentClass.getSimpleName(), id, requestContext.getSenderTenant());

            // Validate ID
            if (id == null) {
                log.warn("Null ID received for find by ID operation");
                return ResponseFactory.responseBadRequest();
            }

            // Execute find by ID operation
            var findByIdFunction = buildFindByIdFunction(requestContext);
            log.debug("Executing find by ID operation for ID: {}", id);
            return findByIdFunction.apply(id)
                    .map(entity -> {
                        log.debug("Converting entity to DTO: {}", entity);
                        return mapper().entityToDto(entity);
                    })
                    .map(dto -> {
                        log.debug("Applying post-find-by-id hook");
                        return afterFindById(dto);
                    })
                    .map(dto -> {
                        log.debug("Final DTO: {}", dto);
                        return ResponseFactory.responseOk(dto);
                    })
                    .orElseGet(() -> {
                        log.info("No entity found with ID: {}", id);
                        return ResponseFactory.responseNotFound();
                    });
        });
    }

    /**
     * Retrieves the count of entities for the specified tenant.
     *
     * @param requestContext the request context containing tenant information
     * @return ResponseEntity containing the count of entities
     */
    @Override
    public final ResponseEntity<Long> subGetCount(RequestContextDto requestContext) {
        return executeWithPerformanceMonitoring("subGetCount", () -> {
            log.info("Get count {} request received for tenant: {}", persistentClass.getSimpleName(), requestContext.getSenderTenant());

            // Execute count operation
            var countFunction = buildCountFunction(requestContext);
            log.debug("Executing count operation");
            var count = countFunction.get();
            log.debug("Count result: {}", count);
            return ResponseFactory.responseOk(count);
        });
    }

    /**
     * Retrieves entities filtered by criteria for the specified tenant.
     *
     * @param requestContext the request context containing tenant information
     * @param criteria       the filter criteria as a string
     * @return ResponseEntity containing the list of filtered full DTOs
     */
    @Override
    public final ResponseEntity<List<F>> subFindAllFilteredByCriteria(RequestContextDto requestContext, String criteria) {
        return executeWithPerformanceMonitoring("subFindAllFilteredByCriteria", () -> {
            log.info("Find all {}s by criteria request received for tenant: {}", persistentClass.getSimpleName(), requestContext.getSenderTenant());
            log.debug("Filter criteria: {}", criteria);

            // Parse criteria and execute filtered find
            var criteriaList = CriteriaHelper.convertStringToCriteria(criteria, ",");
            var findByCriteriaFunction = buildFindByCriteriaFunction(requestContext, criteriaList);
            log.debug("Executing filtered find operation");
            var list = mapper().listEntityToDto(findByCriteriaFunction.get());
            log.debug("Found {} entities matching criteria", list.size());

            // Return appropriate response based on result
            return CollectionUtils.isEmpty(list)
                    ? ResponseFactory.responseNoContent()
                    : ResponseFactory.responseOk(list);
        });
    }

    /**
     * Retrieves paginated entities filtered by criteria for the specified tenant.
     *
     * @param requestContext the request context containing tenant information
     * @param criteria       the filter criteria as a string
     * @param page           the page number
     * @param size           the page size
     * @return ResponseEntity containing the list of filtered full DTOs
     */
    @Override
    public final ResponseEntity<List<F>> subFindAllFilteredByCriteria(RequestContextDto requestContext, String criteria,
                                                                      Integer page, Integer size) {
        return executeWithPerformanceMonitoring("subFindAllFilteredByCriteriaPaginated", () -> {
            log.info("Find all {}s by criteria with pagination {}/{} request received for tenant: {}", persistentClass.getSimpleName(), page, size, requestContext.getSenderTenant());
            log.debug("Filter criteria: {}", criteria);

            // Validate and prepare pagination
            var criteriaList = CriteriaHelper.convertStringToCriteria(criteria, ",");
            var validatedSize = validateAndAdjustPageSize(size);
            var pageRequest = PageRequest.of(page, validatedSize, Sort.by(Sort.Direction.DESC, "createDate"));
            log.debug("Pagination parameters: page={}, size={}", page, validatedSize);

            // Execute paginated filtered find
            var findByCriteriaPaginatedFunction = buildFindByCriteriaPaginatedFunction(requestContext, criteriaList, pageRequest);
            log.debug("Executing paginated filtered find operation");
            var list = mapper().listEntityToDto(findByCriteriaPaginatedFunction.get());
            log.debug("Found {} entities matching criteria", list.size());

            // Return appropriate response based on result
            return CollectionUtils.isEmpty(list)
                    ? ResponseFactory.responseNoContent()
                    : ResponseFactory.responseOk(list);
        });
    }

    /**
     * Retrieves available filter criteria for the entity type.
     *
     * @return ResponseEntity containing the map of filter criteria
     */
    @Override
    public final ResponseEntity<Map<String, String>> subfindAllFilterCriterias() {
        return executeWithPerformanceMonitoring("subfindAllFilterCriterias", () -> {
            log.info("Get filter criteria for {} request received", persistentClass.getSimpleName());

            // Retrieve and validate criteria map
            var criteriaMap = CriteriaHelper.getCriteriaData(persistentClass);
            log.debug("Retrieved {} filter criteria", criteriaMap.size());

            // Return appropriate response based on result
            return CollectionUtils.isEmpty(criteriaMap)
                    ? ResponseFactory.responseNoContent()
                    : ResponseFactory.responseOk(criteriaMap);
        });
    }

    // ========================================
    // LIFECYCLE HOOK METHODS
    // ========================================

    /**
     * Hook called after creating an entity. Can be overridden by subclasses.
     *
     * @param object the created entity
     * @return the processed entity
     */
    @Override
    public T afterCreate(T object) {
        log.debug("Post-create hook called for {}", persistentClass.getSimpleName());
        return object;
    }

    /**
     * Hook called before updating an entity. Can be overridden by subclasses.
     *
     * @param id     the ID of the entity to update
     * @param object the DTO with update data
     * @return the processed DTO
     */
    @Override
    public F beforeUpdate(I id, F object) {
        log.debug("Pre-update hook called for {} with ID: {}", persistentClass.getSimpleName(), id);
        return object;
    }

    /**
     * Hook called before creating an entity. Can be overridden by subclasses.
     *
     * @param object the DTO to create
     * @return the processed DTO
     */
    @Override
    public F beforeCreate(F object) {
        log.debug("Pre-create hook called for {}", persistentClass.getSimpleName());
        return object;
    }

    /**
     * Hook called after updating an entity. Can be overridden by subclasses.
     *
     * @param object the updated entity
     * @return the processed entity
     */
    @Override
    public T afterUpdate(T object) {
        log.debug("Post-update hook called for {}", persistentClass.getSimpleName());
        return object;
    }

    /**
     * Hook called before deleting an entity. Can be overridden by subclasses.
     *
     * @param id the ID of the entity to delete
     * @return true if deletion is allowed, false otherwise
     */
    @Override
    public boolean beforeDelete(I id) {
        log.debug("Pre-delete hook called for {} with ID: {}", persistentClass.getSimpleName(), id);
        return true;
    }

    /**
     * Hook called after deleting an entity. Can be overridden by subclasses.
     *
     * @param id the ID of the deleted entity
     * @return true if post-deletion processing is successful
     */
    @Override
    public boolean afterDelete(I id) {
        log.debug("Post-delete hook called for {} with ID: {}", persistentClass.getSimpleName(), id);
        return true;
    }

    /**
     * Hook called before bulk deletion. Can be overridden by subclasses.
     *
     * @param objects the list of DTOs to delete
     * @return true if deletion is allowed, false otherwise
     */
    @Override
    public boolean beforeDelete(List<F> objects) {
        log.debug("Pre-delete bulk hook called for {} entities of type {}", objects.size(), persistentClass.getSimpleName());
        return true;
    }

    /**
     * Hook called after bulk deletion. Can be overridden by subclasses.
     *
     * @param objects the list of deleted DTOs
     * @return true if post-deletion processing is successful
     */
    @Override
    public boolean afterDelete(List<F> objects) {
        log.debug("Post-delete bulk hook called for {} entities of type {}", objects.size(), persistentClass.getSimpleName());
        return true;
    }

    /**
     * Hook called after finding an entity by ID. Can be overridden by subclasses.
     *
     * @param object the retrieved DTO
     * @return the processed DTO
     */
    @Override
    public F afterFindById(F object) {
        log.debug("Post-find-by-id hook called for {}", persistentClass.getSimpleName());
        return object;
    }

    /**
     * Hook called after retrieving all full entities. Can be overridden by subclasses.
     *
     * @param requestContext the request context
     * @param list           the list of full DTOs
     * @return the processed list of DTOs
     */
    @Override
    public List<F> afterFindAllFull(RequestContextDto requestContext, List<F> list) {
        log.debug("Post-find-all-full hook called for {} entities of type {}", list.size(), persistentClass.getSimpleName());
        return list;
    }

    /**
     * Hook called after retrieving all minimal entities. Can be overridden by subclasses.
     *
     * @param requestContext the request context
     * @param list           the list of minimal DTOs
     * @return the processed list of DTOs
     */
    @Override
    public List<M> afterFindAll(RequestContextDto requestContext, List<M> list) {
        log.debug("Post-find-all hook called for {} entities of type {}", list.size(), persistentClass.getSimpleName());
        return list;
    }

    // ========================================
    // PRIVATE UTILITY METHODS
    // ========================================

    /**
     * Builds a function for creating entities with tenant awareness.
     *
     * @param requestContext the request context
     * @return Function to create an entity
     */
    private Function<T, T> buildCreateFunction(RequestContextDto requestContext) {
        log.debug("Building create function for tenant: {}", requestContext.getSenderTenant());
        return obj -> crudService().create(requestContext.getSenderTenant(), obj);
    }

    /**
     * Builds a function for updating entities with tenant awareness.
     *
     * @param requestContext the request context
     * @return Function to update an entity
     */
    private Function<T, T> buildUpdateFunction(RequestContextDto requestContext) {
        log.debug("Building update function for tenant: {}", requestContext.getSenderTenant());
        return obj -> crudService().update(requestContext.getSenderTenant(), obj);
    }

    /**
     * Builds a function for deleting a single entity with tenant awareness.
     *
     * @param requestContext the request context
     * @return Function to delete an entity by ID
     */
    private Function<I, Void> buildDeleteFunction(RequestContextDto requestContext) {
        log.debug("Building delete function for tenant: {}", requestContext.getSenderTenant());
        return id -> {
            crudService().delete(requestContext.getSenderTenant(), id);
            return null;
        };
    }

    /**
     * Builds a function for bulk deleting entities with tenant awareness.
     *
     * @param requestContext the request context
     * @return Function to delete a list of entities
     */
    private Function<List<T>, Void> buildDeleteBulkFunction(RequestContextDto requestContext) {
        log.debug("Building bulk delete function for tenant: {}", requestContext.getSenderTenant());
        return entities -> {
            crudService().delete(requestContext.getSenderTenant(), entities);
            return null;
        };
    }

    /**
     * Builds a supplier for retrieving all entities with tenant awareness.
     *
     * @param requestContext the request context
     * @return Supplier for retrieving all entities
     */
    private Supplier<List<T>> buildFindAllFunction(RequestContextDto requestContext) {
        log.debug("Building find all function for tenant: {}", requestContext.getSenderTenant());
        return () -> crudService().findAll(requestContext.getSenderTenant());
    }

    /**
     * Builds a supplier for retrieving all entities for the default tenant.
     *
     * @return Supplier for retrieving default tenant entities
     */
    private Supplier<List<T>> buildFindAllDefaultFunction() {
        log.debug("Building find all default function");
        return () -> crudService().findAll(TenantConstants.DEFAULT_TENANT_NAME);
    }

    /**
     * Builds a supplier for retrieving paginated entities with tenant awareness.
     *
     * @param requestContext the request context
     * @param pageRequest    the pagination parameters
     * @return Supplier for retrieving paginated entities
     */
    private Supplier<List<T>> buildFindAllPaginatedFunction(RequestContextDto requestContext, PageRequest pageRequest) {
        log.debug("Building paginated find function for tenant: {}", requestContext.getSenderTenant());
        return () -> crudService().findAll(requestContext.getSenderTenant(), pageRequest);
    }

    /**
     * Builds a function for retrieving an entity by ID with tenant awareness.
     *
     * @param requestContext the request context
     * @return Function to retrieve an entity by ID
     */
    private Function<I, Optional<T>> buildFindByIdFunction(RequestContextDto requestContext) {
        log.debug("Building find by ID function for tenant: {}", requestContext.getSenderTenant());
        return id -> crudService().findById(requestContext.getSenderTenant(), id);
    }

    /**
     * Builds a supplier for counting entities with tenant awareness.
     *
     * @param requestContext the request context
     * @return Supplier for counting entities
     */
    private Supplier<Long> buildCountFunction(RequestContextDto requestContext) {
        log.debug("Building count function for tenant: {}", requestContext.getSenderTenant());
        return () -> crudService().count(requestContext.getSenderTenant());
    }

    /**
     * Builds a supplier for retrieving entities filtered by criteria with tenant awareness.
     *
     * @param requestContext the request context
     * @param criteriaList   the list of criteria
     * @return Supplier for retrieving filtered entities
     */
    private Supplier<List<T>> buildFindByCriteriaFunction(RequestContextDto requestContext, List<QueryCriteria> criteriaList) {
        log.debug("Building find by criteria function for tenant: {}", requestContext.getSenderTenant());
        return () -> crudService().findAllByCriteriaFilter(requestContext.getSenderTenant(), criteriaList);
    }

    /**
     * Builds a supplier for retrieving paginated entities filtered by criteria with tenant awareness.
     *
     * @param requestContext the request context
     * @param criteriaList   the list of criteria
     * @param pageRequest    the pagination parameters
     * @return Supplier for retrieving paginated filtered entities
     */
    private Supplier<List<T>> buildFindByCriteriaPaginatedFunction(RequestContextDto requestContext,
                                                                   List<QueryCriteria> criteriaList,
                                                                   PageRequest pageRequest) {
        log.debug("Building paginated find by criteria function for tenant: {}", requestContext.getSenderTenant());
        return () -> crudService().findAllByCriteriaFilter(requestContext.getSenderTenant(), criteriaList, pageRequest);
    }

    /**
     * Validates that the create request contains a non-null object.
     *
     * @param object the DTO to validate
     * @throws BadArgumentException if the object is null
     */
    private void validateCreateRequest(F object) {
        if (object == null) {
            log.error("Null object received for create operation");
            throw new BadArgumentException("Object cannot be null for create operation");
        }
        log.debug("Create request validation passed");
    }

    /**
     * Validates that the bulk operation size does not exceed the maximum allowed size.
     *
     * @param size the number of entities in the bulk operation
     * @throws BadArgumentException if the size exceeds the maximum
     */
    private void validateBulkSize(int size) {
        if (size > MAX_PAGE_SIZE) {
            log.error("Bulk operation size {} exceeds maximum allowed size {}", size, MAX_PAGE_SIZE);
            throw new BadArgumentException(
                    String.format("Bulk operation size %d exceeds maximum allowed size %d", size, MAX_PAGE_SIZE));
        }
        log.debug("Bulk operation size validation passed: {}", size);
    }

    /**
     * Validates and adjusts the page size for pagination requests.
     *
     * @param size the requested page size
     * @return the validated page size
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
     * Executes an operation with performance monitoring and error handling.
     *
     * @param operationName the name of the operation
     * @param operation     the operation to execute
     * @param <R>           the response type
     * @return ResponseEntity containing the operation result
     */
    private <R> ResponseEntity<R> executeWithPerformanceMonitoring(String operationName, Supplier<ResponseEntity<R>> operation) {
        var stopWatch = new StopWatch(operationName);
        stopWatch.start();
        log.debug("Starting operation: {}", operationName);
        try {
            var result = operation.get();
            stopWatch.stop();
            log.info("Successfully completed {} in {}ms", operationName, stopWatch.getTotalTimeMillis());
            return result;
        } catch (Exception e) {
            stopWatch.stop();
            log.error("Failed to complete {} after {}ms. Error: {}", operationName, stopWatch.getTotalTimeMillis(), e.getMessage(), e);
            return getBackExceptionResponse(e);
        }
    }
}