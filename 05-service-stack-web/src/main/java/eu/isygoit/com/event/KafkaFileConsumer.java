package eu.isygoit.com.event;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

/**
 * Abstract Kafka consumer for processing file-like data as InputStream.
 * Extends the abstract consumer to handle InputStream specifically.
 * Subclasses must set the topic (e.g., via @Value or constructor),
 * be annotated with @Service, and implement processMessage.
 */
@Slf4j
public abstract class KafkaFileConsumer extends AbstractKafkaConsumer<InputStream> {

    @Override
    protected InputStream deserialize(byte[] data) throws Exception {
        if (data == null) {
            throw new IllegalArgumentException("Cannot deserialize null data");
        }
        return new ByteArrayInputStream(data);
    }

    @Override
    protected final void processMessage(InputStream message, Map<String, String> headers) throws Exception {
        process(message, headers);
    }

    /**
     * Process.
     *
     * @param message the message
     * @param headers the headers
     * @throws Exception the exception
     */
    protected abstract void process(InputStream message, Map<String, String> headers) throws Exception;
}