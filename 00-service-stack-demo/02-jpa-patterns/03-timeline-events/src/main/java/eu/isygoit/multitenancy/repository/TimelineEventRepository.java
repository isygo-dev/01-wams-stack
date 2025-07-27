package eu.isygoit.multitenancy.repository;

import eu.isygoit.multitenancy.model.TimeLineEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TimelineEventRepository extends JpaRepository<TimeLineEvent, Long> {
    List<TimeLineEvent> findByElementTypeAndElementIdAndTenant(String elementType, String elementId, String tenant);

    Optional<TimeLineEvent> findFirstByElementIdAndElementTypeOrderByTimestampDesc(String elementId, String elementType);

    List<TimeLineEvent> findByElementIdAndElementTypeOrderByTimestampAsc(String elementId, String elementType);
}
