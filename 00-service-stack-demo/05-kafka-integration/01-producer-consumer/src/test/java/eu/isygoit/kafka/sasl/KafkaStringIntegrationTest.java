package eu.isygoit.kafka.sasl;

import eu.isygoit.kafka.consumer.StringConsumer;
import eu.isygoit.kafka.producer.StringProducer;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
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
 * Integration tests for Kafka string message processing with SASL authentication.
 * Tests the interaction between StringProducer and StringConsumer
 * using Testcontainers-managed Kafka and Zookeeper instances with SASL/PLAIN authentication.
 * Uses the "sasl" Spring profile for configuration.
 */
@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create",
        "app.tenancy.enabled=true",
        "app.tenancy.mode=GDM"
})
@ActiveProfiles("sasl")
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
@ExtendWith(SpringExtension.class)
class KafkaStringIntegrationTest {

    private static final String TOPIC = "string-topic";
    private static final int TIMEOUT_SECONDS = 10;
    private static final BlockingQueue<String> consumedMessages = new LinkedBlockingQueue<>();
    private static final BlockingQueue<Map<String, String>> consumedHeaders = new LinkedBlockingQueue<>();
    private static final String SASL_USERNAME = "admin";
    private static final String SASL_PASSWORD = "admin-secret";

    private static Network network = Network.newNetwork();

    @Container
    private static final GenericContainer<?> zookeeperContainer = new GenericContainer<>(
            DockerImageName.parse("confluentinc/cp-zookeeper:7.8.0"))
            .withNetwork(network)
            .withNetworkAliases("zookeeper")
            .withExposedPorts(2181)
            .withEnv("ZOOKEEPER_CLIENT_PORT", "2181")
            .withEnv("ZOOKEEPER_TICK_TIME", "2000")
            .withEnv("ZOOKEEPER_SERVER_SASL_ENABLED", "true")
            .withEnv("ZOOKEEPER_AUTH_PROVIDER_1", "org.apache.zookeeper.server.auth.SASLAuthenticationProvider")
            .withEnv("ZOOKEEPER_SERVER_AUTHENTICATION", "sasl")
            .withEnv("KAFKA_OPTS", "-Djava.security.auth.login.config=/etc/zookeeper/zookeeper_jaas.conf")
            .withClasspathResourceMapping("zookeeper_jaas.conf", "/etc/zookeeper/zookeeper_jaas.conf", BindMode.READ_ONLY)
            .waitingFor(Wait.forLogMessage(".*Started AdminServer.*", 1)
                    .withStartupTimeout(Duration.ofMinutes(2)));

