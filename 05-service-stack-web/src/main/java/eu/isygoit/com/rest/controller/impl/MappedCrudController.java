package eu.isygoit.com.rest.controller.impl;

import eu.isygoit.com.rest.api.IMappedCrudApi;
import eu.isygoit.com.rest.service.ICrudServiceMethod;
import eu.isygoit.dto.IIdentifiableDto;
import eu.isygoit.dto.common.RequestContextDto;
import eu.isygoit.model.IIdEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * The type Mapped CRUD controller.
 * <p>
 * This abstract controller handles common CRUD operations for entities, including
 * creating, deleting, updating, fetching, and applying filters.
 *
 * @param <I>     the type parameter representing the ID of the entity
 * @param <T>     the type parameter representing the entity itself
 * @param <MIND>  the type parameter representing a minimal data transfer object (DTO)
 * @param <FULLD> the type parameter representing a full data transfer object (DTO)
 * @param <S>     the type parameter representing the service interface
 */
@Slf4j
public abstract class MappedCrudController<I extends Serializable, T extends IIdEntity,
        MIND extends IIdentifiableDto,
        FULLD extends MIND,
        S extends ICrudServiceMethod<I, T>>
        extends CrudControllerSubMethods<I, T, MIND, FULLD, S>
        implements IMappedCrudApi<I, MIND, FULLD> {

    /**
     * Creates a new entity.
     *
     * @param object the entity to create
     * @return ResponseEntity containing the created entity
     */
    @Override
    public final ResponseEntity<FULLD> create(FULLD object) {
        log.info("Creating new entity: {}", object);
        return subCreate(object);
    }

    /**
     * Deletes an entity by its ID.
     *
     * @param requestContext the context of the current request
     * @param id             the ID of the entity to delete
     * @return ResponseEntity confirming the deletion
     */
    @Override
    public final ResponseEntity<String> delete(RequestContextDto requestContext, I id) {
        log.info("Deleting entity with ID: {} in context: {}", id, requestContext);
        return subDelete(requestContext, id);
    }

    /**
     * Fetches all entities.
     *
     * @param requestContext the context of the current request
     * @return ResponseEntity containing the list of all entities
     */
    @Override
    public final ResponseEntity<List<MIND>> getAll(RequestContextDto requestContext) {
        log.info("Fetching all entities in context: {}", requestContext);
        return subGetAll(requestContext);
    }

    /**
     * Fetches entities assigned to the default domain.
     *
     * @param requestContext the context of the current request
     * @return ResponseEntity containing the list of entities assigned to the default domain
     */
    @Override
    public final ResponseEntity<List<MIND>> getAssignedToDefaultDomain(RequestContextDto requestContext) {
        log.info("Fetching entities assigned to default domain in context: {}", requestContext);
        return subGetAssignedToDefaultDomain(requestContext);
    }

    /**
     * Fetches all entities with full data.
     *
     * @param requestContext the context of the current request
     * @return ResponseEntity containing the list of full entities
     */
    @Override
    public final ResponseEntity<List<FULLD>> getAllFull(RequestContextDto requestContext) {
        log.info("Fetching all full entities in context: {}", requestContext);
        return subGetAllFull(requestContext);
    }

    /**
     * Fetches all entities with pagination.
     *
     * @param requestContext the context of the current request
     * @param page           the page number
     * @param size           the page size
     * @return ResponseEntity containing the paginated list of entities
     */
    @Override
    public final ResponseEntity<List<MIND>> getAllPaged(RequestContextDto requestContext, int page, int size) {
        log.info("Fetching paged entities - Page: {}, Size: {}", page, size);
        return subGetAllPaged(requestContext, page, size);
    }

    /**
     * Fetches all full entities with pagination.
     *
     * @param requestContext the context of the current request
     * @param page           the page number
     * @param size           the page size
     * @return ResponseEntity containing the paginated list of full entities
     */
    @Override
    public final ResponseEntity<List<FULLD>> getAllFullPaged(RequestContextDto requestContext, int page, int size) {
        log.info("Fetching paged full entities - Page: {}, Size: {}", page, size);
        return subGetAllFullPaged(requestContext, page, size);
    }

    /**
     * Fetches an entity by its ID.
     *
     * @param requestContext the context of the current request
     * @param id             the ID of the entity
     * @return ResponseEntity containing the entity
     */
    @Override
    public final ResponseEntity<FULLD> getById(RequestContextDto requestContext, I id) {
        log.info("Fetching entity by ID: {} in context: {}", id, requestContext);
        return subGetById(requestContext, id);
    }

    /**
     * Updates an entity by its ID.
     *
     * @param id     the ID of the entity
     * @param object the updated entity
     * @return ResponseEntity containing the updated entity
     */
    @Override
    public final ResponseEntity<FULLD> update(I id, FULLD object) {
        log.info("Updating entity with ID: {} - {}", id, object);
        return subUpdate(id, object);
    }

    /**
     * Gets the count of all entities.
     *
     * @param requestContext the context of the current request
     * @return ResponseEntity containing the count of entities
     */
    @Override
    public final ResponseEntity<Long> getCount(RequestContextDto requestContext) {
        log.info("Fetching entity count in context: {}", requestContext);
        return subGetCount(requestContext);
    }

    /**
     * Fetches entities filtered by criteria.
     *
     * @param requestContext the context of the current request
     * @param criteria       the filtering criteria
     * @return ResponseEntity containing the filtered list of entities
     */
    @Override
    public final ResponseEntity<List<FULLD>> getFiltered(RequestContextDto requestContext, String criteria) {
        log.info("Fetching filtered entities with criteria: {}", criteria);
        return subGetAllFiltered(requestContext, criteria);
    }

    /**
     * Fetches filtered entities with pagination.
     *
     * @param requestContext the context of the current request
     * @param criteria       the filtering criteria
     * @param page           the page number
     * @param size           the page size
     * @return ResponseEntity containing the paginated filtered list of entities
     */
    @Override
    public final ResponseEntity<List<FULLD>> getFilteredPaged(RequestContextDto requestContext, String criteria, int page, int size) {
        log.info("Fetching filtered paged entities - Criteria: {}, Page: {}, Size: {}", criteria, page, size);
        return subGetAllFilteredPaged(requestContext, criteria, page, size);
    }

    /**
     * Gets the available filter criteria.
     *
     * @return ResponseEntity containing the filter criteria
     */
    @Override
    public final ResponseEntity<Map<String, String>> getFilterCriteria() {
        log.info("Fetching available filter criteria");
        return subGetAllFilterCriteria();
    }
}