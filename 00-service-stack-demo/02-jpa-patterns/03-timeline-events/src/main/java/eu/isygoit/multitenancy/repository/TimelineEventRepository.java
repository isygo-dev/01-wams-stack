package eu.isygoit.multitenancy.repository;

import eu.isygoit.multitenancy.model.TimeLineEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * The interface Timeline event repository.
 */
public interface TimelineEventRepository extends JpaRepository<TimeLineEvent, Long> {
    /**
     * Find by element type and element id and tenant list.
     *
     * @param elementType the element type
     * @param elementId   the element id
     * @param tenant      the tenant
     * @return the list
     */
    List<TimeLineEvent> findByElementTypeAndElementIdAndTenant(String elementType, String elementId, String tenant);

    /**
     * Find first by element id and element type order by timestamp desc optional.
     *
     * @param elementId   the element id
     * @param elementType the element type
     * @return the optional
     */
    Optional<TimeLineEvent> findFirstByElementIdAndElementTypeOrderByTimestampDesc(String elementId, String elementType);

    /**
     * Find by element id and element type order by timestamp asc list.
     *
     * @param elementId   the element id
     * @param elementType the element type
     * @return the list
     */
    List<TimeLineEvent> findByElementIdAndElementTypeOrderByTimestampAsc(String elementId, String elementType);
}
