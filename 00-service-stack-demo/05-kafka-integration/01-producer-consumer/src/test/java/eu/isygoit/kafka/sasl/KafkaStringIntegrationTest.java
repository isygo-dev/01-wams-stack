package eu.isygoit.kafka.sasl;

import eu.isygoit.kafka.consumer.StringConsumer;
import eu.isygoit.kafka.producer.StringProducer;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.errors.SaslAuthenticationException;
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
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Kafka string message processing with SASL.
 * Tests the interaction between StringProducer and StringConsumer
 * using a Testcontainers-managed Kafka instance with SASL_PLAINTEXT.
 */
@SpringBootTest(properties = {
        "app.tenancy.enabled=true",
        "app.tenancy.mode=GDM"
})
@AutoConfigureMockMvc
@ActiveProfiles("sasl")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
@ExtendWith(SpringExtension.class)

// === FIXED: Exclude JPA/DataSource auto-configuration ===
@ImportAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        TransactionAutoConfiguration.class
})
// ==========================================================================================

class KafkaStringIntegrationTest {

    private static final String TOPIC = "string-topic-sasl";
    private static final int TIMEOUT_SECONDS = 10;
    private static final BlockingQueue<String> consumedMessages = new LinkedBlockingQueue<>();
    private static final BlockingQueue<Map<String, String>> consumedHeaders = new LinkedBlockingQueue<>();

