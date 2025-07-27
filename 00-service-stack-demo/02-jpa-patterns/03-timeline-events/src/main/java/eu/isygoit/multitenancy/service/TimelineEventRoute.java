package eu.isygoit.multitenancy.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.isygoit.helper.JsonHelper;
import eu.isygoit.multitenancy.dto.TimelineEventMessage;
import eu.isygoit.multitenancy.model.TimeLineEvent;
import eu.isygoit.multitenancy.repository.TimelineEventRepository;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * The type Timeline event route.
 */
@Component
public class TimelineEventRoute extends RouteBuilder {

    @Autowired
    private TimelineEventRepository timelineEventRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    @Override
    public void configure() throws Exception {
        from("seda:timelineEvents?concurrentConsumers=1")
                .routeId("timeline-event-processor")
                .process(exchange -> {
                    String messageBody = exchange.getIn().getBody(String.class);
                    TimelineEventMessage message = objectMapper.readValue(messageBody, TimelineEventMessage.class);

                    TimeLineEvent event = new TimeLineEvent();
                    event.setTenant(message.getTenant());
                    event.setEventType(message.getEventType());
                    event.setElementType(message.getElementType());
                    event.setElementId(message.getElementId());
                    event.setTimestamp(java.time.LocalDateTime.now());
                    event.setModifiedBy(message.getModifiedBy());

                    // Create a wrapper object with "data" field
                    ObjectNode wrapperNode = objectMapper.createObjectNode();

                    // Handle attributes based on event type
                    switch (message.getEventType()) {
                        case CREATED:
                            // Wrap full attributes in "data" field for create
                            wrapperNode.set("data", message.getAttributes());
                            event.setAttributes(wrapperNode);
                            break;
                        case UPDATED:
                            // Find all previous events for this element, ordered by timestamp
                            List<TimeLineEvent> previousEvents = timelineEventRepository
                                    .findByElementIdAndElementTypeOrderByTimestampAsc(
                                            message.getElementId(),
                                            message.getElementType());

                            ObjectNode reconstructedState = objectMapper.createObjectNode();
                            if (!previousEvents.isEmpty()) {
                                // Reconstruct the previous state from all prior events
                                for (TimeLineEvent prevEvent : previousEvents) {
                                    JsonNode prevAttributes = prevEvent.getAttributes();
                                    JsonNode prevData = null;
                                    if (prevEvent.getAttributes() != null) {
                                        if (prevAttributes.isObject()) {
                                            // Direct access for ObjectNode (PostgreSQL)
                                            prevData = prevAttributes.get("data");
                                        } else if (prevAttributes.isTextual()) {
                                            // Parse JSON string for TextNode (H2)
                                            try {
                                                prevAttributes = objectMapper.readTree(prevAttributes.asText());
                                                if (prevAttributes.isObject()) {
                                                    prevData = prevAttributes.get("data");
                                                }
                                            } catch (Exception e) {
                                                log.error("Failed to parse attributes for event ID {}: {}", prevEvent.getId(), e.getMessage());
                                            }
                                        }
                                        if (prevData != null && prevData.isObject()) {
                                            // Merge fields, keeping the last non-null value
                                            prevData.fields().forEachRemaining(field -> {
                                                if (!field.getValue().isNull()) {
                                                    reconstructedState.set(field.getKey(), field.getValue());
                                                } else {
                                                    reconstructedState.remove(field.getKey());
                                                }
                                            });
                                        }
                                    }
                                    // Set tenant from the most recent event
                                    event.setTenant(prevEvent.getTenant());
                                }
                            }

                            // Calculate diff between reconstructed state and current attributes
                            ObjectNode diff = JsonHelper.computeDiff(
                                    reconstructedState.size() > 0 ? reconstructedState : null,
                                    message.getAttributes());
                            wrapperNode.set("data", diff);
                            event.setAttributes(wrapperNode);
                            break;
                        case DELETED:
                            // Find the most recent previous event to set tenant
                            Optional<TimeLineEvent> previousEvent = timelineEventRepository
                                    .findFirstByElementIdAndElementTypeOrderByTimestampDesc(
                                            message.getElementId(),
                                            message.getElementType());
                            if (previousEvent.isPresent() && previousEvent.get().getAttributes() != null) {
                                event.setTenant(previousEvent.get().getTenant());
                            }
                            // Set empty JSON object in "data" field for delete
                            wrapperNode.set("data", objectMapper.createObjectNode());
                            event.setAttributes(wrapperNode);
                            break;
                    }

                    timelineEventRepository.save(event);
                });
    }
}