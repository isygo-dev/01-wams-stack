package eu.isygoit.com.rest.service;

import eu.isygoit.exception.ObjectNotFoundException;
import eu.isygoit.filter.QueryCriteria;
import eu.isygoit.model.IIdAssignable;
import jakarta.transaction.NotSupportedException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * The interface Crud service method.
 *
 * @param <I> the type parameter
 * @param <T> the type parameter
 */
public interface ICrudServiceMethod<I extends Serializable, T extends IIdAssignable<I>>
        extends ICrudServiceEvents<I, T>,
        ICrudServiceUtils<I, T> {

    /**
     * Count long.
     *
     * @return the long
     */
    Long count();

    /**
     * Count long.
     *
     * @param domain the domain
     * @return the long
     */
    Long count(String domain) throws NotSupportedException;

    /**
     * Exists by id boolean.
     *
     * @param id the id
     * @return the boolean
     */
    boolean existsById(I id);

    /**
     * Create t.
     *
     * @param object the object
     * @return the t
     */
    T create(T object);

    /**
     * Create and flush t.
     *
     * @param object the object
     * @return the t
     */
    T createAndFlush(T object);

    /**
     * Create list.
     *
     * @param objects the objects
     * @return the list
     */
    List<T> create(List<T> objects);

    /**
     * Delete.
     *
     * @param senderDomain the sender domain
     * @param id           the id
     */
    void delete(String senderDomain, I id);

    /**
     * Delete.
     *
     * @param senderDomain the sender domain
     * @param objects      the objects
     */
    void delete(String senderDomain, List<T> objects);

    /**
     * Delete.
     *
     * @param id the id
     */
    void delete(I id);

    /**
     * Delete.
     *
     * @param objects the objects
     */
    void delete(List<T> objects);

    /**
     * Find all list.
     *
     * @return the list
     */
    List<T> findAll();

    /**
     * Find all list.
     *
     * @param domain the domain
     * @return the list
     * @throws NotSupportedException the not supported exception
     */
    List<T> findAll(String domain) throws NotSupportedException;

    /**
     * Find all list.
     *
     * @param pageable the pageable
     * @return the list
     */
    List<T> findAll(Pageable pageable);

    /**
     * Find all list.
     *
     * @param domain   the domain
     * @param pageable the pageable
     * @return the list
     * @throws NotSupportedException the not supported exception
     */
    List<T> findAll(String domain, Pageable pageable) throws NotSupportedException;

    /**
     * Find by id t.
     *
     * @param id the id
     * @return the t
     * @throws ObjectNotFoundException the object not found exception
     */
    Optional<T> findById(I id) throws ObjectNotFoundException;

    /**
     * Save or update t.
     *
     * @param object the object
     * @return the t
     */
    T saveOrUpdate(T object);

    /**
     * Save or update list.
     *
     * @param objects the objects
     * @return the list
     */
    List<T> saveOrUpdate(List<T> objects);

    /**
     * Update t.
     *
     * @param object the object
     * @return the t
     */
    T update(T object);

    /**
     * Update and flush t.
     *
     * @param object the object
     * @return the t
     */
    T updateAndFlush(T object);

    /**
     * Update list.
     *
     * @param objects the objects
     * @return the list
     */
    List<T> update(List<T> objects);

    /**
     * Find all by criteria filter list.
     *
     * @param domain   the domain
     * @param criteria the criteria
     * @return the list
     */
    List<T> findAllByCriteriaFilter(String domain, List<QueryCriteria> criteria);

    /**
     * Find all by criteria filter list.
     *
     * @param domain      the domain
     * @param criteria    the criteria
     * @param pageRequest the page request
     * @return the list
     */
    List<T> findAllByCriteriaFilter(String domain, List<QueryCriteria> criteria, PageRequest pageRequest);
}
