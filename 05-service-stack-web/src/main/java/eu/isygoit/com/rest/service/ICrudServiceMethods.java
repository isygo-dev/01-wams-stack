package eu.isygoit.com.rest.service;

import eu.isygoit.exception.ObjectNotFoundException;
import eu.isygoit.filter.QueryCriteria;
import eu.isygoit.model.IIdAssignable;
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
public interface ICrudServiceMethods<I extends Serializable, T extends IIdAssignable<I>> {

    /**
     * Count long.
     *
     * @return the long
     */
    Long count();

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
     * Create list.
     *
     * @param objects the objects
     * @return the list
     */
    List<T> createBatch(List<T> objects);

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
    void deleteBatch(List<T> objects);

    /**
     * Find all list.
     *
     * @return the list
     */
    List<T> findAll();

    /**
     * Find all list.
     *
     * @param pageable the pageable
     * @return the list
     */
    List<T> findAll(Pageable pageable);

    /**
     * Find by id optional.
     *
     * @param id the id
     * @return the optional
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
     * Update list.
     *
     * @param objects the objects
     * @return the list
     */
    List<T> updateBatch(List<T> objects);

    /**
     * Find all by criteria filter list.
     *
     * @param criteria the criteria
     * @return the list
     */
    List<T> findAllByCriteriaFilter(List<QueryCriteria> criteria);

    /**
     * Find all by criteria filter list.
     *
     * @param criteria    the criteria
     * @param pageRequest the page request
     * @return the list
     */
    List<T> findAllByCriteriaFilter(List<QueryCriteria> criteria, PageRequest pageRequest);

    /**
     * Gets by id in.
     *
     * @param ids the ids
     * @return the by id in
     */
    List<T> getByIdIn(List<I> ids);
}
