package eu.isygoit.com.rest.service.tenancy;

import eu.isygoit.exception.ObjectNotFoundException;
import eu.isygoit.jwt.filter.QueryCriteria;
import eu.isygoit.model.IIdAssignable;
import eu.isygoit.model.ITenantAssignable;
import jakarta.transaction.NotSupportedException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * The interface Crud api method.
 *
 * @param <I> the type parameter
 * @param <T> the type parameter
 */
public interface ICrudTenantServiceMethods<I extends Serializable,
        T extends IIdAssignable<I> & ITenantAssignable> {

    /**
     * Count long.
     *
     * @param tenant the tenant
     * @return the long
     * @throws NotSupportedException the not supported exception
     */
    Long count(String tenant);

    /**
     * Exists by id boolean.
     *
     * @param tenant the tenant
     * @param id     the id
     * @return the boolean
     * @throws NotSupportedException the not supported exception
     */
    boolean existsById(String tenant, I id);

    /**
     * Create t.
     *
     * @param tenant the tenant
     * @param object the object
     * @return the t
     * @throws NotSupportedException the not supported exception
     */
    T create(String tenant, T object);

    /**
     * Create list.
     *
     * @param tenant  the tenant
     * @param objects the objects
     * @return the list
     * @throws NotSupportedException the not supported exception
     */
    List<T> createBatch(String tenant, List<T> objects);

    /**
     * Delete.
     *
     * @param tenant the tenant
     * @param id     the id
     * @throws NotSupportedException the not supported exception
     */
    void delete(String tenant, I id);

    /**
     * Delete.
     *
     * @param tenant  the tenant
     * @param objects the objects
     * @throws NotSupportedException the not supported exception
     */
    void deleteBatch(String tenant, List<T> objects);

    /**
     * Find all list.
     *
     * @param tenant the tenant
     * @return the list
     * @throws NotSupportedException the not supported exception
     */
    List<T> findAll(String tenant);

    /**
     * Find all list.
     *
     * @param tenant   the tenant
     * @param pageable the pageable
     * @return the list
     * @throws NotSupportedException the not supported exception
     */
    List<T> findAll(String tenant, Pageable pageable);

    /**
     * Find by id optional.
     *
     * @param tenant the tenant
     * @param id     the id
     * @return the optional
     * @throws ObjectNotFoundException the object not found exception
     */
    Optional<T> findById(String tenant, I id) throws ObjectNotFoundException;

    /**
     * Save or update t.
     *
     * @param tenant the tenant
     * @param object the object
     * @return the t
     * @throws NotSupportedException the not supported exception
     */
    T saveOrUpdate(String tenant, T object);

    /**
     * Save or update list.
     *
     * @param tenant  the tenant
     * @param objects the objects
     * @return the list
     * @throws NotSupportedException the not supported exception
     */
    List<T> saveOrUpdate(String tenant, List<T> objects);

    /**
     * Update t.
     *
     * @param tenant the tenant
     * @param object the object
     * @return the t
     * @throws NotSupportedException the not supported exception
     */
    T update(String tenant, T object);

    /**
     * Update list.
     *
     * @param tenant  the tenant
     * @param objects the objects
     * @return the list
     * @throws NotSupportedException the not supported exception
     */
    List<T> updateBatch(String tenant, List<T> objects);

    /**
     * Find all by criteria filter list.
     *
     * @param tenant   the tenant
     * @param criteria the criteria
     * @return the list
     * @throws NotSupportedException the not supported exception
     */
    List<T> findAllByCriteriaFilter(String tenant, List<QueryCriteria> criteria);

    /**
     * Find all by criteria filter list.
     *
     * @param tenant      the tenant
     * @param criteria    the criteria
     * @param pageRequest the page request
     * @return the list
     * @throws NotSupportedException the not supported exception
     */
    List<T> findAllByCriteriaFilter(String tenant, List<QueryCriteria> criteria, PageRequest pageRequest);

    /**
     * Gets by id in.
     *
     * @param ids the ids
     * @return the by id in
     */
    List<T> getByIdIn(List<I> ids);
}
