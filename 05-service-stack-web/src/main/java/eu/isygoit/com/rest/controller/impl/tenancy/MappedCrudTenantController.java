package eu.isygoit.com.rest.controller.impl.tenancy;

import eu.isygoit.com.rest.api.IMappedCrudApi;
import eu.isygoit.com.rest.service.ICrudServiceUtils;
import eu.isygoit.com.rest.service.tenancy.ICrudTenantServiceEvents;
import eu.isygoit.com.rest.service.tenancy.ICrudTenantServiceOperations;
import eu.isygoit.dto.IDto;
import eu.isygoit.dto.IIdAssignableDto;
import eu.isygoit.dto.common.ContextRequestDto;
import eu.isygoit.dto.common.PaginatedResponseDto;
import eu.isygoit.model.IIdAssignable;
import eu.isygoit.model.ITenantAssignable;
import eu.isygoit.service.RequestContextService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
        M extends IIdAssignableDto<I> & IDto,
        F extends M,
        S extends ICrudTenantServiceOperations<I, T> & ICrudTenantServiceEvents<I, T> & ICrudServiceUtils<I, T>>
        extends CrudTenantControllerOperations<I, T, M, F, S>
        implements IMappedCrudApi<I, M, F> {

    @Getter
    @Autowired
    private RequestContextService requestContextService;

    @Override
    public final ResponseEntity<F> create(
                                          F object) {
        return performCreate(requestContextService.getCurrentContext(), object);
    }

    public final ResponseEntity<List<F>> createBatch( List<F> objects) {
        return performCreate(requestContextService.getCurrentContext(), objects);
    }

    @Override
    public final ResponseEntity<?> delete( I id) {
        return performDelete(requestContextService.getCurrentContext(), id);
    }

    @Override
    public final ResponseEntity<?> deleteBatch(
                                               List<I> ids) {

        return performDelete(requestContextService.getCurrentContext(), mapper().listEntityToDto(crudService().getByIdIn(ids)));
    }

    @Override
    public final ResponseEntity<PaginatedResponseDto<M>> findAll(
                                                                 Integer page,
                                                                 Integer size) {
        return performFindAll(requestContextService.getCurrentContext(), page, size);
    }

    @Override
    public final ResponseEntity<PaginatedResponseDto<F>> findAllFull(Integer page,
                                                                     Integer size) {
        return performFindAllFull(requestContextService.getCurrentContext(), page, size);
    }

    @Override
    public final ResponseEntity<F> findById(
                                            I id) {
        return performFindById(requestContextService.getCurrentContext(), id);
    }

    @Override
    public final ResponseEntity<F> update(
                                          I id,
                                          F object) {
        return performUpdate(requestContextService.getCurrentContext(), id, object);
    }

    @Override
    public ResponseEntity<Long> getCount() {
        return performGetCount(requestContextService.getCurrentContext());
    }


    @Override
    public ResponseEntity<PaginatedResponseDto<F>> findAllFilteredByCriteria(
                                                                             String criteria,
                                                                             Integer page,
                                                                             Integer size) {
        return performFindAllFilteredByCriteria(requestContextService.getCurrentContext(), criteria, page, size);
    }

    @Override
    public ResponseEntity<Map<String, String>> getAnnotatedCriteria() {
        return performGetAnnotatedCriteria();
    }
}
