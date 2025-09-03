package eu.isygoit.com.event;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.AuthenticationException;
import org.apache.kafka.common.errors.AuthorizationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.BiConsumer;

/**
 * Abstract base class for Kafka consumers that can be extended for specific message types.
 * This class handles common logic for consuming messages from Kafka, using byte[] as the value type
 * for maximum flexibility (e.g., strings, JSON, XML, binaries, files).
 * <p>
 * Subclasses must:
 * - Provide the topic (e.g., via @Value or constructor).
 * - Implement the deserialize method to convert byte[] to type T.
 * - Implement the processMessage method to handle the deserialized message.
 * <p>
 * Security Features:
 * - Validates input to prevent null or invalid messages.
 * - Avoids logging sensitive message content.
 * - Handles security-specific Kafka exceptions (e.g., authentication, authorization).
 * - Optional HMAC verification for data integrity, enabled via properties.
 * - Optional AES decryption for sensitive data, enabled via properties.
 * - Metrics integration via Micrometer.
 * <p>
 * Assumptions:
 * - KafkaListenerContainerFactory is configured with String key deserializer and ByteArray value deserializer.
 * - SSL/TLS and SASL are configured in application.yml for secure communication.
 * - For retries, enable @EnableRetry in your Spring configuration class.
 * - For metrics, include Micrometer dependencies and expose endpoints.
 *
 * @param <T> the type parameter
 */
@Slf4j
public abstract class AbstractKafkaConsumer<T> {

    private static final String RECEIVED_HEADERS = "custom_received_headers";

    /**
     * The Enable hmac.
     */
    @Setter
    @Value("${kafka.security.enable-hmac:false}")
    protected boolean enableHmac;

    /**
     * The Meter registry.
     */
    @Autowired(required = false)
    protected MeterRegistry meterRegistry;

    /**
     * The Topic.
     */
    @Getter
    @Setter
    protected String topic; // Set by concrete classes via @Value
    /**
     * The Hmac secret.
     */
    @Setter
    @Value("${kafka.security.hmac-secret:}")
    protected String hmacSecret;
    /**
     * The Enable encryption.
     */
    @Setter
    @Value("${kafka.security.enable-encryption:false}")
    protected boolean enableEncryption;
    /**
     * The Aes key.
     */
    @Setter
    @Value("${kafka.security.aes-key:}")
    protected String aesKey;
    /**
     * The Exceptions.
     */
    @Getter
    BlockingQueue<Throwable> exceptions = new LinkedBlockingQueue<>();

    /**
     * The process method for dynamic message processing.
     */
    private BiConsumer<T, Map<String, String>> processMethod;

