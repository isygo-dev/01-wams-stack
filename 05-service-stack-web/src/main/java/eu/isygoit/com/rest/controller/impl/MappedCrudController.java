package eu.isygoit.com.rest.controller.impl;

import eu.isygoit.com.rest.api.IMappedCrudApi;
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
import java.util.Map;

/**
 * The type Mapped crud controller.
 *
 * @param <I> the type parameter
 * @param <T> the type parameter
 * @param <M> the type parameter
 * @param <F> the type parameter
 * @param <S> the type parameter
 */
@Slf4j
public abstract class MappedCrudController<I extends Serializable, T extends IIdAssignable<I>,
        M extends IIdAssignableDto<I> & IDto,
        F extends M,
        S extends ICrudServiceMethods<I, T> & ICrudServiceEvents<I, T> & ICrudServiceUtils<I, T>>
        extends CrudControllerOperations<I, T, M, F, S>
        implements IMappedCrudApi<I, M, F> {

    @Override
    public final ResponseEntity<F> create(ContextRequestDto requestContext,
                                          F object) {
        return performCreate(requestContext, object);
    }

    public final ResponseEntity<List<F>> createBatch(ContextRequestDto requestContext, List<F> objects) {
        return performCreate(requestContext, objects);
    }

    @Override
    public final ResponseEntity<?> delete(ContextRequestDto requestContext,
                                          I id) {
        return performDelete(requestContext, id);
    }

    @Override
    public final ResponseEntity<?> batchDelete(ContextRequestDto requestContext,
                                               List<I> ids) {
        return performDelete(requestContext, mapper().listEntityToDto(crudService().getByIdIn(ids)));
    }


    @Override
    public final ResponseEntity<List<F>> findAllFull(ContextRequestDto requestContext,
                                                     Integer page,
                                                     Integer size) {
        return performFindAllFull(requestContext, page, size);
    }

    @Override
    public final ResponseEntity<List<M>> findAll(ContextRequestDto requestContext,
                                                 Integer page,
                                                 Integer size) {
        return performFindAll(requestContext, page, size);
    }

    @Override
    public final ResponseEntity<F> findById(ContextRequestDto requestContext,
                                            I id) {
        return performFindById(requestContext, id);
    }

    @Override
    public final ResponseEntity<F> update(ContextRequestDto requestContext,
                                          I id,
                                          F object) {
        return performUpdate(requestContext, id, object);
    }

    @Override
    public ResponseEntity<Long> getCount(ContextRequestDto requestContext) {
        return subGetCount(requestContext);
    }

    @Override
    public ResponseEntity<List<F>> findAllFilteredByCriteria(ContextRequestDto requestContext, String criteria, Integer page, Integer size) {
        return performFindAllFilteredByCriteria(requestContext, criteria, page, size);
    }

    @Override
    public ResponseEntity<Map<String, String>> getAnnotatedCriteria() {
        return subGetAnnotatedCriteria();
    }
}
