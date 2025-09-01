package eu.isygoit.com.event;

import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;

/**
 * Abstract Kafka producer for sending file-like data as streams.
 * Extends the abstract producer to handle InputStream specifically.
 * Subclasses must set the topic (e.g., via @Value or constructor) and be annotated with @Service.
 * <p>
 * Kafka Configuration:
 * Ensure your Kafka producer is configured with ByteArraySerializer for values:
 * spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.ByteArraySerializer
 * spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
 * On the consumer side, use ByteArrayDeserializer and convert back to an InputStream (e.g., new ByteArrayInputStream(data)).
 */
@Slf4j
public abstract class KafkaFileProducer extends AbstractKafkaProducer<InputStream> {

    @Override
    protected byte[] serialize(InputStream inputStream) throws Exception {
        if (inputStream == null) {
            throw new IllegalArgumentException("Cannot serialize null message");
        }
        // Read the InputStream into a byte array
        return inputStream.readAllBytes();
    }
}