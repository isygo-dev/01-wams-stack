package eu.isygoit.model.timeline;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * The type Timeline event message.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimelineEventMessage {
    private TimelineEventType timelineEventType;
    private String elementType;
    private String elementId;
    private String tenant;
    private LocalDateTime timestamp;
    private String modifiedBy;
    private JsonNode attributes;
}
