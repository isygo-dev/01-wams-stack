package eu.isygoit.com.rest.controller.impl;

import eu.isygoit.com.rest.api.IMappedCrudDeleteApi;
import eu.isygoit.com.rest.service.ICrudServiceEvents;
import eu.isygoit.com.rest.service.ICrudServiceMethods;
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
 * The type Mapped crud delete controller.
 *
 * @param <I> the type parameter
 * @param <T> the type parameter
 * @param <M> the type parameter
 * @param <F> the type parameter
 * @param <S> the type parameter
 */
@Slf4j
public abstract class MappedCrudDeleteController<I extends Serializable, T extends IIdAssignable<I>,
        M extends IIdAssignableDto<I> & IDto,
        F extends M,
        S extends ICrudServiceMethods<I, T> & ICrudServiceEvents<I, T> & ICrudServiceUtils<I, T>>
        extends CrudControllerSubMethods<I, T, M, F, S>
        implements IMappedCrudDeleteApi<I> {

    @Override
    public final ResponseEntity<?> delete(ContextRequestDto requestContext,
                                          I id) {
        return subDelete(requestContext, id);
    }

    @Override
    public final ResponseEntity<?> batchDelete(ContextRequestDto requestContext,
                                               List<I> ids) {
        return subDelete(requestContext, mapper().listEntityToDto(crudService().getByIdIn(ids)));
    }
}
