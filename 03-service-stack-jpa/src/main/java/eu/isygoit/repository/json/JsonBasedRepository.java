package eu.isygoit.repository.json;

import eu.isygoit.model.IIdAssignable;
import eu.isygoit.repository.JpaPagingAndSortingRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * The interface Json based repository.
 *
 * @param <T> the type parameter
 * @param <I> the type parameter
 */
public interface JsonBasedRepository<T extends IIdAssignable<I>,
        I extends Serializable>
        extends JpaPagingAndSortingRepository<T, I> {

    /**
     * Count by element type long.
     *
     * @param elementType the element type
     * @return the long
     */
    Long countByElementType(String elementType);

    /**
     * Exists by element type and json id boolean.
     *
     * @param elementType the element type
     * @param id          the id
     * @return the boolean
     */
    @Query(value = """
            SELECT EXISTS (
                SELECT 1 FROM events e 
                WHERE e.element_type = :elementType 
                  AND e.attributes ->> 'id' = :id
            )
            """, nativeQuery = true)
    boolean existsByElementTypeAndJsonId(@Param("elementType") String elementType, @Param("id") String id);

    /**
     * Find by element type and json id optional.
     *
     * @param elementType the element type
     * @param id          the id
     * @return the optional
     */
    @Query(value = """
            SELECT * FROM events e 
            WHERE e.element_type = :elementType 
              AND e.attributes ->> 'id' = :id
            """, nativeQuery = true)
    Optional<T> findByElementTypeAndJsonId(@Param("elementType") String elementType, @Param("id") String id);

    /**
     * Find all by element type list.
     *
     * @param elementType the element type
     * @return the list
     */
    List<T> findAllByElementType(String elementType);

    /**
     * Find all by element type page.
     *
     * @param elementType the element type
     * @param pageable    the pageable
     * @return the page
     */
    Page<T> findAllByElementType(String elementType, Pageable pageable);

    /**
     * Delete by element type and json id int.
     *
     * @param elementType the element type
     * @param id          the id
     * @return the int
     */
    @Modifying
    @Query(value = """
            DELETE FROM events e 
            WHERE e.element_type = :elementType 
              AND e.attributes ->> 'id' = :id
            """, nativeQuery = true)
    int deleteByElementTypeAndJsonId(@Param("elementType") String elementType, @Param("id") String id);

    /**
     * Delete by element type and json id in int.
     *
     * @param elementType the element type
     * @param ids         the ids
     * @return the int
     */
    @Modifying
    @Query(value = """
            DELETE FROM events e 
            WHERE e.element_type = :elementType 
              AND e.attributes ->> 'id' IN (:ids)
            """, nativeQuery = true)
    int deleteByElementTypeAndJsonIdIn(@Param("elementType") String elementType, @Param("ids") List<String> ids);
}