package eu.isygoit.com.rest.service;

import eu.isygoit.exception.ObjectNotFoundException;
import eu.isygoit.filter.QueryCriteria;
import eu.isygoit.model.AssignableId;
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
 * @param <E> the type parameter
 */
public interface ICrudServiceMethod<E extends AssignableId, I extends Serializable>
        extends ICrudServiceEvents<E, I>,
        ICrudServiceUtils<E, I> {

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
    Long count(String domain);

    /**
     * Exists by id boolean.
     *
     * @param id the id
     * @return the boolean
     */
    boolean existsById(I id);

    /**
     * Create e.
     *
     * @param object the object
     * @return the e
     */
    E create(E object);

    /**
     * Create and flush e.
     *
     * @param object the object
     * @return the e
     */
    E createAndFlush(E object);

    List<E> createAndFlush(List<E> object);

    /**
     * Create list.
     *
     * @param objects the objects
     * @return the list
     */
    List<E> create(List<E> objects);

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
    void delete(String senderDomain, List<E> objects);

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
    void delete(List<E> objects);

    /**
     * Find all list.
     *
     * @return the list
     */
    List<E> findAll();

    /**
     * Find all list.
     *
     * @param domain the domain
     * @return the list
     * @throws NotSupportedException the not supported exception
     */
    List<E> findAll(String domain) throws NotSupportedException;

    /**
     * Find all list.
     *
     * @param pageable the pageable
     * @return the list
     */
    List<E> findAll(Pageable pageable);

    /**
     * Find all list.
     *
     * @param domain   the domain
     * @param pageable the pageable
     * @return the list
     * @throws NotSupportedException the not supported exception
     */
    List<E> findAll(String domain, Pageable pageable) throws NotSupportedException;

    /**
     * Find by id optional.
     *
     * @param id the id
     * @return the optional
     * @throws ObjectNotFoundException the object not found exception
     */
    Optional<E> findById(I id) throws ObjectNotFoundException;

    /**
     * Save or update e.
     *
     * @param object the object
     * @return the e
     */
    E saveOrUpdate(E object);

    /**
     * Save or update list.
     *
     * @param objects the objects
     * @return the list
     */
    List<E> saveOrUpdate(List<E> objects);

    /**
     * Update e.
     *
     * @param object the object
     * @return the e
     */
    E update(E object);

    /**
     * Update and flush e.
     *
     * @param object the object
     * @return the e
     */
    E updateAndFlush(E object);

    /**
     * Update list.
     *
     * @param objects the objects
     * @return the list
     */
    List<E> update(List<E> objects);

    List<E> updateAndFlush(List<E> objects);

    /**
     * Find all by criteria filter list.
     *
     * @param domain   the domain
     * @param criteria the criteria
     * @return the list
     */
    List<E> findAllByCriteriaFilter(String domain, List<QueryCriteria> criteria);

    /**
     * Find all by criteria filter list.
     *
     * @param domain      the domain
     * @param criteria    the criteria
     * @param pageRequest the page request
     * @return the list
     */
    List<E> findAllByCriteriaFilter(String domain, List<QueryCriteria> criteria, PageRequest pageRequest);
}
