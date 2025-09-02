package eu.isygoit.kafka;

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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create",
        "app.tenancy.enabled=true",
        "app.tenancy.mode=GDM"
})
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
@ExtendWith(SpringExtension.class)
public class KafkaStringTest {

    @Container
    private static final KafkaContainer kafkaContainer = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.6.0"))
            .withEmbeddedZookeeper()
            .withEnv("KAFKA_BROKER_ID", "1")
            .withEnv("KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR", "1")
            .withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "true");

    private static final String TOPIC = "string-topic";
    private static final BlockingQueue<String> consumedMessages = new LinkedBlockingQueue<>();

    @Autowired
    private StringProducer stringProducer;

    @Autowired
    private StringConsumer stringConsumer;

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.producer.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add("spring.kafka.consumer.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add("kafka.topic.string-topic", () -> TOPIC);
    }

    @BeforeAll
    static void setUp() {
        try (AdminClient adminClient = AdminClient.create(
                Collections.singletonMap(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers()))) {
            adminClient.createTopics(Collections.singletonList(new NewTopic(TOPIC, 1, (short) 1)));
        }
    }

    @BeforeEach
    void setUpConsumer() {
        stringConsumer.setTopic(TOPIC);
        stringConsumer.setProcessMethod((message, headers) -> consumedMessages.add(message));
    }

    @Test
    @Order(1)
    void testStringProducerConsumer() throws Exception {
        // Arrange
        String message = "Test message " + UUID.randomUUID();
        Map<String, String> headers = Collections.singletonMap("test-header", "test-value");

        // Act
        stringProducer.send(message, headers);

        // Assert
        String consumedMessage = consumedMessages.poll(10, TimeUnit.SECONDS);
        assertNotNull(consumedMessage, "No message consumed within timeout");
        assertEquals(message, consumedMessage, "Consumed message does not match sent message");
    }
}