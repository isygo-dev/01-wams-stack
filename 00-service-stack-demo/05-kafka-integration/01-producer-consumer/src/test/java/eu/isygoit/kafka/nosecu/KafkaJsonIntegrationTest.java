package eu.isygoit.kafka.nosecu;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.isygoit.kafka.consumer.JsonConsumer;
import eu.isygoit.kafka.dto.TutorialDto;
import eu.isygoit.kafka.producer.JsonProducer;
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
 * Integration tests for Kafka JSON message processing.
 * Tests the interaction between JsonProducer and JsonConsumer
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
class KafkaJsonIntegrationTest {

    private static final String TOPIC = "json-topic";
    private static final int TIMEOUT_SECONDS = 10;
    private static final BlockingQueue<TutorialDto> consumedMessages = new LinkedBlockingQueue<>();
    private static final BlockingQueue<Map<String, String>> consumedHeaders = new LinkedBlockingQueue<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Container
    private static final KafkaContainer kafkaContainer = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.6.0"))
            .withEmbeddedZookeeper()
            .withEnv("KAFKA_BROKER_ID", "1")
            .withEnv("KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR", "1")
            .withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "true");

    @Autowired
    private JsonProducer jsonProducer;

    @Autowired
    private JsonConsumer jsonConsumer;

    /**
     * Configures Kafka bootstrap servers for producer and consumer.
     */
    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.producer.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add("spring.kafka.consumer.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add("kafka.topic.json-topic", () -> TOPIC);
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
        jsonConsumer.setTopic(TOPIC);
        jsonConsumer.setProcessMethod((message, headers) -> {
            TutorialDto tutorial = message;
            consumedMessages.add(tutorial);
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
     * Tests basic JSON message production and consumption.
     */
    @Test
    @Order(1)
    @DisplayName("Verify single JSON message production and consumption with headers")
    void testSingleJsonMessage() throws Exception {
        // Arrange
        TutorialDto message = TutorialDto.builder()
                .id(1L)
                .tenant("tenant1")
                .title("Test Title " + UUID.randomUUID())
                .description("Test Description")
                .published(true)
                .build();
        Map<String, String> headers = Collections.singletonMap("kafka_test_header", "test_value");

        // Act
        jsonProducer.send(message, headers);

        // Assert
        TutorialDto consumedMessage = consumedMessages.poll(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertNotNull(consumedMessage, "No message consumed within " + TIMEOUT_SECONDS + " seconds");
        assertEquals(message.getId(), consumedMessage.getId(), "Consumed message ID does not match");
        assertEquals(message.getTitle(), consumedMessage.getTitle(), "Consumed message title does not match");
        assertEquals(message.getTenant(), consumedMessage.getTenant(), "Consumed message tenant does not match");
        assertEquals(message.getDescription(), consumedMessage.getDescription(), "Consumed message description does not match");
        assertEquals(message.isPublished(), consumedMessage.isPublished(), "Consumed message published status does not match");
        Map<String, String> consumedHeader = consumedHeaders.poll(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertNotNull(consumedHeader, "No headers consumed within " + TIMEOUT_SECONDS + " seconds");
        assertTrue(consumedHeader.containsKey("kafka_test_header"), "Consumed headers do not contain kafka_test_header");
    }

    /**
     * Tests handling of multiple JSON messages in sequence.
     */
    @Test
    @Order(2)
    @DisplayName("Verify multiple JSON message production and consumption")
    void testMultipleJsonMessages() throws Exception {
        // Arrange
        int messageCount = 5;
        Map<String, String> headers = Collections.singletonMap("kafka_test_header", "test_multi_value");
        TutorialDto[] messages = new TutorialDto[messageCount];
        for (int i = 0; i < messageCount; i++) {
            messages[i] = TutorialDto.builder()
                    .id((long) i)
                    .tenant("tenant" + i)
                    .title("Test Title " + i + " " + UUID.randomUUID())
                    .description("Test Description " + i)
                    .published(i % 2 == 0)
                    .build();
        }

        // Act
        for (TutorialDto message : messages) {
            jsonProducer.send(message, headers);
        }

        // Assert
        for (int i = 0; i < messageCount; i++) {
            TutorialDto consumedMessage = consumedMessages.poll(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            assertNotNull(consumedMessage, "Message " + i + " not consumed within " + TIMEOUT_SECONDS + " seconds");
            assertTrue(Arrays.stream(messages).anyMatch(m -> m.getId().equals(consumedMessage.getId()) &&
                            m.getTitle().equals(consumedMessage.getTitle())),
                    "Consumed message " + consumedMessage.getTitle() + " not in sent messages");
        }
    }

    /**
     * Tests handling of null JSON message.
     */
    @Test
    @Order(3)
    @DisplayName("Verify null JSON message handling")
    void testNullJsonMessage() {
        // Arrange
        Map<String, String> headers = Collections.singletonMap("kafka_test_header", "test_null_value");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> jsonProducer.send(null, headers),
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
        TutorialDto message = TutorialDto.builder()
                .id(2L)
                .tenant("tenant2")
                .title("Test Title " + UUID.randomUUID())
                .description("Test Description")
                .published(false)
                .build();
        Map<String, String> headers = new HashMap<>();
        headers.put("kafka_test_header1", "test_value1");
        headers.put("kafka_test_header2", "test_value2");
        headers.put("kafka_test_header3", "test_value3");

        // Act
        jsonProducer.send(message, headers);

        // Assert
        TutorialDto consumedMessage = consumedMessages.poll(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertNotNull(consumedMessage, "Message not consumed within " + TIMEOUT_SECONDS + " seconds");
        assertEquals(message.getId(), consumedMessage.getId(), "Consumed message ID does not match");
        assertEquals(message.getTitle(), consumedMessage.getTitle(), "Consumed message title does not match");
        Map<String, String> consumedHeader = consumedHeaders.poll(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertNotNull(consumedHeader, "Headers not consumed within " + TIMEOUT_SECONDS + " seconds");
        assertTrue(consumedHeader.containsKey("kafka_test_header1"), "Consumed headers do not contain kafka_test_header1");
        assertTrue(consumedHeader.containsKey("kafka_test_header2"), "Consumed headers do not contain kafka_test_header2");
        assertTrue(consumedHeader.containsKey("kafka_test_header3"), "Consumed headers do not contain kafka_test_header3");
    }
}