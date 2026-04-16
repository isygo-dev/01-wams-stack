package eu.isygoit.com.event;

import eu.isygoit.exception.KafkaPrepareDataException;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AbstractKafkaProducer Tests")
class AbstractKafkaProducerTest {

    @Mock
    private KafkaTemplate<String, byte[]> kafkaTemplate;

    private TestKafkaProducer producer;

    private static class TestKafkaProducer extends AbstractKafkaProducer<String> {
        public TestKafkaProducer(String topic) {
            this.topic = topic;
        }

        @Override
        protected byte[] serialize(String message) {
            return message.getBytes(StandardCharsets.UTF_8);
        }
    }

    @BeforeEach
    void setUp() {
        producer = new TestKafkaProducer("test-topic");
        producer.kafkaTemplate = kafkaTemplate;
        // Default values for @Value fields as they won't be injected in unit test without Spring
        ReflectionTestUtils.setField(producer, "enableHmac", false);
        ReflectionTestUtils.setField(producer, "enableEncryption", false);
    }

    @Test
    @DisplayName("Should send message successfully")
    void testSendSuccess() {
        String message = "Hello Kafka";
        producer.send(message);

        ArgumentCaptor<ProducerRecord<String, byte[]>> captor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate).send(captor.capture());

        ProducerRecord<String, byte[]> record = captor.getValue();
        assertEquals("test-topic", record.topic());
        assertArrayEquals(message.getBytes(StandardCharsets.UTF_8), record.value());
    }

    @Test
    @DisplayName("Should send message with custom headers")
    void testSendWithHeaders() {
        String message = "Hello Kafka with Headers";
        Map<String, String> headers = new HashMap<>();
        headers.put("header1", "value1");

        producer.send(message, headers);

        ArgumentCaptor<ProducerRecord<String, byte[]>> captor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate).send(captor.capture());

        ProducerRecord<String, byte[]> record = captor.getValue();
        assertEquals("value1", new String(record.headers().lastHeader("header1").value(), StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when message is null")
    void testSendNullMessage() {
        assertThrows(IllegalArgumentException.class, () -> producer.send(null));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when topic is not set")
    void testSendNoTopic() {
        producer = new TestKafkaProducer(null);
        producer.kafkaTemplate = kafkaTemplate;
        assertThrows(IllegalArgumentException.class, () -> producer.send("message"));
    }

    @Test
    @DisplayName("Should encrypt data when enabled")
    void testSendWithEncryption() {
        ReflectionTestUtils.setField(producer, "enableEncryption", true);
        ReflectionTestUtils.setField(producer, "aesKey", "1234567890123456"); // 16 bytes for AES-128

        producer.send("secret message");

        ArgumentCaptor<ProducerRecord<String, byte[]>> captor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate).send(captor.capture());

        byte[] sentData = captor.getValue().value();
        assertNotEquals("secret message", new String(sentData, StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("Should add HMAC signature when enabled")
    void testSendWithHmac() {
        ReflectionTestUtils.setField(producer, "enableHmac", true);
        ReflectionTestUtils.setField(producer, "hmacSecret", "my-secret");

        producer.send("signed message");

        ArgumentCaptor<ProducerRecord<String, byte[]>> captor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate).send(captor.capture());

        String sentData = new String(captor.getValue().value(), StandardCharsets.UTF_8);
        assertTrue(sentData.contains("|"));
    }
}
