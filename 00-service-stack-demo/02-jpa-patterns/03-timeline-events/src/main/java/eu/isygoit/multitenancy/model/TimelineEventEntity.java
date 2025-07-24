package eu.isygoit.multitenancy.model;

import com.fasterxml.jackson.databind.JsonNode;
import eu.isygoit.model.jakarta.AbstractEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
public abstract class TimelineEventEntity extends AbstractEntity<Long> {

    @Id
    @SequenceGenerator(name = "timeline_event_seq", sequenceName = "timeline_event_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "timeline_event_seq")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType eventType;

    @Column(name = "ELEMENT_TYPE", nullable = false)
    private String elementType;

    @Column(nullable = false)
    private String elementId;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column
    private String modifiedBy;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private JsonNode attributes;
}

