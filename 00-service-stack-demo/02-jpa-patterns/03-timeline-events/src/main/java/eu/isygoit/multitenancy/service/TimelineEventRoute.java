package eu.isygoit.multitenancy.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.isygoit.helper.JsonHelper;
import eu.isygoit.multitenancy.dto.TimelineEventMessage;
import eu.isygoit.multitenancy.model.TimeLineEvent;
import eu.isygoit.multitenancy.repository.TimelineEventRepository;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TimelineEventRoute extends RouteBuilder {

    @Autowired
    private TimelineEventRepository timelineEventRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void configure() throws Exception {
        from("seda:timelineEvents?concurrentConsumers=1")
                .routeId("timeline-event-processor")
                .process(exchange -> {
                    String messageBody = exchange.getIn().getBody(String.class);
                    TimelineEventMessage message = objectMapper.readValue(messageBody, TimelineEventMessage.class);

                    TimeLineEvent event = new TimeLineEvent();
                    event.setEventType(message.getEventType());
                    event.setElementType(message.getElementType());
                    event.setElementId(message.getElementId());
                    event.setTenant(message.getTenant());
                    event.setTimestamp(java.time.LocalDateTime.now());
                    event.setModifiedBy(message.getModifiedBy());
                    event.setAttributes(message.getAttributes());

                    timelineEventRepository.save(event);
                });
    }
}
