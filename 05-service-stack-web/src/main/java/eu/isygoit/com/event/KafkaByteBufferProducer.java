package eu.isygoit.com.event;

import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;

/**
 * Abstract Kafka producer for sending ByteBuffer data.
 * Extends the abstract producer to handle ByteBuffer specifically.
 * Subclasses must set the topic (e.g., via @Value or constructor) and be annotated with @Service.
 */
@Slf4j
public abstract class KafkaByteBufferProducer extends AbstractKafkaProducer<ByteBuffer> {

    @Override
    protected byte[] serialize(ByteBuffer buffer) throws Exception {
        if (buffer == null) {
            throw new IllegalArgumentException("Cannot serialize null message");
        }
        return buffer.array(); // Assumes direct buffer; adjust if needed
    }
}