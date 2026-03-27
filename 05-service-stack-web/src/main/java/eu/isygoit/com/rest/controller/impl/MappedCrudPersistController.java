package eu.isygoit.com.rest.controller.impl;

import eu.isygoit.com.rest.api.IMappedCrudPersistApi;
import eu.isygoit.com.rest.service.ICrudServiceEvents;
import eu.isygoit.com.rest.service.ICrudServiceOperations;
import eu.isygoit.com.rest.service.ICrudServiceUtils;
import eu.isygoit.dto.IDto;
import eu.isygoit.dto.IIdAssignableDto;
import eu.isygoit.dto.common.ContextRequestDto;
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
        M extends IIdAssignableDto<I> & IDto,
        F extends M,
        S extends ICrudServiceOperations<I, T> & ICrudServiceEvents<I, T> & ICrudServiceUtils<I, T>>
        extends CrudControllerOperations<I, T, M, F, S>
        implements IMappedCrudPersistApi<I, F> {

    public final ResponseEntity<F> create(ContextRequestDto requestContext, F object) {
        return performCreate(requestContext, object);
    }

    public final ResponseEntity<List<F>> createBatch(ContextRequestDto requestContext, List<F> objects) {
        return performCreate(requestContext, objects);
    }

    public final ResponseEntity<F> update(ContextRequestDto requestContext, I id, F object) {
        return performUpdate(requestContext, id, object);
    }
}
