package eu.isygoit.com.rest.service;

import eu.isygoit.model.IIdAssignable;

import java.io.Serializable;
import java.util.List;

/**
 * The interface Crud api events.
 *
 * @param <I> the type parameter
 * @param <T> the type parameter
 */
public interface ICrudServiceEvents<I extends Serializable, T extends IIdAssignable<I>> {

    /**
     * Before update t.
     *
     * @param object the object
     * @return the t
     */
    T beforeUpdate(T object);

    /**
     * After update t.
     *
     * @param object the object
     * @return the t
     */
    T afterUpdate(T object);

    /**
     * Before delete.
     *
     * @param id the id
     */
    void beforeDelete(I id);

    /**
     * After delete.
     *
     * @param id the id
     */
    void afterDelete(I id);

    /**
     * Before delete.
     *
     * @param objects the objects
     */
    void beforeDelete(List<T> objects);

    /**
     * After delete.
     *
     * @param objects the objects
     */
    void afterDelete(List<T> objects);

    /**
     * Before create t.
     *
     * @param object the object
     * @return the t
     */
    T beforeCreate(T object);

    /**
     * After find all list.
     *
     * @param list the list
     * @return the list
     */
    List<T> afterFindAll(List<T> list);

    /**
     * After find by id t.
     *
     * @param object the object
     * @return the t
     */
    T afterFindById(T object);

    /**
     * After create t.
     *
     * @param object the object
     * @return the t
     */
    T afterCreate(T object);
}
