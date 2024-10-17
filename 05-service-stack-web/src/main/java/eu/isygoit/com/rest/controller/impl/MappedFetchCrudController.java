package eu.isygoit.com.rest.controller.impl;

import eu.isygoit.com.rest.api.IMappedFetchCrudApi;
import eu.isygoit.com.rest.service.ICrudServiceMethod;
import eu.isygoit.dto.IIdentifiableDto;
import eu.isygoit.dto.common.RequestContextDto;
import eu.isygoit.model.IIdEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

/**
 * The type Mapped fetch crud controller.
 *
 * @param <I>     the type parameter
 * @param <T>     the type parameter
 * @param <MIND>  the type parameter
 * @param <FULLD> the type parameter
 * @param <S>     the type parameter
 */
@Slf4j
public abstract class MappedFetchCrudController<I, T extends IIdEntity,
        MIND extends IIdentifiableDto,
        FULLD extends MIND,
        S extends ICrudServiceMethod<I, T>>
        extends CrudControllerSubMethods<I, T, MIND, FULLD, S>
        implements IMappedFetchCrudApi<I, MIND, FULLD> {


    @Override
    public final ResponseEntity<List<MIND>> findAll(RequestContextDto requestContext) {
        return subFindAll(requestContext);
    }

    @Override
    public final ResponseEntity<List<MIND>> findAllDefault(RequestContextDto requestContext) {
        return subFindAll(requestContext);
    }

    @Override
    public final ResponseEntity<List<FULLD>> findAllFull(RequestContextDto requestContext) {
        return subFindAllFull(requestContext);
    }

    @Override
    public final ResponseEntity<List<MIND>> findAll(RequestContextDto requestContext, Integer page, Integer size) {
        return subFindAll(requestContext, page, size);
    }

    @Override
    public final ResponseEntity<List<FULLD>> findAllFull(RequestContextDto requestContext, Integer page, Integer size) {
        return subFindAllFull(requestContext, page, size);
    }

    @Override
    public final ResponseEntity<FULLD> findById(RequestContextDto requestContext, I id) {
        return subFindById(requestContext, id);
    }

    @Override
    public ResponseEntity<Long> getCount(RequestContextDto requestContext) {
        return subGetCount(requestContext);
    }

    @Override
    public ResponseEntity<List<FULLD>> findAllFilteredByCriteria(RequestContextDto requestContext, String criteria) {
        return subFindAllFilteredByCriteria(requestContext, criteria);
    }

    @Override
    public ResponseEntity<List<FULLD>> findAllFilteredByCriteria(RequestContextDto requestContext, String criteria, Integer page, Integer size) {
        return subFindAllFilteredByCriteria(requestContext, criteria, page, size);
    }

    @Override
    public ResponseEntity<Map<String, String>> findAllFilterCriteria() {
        return subFindAllFilterCriteria();
    }
}
