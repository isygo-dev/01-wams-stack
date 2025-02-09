package eu.isygoit.com.rest.controller.impl;

import eu.isygoit.com.rest.api.IMappedCrudApi;
import eu.isygoit.com.rest.service.ICrudServiceMethod;
import eu.isygoit.dto.IIdentifiableDto;
import eu.isygoit.dto.common.RequestContextDto;
import eu.isygoit.model.IIdEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * The type Mapped crud controller.
 *
 * @param <I>     the type parameter
 * @param <T>     the type parameter
 * @param <MIND>  the type parameter
 * @param <FULLD> the type parameter
 * @param <S>     the type parameter
 */
@Slf4j
public abstract class MappedCrudController<I extends Serializable, T extends IIdEntity,
        MIND extends IIdentifiableDto,
        FULLD extends MIND,
        S extends ICrudServiceMethod<I, T>>
        extends CrudControllerSubMethods<I, T, MIND, FULLD, S>
        implements IMappedCrudApi<I, MIND, FULLD> {

    @Override
    public final ResponseEntity<FULLD> create(//RequestContextDto requestContext,
                                              FULLD object) {
        return subCreate(object);
    }

    @Override
    public final ResponseEntity<String> delete(RequestContextDto requestContext, I id) {
        return subDelete(requestContext, id);
    }

    @Override
    public final ResponseEntity<List<MIND>> getAll(RequestContextDto requestContext) {
        return subGetAll(requestContext);
    }

    @Override
    public final ResponseEntity<List<MIND>> getAssignedToDefaultDomain(RequestContextDto requestContext) {
        return subGetAllDefault(requestContext);
    }

    @Override
    public final ResponseEntity<List<FULLD>> getAllFull(RequestContextDto requestContext) {
        return subGetAllFull(requestContext);
    }

    @Override
    public final ResponseEntity<List<MIND>> getAllPaged(RequestContextDto requestContext,
                                                        int page,
                                                        int size) {
        return subGetAllPaged(requestContext, page, size);
    }

    @Override
    public final ResponseEntity<List<FULLD>> getAllFullPaged(RequestContextDto requestContext,
                                                             int page,
                                                             int size) {
        return subGetAllFullPaged(requestContext, page, size);
    }

    @Override
    public final ResponseEntity<FULLD> getById(RequestContextDto requestContext,
                                               I id) {
        return subFindById(requestContext, id);
    }

    @Override
    public final ResponseEntity<FULLD> update(//RequestContextDto requestContext,
                                              I id,
                                              FULLD object) {
        return subUpdate(id, object);
    }

    @Override
    public ResponseEntity<Long> getCount(RequestContextDto requestContext) {
        return subGetCount(requestContext);
    }

    @Override
    public ResponseEntity<List<FULLD>> getFiltered(RequestContextDto requestContext, String criteria) {
        return subGetAllFiltered(requestContext, criteria);
    }

    @Override
    public ResponseEntity<List<FULLD>> getFilteredPaged(RequestContextDto requestContext, String criteria, int page, int size) {
        return subGetAllFilteredPaged(requestContext, criteria, page, size);
    }

    @Override
    public ResponseEntity<Map<String, String>> getFilterCriteria() {
        return subGetAllFilterCriteria();
    }
}
