package eu.isygoit.com.event;

import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * Abstract Kafka producer for sending string data.
 * Extends the abstract producer to handle String specifically.
 * Subclasses must set the topic (e.g., via @Value or constructor) and be annotated with @Service.
 */
@Slf4j
public abstract class KafkaStringProducer extends AbstractKafkaProducer<String> {

    @Override
    protected byte[] serialize(String message) {
        if (message == null) {
            throw new IllegalArgumentException("Cannot serialize null message");
        }
        return message.getBytes(StandardCharsets.UTF_8);
    }
}