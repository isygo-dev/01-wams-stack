package eu.isygoit.com.rest.controller;

import eu.isygoit.com.rest.service.ICrudServiceMethod;
import eu.isygoit.dto.IIdentifiableDto;
import eu.isygoit.dto.common.RequestContextDto;
import eu.isygoit.model.AssignableId;
import org.springframework.http.ResponseEntity;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * The interface Crud controller sub methods.
 *
 * @param <I> the type parameter
 * @param <E> the type parameter
 * @param <M> the type parameter
 * @param <F> the type parameter
 * @param <S> the type parameter
 */
public interface ICrudControllerSubMethods<I extends Serializable, E extends AssignableId, M extends IIdentifiableDto, F extends M, S extends ICrudServiceMethod<I, E>>
        extends ICrudControllerEvents<I, E, M, F> {

    /**
     * Sub create response entity.
     *
     * @param object the object
     * @return the response entity
     */
    ResponseEntity<F> subCreate(F object);

    /**
     * Sub create response entity.
     *
     * @param objects the objects
     * @return the response entity
     */
    ResponseEntity<List<F>> subCreate(List<F> objects);

    /**
     * Sub update response entity.
     *
     * @param id     the id
     * @param object the object
     * @return the response entity
     */
    ResponseEntity<F> subUpdate(I id, F object);

    /**
     * Sub update response entity.
     *
     * @param objects the objects
     * @return the response entity
     */
    ResponseEntity<List<F>> subUpdate(List<F> objects);

    /**
     * Sub delete response entity.
     *
     * @param requestContext the request context
     * @param id             the id
     * @return the response entity
     */
    ResponseEntity<?> subDelete(RequestContextDto requestContext, I id);

    /**
     * Sub delete response entity.
     *
     * @param requestContext the request context
     * @param objects        the objects
     * @return the response entity
     */
    ResponseEntity<?> subDelete(RequestContextDto requestContext, List<F> objects);

    /**
     * Sub find all full response entity.
     *
     * @param requestContext the request context
     * @return the response entity
     */
    ResponseEntity<List<F>> subFindAllFull(RequestContextDto requestContext);

    /**
     * Sub find all full response entity.
     *
     * @param requestContext the request context
     * @param page           the page
     * @param size           the size
     * @return the response entity
     */
    ResponseEntity<List<F>> subFindAllFull(RequestContextDto requestContext, Integer page, Integer size);

    /**
     * Sub find all response entity.
     *
     * @param requestContext the request context
     * @return the response entity
     */
    ResponseEntity<List<M>> subFindAll(RequestContextDto requestContext);

    /**
     * Sub find all default response entity.
     *
     * @param requestContext the request context
     * @return the response entity
     */
    ResponseEntity<List<M>> subFindAllDefault(RequestContextDto requestContext);

    /**
     * Sub find all response entity.
     *
     * @param requestContext the request context
     * @param page           the page
     * @param size           the size
     * @return the response entity
     */
    ResponseEntity<List<M>> subFindAll(RequestContextDto requestContext, Integer page, Integer size);

    /**
     * Sub find by id response entity.
     *
     * @param requestContext the request context
     * @param id             the id
     * @return the response entity
     */
    ResponseEntity<F> subFindById(RequestContextDto requestContext, I id);


    /**
     * Sub get count response entity.
     *
     * @param requestContext the request context
     * @return the response entity
     */
    ResponseEntity<Long> subGetCount(RequestContextDto requestContext);

    /**
     * Sub find all filter criteria response entity.
     *
     * @return the response entity
     */
    ResponseEntity<Map<String, String>> subFindAllFilterCriteria();

    /**
     * Sub find all filtered by criteria response entity.
     *
     * @param requestContext the request context
     * @param criteria       the criteria
     * @return the response entity
     */
    ResponseEntity<List<F>> subFindAllFilteredByCriteria(RequestContextDto requestContext, String criteria);

    /**
     * Sub find all filtered by criteria response entity.
     *
     * @param requestContext the request context
     * @param criteria       the criteria
     * @param page           the page
     * @param size           the size
     * @return the response entity
     */
    ResponseEntity<List<F>> subFindAllFilteredByCriteria(RequestContextDto requestContext, String criteria, Integer page, Integer size);
}
