package eu.isygoit.multitenancy.dto;

import com.fasterxml.jackson.databind.JsonNode;
import eu.isygoit.multitenancy.model.EventType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TimelineEventMessage {
    private EventType eventType;
    private String elementType;
    private String elementId;
    private String tenant;
    private LocalDateTime timestamp;
    private String modifiedBy;
    private JsonNode attributes;
}
