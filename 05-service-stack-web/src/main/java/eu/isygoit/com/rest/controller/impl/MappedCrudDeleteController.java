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
 * The type Mapped crud delete controller.
 *
 * @param <I> the type parameter
 * @param <E> the type parameter
 * @param <M> the type parameter
 * @param <F> the type parameter
 * @param <S> the type parameter
 */
@Slf4j
public abstract class MappedCrudDeleteController<I extends Serializable, E extends IIdEntity,
        M extends IIdentifiableDto,
        F extends M,
        S extends ICrudServiceMethod<I, E>>
        extends CrudControllerSubMethods<I, E, M, F, S>
        implements IMappedCrudDeleteApi<I> {

    @Override
    public final ResponseEntity<?> delete(RequestContextDto requestContext,
                                          I id) {
        return subDelete(requestContext, id);
    }
}