    @Container
    private static final KafkaContainer kafkaContainer = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.8.0"))
            .withNetwork(network)
            .withNetworkAliases("kafka")
            .withExposedPorts(9092, 9093)
            .withEnv("KAFKA_BROKER_ID", "1")
            .withEnv("KAFKA_ZOOKEEPER_CONNECT", "zookeeper:2181")
            .withEnv("KAFKA_ZOOKEEPER_PROTOCOL", "SASL")
            .withEnv("KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR", "1")
            .withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "true")
            .withEnv("KAFKA_SASL_ENABLED_MECHANISMS", "PLAIN")
            .withEnv("KAFKA_SASL_MECHANISM_INTER_BROKER_PROTOCOL", "PLAIN")
            .withEnv("KAFKA_AUTHORIZER_CLASS_NAME", "kafka.security.authorizer.AclAuthorizer")
            .withEnv("KAFKA_SUPER_USERS", "User:admin")
            .withEnv("KAFKA_LISTENER_SECURITY_PROTOCOL_MAP", "SASL_PLAINTEXT:SASL_PLAINTEXT,PLAINTEXT:PLAINTEXT,BROKER:SASL_PLAINTEXT")
            .withEnv("KAFKA_LISTENERS", "SASL_PLAINTEXT://0.0.0.0:9092,PLAINTEXT://0.0.0.0:9093,BROKER://0.0.0.0:9094")
            .withEnv("KAFKA_INTER_BROKER_LISTENER_NAME", "BROKER")
            .withEnv("KAFKA_ADVERTISED_LISTENERS", "SASL_PLAINTEXT://host.docker.internal:9092,PLAINTEXT://localhost:9093,BROKER://kafka:9094")
            .withEnv("KAFKA_OPTS", "-Djava.security.auth.login.config=/etc/kafka/kafka_server_jaas.conf")
            .withClasspathResourceMapping("kafka_jaas.conf", "/etc/kafka/kafka_server_jaas.conf", BindMode.READ_ONLY)
            .waitingFor(Wait.forLogMessage(".*\\[KafkaServer id=\\d+\\] started.*", 1)
                    .withStartupTimeout(Duration.ofMinutes(2)))
            .dependsOn(zookeeperContainer);

    @Autowired
    private StringProducer stringProducer;

    @Autowired
    private StringConsumer stringConsumer;

    /**
     * Configures Kafka bootstrap servers and SASL settings for producer and consumer under the "sasl" profile.
     */
    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        // Use the mapped port for SASL_PLAINTEXT
        registry.add("spring.kafka.producer.bootstrap-servers", () -> "host.docker.internal:" + kafkaContainer.getMappedPort(9092));
        registry.add("spring.kafka.consumer.bootstrap-servers", () -> "host.docker.internal:" + kafkaContainer.getMappedPort(9092));
        registry.add("kafka.topic.string-topic", () -> TOPIC);
        registry.add("spring.kafka.producer.properties.sasl.mechanism", () -> "PLAIN");
        registry.add("spring.kafka.producer.properties.security.protocol", () -> "SASL_PLAINTEXT");
        registry.add("spring.kafka.producer.properties.sasl.jaas.config",
                () -> "org.apache.kafka.common.security.plain.PlainLoginModule required " +
                        "username=\"" + SASL_USERNAME + "\" " +
                        "password=\"" + SASL_PASSWORD + "\";");
        registry.add("spring.kafka.consumer.properties.sasl.mechanism", () -> "PLAIN");
        registry.add("spring.kafka.consumer.properties.security.protocol", () -> "SASL_PLAINTEXT");
        registry.add("spring.kafka.consumer.properties.sasl.jaas.config",
                () -> "org.apache.kafka.common.security.plain.PlainLoginModule required " +
                        "username=\"" + SASL_USERNAME + "\" " +
                        "password=\"" + SASL_PASSWORD + "\";");
    }

    /**
     * Sets up Kafka topic before all tests with SASL authentication.
     */
    @BeforeAll
    static void setUp() {
        Map<String, Object> adminProps = new HashMap<>();
        adminProps.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "host.docker.internal:" + kafkaContainer.getMappedPort(9092));
        adminProps.put("sasl.mechanism", "PLAIN");
        adminProps.put("security.protocol", "SASL_PLAINTEXT");
        adminProps.put("sasl.jaas.config",
                "org.apache.kafka.common.security.plain.PlainLoginModule required " +
                        "username=\"" + SASL_USERNAME + "\" " +
                        "password=\"" + SASL_PASSWORD + "\";");

        try (AdminClient adminClient = AdminClient.create(adminProps)) {
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

    /**
     * Tests basic message production and consumption with SASL authentication.
     */
    @Test
    @Order(1)
    @DisplayName("Verify single message production and consumption with headers and SASL")
    void testSingleMessage() throws Exception {
        // Arrange
        String message = "Test message " + UUID.randomUUID();
        Map<String, String> headers = Collections.singletonMap("kafka_test_header", "test_value");

        // Act
        stringProducer.send(message, headers);

        // Assert
        String consumedMessage = consumedMessages.poll(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertNotNull(consumedMessage, "No message consumed within " + TIMEOUT_SECONDS + " seconds");
        assertEquals(message, consumedMessage, "Consumed message does not match sent message");
        Map<String, String> consumedHeader = consumedHeaders.poll(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertNotNull(consumedHeader, "No headers consumed within " + TIMEOUT_SECONDS + " seconds");
        assertTrue(consumedHeader.containsKey("kafka_test_header"), "Consumed headers do not contain kafka_test_header");
    }

    /**
     * Tests handling of multiple messages in sequence with SASL authentication.
     */
    @Test
    @Order(2)
    @DisplayName("Verify multiple message production and consumption with SASL")
    void testMultipleMessages() throws Exception {
        // Arrange
        int messageCount = 5;
        Map<String, String> headers = Collections.singletonMap("kafka_test_header", "test_multi_value");
        String[] messages = new String[messageCount];
        for (int i = 0; i < messageCount; i++) {
            messages[i] = "Test message " + i + " " + UUID.randomUUID();
        }

        // Act
        for (String message : messages) {
            stringProducer.send(message, headers);
        }

        // Assert
        for (int i = 0; i < messageCount; i++) {
            String consumedMessage = consumedMessages.poll(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            assertNotNull(consumedMessage, "Message " + i + " not consumed within " + TIMEOUT_SECONDS + " seconds");
            assertTrue(Arrays.asList(messages).contains(consumedMessage),
                    "Consumed message " + consumedMessage + " not in sent messages");
        }
    }

    /**
     * Tests handling of empty message with SASL authentication.
     */
    @Test
    @Order(3)
    @DisplayName("Verify empty message handling with SASL")
    void testEmptyMessage() throws Exception {
        // Arrange
        Map<String, String> headers = Collections.singletonMap("kafka_test_header", "test_empty_value");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> stringProducer.send(null, headers),
                "Producer should throw IllegalArgumentException for null message");

        assertEquals("Message cannot be null", exception.getMessage(),
                "Exception message should match expected text");
    }

    /**
     * Tests header propagation with multiple headers and SASL authentication.
     */
    @Test
    @Order(4)
    @DisplayName("Verify multiple headers propagation with SASL")
    void testMultipleHeaders() throws Exception {
        // Arrange
        String message = "Test message " + UUID.randomUUID();
        Map<String, String> headers = new HashMap<>();
        headers.put("kafka_test_header1", "test_value1");
        headers.put("kafka_test_header2", "test_value2");
        headers.put("kafka_test_header3", "test_value3");

        // Act
        stringProducer.send(message, headers);

        // Assert
        String consumedMessage = consumedMessages.poll(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertNotNull(consumedMessage, "Message not consumed within " + TIMEOUT_SECONDS + " seconds");
        assertEquals(message, consumedMessage, "Consumed message does not match sent message");
        Map<String, String> consumedHeader = consumedHeaders.poll(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertNotNull(consumedHeader, "Headers not consumed within " + TIMEOUT_SECONDS + " seconds");
        assertTrue(consumedHeader.containsKey("kafka_test_header1"), "Consumed headers do not contain kafka_test_header1");
        assertTrue(consumedHeader.containsKey("kafka_test_header2"), "Consumed headers do not contain kafka_test_header2");
        assertTrue(consumedHeader.containsKey("kafka_test_header3"), "Consumed headers do not contain kafka_test_header3");
    }

    /**
     * Tests consumer error handling with null message and SASL authentication.
     */
    @Test
    @Order(5)
    @DisplayName("Verify null message handling with SASL - should throw IllegalArgumentException")
    void testNullMessage() {
        // Arrange
        Map<String, String> headers = Collections.singletonMap("kafka_test_header", "test_null_value");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> stringProducer.send(null, headers),
                "Producer should throw IllegalArgumentException for null message");

        assertEquals("Message cannot be null", exception.getMessage(),
                "Exception message should match expected text");
    }

    /**
     * Tests unauthorized access attempt with incorrect SASL credentials.
     */
    @Test
    @Order(6)
    @DisplayName("Verify unauthorized access with incorrect SASL credentials")
    void testUnauthorizedAccess() {
        // Arrange: Create a producer with incorrect credentials
        Map<String, Object> wrongConfig = new HashMap<>();
        wrongConfig.put("bootstrap.servers", "host.docker.internal:" + kafkaContainer.getMappedPort(9092));
        wrongConfig.put("sasl.mechanism", "PLAIN");
        wrongConfig.put("security.protocol", "SASL_PLAINTEXT");
        wrongConfig.put("sasl.jaas.config",
                "org.apache.kafka.common.security.plain.PlainLoginModule required " +
                        "username=\"wronguser\" " +
                        "password=\"wrongpassword\";");

        KafkaTemplate<String, byte[]> unauthorizedTemplate = new KafkaTemplate<>(
                new org.springframework.kafka.core.DefaultKafkaProducerFactory<>(wrongConfig));
        StringProducer unauthorizedProducer = new StringProducer() {
            {
                this.kafkaTemplate = unauthorizedTemplate;
                this.topic = TOPIC;
            }
        };

        // Act & Assert
        assertThrows(org.apache.kafka.common.errors.AuthenticationException.class,
                () -> unauthorizedProducer.send("Test message", null),
                "Should throw AuthenticationException for unauthorized access");
    }
}