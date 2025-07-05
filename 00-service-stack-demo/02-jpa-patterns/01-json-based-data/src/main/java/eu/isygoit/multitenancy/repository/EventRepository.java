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

public interface EventRepository extends JpaPagingAndSortingTenantAssignableRepository<EventEntity, Long> {

    Long countByElementType(String elementType);

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
            )
            """, nativeQuery = true)
    boolean existsByElementTypeAndJsonId(@Param("elementType") String elementType, @Param("id") String id);

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
            """, nativeQuery = true)
    Optional<EventEntity> findByElementTypeAndJsonId(@Param("elementType") String elementType, @Param("id") String id);

    @Query(value = """
            SELECT * FROM events e 
            WHERE e.element_type = :elementType 
              AND e.attributes ->> 'id' = :id
              AND e.tenant_id = :tenant
            """, nativeQuery = true)
    Optional<EventEntity> findByElementTypeAndJsonIdAndTenant(@Param("elementType") String elementType, @Param("id") String id, @Param("tenant") String tenant);

    List<EventEntity> findAllByElementType(String elementType);

    List<EventEntity> findAllByElementTypeAndTenant(String elementType, String tenant);

    Page<EventEntity> findAllByElementType(String elementType, Pageable pageable);

    Page<EventEntity> findAllByElementTypeAndTenant(String elementType, String tenant, Pageable pageable);

    @Modifying
    @Query(value = """
            DELETE FROM events e 
            WHERE e.element_type = :elementType 
              AND e.attributes ->> 'id' = :id
            """, nativeQuery = true)
    void deleteByElementTypeAndJsonId(@Param("elementType") String elementType, @Param("id") String id);

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
            """, nativeQuery = true)
    void deleteByElementTypeAndJsonIdIn(@Param("elementType") String elementType, @Param("ids") List<String> ids);

    @Modifying
    @Query(value = """
            DELETE FROM events e 
            WHERE e.element_type = :elementType 
              AND e.attributes ->> 'id' IN (:ids)
              AND e.tenant_id = :tenant
            """, nativeQuery = true)
    void deleteByElementTypeAndJsonIdInAndTenant(@Param("elementType") String elementType, @Param("ids") List<String> ids, @Param("tenant") String tenant);
}