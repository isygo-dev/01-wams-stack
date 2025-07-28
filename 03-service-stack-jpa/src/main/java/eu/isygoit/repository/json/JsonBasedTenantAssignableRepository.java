package eu.isygoit.repository.json;

import eu.isygoit.annotation.IgnoreRepository;
import eu.isygoit.model.IIdAssignable;
import eu.isygoit.model.ITenantAssignable;
import eu.isygoit.repository.tenancy.JpaPagingAndSortingTenantAssignableRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * The interface Json based tenant assignable repository.
 *
 * @param <T> the type parameter
 * @param <I> the type parameter
 */
@IgnoreRepository
@NoRepositoryBean
public interface JsonBasedTenantAssignableRepository<T extends ITenantAssignable & IIdAssignable<I>,
        I extends Serializable>
        extends JpaPagingAndSortingTenantAssignableRepository<T, I> {

    /**
     * Count by element type and tenant long.
     *
     * @param elementType the element type
     * @param tenant      the tenant
     * @return the long
     */
    @Query(value = """
            SELECT COUNT(*) FROM events e 
            WHERE e.element_type = :elementType 
              AND e.tenant_id = :tenant
            """, nativeQuery = true)
    Long countByElementTypeAndTenant(@Param("elementType") String elementType, @Param("tenant") String tenant);

    /**
     * Exists by element type and json id and tenant boolean.
     *
     * @param elementType the element type
     * @param id          the id
     * @param tenant      the tenant
     * @return the boolean
     */
    @Query(value = """
            SELECT EXISTS (
                SELECT 1 FROM events e 
                WHERE e.element_type = :elementType 
                  AND e.attributes ->> 'id' = :id
                  AND e.tenant_id = :tenant
            )
            """, nativeQuery = true)
    boolean existsByElementTypeAndJsonIdAndTenant(@Param("elementType") String elementType, @Param("id") String id, @Param("tenant") String tenant);

    /**
     * Find by element type and json id and tenant optional.
     *
     * @param elementType the element type
     * @param id          the id
     * @param tenant      the tenant
     * @return the optional
     */
    @Query(value = """
            SELECT * FROM events e 
            WHERE e.element_type = :elementType 
              AND e.attributes ->> 'id' = :id
              AND e.tenant_id = :tenant
            """, nativeQuery = true)
    Optional<T> findByElementTypeAndJsonIdAndTenant(@Param("elementType") String elementType, @Param("id") String id, @Param("tenant") String tenant);

    /**
     * Find all by element type and tenant list.
     *
     * @param elementType the element type
     * @param tenant      the tenant
     * @return the list
     */
    List<T> findAllByElementTypeAndTenant(String elementType, String tenant);

    /**
     * Find all by element type and tenant page.
     *
     * @param elementType the element type
     * @param tenant      the tenant
     * @param pageable    the pageable
     * @return the page
     */
    Page<T> findAllByElementTypeAndTenant(String elementType, String tenant, Pageable pageable);

    /**
     * Delete by element type and json id and tenant int.
     *
     * @param elementType the element type
     * @param id          the id
     * @param tenant      the tenant
     * @return the int
     */
    @Modifying
    @Query(value = """
            DELETE FROM events e 
            WHERE e.element_type = :elementType 
              AND e.attributes ->> 'id' = :id
              AND e.tenant_id = :tenant
            """, nativeQuery = true)
    int deleteByElementTypeAndJsonIdAndTenant(@Param("elementType") String elementType, @Param("id") String id, @Param("tenant") String tenant);

    /**
     * Delete by element type and json id in and tenant int.
     *
     * @param elementType the element type
     * @param ids         the ids
     * @param tenant      the tenant
     * @return the int
     */
    @Modifying
    @Query(value = """
            DELETE FROM events e 
            WHERE e.element_type = :elementType 
              AND e.attributes ->> 'id' IN (:ids)
              AND e.tenant_id = :tenant
            """, nativeQuery = true)
    int deleteByElementTypeAndJsonIdInAndTenant(@Param("elementType") String elementType, @Param("ids") List<String> ids, @Param("tenant") String tenant);
}

