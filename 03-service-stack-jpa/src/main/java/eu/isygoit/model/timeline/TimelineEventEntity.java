package eu.isygoit.model.timeline;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * The type Timeline event entity.
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
public abstract class TimelineEventEntity implements ITimelineEventEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TimelineEventType eventType;

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

