package eu.isygoit.com.rest.controller.impl;

import eu.isygoit.com.rest.api.IMappedCrudFetchApi;
import eu.isygoit.com.rest.service.ICrudServiceEvents;
import eu.isygoit.com.rest.service.ICrudServiceMethods;
import eu.isygoit.com.rest.service.ICrudServiceUtils;
import eu.isygoit.com.rest.service.ICrudTenantAssignableServiceMethods;
import eu.isygoit.dto.IIdAssignableDto;
import eu.isygoit.dto.common.RequestContextDto;
import eu.isygoit.model.IIdAssignable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * The type Mapped crud fetch controller.
 *
 * @param <I> the type parameter
 * @param <T> the type parameter
 * @param <M> the type parameter
 * @param <F> the type parameter
 * @param <S> the type parameter
 */
@Slf4j
public abstract class MappedCrudFetchController<I extends Serializable, T extends IIdAssignable<I>,
        M extends IIdAssignableDto,
        F extends M,
        S extends ICrudServiceMethods<I, T> & ICrudTenantAssignableServiceMethods<I, T> & ICrudServiceEvents<I, T> & ICrudServiceUtils<I, T>>
        extends CrudControllerSubMethods<I, T, M, F, S>
        implements IMappedCrudFetchApi<I, M, F> {


    @Override
    public final ResponseEntity<List<M>> findAll(RequestContextDto requestContext) {
        return subFindAll(requestContext);
    }

    @Override
    public final ResponseEntity<List<M>> findAllDefault(RequestContextDto requestContext) {
        return subFindAll(requestContext);
    }

    @Override
    public final ResponseEntity<List<F>> findAllFull(RequestContextDto requestContext) {
        return subFindAllFull(requestContext);
    }

    @Override
    public final ResponseEntity<List<M>> findAll(RequestContextDto requestContext, Integer page, Integer size) {
        return subFindAll(requestContext, page, size);
    }

    @Override
    public final ResponseEntity<List<F>> findAllFull(RequestContextDto requestContext, Integer page, Integer size) {
        return subFindAllFull(requestContext, page, size);
    }

    @Override
    public final ResponseEntity<F> findById(RequestContextDto requestContext, I id) {
        return subFindById(requestContext, id);
    }

    @Override
    public ResponseEntity<Long> getCount(RequestContextDto requestContext) {
        return subGetCount(requestContext);
    }

    @Override
    public ResponseEntity<List<F>> findAllFilteredByCriteria(RequestContextDto requestContext, String criteria) {
        return subFindAllFilteredByCriteria(requestContext, criteria);
    }

    @Override
    public ResponseEntity<List<F>> findAllFilteredByCriteria(RequestContextDto requestContext, String criteria, Integer page, Integer size) {
        return subFindAllFilteredByCriteria(requestContext, criteria, page, size);
    }

    @Override
    public ResponseEntity<Map<String, String>> findAllFilterCriterias() {
        return subfindAllFilterCriterias();
    }
}
