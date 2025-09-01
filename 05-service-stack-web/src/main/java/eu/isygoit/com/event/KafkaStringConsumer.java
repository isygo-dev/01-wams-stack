package eu.isygoit.com.event;

import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Abstract Kafka consumer for processing string data.
 * Extends the abstract consumer to handle String specifically.
 * Subclasses must set the topic (e.g., via @Value or constructor),
 * be annotated with @Service, and implement processMessage.
 */
@Slf4j
public abstract class KafkaStringConsumer extends AbstractKafkaConsumer<String> {

    @Override
    protected String deserialize(byte[] data) throws Exception {
        if (data == null) {
            throw new IllegalArgumentException("Cannot deserialize null data");
        }
        return new String(data, StandardCharsets.UTF_8);
    }

    @Override
    protected final void processMessage(String message, Map<String, String> headers) throws Exception {
        process(message, headers);
    }

    /**
     * Process.
     *
     * @param message the message
     * @param headers the headers
     * @throws Exception the exception
     */
    protected abstract void process(String message, Map<String, String> headers) throws Exception;
}