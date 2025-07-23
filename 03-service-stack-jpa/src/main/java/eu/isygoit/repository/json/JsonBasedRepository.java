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

public interface JsonBasedRepository<T extends IIdAssignable<I>,
        I extends Serializable>
        extends JpaPagingAndSortingRepository<T, I> {

    Long countByElementType(String elementType);

    @Query(value = """
            SELECT EXISTS (
                SELECT 1 FROM events e 
                WHERE e.element_type = :elementType 
                  AND e.attributes ->> 'id' = :id
            )
            """, nativeQuery = true)
    boolean existsByElementTypeAndJsonId(@Param("elementType") String elementType, @Param("id") String id);

    @Query(value = """
            SELECT * FROM events e 
            WHERE e.element_type = :elementType 
              AND e.attributes ->> 'id' = :id
            """, nativeQuery = true)
    Optional<T> findByElementTypeAndJsonId(@Param("elementType") String elementType, @Param("id") String id);

    List<T> findAllByElementType(String elementType);

    Page<T> findAllByElementType(String elementType, Pageable pageable);

    @Modifying
    @Query(value = """
            DELETE FROM events e 
            WHERE e.element_type = :elementType 
              AND e.attributes ->> 'id' = :id
            """, nativeQuery = true)
    int deleteByElementTypeAndJsonId(@Param("elementType") String elementType, @Param("id") String id);

    @Modifying
    @Query(value = """
            DELETE FROM events e 
            WHERE e.element_type = :elementType 
              AND e.attributes ->> 'id' IN (:ids)
            """, nativeQuery = true)
    int deleteByElementTypeAndJsonIdIn(@Param("elementType") String elementType, @Param("ids") List<String> ids);
}