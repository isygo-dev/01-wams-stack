package eu.isygoit.com.rest.controller.impl;

import eu.isygoit.com.rest.api.IMappedCrudFetchApi;
import eu.isygoit.com.rest.service.ICrudServiceEvents;
import eu.isygoit.com.rest.service.ICrudServiceOperations;
import eu.isygoit.com.rest.service.ICrudServiceUtils;
import eu.isygoit.dto.IDto;
import eu.isygoit.dto.IIdAssignableDto;
import eu.isygoit.dto.common.ContextRequestDto;
import eu.isygoit.dto.common.PaginatedResponseDto;
import eu.isygoit.model.IIdAssignable;
import eu.isygoit.service.RequestContextService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.io.Serializable;
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
        M extends IIdAssignableDto<I> & IDto,
        F extends M,
        S extends ICrudServiceOperations<I, T> & ICrudServiceEvents<I, T> & ICrudServiceUtils<I, T>>
        extends CrudControllerOperations<I, T, M, F, S>
        implements IMappedCrudFetchApi<I, M, F> {

    @Getter
    @Autowired
    private RequestContextService requestContextService;

    @Override
    public final ResponseEntity<PaginatedResponseDto<M>> findAll(Integer page, Integer size) {
        return performFindAll(requestContextService.getCurrentContext(), page, size);
    }

    @Override
    public final ResponseEntity<PaginatedResponseDto<F>> findAllFull(Integer page, Integer size) {
        return performFindAllFull(requestContextService.getCurrentContext(), page, size);
    }

    @Override
    public final ResponseEntity<F> findById(I id) {
        return performFindById(requestContextService.getCurrentContext(), id);
    }

    @Override
    public ResponseEntity<Long> getCount() {
        return performGetCount(requestContextService.getCurrentContext());
    }


    @Override
    public ResponseEntity<PaginatedResponseDto<F>> findAllFilteredByCriteria(String criteria, Integer page, Integer size) {
        return performFindAllFilteredByCriteria(requestContextService.getCurrentContext(), criteria, page, size);
    }

    @Override
    public ResponseEntity<Map<String, String>> getAnnotatedCriteria() {
        return performGetAnnotatedCriteria();
    }
}
