package eu.isygoit.model.timeline;

import com.fasterxml.jackson.databind.JsonNode;
import eu.isygoit.model.jakarta.AbstractEntity;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * The type Timeline event entity.
 */
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
public abstract class TimelineEventEntity<I extends Serializable> extends AbstractEntity<I> implements ITimelineEventEntity {

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

