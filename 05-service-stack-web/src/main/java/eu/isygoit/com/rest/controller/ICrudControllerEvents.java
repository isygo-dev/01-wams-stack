package eu.isygoit.com.rest.controller;

import eu.isygoit.dto.IIdentifiableDto;
import eu.isygoit.dto.common.RequestContextDto;
import eu.isygoit.model.AssignableId;

import java.io.Serializable;
import java.util.List;

/**
 * The interface Crud controller events.
 *
 * @param <I> the type parameter
 * @param <E> the type parameter
 * @param <M> the type parameter
 * @param <F> the type parameter
 */
interface ICrudControllerEvents<E extends AssignableId,
        I extends Serializable,
        M extends IIdentifiableDto, F extends M> {

    /**
     * Before create f.
     *
     * @param object the object
     * @return the f
     */
    F beforeCreate(F object);

    /**
     * After create e.
     *
     * @param object the object
     * @return the e
     */
    E afterCreate(E object);

    /**
     * Before update f.
     *
     * @param id     the id
     * @param object the object
     * @return the f
     */
    F beforeUpdate(I id, F object);

    /**
     * After update e.
     *
     * @param object the object
     * @return the e
     */
    E afterUpdate(E object);

    /**
     * Before delete boolean.
     *
     * @param id the id
     * @return the boolean
     */
    boolean beforeDelete(I id);

    /**
     * After delete boolean.
     *
     * @param id the id
     * @return the boolean
     */
    boolean afterDelete(I id);

    /**
     * Before delete boolean.
     *
     * @param objects the objects
     * @return the boolean
     */
    boolean beforeDelete(List<F> objects);

    /**
     * After delete boolean.
     *
     * @param objects the objects
     * @return the boolean
     */
    boolean afterDelete(List<F> objects);

    /**
     * After find by id f.
     *
     * @param object the object
     * @return the f
     */
    F afterFindById(F object);

    /**
     * After find all full list.
     *
     * @param requestContext the request context
     * @param list           the list
     * @return the list
     */
    List<F> afterFindAllFull(RequestContextDto requestContext, List<F> list);

    /**
     * After find all list.
     *
     * @param requestContext the request context
     * @param list           the list
     * @return the list
     */
    List<M> afterFindAll(RequestContextDto requestContext, List<M> list);
}
