package eu.isygoit.model.timeline;

import com.fasterxml.jackson.databind.JsonNode;
import eu.isygoit.model.ITenantAssignable;
import lombok.*;

import java.time.LocalDateTime;

/**
 * The type Timeline event message.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimelineEventMessage implements ITenantAssignable {

    @Setter
    private String tenant;

    private TimelineEventType timelineEventType;
    private String elementType;
    private String elementId;
    private LocalDateTime timestamp;
    private String modifiedBy;
    private JsonNode attributes;
}