    @Container
    private static final KafkaContainer kafkaContainer = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.6.0"))
            .withEmbeddedZookeeper()
            .withExposedPorts(9092, 9093)
            .withEnv("KAFKA_BROKER_ID", "1")
            .withEnv("KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR", "1")
            .withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "true")
            .withEnv("KAFKA_LISTENER_SECURITY_PROTOCOL_MAP", "CONTROLLER:SASL_PLAINTEXT,PLAINTEXT:SASL_PLAINTEXT,BROKER:SASL_PLAINTEXT")
            .withEnv("KAFKA_ADVERTISED_LISTENERS", "PLAINTEXT://localhost:9093,BROKER://localhost:9092")
            .withEnv("KAFKA_LISTENERS", "PLAINTEXT://0.0.0.0:9093,BROKER://0.0.0.0:9092")
            .withEnv("KAFKA_INTER_BROKER_LISTENER_NAME", "BROKER")
            .withEnv("KAFKA_SASL_ENABLED_MECHANISMS", "PLAIN")
            .withEnv("KAFKA_SASL_MECHANISM_INTER_BROKER_PROTOCOL", "PLAIN")
            .withEnv("KAFKA_SASL_MECHANISM_CONTROLLER_PROTOCOL", "PLAIN")
            .withEnv("KAFKA_OPTS", "-Djava.security.auth.login.config=/etc/kafka/kafka_jaas.conf")
            .withEnv("ZOOKEEPER_SASL_ENABLED", "true")
            .withEnv("ZOOKEEPER_CLIENT_PORT", "2181")
            .withEnv("ZOOKEEPER_AUTH_PROVIDER_1", "zookeeper.authProvider.SASLAuthenticationProvider")
            .withEnv("ZOOKEEPER_SERVER_JVMFLAGS", "-Djava.security.auth.login.config=/etc/kafka/zookeeper_jaas.conf")
            // FIXED: Use forClasspathResource with correct path (files must be in src/test/resources)
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("kafka_jaas.conf"),
                    "/etc/kafka/kafka_jaas.conf"
            )
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("zookeeper_jaas.conf"),
                    "/etc/kafka/zookeeper_jaas.conf"
            );

    @Autowired
    private StringProducer stringProducer;

    @Autowired
    private StringConsumer stringConsumer;

    /**
     * Configures Kafka bootstrap servers for producer and consumer.
     */
    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.producer.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add("spring.kafka.consumer.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add("kafka.topic.string-topic", () -> TOPIC);
    }

    /**
     * Sets up Kafka topic before all tests.
     */
    @BeforeAll
    static void setUp() {
        Map<String, Object> adminConfig = new HashMap<>();
        adminConfig.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
        adminConfig.put("security.protocol", "SASL_PLAINTEXT");
        adminConfig.put("sasl.mechanism", "PLAIN");
        adminConfig.put("sasl.jaas.config",
                "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"admin\" password=\"admin-secret\";");

        try (AdminClient adminClient = AdminClient.create(adminConfig)) {
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
        stringConsumer.setTopic(TOPIC);
        stringConsumer.setProcessMethod((message, headers) -> {
            consumedMessages.add(message);
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

    // ==================== Your tests (unchanged) ====================

    @Test
    @Order(1)
    @DisplayName("Verify single message production and consumption with headers")
    void testSingleMessage() throws Exception {
        String message = "Test message " + UUID.randomUUID();
        Map<String, String> headers = Collections.singletonMap("kafka_test_header", "test_value");

        stringProducer.send(message, headers);

        String consumedMessage = consumedMessages.poll(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertNotNull(consumedMessage);
        assertEquals(message, consumedMessage);

        Map<String, String> consumedHeader = consumedHeaders.poll(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertNotNull(consumedHeader);
        assertTrue(consumedHeader.containsKey("kafka_test_header"));
    }

    @Test
    @Order(2)
    @DisplayName("Verify multiple message production and consumption")
    void testMultipleMessages() throws Exception {
        int messageCount = 5;
        Map<String, String> headers = Collections.singletonMap("kafka_test_header", "test_multi_value");
        String[] messages = new String[messageCount];
        for (int i = 0; i < messageCount; i++) {
            messages[i] = "Test message " + i + " " + UUID.randomUUID();
        }

        for (String message : messages) {
            stringProducer.send(message, headers);
        }

        for (int i = 0; i < messageCount; i++) {
            String consumedMessage = consumedMessages.poll(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            assertNotNull(consumedMessage);
            assertTrue(Arrays.asList(messages).contains(consumedMessage));
        }
    }

    @Test
    @Order(3)
    @DisplayName("Verify empty message handling")
    void testEmptyMessage() {
        Map<String, String> headers = Collections.singletonMap("kafka_test_header", "test_empty_value");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> stringProducer.send(null, headers));

        assertEquals("Message cannot be null", exception.getMessage());
    }

    @Test
    @Order(4)
    @DisplayName("Verify multiple headers propagation")
    void testMultipleHeaders() throws Exception {
        String message = "Test message " + UUID.randomUUID();
        Map<String, String> headers = new HashMap<>();
        headers.put("kafka_test_header1", "test_value1");
        headers.put("kafka_test_header2", "test_value2");
        headers.put("kafka_test_header3", "test_value3");

        stringProducer.send(message, headers);

        String consumedMessage = consumedMessages.poll(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertNotNull(consumedMessage);
        assertEquals(message, consumedMessage);

        Map<String, String> consumedHeader = consumedHeaders.poll(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertNotNull(consumedHeader);
        assertTrue(consumedHeader.containsKey("kafka_test_header1"));
        assertTrue(consumedHeader.containsKey("kafka_test_header2"));
        assertTrue(consumedHeader.containsKey("kafka_test_header3"));
    }

    @Test
    @Order(5)
    @DisplayName("Verify null message handling")
    void testNullMessage() {
        Map<String, String> headers = Collections.singletonMap("kafka_test_header", "test_null_value");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> stringProducer.send(null, headers));

        assertEquals("Message cannot be null", exception.getMessage());
    }

    @Test
    @Order(6)
    @DisplayName("Verify unauthorized access handling")
    void testUnauthorizedAccess() {
        Map<String, Object> wrongConfig = new HashMap<>();
        wrongConfig.put("bootstrap.servers", "localhost:" + kafkaContainer.getMappedPort(9092));
        wrongConfig.put("sasl.mechanism", "PLAIN");
        wrongConfig.put("security.protocol", "SASL_PLAINTEXT");
        wrongConfig.put("sasl.jaas.config",
                "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"wronguser\" password=\"wrongpassword\";");
        wrongConfig.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        wrongConfig.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");

        KafkaTemplate<String, byte[]> unauthorizedTemplate = new KafkaTemplate<>(
                new org.springframework.kafka.core.DefaultKafkaProducerFactory<>(wrongConfig));

        StringProducer unauthorizedProducer = new StringProducer() {{
            this.kafkaTemplate = unauthorizedTemplate;
            this.topic = TOPIC;
        }};

        RuntimeException exception = assertThrows(KafkaException.class,
                () -> unauthorizedProducer.send("Test message", null));

        assertTrue(exception.getCause() instanceof SaslAuthenticationException);
        assertTrue(exception.getCause().getMessage().contains("Invalid username or password"));
    }
}