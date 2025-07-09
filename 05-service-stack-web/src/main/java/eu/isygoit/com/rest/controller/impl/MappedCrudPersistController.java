package eu.isygoit.com.rest.controller.impl;

import eu.isygoit.com.rest.api.IMappedCrudPersistApi;
import eu.isygoit.com.rest.service.ICrudServiceEvents;
import eu.isygoit.com.rest.service.ICrudServiceMethods;
import eu.isygoit.com.rest.service.ICrudServiceUtils;
import eu.isygoit.dto.IIdAssignableDto;
import eu.isygoit.dto.common.RequestContextDto;
import eu.isygoit.model.IIdAssignable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.io.Serializable;
import java.util.List;

/**
 * The type Mapped crud persist controller.
 *
 * @param <I> the type parameter
 * @param <T> the type parameter
 * @param <M> the type parameter
 * @param <F> the type parameter
 * @param <S> the type parameter
 */
@Slf4j
public abstract class MappedCrudPersistController<I extends Serializable, T extends IIdAssignable<I>,
        M extends IIdAssignableDto<I>,
        F extends M,
        S extends ICrudServiceMethods<I, T> & ICrudServiceEvents<I, T> & ICrudServiceUtils<I, T>>
        extends CrudControllerSubMethods<I, T, M, F, S>
        implements IMappedCrudPersistApi<I, F> {

    public final ResponseEntity<F> create(RequestContextDto requestContext, F object) {
        return subCreate(requestContext, object);
    }

    public final ResponseEntity<List<F>> createBatch(RequestContextDto requestContext, List<F> objects) {
        return subCreate(requestContext, objects);
    }

    public final ResponseEntity<F> update(RequestContextDto requestContext, I id, F object) {
        return subUpdate(requestContext, id, object);
    }
}
