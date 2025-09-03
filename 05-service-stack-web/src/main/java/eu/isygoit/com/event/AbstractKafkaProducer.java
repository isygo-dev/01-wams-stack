package eu.isygoit.com.event;

import eu.isygoit.exception.KafkaPrepareDataException;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * Abstract base class for Kafka producers that can be extended for specific message types.
 * This class handles the common logic for sending messages to Kafka, using byte[] as the value type
 * for maximum flexibility (e.g., strings, JSON, XML, binaries, files).
 * <p>
 * Subclasses must:
 * - Provide the topic (e.g., via @Value or constructor).
 * - Implement the serialize method to convert the message type T to byte[].
 * <p>
 * Security Features:
 * - Validates input to prevent null or invalid messages.
 * - Avoids logging sensitive message content.
 * - Handles security-specific Kafka exceptions (e.g., authentication, authorization).
 * - Optional HMAC signing for data integrity, enabled via properties.
 * - Optional AES encryption for sensitive data, enabled via properties.
 * <p>
 * Assumptions:
 * - KafkaTemplate is configured with String key serializer and ByteArray value serializer.
 * - SSL/TLS and SASL are configured in application.yml for secure communication.
 * - Error handling is enhanced for security; further customize as needed (e.g., retries).
 * - For retries, enable @EnableRetry in your Spring configuration class.
 * - For metrics, include Micrometer dependencies and expose endpoints.
 *
 * @param <T> the type parameter
 */
@Slf4j
public abstract class AbstractKafkaProducer<T> {

    /**
     * The Kafka template.
     */
    @Autowired(required = false)
    protected KafkaTemplate<String, byte[]> kafkaTemplate;

    /**
     * The Topic.
     */
    protected String topic;

    @Value("${kafka.security.enable-hmac:false}")
    private boolean enableHmac;

    @Value("${kafka.security.hmac-secret:}")
    private String hmacSecret;

    @Value("${kafka.security.enable-encryption:false}")
    private boolean enableEncryption;

    @Value("${kafka.security.aes-key:}")
    private String aesKey;

    @Autowired(required = false)
    private MeterRegistry meterRegistry;

