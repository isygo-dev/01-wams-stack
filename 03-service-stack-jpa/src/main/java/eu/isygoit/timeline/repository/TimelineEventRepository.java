package eu.isygoit.timeline.repository;

import eu.isygoit.annotation.IgnoreRepository;
import eu.isygoit.timeline.schema.TimelineEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * The interface Timeline event repository.
 */
@IgnoreRepository
@NoRepositoryBean
public interface TimelineEventRepository<T extends TimelineEventEntity, I extends Serializable> extends JpaRepository<T, I> {
    /**
     * Find by element type and element id and tenant list.
     *
     * @param elementType the element type
     * @param elementId   the element id
     * @param tenant      the tenant
     * @return the list
     */
    List<T> findByElementTypeAndElementIdAndTenant(String elementType, String elementId, String tenant);

    /**
     * Find first by element id and element type order by timestamp desc optional.
     *
     * @param elementId   the element id
     * @param elementType the element type
     * @return the optional
     */
    Optional<T> findFirstByElementIdAndElementTypeOrderByTimestampDesc(String elementId, String elementType);

    /**
     * Find by element id and element type order by timestamp asc list.
     *
     * @param elementId   the element id
     * @param elementType the element type
     * @return the list
     */
    List<T> findByElementIdAndElementTypeOrderByTimestampAsc(String elementId, String elementType);
}
