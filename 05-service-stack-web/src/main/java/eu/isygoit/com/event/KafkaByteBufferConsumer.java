package eu.isygoit.com.event;

import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Abstract Kafka consumer for processing ByteBuffer data.
 * Extends the abstract consumer to handle ByteBuffer specifically.
 * Subclasses must set the topic (e.g., via @Value or constructor),
 * be annotated with @Service, and implement processMessage.
 */
@Slf4j
public abstract class KafkaByteBufferConsumer extends AbstractKafkaConsumer<ByteBuffer> {

    @Override
    protected ByteBuffer deserialize(byte[] data) throws Exception {
        if (data == null) {
            throw new IllegalArgumentException("Cannot deserialize null data");
        }
        return ByteBuffer.wrap(data);
    }

    @Override
    protected final void processMessage(ByteBuffer message, Map<String, String> headers) throws Exception {
        process(message, headers);
    }

    /**
     * Process.
     *
     * @param message the message
     * @param headers the headers
     * @throws Exception the exception
     */
    protected abstract void process(ByteBuffer message, Map<String, String> headers) throws Exception;
}