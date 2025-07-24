package eu.isygoit.multitenancy.repository;

import eu.isygoit.multitenancy.model.TimeLineEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TimelineEventRepository extends JpaRepository<TimeLineEvent, Long> {
    List<TimeLineEvent> findByElementTypeAndElementIdAndTenant(String elementType, String elementId, String tenant);
}
