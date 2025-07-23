package eu.isygoit.com.rest.controller.impl;

import eu.isygoit.com.rest.api.IMappedCrudApi;
import eu.isygoit.dto.IIdAssignableDto;
import eu.isygoit.dto.common.RequestContextDto;
import eu.isygoit.model.IIdAssignable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Abstract fake controller class for simulating CRUD operations via REST API.
 * This class is intended for testing or placeholder purposes and throws
 * {@link UnsupportedOperationException} for all operations. It implements the
 * {@link IMappedCrudApi} interface but does not provide actual functionality.
 *
 * @param <I> the type of the identifier, extending {@link Serializable}
 * @param <T> the entity type, extending {@link IIdAssignable}
 * @param <M> the main DTO type, extending {@link IIdAssignableDto}
 * @param <F> the full DTO type, extending {@link M}
 */
@Slf4j
public abstract class MappedFakeCrudController<
        I extends Serializable,
        T extends IIdAssignable<I>,
        M extends IIdAssignableDto<I>,
        F extends M
        > implements IMappedCrudApi<I, M, F> {

    private static final UnsupportedOperationException UNSUPPORTED_OPERATION =
            new UnsupportedOperationException("This is a fake controller");

    /**
     * Deletes an entity by ID. This operation is not supported.
     *
     * @param requestContext the request context containing metadata
     * @param id            the ID of the entity to delete
     * @return never returns; always throws {@link UnsupportedOperationException}
     * @throws UnsupportedOperationException as this is a fake controller
     */
    @Override
    public ResponseEntity<?> delete(RequestContextDto requestContext, I id) {
        //Objects.requireNonNull(requestContext, "Request context must not be null");
        //Objects.requireNonNull(id, "Entity ID must not be null");
        log.warn("Attempted to call delete on fake controller for ID: {}", id);
        throw UNSUPPORTED_OPERATION;
    }

    /**
     * Deletes multiple entities by their IDs. This operation is not supported.
     *
     * @param requestContext the request context containing metadata
     * @param ids           the list of entity IDs to delete
     * @return never returns; always throws {@link UnsupportedOperationException}
     * @throws UnsupportedOperationException as this is a fake controller
     */
    @Override
    public ResponseEntity<?> batchDelete(RequestContextDto requestContext, List<I> ids) {
        //Objects.requireNonNull(requestContext, "Request context must not be null");
        //Objects.requireNonNull(ids, "IDs list must not be null");
        log.warn("Attempted to call batchDelete on fake controller for {} IDs", ids.size());
        throw UNSUPPORTED_OPERATION;
    }

    /**
     * Retrieves all entities with pagination. This operation is not supported.
     *
     * @param requestContext the request context containing metadata
     * @param page          the page number for pagination
     * @param size          the page size for pagination
     * @return never returns; always throws {@link UnsupportedOperationException}
     * @throws UnsupportedOperationException as this is a fake controller
     */
    @Override
    public ResponseEntity<List<M>> findAll(RequestContextDto requestContext, Integer page, Integer size) {
        //Objects.requireNonNull(requestContext, "Request context must not be null");
        //Objects.requireNonNull(page, "Page number must not be null");
        //Objects.requireNonNull(size, "Page size must not be null");
        log.warn("Attempted to call findAll on fake controller with page: {}, size: {}", page, size);
        throw UNSUPPORTED_OPERATION;
    }

    /**
     * Retrieves all entities with full details and pagination. This operation is not supported.
     *
     * @param requestContext the request context containing metadata
     * @param page          the page number for pagination
     * @param size          the page size for pagination
     * @return never returns; always throws {@link UnsupportedOperationException}
     * @throws UnsupportedOperationException as this is a fake controller
     */
    @Override
    public ResponseEntity<List<F>> findAllFull(RequestContextDto requestContext, Integer page, Integer size) {
        //Objects.requireNonNull(requestContext, "Request context must not be null");
        //Objects.requireNonNull(page, "Page number must not be null");
        //Objects.requireNonNull(size, "Page size must not be null");
        log.warn("Attempted to call findAllFull on fake controller with page: {}, size: {}", page, size);
        throw UNSUPPORTED_OPERATION;
    }

    /**
     * Retrieves an entity by ID. This operation is not supported.
     *
     * @param requestContext the request context containing metadata
     * @param id            the ID of the entity
     * @return never returns; always throws {@link UnsupportedOperationException}
     * @throws UnsupportedOperationException as this is a fake controller
     */
    @Override
    public ResponseEntity<F> findById(RequestContextDto requestContext, I id) {
        //Objects.requireNonNull(requestContext, "Request context must not be null");
        //Objects.requireNonNull(id, "Entity ID must not be null");
        log.warn("Attempted to call findById on fake controller for ID: {}", id);
        throw UNSUPPORTED_OPERATION;
    }

    /**
     * Retrieves the count of all entities. This operation is not supported.
     *
     * @param requestContext the request context containing metadata
     * @return never returns; always throws {@link UnsupportedOperationException}
     * @throws UnsupportedOperationException as this is a fake controller
     */
    @Override
    public ResponseEntity<Long> getCount(RequestContextDto requestContext) {
        //Objects.requireNonNull(requestContext, "Request context must not be null");
        log.warn("Attempted to call getCount on fake controller");
        throw UNSUPPORTED_OPERATION;
    }

    /**
     * Creates a new entity. This operation is not supported.
     *
     * @param requestContext the request context containing metadata
     * @param object        the DTO containing entity data
     * @return never returns; always throws {@link UnsupportedOperationException}
     * @throws UnsupportedOperationException as this is a fake controller
     */
    @Override
    public ResponseEntity<F> create(RequestContextDto requestContext, F object) {
        //Objects.requireNonNull(requestContext, "Request context must not be null");
        //Objects.requireNonNull(object, "DTO must not be null");
        log.warn("Attempted to call create on fake controller for DTO with ID: {}", object.getId());
        throw UNSUPPORTED_OPERATION;
    }

    /**
     * Creates multiple entities in a batch. This operation is not supported.
     *
     * @param requestContext the request context containing metadata
     * @param objects       the list of DTOs containing entity data
     * @return never returns; always throws {@link UnsupportedOperationException}
     * @throws UnsupportedOperationException as this is a fake controller
     */
    @Override
    public ResponseEntity<List<F>> createBatch(RequestContextDto requestContext, List<F> objects) {
        //Objects.requireNonNull(requestContext, "Request context must not be null");
        //Objects.requireNonNull(objects, "Objects list must not be null");
        log.warn("Attempted to call createBatch on fake controller for {} objects", objects.size());
        throw UNSUPPORTED_OPERATION;
    }

    /**
     * Updates an entity by ID. This operation is not supported.
     *
     * @param requestContext the request context containing metadata
     * @param id            the ID of the entity to update
     * @param object        the DTO containing updated entity data
     * @return never returns; always throws {@link UnsupportedOperationException}
     * @throws UnsupportedOperationException as this is a fake controller
     */
    @Override
    public ResponseEntity<F> update(RequestContextDto requestContext, I id, F object) {
        //Objects.requireNonNull(requestContext, "Request context must not be null");
        //Objects.requireNonNull(id, "Entity ID must not be null");
        //Objects.requireNonNull(object, "DTO must not be null");
        log.warn("Attempted to call update on fake controller for ID: {}", id);
        throw UNSUPPORTED_OPERATION;
    }

    /**
     * Retrieves entities filtered by criteria with pagination. This operation is not supported.
     *
     * @param requestContext the request context containing metadata
     * @param criteria      the filtering criteria
     * @param page          the page number for pagination
     * @param size          the page size for pagination
     * @return never returns; always throws {@link UnsupportedOperationException}
     * @throws UnsupportedOperationException as this is a fake controller
     */
    @Override
    public ResponseEntity<List<F>> findAllFilteredByCriteria(RequestContextDto requestContext, String criteria, Integer page, Integer size) {
        //Objects.requireNonNull(requestContext, "Request context must not be null");
        //Objects.requireNonNull(criteria, "Criteria must not be null");
        //Objects.requireNonNull(page, "Page number must not be null");
        //Objects.requireNonNull(size, "Page size must not be null");
        log.warn("Attempted to call findAllFilteredByCriteria on fake controller with criteria: {}, page: {}, size: {}", criteria, page, size);
        throw UNSUPPORTED_OPERATION;
    }

    /**
     * Retrieves annotated criteria for filtering. This operation is not supported.
     *
     * @return never returns; always throws {@link UnsupportedOperationException}
     * @throws UnsupportedOperationException as this is a fake controller
     */
    @Override
    public ResponseEntity<Map<String, String>> getAnnotatedCriteria() {
        log.warn("Attempted to call getAnnotatedCriteria on fake controller");
        throw UNSUPPORTED_OPERATION;
    }
}