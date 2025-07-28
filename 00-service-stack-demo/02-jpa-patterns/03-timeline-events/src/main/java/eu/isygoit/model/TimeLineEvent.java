package eu.isygoit.model;

import eu.isygoit.model.timeline.TimelineEventEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * The type Time line event.
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "TIME_LINE")
public class TimeLineEvent extends TimelineEventEntity implements ITenantAssignable, IIdAssignable<Long> {

    @Id
    @SequenceGenerator(name = "timeline_event_sequence", sequenceName = "timeline_event_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "timeline_event_sequence")
    private Long id;

    @Column(name = "TENANT_ID", nullable = false, updatable = false)
    private String tenant;
}
