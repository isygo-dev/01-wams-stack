package eu.isygoit.com.rest.service;

import eu.isygoit.model.IIdEntity;

import java.io.Serializable;
import java.util.List;

/**
 * The interface Crud service events.
 *
 * @param <I> the type parameter
 * @param <E> the type parameter
 */
public interface ICrudServiceEvents<I extends Serializable, E extends IIdEntity> {

    /**
     * Before update t.
     *
     * @param object the object
     * @return the t
     */
    E beforeUpdate(E object);

    /**
     * After update t.
     *
     * @param object the object
     * @return the t
     */
    E afterUpdate(E object);

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
    void beforeDelete(List<E> objects);

    /**
     * After delete.
     *
     * @param objects the objects
     */
    void afterDelete(List<E> objects);

    /**
     * Before create t.
     *
     * @param object the object
     * @return the t
     */
    E beforeCreate(E object);

    /**
     * After find all list.
     *
     * @param list the list
     * @return the list
     */
    List<E> afterFindAll(List<E> list);

    /**
     * After find by id t.
     *
     * @param object the object
     * @return the t
     */
    E afterFindById(E object);

    /**
     * After create t.
     *
     * @param object the object
     * @return the t
     */
    E afterCreate(E object);
}