    /**
     * Consumes a message from Kafka, performing validation, HMAC verification, and decryption.
     *
     * @param message       the raw byte[] message from Kafka
     * @param receivedTopic the received topic
     * @param partition     the partition
     * @param offset        the offset
     * @param headers       Kafka headers
     * @throws IllegalArgumentException if the message or topic is invalid
     * @throws RuntimeException         if processing fails
     */
    @KafkaListener(topics = "#{__listener.topic}", containerFactory = "kafkaListenerContainerFactory")
    @Retryable(value = {KafkaException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public void consume(@Payload byte[] message,
                        @Header(KafkaHeaders.RECEIVED_TOPIC) String receivedTopic,
                        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                        @Header(KafkaHeaders.OFFSET) long offset,
                        @Headers Map<String, String> headers) {
        exceptions.clear();
        if (message == null || message.length == 0) {
            log.error("Received null message on topic {}", receivedTopic);
            throw new IllegalArgumentException("Message cannot be null");
        }

        if (receivedTopic == null || receivedTopic.trim().isEmpty()) {
            log.error("Received invalid topic");
            throw new IllegalArgumentException("Topic must be non-empty");
        }

        Timer timer = (meterRegistry != null) ? meterRegistry.timer("kafka.consumer.process", "topic", receivedTopic) : null;
        if (timer != null) {
            timer.record(() -> processMessageWithSecurity(message, headers, receivedTopic, partition, offset));
        } else {
            processMessageWithSecurity(message, headers, receivedTopic, partition, offset);
        }
    }

    /**
     * Sets a custom process method for testing or dynamic behavior.
     *
     * @param processMethod the BiConsumer to handle message processing
     */
    public void setProcessMethod(BiConsumer<T, Map<String, String>> processMethod) {
        this.processMethod = processMethod;
    }

    private void processMessageWithSecurity(byte[] message, Map<String, String> headers, String topic, int partition, long offset) {
        try {
            byte[] data = message;
            if (enableHmac && !hmacSecret.isEmpty()) {
                data = verifyHmacSignature(message);
            }
            if (enableEncryption && !aesKey.isEmpty()) {
                data = decrypt(data);
            }
            T deserializedMessage = deserialize(data);
            if (deserializedMessage == null) {
                log.error("Deserialized message is null for topic {}, partition {}, offset {}", topic, partition, offset);
                throw new IllegalStateException("Deserialization returned null");
            }
            log.info("Processing message from topic {}, partition {}, offset {}", topic, partition, offset);
            if (processMethod != null) {
                processMethod.accept(deserializedMessage, headers);
            } else {
                processMessage(deserializedMessage, headers);
            }
            log.debug("Message processed successfully from topic {}, partition {}, offset {}", topic, partition, offset);
        } catch (AuthenticationException e) {
            log.error("Authentication failed for topic {}: {}", topic, e.getMessage());
            exceptions.add(e);
            throw new RuntimeException("Kafka authentication failed", e);
        } catch (AuthorizationException e) {
            log.error("Authorization failed for topic {}: {}", topic, e.getMessage());
            exceptions.add(e);
            throw new RuntimeException("Kafka authorization failed", e);
        } catch (KafkaException e) {
            log.error("Kafka error processing message from topic {}: {}", topic, e.getMessage());
            exceptions.add(e);
            throw new RuntimeException("Kafka processing failed", e);
        } catch (Exception e) {
            log.error("Failed to process message from topic {}, partition {}, offset {}: {}", topic, partition, offset, e.getMessage());
            exceptions.add(e);
            throw new RuntimeException("Kafka processing failed", e);
        }
    }

    /**
     * Verifies the HMAC signature of the message.
     *
     * @param message the signed message
     * @return the original data if verified
     * @throws Exception if verification fails
     */
    private byte[] verifyHmacSignature(byte[] message) throws Exception {
        String messageString = new String(message, StandardCharsets.UTF_8);
        String[] parts = messageString.split("\\|", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid signed message format");
        }
        byte[] data = Base64.getDecoder().decode(parts[0]);
        String receivedSignature = parts[1];
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(hmacSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(keySpec);
        byte[] computedSignature = mac.doFinal(data);
        String computedSignatureEncoded = Base64.getEncoder().encodeToString(computedSignature);
        if (!computedSignatureEncoded.equals(receivedSignature)) {
            throw new SecurityException("HMAC signature verification failed");
        }
        return data;
    }

    /**
     * Decrypts the message using AES if enabled.
     *
     * @param data the encrypted data
     * @return the decrypted data
     * @throws Exception if decryption fails
     */
    private byte[] decrypt(byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        SecretKeySpec keySpec = new SecretKeySpec(aesKey.getBytes(StandardCharsets.UTF_8), "AES");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        return cipher.doFinal(data);
    }

    /**
     * Abstract method for subclasses to implement deserialization from byte[] to T.
     *
     * @param data the raw byte array
     * @return the deserialized message
     * @throws Exception if deserialization fails
     */
    protected abstract T deserialize(byte[] data) throws Exception;

    /**
     * Abstract method for subclasses to implement message processing.
     *
     * @param message the deserialized message
     * @param headers Kafka headers
     * @throws Exception if processing fails
     */
    protected abstract void processMessage(T message, Map<String, String> headers) throws Exception;
}