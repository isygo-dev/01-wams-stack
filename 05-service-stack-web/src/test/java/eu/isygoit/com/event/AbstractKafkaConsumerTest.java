package eu.isygoit.com.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AbstractKafkaConsumer Tests")
class AbstractKafkaConsumerTest {

    private TestKafkaConsumer consumer;
    private AtomicReference<String> processedMessage = new AtomicReference<>();

    private static class TestKafkaConsumer extends AbstractKafkaConsumer<String> {
        @Override
        protected String deserialize(byte[] data) {
            return new String(data, StandardCharsets.UTF_8);
        }

        @Override
        protected void processMessage(String message, Map<String, String> headers) {
            // Handled by setProcessMethod in setup for some tests, or directly here
        }
    }

    @BeforeEach
    void setUp() {
        consumer = new TestKafkaConsumer();
        consumer.setTopic("test-topic");
        ReflectionTestUtils.setField(consumer, "enableHmac", false);
        ReflectionTestUtils.setField(consumer, "enableEncryption", false);
        processedMessage.set(null);
        consumer.setProcessMethod((msg, headers) -> processedMessage.set(msg));
    }

    @Test
    @DisplayName("Should consume and process message successfully")
    void testConsumeSuccess() {
        byte[] message = "Hello Kafka".getBytes(StandardCharsets.UTF_8);
        consumer.consume(message, "test-topic", 0, 0, new HashMap<>());

        assertEquals("Hello Kafka", processedMessage.get());
        assertTrue(consumer.getExceptions().isEmpty());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when message is null or empty")
    void testConsumeNullOrEmpty() {
        assertThrows(RuntimeException.class, () -> consumer.consume(null, "test-topic", 0, 0, new HashMap<>()));
        assertThrows(RuntimeException.class, () -> consumer.consume(new byte[0], "test-topic", 0, 0, new HashMap<>()));
    }

    @Test
    @DisplayName("Should decrypt data when enabled")
    void testConsumeWithEncryption() throws Exception {
        ReflectionTestUtils.setField(consumer, "enableEncryption", true);
        String key = "1234567890123456"; // 16 bytes
        ReflectionTestUtils.setField(consumer, "aesKey", key);

        // Manually encrypt for testing
        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES");
        javax.crypto.spec.SecretKeySpec keySpec = new javax.crypto.spec.SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, keySpec);
        byte[] encryptedData = cipher.doFinal("encrypted message".getBytes(StandardCharsets.UTF_8));

        consumer.consume(encryptedData, "test-topic", 0, 0, new HashMap<>());

        assertEquals("encrypted message", processedMessage.get());
    }

    @Test
    @DisplayName("Should verify HMAC signature when enabled")
    void testConsumeWithHmac() throws Exception {
        ReflectionTestUtils.setField(consumer, "enableHmac", true);
        String secret = "my-secret";
        ReflectionTestUtils.setField(consumer, "hmacSecret", secret);

        byte[] data = "signed message".getBytes(StandardCharsets.UTF_8);
        
        // Manually sign for testing
        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
        javax.crypto.spec.SecretKeySpec keySpec = new javax.crypto.spec.SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(keySpec);
        byte[] signature = mac.doFinal(data);
        String encodedSignature = Base64.getEncoder().encodeToString(signature);
        String signedMessage = Base64.getEncoder().encodeToString(data) + "|" + encodedSignature;
        byte[] messageBytes = signedMessage.getBytes(StandardCharsets.UTF_8);

        consumer.consume(messageBytes, "test-topic", 0, 0, new HashMap<>());

        assertEquals("signed message", processedMessage.get());
    }

    @Test
    @DisplayName("Should fail when HMAC signature is invalid")
    void testConsumeWithInvalidHmac() {
        ReflectionTestUtils.setField(consumer, "enableHmac", true);
        ReflectionTestUtils.setField(consumer, "hmacSecret", "my-secret");

        String invalidSignedMessage = Base64.getEncoder().encodeToString("data".getBytes()) + "|invalid-signature";
        byte[] messageBytes = invalidSignedMessage.getBytes(StandardCharsets.UTF_8);

        assertThrows(RuntimeException.class, () -> consumer.consume(messageBytes, "test-topic", 0, 0, new HashMap<>()));
        assertFalse(consumer.getExceptions().isEmpty());
    }
}
