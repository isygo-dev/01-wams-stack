package eu.isygoit.multitenancy.service;

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

import java.util.Optional;

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

                    // Find the most recent previous event for this element
                    Optional<TimeLineEvent> previousEvent = timelineEventRepository
                            .findFirstByElementIdAndElementTypeOrderByTimestampDesc(
                                    message.getElementId(),
                                    message.getElementType());

                    // Handle attributes based on event type
                    switch (message.getEventType()) {
                        case CREATED:
                            // Wrap full attributes in "data" field for create
                            wrapperNode.set("data", message.getAttributes());
                            event.setAttributes(wrapperNode);
                            break;
                        case UPDATED:
                            if (previousEvent.isPresent() && previousEvent.get().getAttributes() != null) {
                                // Extract previous attributes from "data" field
                                event.setTenant(previousEvent.get().getTenant());
                                ObjectNode previousAttributes = (ObjectNode) objectMapper.readTree(previousEvent.get().getAttributes().asText());
                                //(ObjectNode) previousEvent.get().getAttributes().get("data");
                                if (previousAttributes != null) {
                                    // Calculate diff between old and new attributes
                                    ObjectNode diff = JsonHelper.computeDiff(
                                            previousAttributes.get("data"),
                                            message.getAttributes());
                                    wrapperNode.set("data", diff);
                                    event.setAttributes(wrapperNode);
                                } else {
                                    // If no previous data, wrap full attributes in "data"
                                    wrapperNode.set("data", message.getAttributes());
                                    event.setAttributes(wrapperNode);
                                }
                            } else {
                                // If no previous event, wrap full attributes in "data"
                                wrapperNode.set("data", message.getAttributes());
                                event.setAttributes(wrapperNode);
                            }
                            break;
                        case DELETED:
                            // Find the most recent previous event for this element
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