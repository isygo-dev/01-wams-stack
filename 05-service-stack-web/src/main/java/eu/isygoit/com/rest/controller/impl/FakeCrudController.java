package eu.isygoit.com.rest.controller.impl;

import eu.isygoit.com.rest.api.IMappedCrudApi;
import eu.isygoit.dto.IIdentifiableDto;
import eu.isygoit.dto.common.RequestContextDto;
import eu.isygoit.model.IIdEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

/**
 * The type Fake crud controller.
 *
 * @param <I>     the type parameter
 * @param <T>     the type parameter
 * @param <MIND>  the type parameter
 * @param <FULLD> the type parameter
 */
@Slf4j
public abstract class FakeCrudController<I, T extends IIdEntity,
        MIND extends IIdentifiableDto,
        FULLD extends MIND>
        implements IMappedCrudApi<I, MIND, FULLD> {


    @Override
    public ResponseEntity<?> delete(RequestContextDto requestContext,
                                    I id) {
        throw new UnsupportedOperationException("This is a fake controller");
    }

    @Override
    public ResponseEntity<List<MIND>> findAll(RequestContextDto requestContext) {
        throw new UnsupportedOperationException("This is a fake controller");
    }

    @Override
    public ResponseEntity<List<MIND>> findAll(RequestContextDto requestContext,
                                              Integer page,
                                              Integer size) {
        throw new UnsupportedOperationException("This is a fake controller");
    }

    @Override
    public ResponseEntity<List<FULLD>> findAllFull(RequestContextDto requestContext) {
        throw new UnsupportedOperationException("This is a fake controller");
    }

    @Override
    public ResponseEntity<List<FULLD>> findAllFull(RequestContextDto requestContext,
                                                   Integer page,
                                                   Integer size) {
        throw new UnsupportedOperationException("This is a fake controller");
    }

    @Override
    public ResponseEntity<FULLD> findById(RequestContextDto requestContext,
                                          I id) {
        throw new UnsupportedOperationException("This is a fake controller");
    }

    @Override
    public ResponseEntity<Long> getCount(RequestContextDto requestContext) {
        throw new UnsupportedOperationException("This is a fake controller");
    }

    @Override
    public ResponseEntity<FULLD> create(//RequestContextDto requestContext,
                                        FULLD object) {
        throw new UnsupportedOperationException("This is a fake controller");
    }

    @Override
    public ResponseEntity<FULLD> update(//RequestContextDto requestContext,
                                        I id,
                                        FULLD object) {
        throw new UnsupportedOperationException("This is a fake controller");
    }

    @Override
    public ResponseEntity<List<MIND>> findAllDefault(RequestContextDto requestContext) {
        throw new UnsupportedOperationException("This is a fake controller");
    }

    @Override
    public ResponseEntity<List<FULLD>> findAllFilteredByCriteria(RequestContextDto requestContext, String criteria) {
        throw new UnsupportedOperationException("This is a fake controller");
    }

    @Override
    public ResponseEntity<List<FULLD>> findAllFilteredByCriteria(RequestContextDto requestContext, String criteria, Integer page, Integer size) {
        throw new UnsupportedOperationException("This is a fake controller");
    }

    @Override
    public ResponseEntity<Map<String, String>> findAllFilterCriteria() {
        throw new UnsupportedOperationException("This is a fake controller");
    }
}
