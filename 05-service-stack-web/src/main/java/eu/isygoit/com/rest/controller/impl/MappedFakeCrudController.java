package eu.isygoit.com.rest.controller.impl;

import eu.isygoit.com.rest.api.IMappedCrudApi;
import eu.isygoit.dto.IIdAssignableDto;
import eu.isygoit.dto.common.RequestContextDto;
import eu.isygoit.model.IIdAssignable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * The type Fake crud controller.
 *
 * @param <I> the type parameter
 * @param <T> the type parameter
 * @param <M> the type parameter
 * @param <F> the type parameter
 */
@Slf4j
public abstract class MappedFakeCrudController<I extends Serializable, T extends IIdAssignable<I>,
        M extends IIdAssignableDto<I>,
        F extends M>
        implements IMappedCrudApi<I, M, F> {


    @Override
    public ResponseEntity<?> delete(RequestContextDto requestContext,
                                    I id) {
        throw new UnsupportedOperationException("This is a fake controller");
    }

    @Override
    public ResponseEntity<List<M>> findAll(RequestContextDto requestContext) {
        throw new UnsupportedOperationException("This is a fake controller");
    }

    @Override
    public ResponseEntity<List<M>> findAll(RequestContextDto requestContext,
                                           Integer page,
                                           Integer size) {
        throw new UnsupportedOperationException("This is a fake controller");
    }

    @Override
    public ResponseEntity<List<F>> findAllFull(RequestContextDto requestContext) {
        throw new UnsupportedOperationException("This is a fake controller");
    }

    @Override
    public ResponseEntity<List<F>> findAllFull(RequestContextDto requestContext,
                                               Integer page,
                                               Integer size) {
        throw new UnsupportedOperationException("This is a fake controller");
    }

    @Override
    public ResponseEntity<F> findById(RequestContextDto requestContext,
                                      I id) {
        throw new UnsupportedOperationException("This is a fake controller");
    }

    @Override
    public ResponseEntity<Long> getCount(RequestContextDto requestContext) {
        throw new UnsupportedOperationException("This is a fake controller");
    }

    @Override
    public ResponseEntity<F> create(RequestContextDto requestContext,
                                    F object) {
        throw new UnsupportedOperationException("This is a fake controller");
    }

    public ResponseEntity<List<F>> createBatch(RequestContextDto requestContext, List<F> objects) {
        throw new UnsupportedOperationException("This is a fake controller");
    }

    @Override
    public ResponseEntity<F> update(RequestContextDto requestContext,
                                    I id,
                                    F object) {
        throw new UnsupportedOperationException("This is a fake controller");
    }

    @Override
    public ResponseEntity<List<M>> findAllDefault(RequestContextDto requestContext) {
        throw new UnsupportedOperationException("This is a fake controller");
    }

    @Override
    public ResponseEntity<List<F>> findAllFilteredByCriteria(RequestContextDto requestContext, String criteria) {
        throw new UnsupportedOperationException("This is a fake controller");
    }

    @Override
    public ResponseEntity<List<F>> findAllFilteredByCriteria(RequestContextDto requestContext, String criteria, Integer page, Integer size) {
        throw new UnsupportedOperationException("This is a fake controller");
    }

    @Override
    public ResponseEntity<Map<String, String>> findAllFilterCriterias() {
        throw new UnsupportedOperationException("This is a fake controller");
    }
}
