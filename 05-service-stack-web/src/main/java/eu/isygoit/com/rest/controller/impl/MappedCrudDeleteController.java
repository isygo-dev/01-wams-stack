package eu.isygoit.com.rest.controller.impl;

import eu.isygoit.com.rest.api.IMappedCrudDeleteApi;
import eu.isygoit.com.rest.service.ICrudServiceMethod;
import eu.isygoit.dto.IIdentifiableDto;
import eu.isygoit.dto.common.RequestContextDto;
import eu.isygoit.model.IIdEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.io.Serializable;

/**
 * The type Mapped CRUD delete controller.
 *
 * This abstract controller handles the deletion of entities. It implements
 * the logic for deleting entities by their ID and provides a base for deletion
 * operations in subclasses.
 *
 * @param <I>     the type parameter representing the ID of the entity
 * @param <T>     the type parameter representing the entity
 * @param <MIND>  the type parameter representing the minimal DTO (view model)
 * @param <FULLD> the type parameter representing the full DTO (detailed model)
 * @param <S>     the type parameter representing the service interface
 */
@Slf4j
public abstract class MappedCrudDeleteController<I extends Serializable, T extends IIdEntity,
        MIND extends IIdentifiableDto,
        FULLD extends MIND,
        S extends ICrudServiceMethod<I, T>>
        extends CrudControllerSubMethods<I, T, MIND, FULLD, S>
        implements IMappedCrudDeleteApi<I> {

    /**
     * Deletes an entity by its ID.
     *
     * @param requestContext the context of the current request, typically includes domain, user info, etc.
     * @param id the ID of the entity to be deleted
     * @return ResponseEntity containing a confirmation message
     */
    @Override
    public final ResponseEntity<String> delete(RequestContextDto requestContext, I id) {
        log.info("Received delete request for entity with ID: {} in context: {}", id, requestContext);

        // Delegate the deletion logic to the corresponding method in the subclass
        return subDelete(requestContext, id);
    }
}
