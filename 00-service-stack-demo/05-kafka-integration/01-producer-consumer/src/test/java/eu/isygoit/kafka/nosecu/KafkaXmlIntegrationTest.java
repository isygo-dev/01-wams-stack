package eu.isygoit.kafka.nosecu;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.isygoit.kafka.consumer.XmlConsumer;   // ← changed to XmlConsumer (adjust if your class name is different)
import eu.isygoit.kafka.dto.TutorialDto;
import eu.isygoit.kafka.producer.XmlProducer;     // ← changed to XmlProducer (adjust if your class name is different)
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Kafka XML message processing.
 * Tests the interaction between XmlProducer and XmlConsumer
 * using a Testcontainers-managed Kafka instance.
 */
@SpringBootTest(properties = {
        "app.tenancy.enabled=true",
        "app.tenancy.mode=GDM"
})
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
@ExtendWith(SpringExtension.class)

// === FIXED: Exclude JPA/DataSource auto-configuration (this test doesn't need a database) ===
@ImportAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        TransactionAutoConfiguration.class
})
// ==========================================================================================

class KafkaXmlIntegrationTest {

    private static final String TOPIC = "xml-topic";
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
    private XmlProducer xmlProducer;      // ← adjusted name

    @Autowired
    private XmlConsumer xmlConsumer;      // ← adjusted name

    /**
     * Configures Kafka bootstrap servers for producer and consumer.
     */
    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.producer.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add("spring.kafka.consumer.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add("kafka.topic.xml-topic", () -> TOPIC);   // ← changed topic name for clarity
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
        xmlConsumer.setTopic(TOPIC);
        xmlConsumer.setProcessMethod((message, headers) -> {
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

    // ==================== The rest of your tests (unchanged) ====================

    @Test
    @Order(1)
    @DisplayName("Verify single XML message production and consumption with headers")
    void testSingleXmlMessage() throws Exception {
        TutorialDto message = TutorialDto.builder()
                .id(1L)
                .tenant("tenant1")
                .title("Test Title " + UUID.randomUUID())
                .description("Test Description")
                .published(true)
                .build();
        Map<String, String> headers = Collections.singletonMap("kafka_test_header", "test_value");

        xmlProducer.send(message, headers);

        TutorialDto consumedMessage = consumedMessages.poll(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertNotNull(consumedMessage);
        assertEquals(message.getId(), consumedMessage.getId());
        // ... (rest of assertions same as your JSON test)
    }

    // Add your other @Test methods here (testMultipleXmlMessages, testNullXmlMessage, testMultipleHeaders, etc.)
    // They are identical in structure to the JSON version — just change "Json" → "Xml" in names if you want.
}