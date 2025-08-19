package eu.isygoit.com.rest.controller;

import eu.isygoit.com.rest.service.ICrudServiceUtils;
import eu.isygoit.dto.IDto;
import eu.isygoit.dto.IIdAssignableDto;
import eu.isygoit.dto.common.ContextRequestDto;
import eu.isygoit.model.IIdAssignable;
import org.springframework.http.ResponseEntity;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * The interface Crud controller sub methods.
 *
 * @param <I> the type parameter
 * @param <T> the type parameter
 * @param <M> the type parameter
 * @param <F> the type parameter
 * @param <S> the type parameter
 */
public interface ICrudControllerSubMethods<I extends Serializable,
        T extends IIdAssignable<I>,
        M extends IIdAssignableDto<I> & IDto,
        F extends M,
        S extends ICrudServiceUtils<I, T>>
        extends ICrudControllerEvents<I, T, M, F> {

    /**
     * Sub create response entity.
     *
     * @param requestContext the request context
     * @param object         the object
     * @return the response entity
     */
    ResponseEntity<F> subCreate(ContextRequestDto requestContext, F object);

    /**
     * Sub create response entity.
     *
     * @param requestContext the request context
     * @param objects        the objects
     * @return the response entity
     */
    ResponseEntity<List<F>> subCreate(ContextRequestDto requestContext, List<F> objects);

    /**
     * Sub update response entity.
     *
     * @param requestContext the request context
     * @param id             the id
     * @param object         the object
     * @return the response entity
     */
    ResponseEntity<F> subUpdate(ContextRequestDto requestContext, I id, F object);

    /**
     * Sub update response entity.
     *
     * @param requestContext the request context
     * @param objects        the objects
     * @return the response entity
     */
    ResponseEntity<List<F>> subUpdate(ContextRequestDto requestContext, List<F> objects);

    /**
     * Sub delete response entity.
     *
     * @param requestContext the request context
     * @param id             the id
     * @return the response entity
     */
    ResponseEntity<Void> subDelete(ContextRequestDto requestContext, I id);

    /**
     * Sub delete response entity.
     *
     * @param requestContext the request context
     * @param objects        the objects
     * @return the response entity
     */
    ResponseEntity<Void> subDelete(ContextRequestDto requestContext, List<F> objects);

    /**
     * Sub find all full response entity.
     *
     * @param requestContext the request context
     * @param page           the page
     * @param size           the size
     * @return the response entity
     */
    ResponseEntity<List<F>> subFindAllFull(ContextRequestDto requestContext, Integer page, Integer size);

    /**
     * Sub find all response entity.
     *
     * @param requestContext the request context
     * @param page           the page
     * @param size           the size
     * @return the response entity
     */
    ResponseEntity<List<M>> subFindAll(ContextRequestDto requestContext, Integer page, Integer size);

    /**
     * Sub find by id response entity.
     *
     * @param requestContext the request context
     * @param id             the id
     * @return the response entity
     */
    ResponseEntity<F> subFindById(ContextRequestDto requestContext, I id);


    /**
     * Sub get count response entity.
     *
     * @param requestContext the request context
     * @return the response entity
     */
    ResponseEntity<Long> subGetCount(ContextRequestDto requestContext);

    /**
     * Sub find all filter criteria response entity.
     *
     * @return the response entity
     */
    ResponseEntity<Map<String, String>> subGetAnnotatedCriteria();

    /**
     * Sub find all filtered by criteria response entity.
     *
     * @param requestContext the request context
     * @param criteria       the criteria
     * @param page           the page
     * @param size           the size
     * @return the response entity
     */
    ResponseEntity<List<F>> subFindAllFilteredByCriteria(ContextRequestDto requestContext, String criteria, Integer page, Integer size);
}