    /**
     * Sends the message to the Kafka topic after serialization, with optional signing and encryption.
     *
     * @param message the message of type T to send
     * @throws IllegalArgumentException if the message or topic is null/invalid
     * @throws RuntimeException         if serialization or sending fails
     */
    @Retryable(value = {KafkaException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public void send(T message) {
        send(message, null);
    }

    /**
     * Sends the message with optional custom headers.
     *
     * @param message       the message of type T to send
     * @param customHeaders optional map of custom headers
     * @throws IllegalArgumentException if the message or topic is null/invalid
     * @throws RuntimeException         if serialization or sending fails
     */
    public void send(T message, Map<String, String> customHeaders) {
        String resolvedTopic = resolveTopic(message);
        if (message == null) {
            log.error("Cannot send null message to topic {}", resolvedTopic);
            throw new IllegalArgumentException("Message cannot be null");
        }
        if (resolvedTopic == null || resolvedTopic.trim().isEmpty()) {
            log.error("Topic is not set or invalid");
            throw new IllegalArgumentException("Topic must be set and non-empty");
        }

        Timer timer = (meterRegistry != null) ? meterRegistry.timer("kafka.producer.send", "topic", resolvedTopic) : null;
        if (timer != null) {
            timer.record(() -> performSend(message, customHeaders, resolvedTopic, false));
        } else {
            performSend(message, customHeaders, resolvedTopic, false);
        }
    }

    /**
     * Sends the message asynchronously with optional custom headers.
     *
     * @param message       the message of type T to send
     * @param customHeaders optional map of custom headers
     */
    public void sendAsync(T message, Map<String, String> customHeaders) {
        String resolvedTopic = resolveTopic(message);
        if (message == null) {
            log.error("Cannot send null message to topic {}", resolvedTopic);
            throw new IllegalArgumentException("Message cannot be null");
        }
        if (resolvedTopic == null || resolvedTopic.trim().isEmpty()) {
            log.error("Topic is not set or invalid");
            throw new IllegalArgumentException("Topic must be set and non-empty");
        }

        try {
            byte[] data = prepareData(message);
            log.info("Sending async message to topic {}", resolvedTopic);
            RecordHeaders recordHeaders = new RecordHeaders();
            if (customHeaders != null) {
                customHeaders.forEach((k, v) -> recordHeaders.add(k, v.getBytes(StandardCharsets.UTF_8)));
            }
            ProducerRecord<String, byte[]> record = new ProducerRecord<>(resolvedTopic,
                    null,
                    null, null,
                    data,
                    recordHeaders);
            ListenableFuture<SendResult<String, byte[]>> future = (ListenableFuture<SendResult<String, byte[]>>) kafkaTemplate.send(record);
            future.addCallback(new ListenableFutureCallback<>() {
                @Override
                public void onSuccess(SendResult<String, byte[]> result) {
                    log.debug("Message sent to topic {}, partition {}, offset {}", resolvedTopic, result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                }

                @Override
                public void onFailure(Throwable ex) {
                    log.error("Failed to send async message to topic {}: {}", resolvedTopic, ex.getMessage());
                }
            });
        } catch (Exception e) {
            log.error("Failed to prepare async message for topic {}: {}", resolvedTopic, e.getMessage());
            throw new RuntimeException("Kafka async send failed", e);
        }
    }

    private void performSend(T message, Map<String, String> customHeaders, String resolvedTopic, boolean async) {

        byte[] data = null;
        try {
            data = prepareData(message);
        } catch (Exception e) {
            throw new KafkaPrepareDataException(e);
        }
        log.info("Sending message to topic {}", resolvedTopic);
        RecordHeaders recordHeaders = new RecordHeaders();
        if (customHeaders != null) {
            customHeaders.forEach((k, v) -> recordHeaders.add(k, v.getBytes(StandardCharsets.UTF_8)));
        }
        ProducerRecord<String, byte[]> record = new ProducerRecord<>(resolvedTopic,
                null,
                null, null,
                data,
                recordHeaders);
        kafkaTemplate.send(record);
        log.debug("Message successfully sent to topic {}", resolvedTopic);
    }

    private byte[] prepareData(T message) throws Exception {
        byte[] data = serialize(message);
        if (data == null) {
            log.error("Serialized data is null for topic {}", topic);
            throw new IllegalStateException("Serialization returned null");
        }
        if (enableEncryption && !aesKey.isEmpty()) {
            data = encrypt(data);
        }
        if (enableHmac && !hmacSecret.isEmpty()) {
            data = addHmacSignature(data);
        }
        return data;
    }

    /**
     * Encrypts the data using AES if enabled.
     *
     * @param data the data to encrypt
     * @return the encrypted data
     * @throws Exception if encryption fails
     */
    private byte[] encrypt(byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        SecretKeySpec keySpec = new SecretKeySpec(aesKey.getBytes(StandardCharsets.UTF_8), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        return cipher.doFinal(data);
    }

    /**
     * Adds an HMAC signature to the data for integrity if enabled.
     *
     * @param data the data to sign
     * @return the signed data
     * @throws Exception if signing fails
     */
    private byte[] addHmacSignature(byte[] data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(hmacSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(keySpec);
        byte[] signature = mac.doFinal(data);
        String encodedSignature = Base64.getEncoder().encodeToString(signature);
        String signedMessage = Base64.getEncoder().encodeToString(data) + "|" + encodedSignature;
        return signedMessage.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Resolves the topic for the message. Subclasses can override for dynamic resolution.
     *
     * @param message the message
     * @return the resolved topic
     */
    protected String resolveTopic(T message) {
        return topic;
    }

    /**
     * Abstract method for subclasses to implement serialization from T to byte[].
     *
     * @param message the message to serialize
     * @return the serialized byte array
     * @throws Exception if serialization fails
     */
    protected abstract byte[] serialize(T message) throws Exception;
}