package eu.isygoit.com.rest.controller.impl;

import eu.isygoit.com.rest.api.IMappedCrudFetchApi;
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
 * The type Mapped CRUD fetch controller.
 * <p>
 * This abstract controller handles fetching entities with various filtering,
 * pagination, and counting operations. It supports the mapping of entities
 * to DTOs (both full and minimal) and provides a base for fetching operations.
 *
 * @param <I>     the type parameter representing the ID of the entity
 * @param <T>     the type parameter representing the entity
 * @param <MIND>  the type parameter representing the minimal DTO (view model)
 * @param <FULLD> the type parameter representing the full DTO (detailed model)
 * @param <S>     the type parameter representing the service interface
 */
@Slf4j
public abstract class MappedCrudFetchController<I extends Serializable, T extends IIdEntity,
        MIND extends IIdentifiableDto,
        FULLD extends MIND,
        S extends ICrudServiceMethod<I, T>>
        extends CrudControllerSubMethods<I, T, MIND, FULLD, S>
        implements IMappedCrudFetchApi<I, MIND, FULLD> {

    /**
     * Fetches all entities in a minimal DTO format.
     *
     * @param requestContext the context of the current request
     * @return ResponseEntity containing the list of minimal DTOs
     */
    @Override
    public final ResponseEntity<List<MIND>> getAll(RequestContextDto requestContext) {
        log.info("Fetching all entities (minimal DTO) with context: {}", requestContext);
        // Delegate the logic to the corresponding method in the subclass
        return subGetAll(requestContext);
    }

    /**
     * Fetches all entities assigned to the default domain in a minimal DTO format.
     *
     * @param requestContext the context of the current request
     * @return ResponseEntity containing the list of minimal DTOs assigned to the default domain
     */
    @Override
    public final ResponseEntity<List<MIND>> getAssignedToDefaultDomain(RequestContextDto requestContext) {
        log.info("Fetching all entities assigned to the default domain");
        return subGetAll(requestContext);
    }

    /**
     * Fetches all entities in a full DTO format.
     *
     * @param requestContext the context of the current request
     * @return ResponseEntity containing the list of full DTOs
     */
    @Override
    public final ResponseEntity<List<FULLD>> getAllFull(RequestContextDto requestContext) {
        log.info("Fetching all entities (full DTO) with context: {}", requestContext);
        return subGetAllFull(requestContext);
    }

    /**
     * Fetches all entities in a minimal DTO format with pagination.
     *
     * @param requestContext the context of the current request
     * @param page           the page number for pagination
     * @param size           the size of the page (number of records per page)
     * @return ResponseEntity containing the paginated list of minimal DTOs
     */
    @Override
    public final ResponseEntity<List<MIND>> getAllPaged(RequestContextDto requestContext, int page, int size) {
        log.info("Fetching all entities (minimal DTO) paged with page: {} and size: {}", page, size);
        return subGetAllPaged(requestContext, page, size);
    }

    /**
     * Fetches all entities in a full DTO format with pagination.
     *
     * @param requestContext the context of the current request
     * @param page           the page number for pagination
     * @param size           the size of the page (number of records per page)
     * @return ResponseEntity containing the paginated list of full DTOs
     */
    @Override
    public final ResponseEntity<List<FULLD>> getAllFullPaged(RequestContextDto requestContext, int page, int size) {
        log.info("Fetching all entities (full DTO) paged with page: {} and size: {}", page, size);
        return subGetAllFullPaged(requestContext, page, size);
    }

    /**
     * Fetches a single entity by its ID in a full DTO format.
     *
     * @param requestContext the context of the current request
     * @param id             the ID of the entity to be fetched
     * @return ResponseEntity containing the full DTO of the entity
     */
    @Override
    public final ResponseEntity<FULLD> getById(RequestContextDto requestContext, I id) {
        log.info("Fetching entity by ID: {} with context: {}", id, requestContext);
        return subGetById(requestContext, id);
    }

    /**
     * Fetches the count of all entities.
     *
     * @param requestContext the context of the current request
     * @return ResponseEntity containing the count of all entities
     */
    @Override
    public final ResponseEntity<Long> getCount(RequestContextDto requestContext) {
        log.info("Fetching the count of all entities with context: {}", requestContext);
        return subGetCount(requestContext);
    }

    /**
     * Fetches entities filtered by a given criteria in a full DTO format.
     *
     * @param requestContext the context of the current request
     * @param criteria       the filtering criteria
     * @return ResponseEntity containing the filtered list of full DTOs
     */
    @Override
    public final ResponseEntity<List<FULLD>> getFiltered(RequestContextDto requestContext, String criteria) {
        log.info("Fetching filtered entities (full DTO) with criteria: {}", criteria);
        return subGetAllFiltered(requestContext, criteria);
    }

    /**
     * Fetches filtered entities in a full DTO format with pagination.
     *
     * @param requestContext the context of the current request
     * @param criteria       the filtering criteria
     * @param page           the page number for pagination
     * @param size           the size of the page (number of records per page)
     * @return ResponseEntity containing the paginated list of filtered full DTOs
     */
    @Override
    public final ResponseEntity<List<FULLD>> getFilteredPaged(RequestContextDto requestContext, String criteria, int page, int size) {
        log.info("Fetching filtered entities (full DTO) paged with criteria: {}, page: {}, size: {}", criteria, page, size);
        return subGetAllFilteredPaged(requestContext, criteria, page, size);
    }

    /**
     * Fetches the filter criteria for the entities.
     *
     * @return ResponseEntity containing the filter criteria map (key-value pairs)
     */
    @Override
    public final ResponseEntity<Map<String, String>> getFilterCriteria() {
        log.info("Fetching the filter criteria");
        return subGetAllFilterCriteria();
    }
}