package eu.isygoit.com.rest.controller.impl.tenancy;

import eu.isygoit.com.rest.api.IMappedCrudApi;
import eu.isygoit.com.rest.service.ICrudServiceUtils;
import eu.isygoit.com.rest.service.tenancy.ICrudTenantServiceEvents;
import eu.isygoit.com.rest.service.tenancy.ICrudTenantServiceMethods;
import eu.isygoit.dto.IIdAssignableDto;
import eu.isygoit.dto.common.RequestContextDto;
import eu.isygoit.model.IIdAssignable;
import eu.isygoit.model.ITenantAssignable;
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
public abstract class MappedCrudTenantController<I extends Serializable,
        T extends IIdAssignable<I> & ITenantAssignable,
        M extends IIdAssignableDto<I>,
        F extends M,
        S extends ICrudTenantServiceMethods<I, T> & ICrudTenantServiceEvents<I, T> & ICrudServiceUtils<I, T>>
        extends CrudTenantControllerSubMethods<I, T, M, F, S>
        implements IMappedCrudApi<I, M, F> {

    @Override
    public final ResponseEntity<F> create(RequestContextDto requestContext,
                                          F object) {
        return subCreate(requestContext, object);
    }

    public final ResponseEntity<List<F>> createBatch(RequestContextDto requestContext, List<F> objects) {
        return subCreate(requestContext, objects);
    }

    @Override
    public final ResponseEntity<?> delete(RequestContextDto requestContext, I id) {
        return subDelete(requestContext, id);
    }

    @Override
    public final ResponseEntity<?> batchDelete(RequestContextDto requestContext,
                                               List<I> ids) {

        return subDelete(requestContext, mapper().listEntityToDto(crudService().getByIdIn(ids)));
    }

    @Override
    public final ResponseEntity<List<M>> findAll(RequestContextDto requestContext,
                                                 Integer page,
                                                 Integer size) {
        return subFindAll(requestContext, page, size);
    }

    @Override
    public final ResponseEntity<List<F>> findAllFull(RequestContextDto requestContext,
                                                     Integer page,
                                                     Integer size) {
        return subFindAllFull(requestContext, page, size);
    }

    @Override
    public final ResponseEntity<F> findById(RequestContextDto requestContext,
                                            I id) {
        return subFindById(requestContext, id);
    }

    @Override
    public final ResponseEntity<F> update(RequestContextDto requestContext,
                                          I id,
                                          F object) {
        return subUpdate(requestContext, id, object);
    }

    @Override
    public ResponseEntity<Long> getCount(RequestContextDto requestContext) {
        return subGetCount(requestContext);
    }


    @Override
    public ResponseEntity<List<F>> findAllFilteredByCriteria(RequestContextDto requestContext,
                                                             String criteria,
                                                             Integer page,
                                                             Integer size) {
        return subFindAllFilteredByCriteria(requestContext, criteria, page, size);
    }

    @Override
    public ResponseEntity<Map<String, String>> getAnnotatedCriteria() {
        return subGetAnnotatedCriteria();
    }
}
