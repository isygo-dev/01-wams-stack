package eu.isygoit.com.rest.controller.impl;

import eu.isygoit.com.rest.api.IMappedCrudPersistApi;
import eu.isygoit.com.rest.service.ICrudServiceMethod;
import eu.isygoit.dto.IIdentifiableDto;
import eu.isygoit.model.IIdEntity;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import java.io.Serializable;

/**
 * The type Mapped crud persist controller.
 *
 * @param <I> the type parameter
 * @param <E> the type parameter
 * @param <M> the type parameter
 * @param <F> the type parameter
 * @param <S> the type parameter
 */
@Slf4j
public abstract class MappedCrudPersistController<I extends Serializable, E extends IIdEntity,
        M extends IIdentifiableDto,
        F extends M,
        S extends ICrudServiceMethod<I, E>>
        extends CrudControllerSubMethods<I, E, M, F, S>
        implements IMappedCrudPersistApi<I, F> {

    public final ResponseEntity<F> create(F object) {
        return subCreate(object);
    }

    public final ResponseEntity<F> update(I id, F object) {
        return subUpdate(id, object);
    }
}
