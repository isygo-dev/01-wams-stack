package eu.isygoit.com.event;

import lombok.extern.slf4j.Slf4j;

/**
 * Abstract Kafka producer for sending raw byte array data.
 * Extends the abstract producer to handle byte[] directly.
 * Subclasses must set the topic (e.g., via @Value or constructor) and be annotated with @Service.
 */
@Slf4j
public abstract class KafkaBinaryProducer extends AbstractKafkaProducer<byte[]> {

    @Override
    protected byte[] serialize(byte[] message) {
        if (message == null) {
            throw new IllegalArgumentException("Cannot serialize null message");
        }
        return message;  // No conversion needed
    }
}