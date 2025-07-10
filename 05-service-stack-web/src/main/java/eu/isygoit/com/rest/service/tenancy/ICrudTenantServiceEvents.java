package eu.isygoit.com.rest.service.tenancy;

import eu.isygoit.model.IIdAssignable;

import java.io.Serializable;
import java.util.List;

/**
 * The interface Crud api events.
 *
 * @param <I> the type parameter
 * @param <T> the type parameter
 */
public interface ICrudTenantServiceEvents<I extends Serializable, T extends IIdAssignable<I>> {

    /**
     * Before update t.
     *
     * @param tenant the tenant
     * @param object the object
     * @return the t
     */
    T beforeUpdate(String tenant, T object);

    /**
     * After update t.
     *
     * @param tenant the tenant
     * @param object the object
     * @return the t
     */
    T afterUpdate(String tenant, T object);

    /**
     * Before delete.
     *
     * @param tenant the tenant
     * @param id     the id
     */
    void beforeDelete(String tenant, I id);

    /**
     * After delete.
     *
     * @param tenant the tenant
     * @param id     the id
     */
    void afterDelete(String tenant, I id);

    /**
     * Before delete.
     *
     * @param tenant  the tenant
     * @param objects the objects
     */
    void beforeDelete(String tenant, List<T> objects);

    /**
     * After delete.
     *
     * @param tenant  the tenant
     * @param objects the objects
     */
    void afterDelete(String tenant, List<T> objects);

    /**
     * Before create t.
     *
     * @param tenant the tenant
     * @param object the object
     * @return the t
     */
    T beforeCreate(String tenant, T object);

    /**
     * After find all list.
     *
     * @param tenant the tenant
     * @param list   the list
     * @return the list
     */
    List<T> afterFindAll(String tenant, List<T> list);

    /**
     * After find by id t.
     *
     * @param tenant the tenant
     * @param object the object
     * @return the t
     */
    T afterFindById(String tenant, T object);

    /**
     * After create t.
     *
     * @param tenant the tenant
     * @param object the object
     * @return the t
     */
    T afterCreate(String tenant, T object);
}
