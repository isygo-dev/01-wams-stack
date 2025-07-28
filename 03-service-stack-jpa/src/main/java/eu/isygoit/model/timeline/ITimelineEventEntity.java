package eu.isygoit.model.timeline;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;

/**
 * The type Timeline event entity.
 */
public interface ITimelineEventEntity {

    TimelineEventType getEventType();

    void setEventType(TimelineEventType timelineEventType);

    String getElementType();

    void setElementType(String elementType);

    String getElementId();

    void setElementId(String elementId);

    LocalDateTime getTimestamp();

    void setTimestamp(LocalDateTime timestamp);

    String getModifiedBy();

    void setModifiedBy(String modifiedBy);

    JsonNode getAttributes();

    void setAttributes(JsonNode attributes);
}

