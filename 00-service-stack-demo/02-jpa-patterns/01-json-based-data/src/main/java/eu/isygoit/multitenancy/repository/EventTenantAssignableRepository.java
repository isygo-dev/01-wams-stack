package eu.isygoit.multitenancy.repository;

import eu.isygoit.multitenancy.model.EventEntity;
import eu.isygoit.repository.JpaPagingAndSortingTenantAssignableRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EventTenantAssignableRepository extends JpaPagingAndSortingTenantAssignableRepository<EventEntity, Long> {

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
    Optional<EventEntity> findByElementTypeAndJsonIdAndTenant(@Param("elementType") String elementType, @Param("id") String id, @Param("tenant") String tenant);

    List<EventEntity> findAllByElementTypeAndTenant(String elementType, String tenant);

    Page<EventEntity> findAllByElementTypeAndTenant(String elementType, String tenant, Pageable pageable);

    @Modifying
    @Query(value = """
            DELETE FROM events e 
            WHERE e.element_type = :elementType 
              AND e.attributes ->> 'id' = :id
              AND e.tenant_id = :tenant
            """, nativeQuery = true)
    void deleteByElementTypeAndJsonIdAndTenant(@Param("elementType") String elementType, @Param("id") String id, @Param("tenant") String tenant);

    @Modifying
    @Query(value = """
            DELETE FROM events e 
            WHERE e.element_type = :elementType 
              AND e.attributes ->> 'id' IN (:ids)
              AND e.tenant_id = :tenant
            """, nativeQuery = true)
    void deleteByElementTypeAndJsonIdInAndTenant(@Param("elementType") String elementType, @Param("ids") List<String> ids, @Param("tenant") String tenant);
}