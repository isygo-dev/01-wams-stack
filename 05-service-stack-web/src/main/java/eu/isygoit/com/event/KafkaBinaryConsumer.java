package eu.isygoit.com.event;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Abstract Kafka consumer for processing raw byte array data.
 * Extends the abstract consumer to handle byte[] directly.
 * Subclasses must set the topic (e.g., via @Value or constructor),
 * be annotated with @Service, and implement processMessage.
 */
@Slf4j
public abstract class KafkaBinaryConsumer extends AbstractKafkaConsumer<byte[]> {

    @Override
    protected byte[] deserialize(byte[] data) throws Exception {
        if (data == null) {
            throw new IllegalArgumentException("Cannot deserialize null data");
        }
        return data; // No conversion needed
    }

    @Override
    protected final void processMessage(byte[] message, Map<String, String> headers) throws Exception {
        processMessage(message, headers);
    }

    /**
     * Process.
     *
     * @param message the message
     * @param headers the headers
     * @throws Exception the exception
     */
    protected abstract void process(byte[] message, Map<String, String> headers) throws Exception;
}