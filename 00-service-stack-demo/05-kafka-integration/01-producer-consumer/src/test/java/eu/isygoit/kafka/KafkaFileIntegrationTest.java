package eu.isygoit.kafka;

import eu.isygoit.kafka.consumer.FileConsumer;
import eu.isygoit.kafka.producer.FileProducer;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Kafka file message processing.
 * Tests the interaction between FileProducer and FileConsumer
 * using a Testcontainers-managed Kafka instance.
 */
@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create",
        "app.tenancy.enabled=true",
        "app.tenancy.mode=GDM"
})
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
@ExtendWith(SpringExtension.class)
class KafkaFileIntegrationTest {

    private static final String TOPIC = "file-topic";
    private static final int TIMEOUT_SECONDS = 10;
    private static final BlockingQueue<String> consumedMessages = new LinkedBlockingQueue<>();
    private static final BlockingQueue<Map<String, String>> consumedHeaders = new LinkedBlockingQueue<>();

    @Container
    private static final KafkaContainer kafkaContainer = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.6.0"))
            .withEmbeddedZookeeper()
            .withEnv("KAFKA_BROKER_ID", "1")
            .withEnv("KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR", "1")
            .withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "true");

    @Autowired
    private FileProducer fileProducer;

    @Autowired
    private FileConsumer fileConsumer;

    /**
     * Configures Kafka bootstrap servers for producer and consumer.
     */
    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.producer.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add("spring.kafka.consumer.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add("kafka.topic.file-topic", () -> TOPIC);
    }

    /**
     * Sets up Kafka topic before all tests.
     */
    @BeforeAll
    static void setUp() {
        try (AdminClient adminClient = AdminClient.create(
                Collections.singletonMap(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers()))) {
            adminClient.createTopics(Collections.singletonList(new NewTopic(TOPIC, 1, (short) 1)));
        }
    }

    /**
     * Configures consumer before each test.
     */
    @BeforeEach
    void setUpConsumer() {
        consumedMessages.clear();
        consumedHeaders.clear();
        fileConsumer.setTopic(TOPIC);
        fileConsumer.setProcessMethod((message, headers) -> {
            String content = null;
            try {
                content = new String(message.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            consumedMessages.add(content);
            consumedHeaders.add(headers);
        });
    }

    /**
     * Cleans up after each test.
     */
    @AfterEach
    void tearDown() {
        consumedMessages.clear();
        consumedHeaders.clear();
    }

    /**
     * Tests basic file message production and consumption.
     */
    @Test
    @Order(1)
    @DisplayName("Verify single file message production and consumption with headers")
    void testSingleFileMessage() throws Exception {
        // Arrange
        String messageContent = "Test file message " + UUID.randomUUID();
        InputStream message = new ByteArrayInputStream(messageContent.getBytes(StandardCharsets.UTF_8));
        Map<String, String> headers = Collections.singletonMap("kafka_test_header", "test_value");

        // Act
        fileProducer.send(message, headers);

        // Assert
        String consumedMessage = consumedMessages.poll(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertNotNull(consumedMessage, "No message consumed within " + TIMEOUT_SECONDS + " seconds");
        assertEquals(messageContent, consumedMessage, "Consumed message does not match sent message");
        Map<String, String> consumedHeader = consumedHeaders.poll(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertNotNull(consumedHeader, "No headers consumed within " + TIMEOUT_SECONDS + " seconds");
        assertTrue(consumedHeader.containsKey("kafka_test_header"), "Consumed headers do not contain kafka_test_header");
    }

    /**
     * Tests handling of multiple file messages in sequence.
     */
    @Test
    @Order(2)
    @DisplayName("Verify multiple file message production and consumption")
    void testMultipleFileMessages() throws Exception {
        // Arrange
        int messageCount = 5;
        Map<String, String> headers = Collections.singletonMap("kafka_test_header", "test_multi_value");
        String[] messageContents = new String[messageCount];
        InputStream[] messages = new InputStream[messageCount];
        for (int i = 0; i < messageCount; i++) {
            messageContents[i] = "Test file message " + i + " " + UUID.randomUUID();
            messages[i] = new ByteArrayInputStream(messageContents[i].getBytes(StandardCharsets.UTF_8));
        }

        // Act
        for (InputStream message : messages) {
            fileProducer.send(message, headers);
        }

        // Assert
        for (int i = 0; i < messageCount; i++) {
            String consumedMessage = consumedMessages.poll(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            assertNotNull(consumedMessage, "Message " + i + " not consumed within " + TIMEOUT_SECONDS + " seconds");
            assertTrue(Arrays.asList(messageContents).contains(consumedMessage),
                    "Consumed message " + consumedMessage + " not in sent messages");
        }
    }

    /**
     * Tests handling of null file message.
     */
    @Test
    @Order(3)
    @DisplayName("Verify null file message handling")
    void testNullFileMessage() {
        // Arrange
        Map<String, String> headers = Collections.singletonMap("kafka_test_header", "test_null_value");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> fileProducer.send(null, headers),
                "Producer should throw IllegalArgumentException for null message");

        assertEquals("Message cannot be null", exception.getMessage(),
                "Exception message should match expected text");
    }

    /**
     * Tests header propagation with multiple headers.
     */
    @Test
    @Order(4)
    @DisplayName("Verify multiple headers propagation")
    void testMultipleHeaders() throws Exception {
        // Arrange
        String messageContent = "Test file message " + UUID.randomUUID();
        InputStream message = new ByteArrayInputStream(messageContent.getBytes(StandardCharsets.UTF_8));
        Map<String, String> headers = new HashMap<>();
        headers.put("kafka_test_header1", "test_value1");
        headers.put("kafka_test_header2", "test_value2");
        headers.put("kafka_test_header3", "test_value3");

        // Act
        fileProducer.send(message, headers);

        // Assert
        String consumedMessage = consumedMessages.poll(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertNotNull(consumedMessage, "Message not consumed within " + TIMEOUT_SECONDS + " seconds");
        assertEquals(messageContent, consumedMessage, "Consumed message does not match sent message");
        Map<String, String> consumedHeader = consumedHeaders.poll(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertNotNull(consumedHeader, "Headers not consumed within " + TIMEOUT_SECONDS + " seconds");
        assertTrue(consumedHeader.containsKey("kafka_test_header1"), "Consumed headers do not contain kafka_test_header1");
        assertTrue(consumedHeader.containsKey("kafka_test_header2"), "Consumed headers do not contain kafka_test_header2");
        assertTrue(consumedHeader.containsKey("kafka_test_header3"), "Consumed headers do not contain kafka_test_header3");
    }
}