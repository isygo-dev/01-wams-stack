package eu.isygoit.com.rest.controller;

import eu.isygoit.dto.IIdentifiableDto;
import eu.isygoit.dto.common.RequestContextDto;
import eu.isygoit.model.IIdEntity;

import java.util.List;

/**
 * The interface Crud controller events.
 *
 * @param <I>     the type parameter
 * @param <T>     the type parameter
 * @param <MIND>  the type parameter
 * @param <FULLD> the type parameter
 */
interface ICrudControllerEvents<I, T extends IIdEntity, MIND extends IIdentifiableDto, FULLD extends MIND> {

    /**
     * Before create fulld.
     *
     * @param object the object
     * @return the fulld
     */
    FULLD beforeCreate(FULLD object);

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
    FULLD beforeUpdate(I id, FULLD object);

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
    boolean beforeDelete(List<FULLD> objects);

    /**
     * After delete boolean.
     *
     * @param objects the objects
     * @return the boolean
     */
    boolean afterDelete(List<FULLD> objects);

    /**
     * After find by id fulld.
     *
     * @param object the object
     * @return the fulld
     */
    FULLD afterFindById(FULLD object);

    /**
     * After find all full list.
     *
     * @param requestContext the request context
     * @param list           the list
     * @return the list
     */
    List<FULLD> afterGetAllFull(RequestContextDto requestContext, List<FULLD> list);

    /**
     * After find all list.
     *
     * @param requestContext the request context
     * @param list           the list
     * @return the list
     */
    List<MIND> afterGetAll(RequestContextDto requestContext, List<MIND> list);
}
