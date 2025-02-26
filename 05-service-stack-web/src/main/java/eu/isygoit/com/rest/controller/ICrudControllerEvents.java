package eu.isygoit.com.rest.controller;

import eu.isygoit.dto.IIdentifiableDto;
import eu.isygoit.dto.common.RequestContextDto;
import eu.isygoit.model.IIdAssignable;

import java.io.Serializable;
import java.util.List;

/**
 * The interface Crud controller events.
 *
 * @param <I>     the type parameter
 * @param <T>     the type parameter
 * @param <M>  the type parameter
 * @param <F> the type parameter
 */
interface ICrudControllerEvents<I extends Serializable, T extends IIdAssignable, M extends IIdentifiableDto, F extends M> {

    /**
     * Before create fulld.
     *
     * @param object the object
     * @return the fulld
     */
    F beforeCreate(F object);

    /**
     * After create t.
     *
     * @param object the object
     * @return the t
     */
    T afterCreate(T object);

    /**
     * Before update fulld.
     *
     * @param id     the id
     * @param object the object
     * @return the fulld
     */
    F beforeUpdate(I id, F object);

    /**
     * After update t.
     *
     * @param object the object
     * @return the t
     */
    T afterUpdate(T object);

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
     * After find by id fulld.
     *
     * @param object the object
     * @return the fulld
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
