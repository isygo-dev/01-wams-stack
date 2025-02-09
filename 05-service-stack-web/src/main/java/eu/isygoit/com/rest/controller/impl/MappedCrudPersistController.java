package eu.isygoit.com.rest.controller.impl;

import eu.isygoit.com.rest.api.IMappedCrudPersistApi;
import eu.isygoit.com.rest.service.ICrudServiceMethod;
import eu.isygoit.dto.IIdentifiableDto;
import eu.isygoit.model.IIdEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.io.Serializable;

/**
 * The type Mapped crud persist controller.
 *
 * @param <I>     the type parameter
 * @param <T>     the type parameter
 * @param <MIND>  the type parameter
 * @param <FULLD> the type parameter
 * @param <S>     the type parameter
 */
@Slf4j
public abstract class MappedCrudPersistController<I extends Serializable, T extends IIdEntity,
        MIND extends IIdentifiableDto,
        FULLD extends MIND,
        S extends ICrudServiceMethod<I, T>>
        extends CrudControllerSubMethods<I, T, MIND, FULLD, S>
        implements IMappedCrudPersistApi<I, FULLD> {

    public final ResponseEntity<FULLD> create(FULLD object) {
        return subCreate(object);
    }

    public final ResponseEntity<FULLD> update(I id, FULLD object) {
        return subUpdate(id, object);
    }
}
