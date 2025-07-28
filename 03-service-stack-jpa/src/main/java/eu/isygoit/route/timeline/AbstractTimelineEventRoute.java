package eu.isygoit.route.timeline;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.isygoit.helper.JsonHelper;
import eu.isygoit.model.IIdAssignable;
import eu.isygoit.model.ITenantAssignable;
import eu.isygoit.model.timeline.ITimelineEventEntity;
import eu.isygoit.model.timeline.TimelineEventMessage;
import eu.isygoit.repository.timeline.TimelineEventRepository;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Optional;

/**
 * The type Timeline event route.
 */
public abstract class AbstractTimelineEventRoute<T extends ITimelineEventEntity & IIdAssignable> extends RouteBuilder {

    private final TimelineEventRepository timelineEventRepository;

    protected AbstractTimelineEventRoute(TimelineEventRepository timelineEventRepository) {
        this.timelineEventRepository = timelineEventRepository;
    }

    @Transactional
    @Override
    public void configure() throws Exception {
        from("seda:timelineEvents?concurrentConsumers=1")
                .routeId("timeline-event-processor")
                .process(exchange -> {
                    String messageBody = exchange.getIn().getBody(String.class);
                    TimelineEventMessage message = JsonHelper.fromJson(messageBody, TimelineEventMessage.class);

                    // Get the actual class type of T
                    ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
                    Class<T> clazz = (Class<T>) parameterizedType.getActualTypeArguments()[0];

                    // Create instance using reflection
                    T event;
                    try {
                        event = clazz.getDeclaredConstructor().newInstance();
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to instantiate " + clazz.getName(), e);
                    }

                    if (event instanceof ITenantAssignable tenantAssignable) {
                        tenantAssignable.setTenant(message.getTenant());
                    }

                    event.setEventType(message.getTimelineEventType());
                    event.setElementType(message.getElementType());
                    event.setElementId(message.getElementId());
                    event.setTimestamp(java.time.LocalDateTime.now());
                    event.setModifiedBy(message.getModifiedBy());

                    // Create a wrapper object with "data" field
                    ObjectNode wrapperNode = (ObjectNode) JsonHelper.createEmptyNode();

                    // Handle attributes based on event type
                    switch (message.getTimelineEventType()) {
                        case CREATED:
                            // Wrap full attributes in "data" field for create
                            wrapperNode.set("data", message.getAttributes());
                            event.setAttributes(wrapperNode);
                            break;
                        case UPDATED:
                            // Find all previous events for this element, ordered by timestamp
                            List<T> previousEvents = timelineEventRepository
                                    .findByElementIdAndElementTypeOrderByTimestampAsc(
                                            message.getElementId(),
                                            message.getElementType());

                            ObjectNode reconstructedState = (ObjectNode) JsonHelper.createEmptyNode();
                            if (!previousEvents.isEmpty()) {
                                // Reconstruct the previous state from all prior events
                                for (T prevEvent : previousEvents) {
                                    JsonNode prevAttributes = prevEvent.getAttributes();
                                    JsonNode prevData = null;
                                    if (prevEvent.getAttributes() != null) {
                                        if (prevAttributes.isObject()) {
                                            // Direct access for ObjectNode (PostgreSQL)
                                            prevData = prevAttributes.get("data");
                                        } else if (prevAttributes.isTextual()) {
                                            // Parse JSON string for TextNode (H2)
                                            try {
                                                prevAttributes = JsonHelper.jsonToJsonNode(prevAttributes.asText());
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
                                    if (event instanceof ITenantAssignable tenantAssignable
                                            && prevEvent instanceof ITenantAssignable prevTenantAssignable) {
                                        tenantAssignable.setTenant(prevTenantAssignable.getTenant());
                                    }
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
                            Optional<T> previousEvent = timelineEventRepository
                                    .findFirstByElementIdAndElementTypeOrderByTimestampDesc(
                                            message.getElementId(),
                                            message.getElementType());
                            if (previousEvent.isPresent() && previousEvent.get().getAttributes() != null) {
                                T prevEvent = previousEvent.get();
                                // Set tenant from the most recent event
                                if (event instanceof ITenantAssignable tenantAssignable
                                        && prevEvent instanceof ITenantAssignable prevTenantAssignable) {
                                    tenantAssignable.setTenant(prevTenantAssignable.getTenant());
                                }
                            }
                            // Set empty JSON object in "data" field for delete
                            wrapperNode.set("data", JsonHelper.createEmptyNode());
                            event.setAttributes(wrapperNode);
                            break;
                    }

                    timelineEventRepository.save(event);
                });
    }
}