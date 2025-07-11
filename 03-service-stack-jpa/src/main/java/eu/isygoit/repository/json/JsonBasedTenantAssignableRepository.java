package eu.isygoit.repository.json;

import eu.isygoit.jwt.filter.QueryCriteria;
import eu.isygoit.model.IIdAssignable;
import eu.isygoit.model.ITenantAssignable;
import eu.isygoit.model.json.JsonElement;
import eu.isygoit.repository.tenancy.JpaPagingAndSortingTenantAssignableRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JsonBasedTenantAssignableRepository<T extends ITenantAssignable & IIdAssignable<I>,
        I extends Serializable>
        extends JpaPagingAndSortingTenantAssignableRepository<T, I> {

    @Query(value = """
            SELECT COUNT(*) FROM events e 
            WHERE e.element_type = :elementType 
              AND e.tenant_id = :tenant
            """, nativeQuery = true)
    Long countByElementTypeAndTenant(@Param("elementType") String elementType, @Param("tenant") String tenant);

    @Query(value = """
            SELECT EXISTS (
                SELECT 1 FROM events e 
                WHERE e.element_type = :elementType 
                  AND e.attributes ->> 'id' = :id
                  AND e.tenant_id = :tenant
            )
            """, nativeQuery = true)
    boolean existsByElementTypeAndJsonIdAndTenant(@Param("elementType") String elementType, @Param("id") String id, @Param("tenant") String tenant);

    @Query(value = """
            SELECT * FROM events e 
            WHERE e.element_type = :elementType 
              AND e.attributes ->> 'id' = :id
              AND e.tenant_id = :tenant
            """, nativeQuery = true)
    Optional<T> findByElementTypeAndJsonIdAndTenant(@Param("elementType") String elementType, @Param("id") String id, @Param("tenant") String tenant);

    List<T> findAllByElementTypeAndTenant(String elementType, String tenant);

    Page<T> findAllByElementTypeAndTenant(String elementType, String tenant, Pageable pageable);

    @Modifying
    @Query(value = """
            DELETE FROM events e 
            WHERE e.element_type = :elementType 
              AND e.attributes ->> 'id' = :id
              AND e.tenant_id = :tenant
            """, nativeQuery = true)
    int deleteByElementTypeAndJsonIdAndTenant(@Param("elementType") String elementType, @Param("id") String id, @Param("tenant") String tenant);

    @Modifying
    @Query(value = """
            DELETE FROM events e 
            WHERE e.element_type = :elementType 
              AND e.attributes ->> 'id' IN (:ids)
              AND e.tenant_id = :tenant
            """, nativeQuery = true)
    int deleteByElementTypeAndJsonIdInAndTenant(@Param("elementType") String elementType, @Param("ids") List<String> ids, @Param("tenant") String tenant);
}

