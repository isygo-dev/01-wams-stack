package eu.isygoit.com.rest.controller.impl;

import eu.isygoit.com.rest.api.IMappedCrudPersistApi;
import eu.isygoit.com.rest.service.ICrudServiceMethod;
import eu.isygoit.dto.IIdentifiableDto;
import eu.isygoit.model.IIdEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.io.Serializable;

/**
 * The type Mapped CRUD persist controller.
 * <p>
 * This abstract controller handles the basic CRUD operations for entities and
 * supports persistence actions with a DTO to entity mapping.
 *
 * @param <I>     the type parameter representing the ID
 * @param <T>     the type parameter representing the entity
 * @param <MIND>  the type parameter representing the minimal DTO
 * @param <FULLD> the type parameter representing the full DTO
 * @param <S>     the type parameter representing the service interface
 */
@Slf4j
public abstract class MappedCrudPersistController<I extends Serializable, T extends IIdEntity,
        MIND extends IIdentifiableDto,
        FULLD extends MIND,
        S extends ICrudServiceMethod<I, T>>
        extends CrudControllerSubMethods<I, T, MIND, FULLD, S>
        implements IMappedCrudPersistApi<I, FULLD> {

    /**
     * Creates a new entity with the given full DTO.
     *
     * @param object the full DTO containing the data for the entity
     * @return ResponseEntity containing the created entity as a DTO
     */
    public final ResponseEntity<FULLD> create(FULLD object) {
        log.info("Create request received for object: {}", object);
        // Delegate the creation logic to the subclass implementation
        return subCreate(object);
    }

    /**
     * Updates an existing entity with the given ID and full DTO.
     *
     * @param id     the ID of the entity to be updated
     * @param object the full DTO containing the updated data
     * @return ResponseEntity containing the updated entity as a DTO
     */
    public final ResponseEntity<FULLD> update(I id, FULLD object) {
        log.info("Update request received for entity with ID: {}", id);
        // Delegate the update logic to the subclass implementation
        return subUpdate(id, object);
    }
}
