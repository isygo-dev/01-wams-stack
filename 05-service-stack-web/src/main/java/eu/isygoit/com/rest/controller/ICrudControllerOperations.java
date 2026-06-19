package eu.isygoit.com.rest.controller;

import eu.isygoit.com.rest.service.ICrudServiceUtils;
import eu.isygoit.dto.IDto;
import eu.isygoit.dto.IIdAssignableDto;
import eu.isygoit.dto.common.RequestContextDto;
import eu.isygoit.dto.common.PaginatedResponseDto;
import eu.isygoit.model.IIdAssignable;
import jakarta.validation.Valid;
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
public interface ICrudControllerOperations<I extends Serializable,
        T extends IIdAssignable<I>,
        M extends IIdAssignableDto<I> & IDto,
        F extends M,
        S extends ICrudServiceUtils<I, T>>
        extends ICrudControllerEvents<I, T, M, F> {

    /**
     * Sub create response entity.
     *
     * @param object the object
     * @return the response entity
     */
    ResponseEntity<F> performCreate(RequestContextDto requestContext, @Valid F object);

    /**
     * Sub create response entity.
     *
     * @param objects the objects
     * @return the response entity
     */
    ResponseEntity<List<F>> performCreate(RequestContextDto requestContext, @Valid List<F> objects);

    /**
     * Sub update response entity.
     *
     * @param id     the id
     * @param object the object
     * @return the response entity
     */
    ResponseEntity<F> performUpdate(RequestContextDto requestContext, I id, @Valid F object);

    /**
     * Sub update response entity.
     *
     * @param objects the objects
     * @return the response entity
     */
    ResponseEntity<List<F>> performUpdate(RequestContextDto requestContext, @Valid List<F> objects);

    /**
     * Sub delete response entity.
     *
     * @param id the id
     * @return the response entity
     */
    ResponseEntity<Void> performDelete(RequestContextDto requestContext, I id);

    /**
     * Sub delete response entity.
     *
     * @param objects the objects
     * @return the response entity
     */
    ResponseEntity<Void> performDelete(RequestContextDto requestContext, @Valid List<F> objects);

    /**
     * Sub find all full response entity.
     *
     * @param page the page
     * @param size the size
     * @return the response entity
     */
    ResponseEntity<PaginatedResponseDto<F>> performFindAllFull(RequestContextDto requestContext, Integer page, Integer size);

    /**
     * Sub find all response entity.
     *
     * @param page the page
     * @param size the size
     * @return the response entity
     */
    ResponseEntity<PaginatedResponseDto<M>> performFindAll(RequestContextDto requestContext, Integer page, Integer size);

    ResponseEntity<List<M>> performFindAllList(RequestContextDto context);

    ResponseEntity<List<F>> performFindAllListFull(RequestContextDto context);

    /**
     * Sub find by id response entity.
     *
     * @param id the id
     * @return the response entity
     */
    ResponseEntity<F> performFindById(RequestContextDto requestContext, I id);


    /**
     * Sub get count response entity.
     *
     * @return the response entity
     */
    ResponseEntity<Long> performGetCount(RequestContextDto requestContext);

    /**
     * Sub find all filter criteria response entity.
     *
     * @return the response entity
     */
    ResponseEntity<Map<String, String>> performGetAnnotatedCriteria();

    /**
     * Sub find all filtered by criteria response entity.
     *
     * @param criteria the criteria
     * @param page     the page
     * @param size     the size
     * @return the response entity
     */
    ResponseEntity<PaginatedResponseDto<F>> performFindAllFilteredByCriteria(RequestContextDto requestContext, String criteria, Integer page, Integer size);
}
