# Kafka Producer and Consumer Implementation

This project provides a set of abstract Kafka producer and consumer classes designed to handle various data types (
String, JSON, XML, Binary, ByteBuffer, and File-like data) with a focus on flexibility, security, and extensibility. The
implementation is built using Spring Kafka and is fully compatible with **Java 17**. It includes robust security
features to ensure safe and reliable message processing and sending.

## Overview

### Producer Classes

The following abstract producer classes extend `AbstractKafkaProducer<T>`:

- **KafkaStringProducer**: Sends string data encoded in UTF-8.
- **KafkaJsonProducer<T>**: Sends JSON data, serializing a specified type `T` with optional JSON schema validation.
- **KafkaXmlProducer<T>**: Sends XML data, serializing a specified type `T` with optional XSD schema validation.
- **KafkaBinaryProducer**: Sends raw byte array data directly.
- **KafkaByteBufferProducer**: Sends data as `ByteBuffer`.
- **KafkaFileProducer**: Sends data as `InputStream` for file-like data (reads stream into bytes).

Each producer must:

- Specify the Kafka topic (e.g., via `@Value` or constructor).
- Be annotated with `@Service` for Spring component scanning.

### Consumer Classes

The following abstract consumer classes extend `AbstractKafkaConsumer<T>`:

- **KafkaStringConsumer**: Processes string data encoded in UTF-8.
- **KafkaJsonConsumer<T>**: Processes JSON data, deserializing it into a specified type `T` with optional JSON schema
  validation.
- **KafkaXmlConsumer<T>**: Processes XML data, deserializing it into a specified type `T` with optional XSD schema
  validation.
- **KafkaBinaryConsumer**: Processes raw byte array data directly.
- **KafkaByteBufferConsumer**: Processes data as `ByteBuffer`.
- **KafkaFileConsumer**: Processes data as `InputStream` for file-like data.

Each consumer must:

- Specify the Kafka topic (e.g., via `@Value` or constructor).
- Be annotated with `@Service` for Spring component scanning.
- Implement the `process` method to handle the deserialized message.

## Usage

### Prerequisites

- **Java 17**: The project is fully compatible with Java 17.
- **Spring Boot**: Use Spring Boot 2.7.x or 3.x for Java 17 support.
- **Kafka Cluster**: A running Kafka cluster with SSL/TLS and SASL configured (if security is enabled).
- **Micrometer**: For metrics (optional, include dependencies if needed).
- **Retry Support**: Enable `@EnableRetry` in your Spring configuration for retry functionality.
- **Dependencies**: Include Jackson for JSON/XML processing, `com.networknt` for JSON schema validation, and Kafka
  client libraries.

### Creating a Concrete Producer

To create a producer, extend one of the abstract producer classes. Below is an example for a JSON producer:

```java
package com.example;

import eu.isygoit.com.event.KafkaJsonProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MyJsonProducer extends KafkaJsonProducer<MyData> {

    @Value("${kafka.topic.my-topic}")
    protected String topic;

    @Value("${kafka.schemas.email-schema}")
    protected String jsonSchemaPath;
}
```

- **Set the Topic**: Use `@Value` to inject the topic from `application.yml`.
- **Set Schema Path (if applicable)**: For JSON or XML producers, specify the schema path for validation.
- **Register as a Service**: Annotate with `@Service` for Spring to manage the bean.

To send a message, autowire the producer and call the `send` method:

```java

@Autowired
private MyJsonProducer myJsonProducer;

// ...

myJsonProducer.

send(myData); // Or send(myData, customHeadersMap)
```

### Creating a Concrete Consumer

To create a consumer, extend one of the abstract consumer classes and implement the required methods. Below is an
example for a JSON consumer:

```java
package com.example;

import eu.isygoit.com.event.KafkaJsonConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class MyJsonConsumer extends KafkaJsonConsumer<MyData> {

    @Value("${kafka.topic.my-topic}")
    private String topic;

    @Value("${kafka.schemas.email-schema}")
    private String jsonSchemaPath;

    @Override
    protected void process(MyData message, Map<String, String> headers) throws Exception {
        // Process the deserialized MyData object
        log.info("Received message: {}", message);
    }
}
```

- **Set the Topic**: Use `@Value` to inject the topic from `application.yml`.
- **Set Schema Path (if applicable)**: For JSON or XML consumers, specify the schema path for validation.
- **Implement `process`**: Define how to handle the deserialized message and headers.
- **Register as a Service**: Annotate with `@Service` for Spring to manage the bean.

### Kafka Configuration

#### Producer Configuration

The `AbstractKafkaProducer` uses a `KafkaTemplate<String, byte[]>`. Configure the template in your Spring application:

```java
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaConfig {

    @Bean
    public ProducerFactory<String, byte[]> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        // Properties loaded from application.yml
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, byte[]> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
```

- **Key Serializer**: `StringSerializer`
- **Value Serializer**: `ByteArraySerializer`
- **Producer Properties**: Loaded from `application.yml` (e.g., SSL, SASL).

#### Consumer Configuration

The `AbstractKafkaConsumer` uses a `KafkaListener` with a `ConcurrentKafkaListenerContainerFactory`. Configure the
container factory:

```java
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

@Configuration
public class KafkaConfig {

    @Bean
    public ConsumerFactory<String, byte[]> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        // Properties loaded from application.yml
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, byte[]> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, byte[]> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(3); // Adjust based on your needs
        return factory;
    }
}
```

- **Key Deserializer**: `StringDeserializer`
- **Value Deserializer**: `ByteArrayDeserializer`
- **Consumer Properties**: Loaded from `application.yml` (e.g., SSL, SASL, group ID).
- **Java 17 Compatibility**: Ensure Spring Boot 2.7.x or 3.x and `kafka-clients` 3.0.0 or later are used.

## Security Features

The implementation incorporates robust security measures for both producers and consumers:

1. **SSL/TLS Encryption**:
    - Encrypts data in transit using SSL/TLS.
    - Configured via `application.yml` with truststore and keystore settings.

2. **SASL Authentication**:
    - Supports SASL mechanisms (e.g., SASL/PLAIN, SASL/GSSAPI) for producer and consumer authentication.
    - Ensures only authorized clients can send or consume messages.

3. **Input Validation**:
    - Validates messages and topics to prevent null or invalid inputs.
    - Subclasses (e.g., `KafkaJsonProducer`, `KafkaJsonConsumer`) include type-specific validation for JSON and XML
      data.

4. **Secure Logging**:
    - Avoids logging sensitive message content to prevent PII exposure.
    - Uses debug-level logging for successful message sending/processing to minimize log exposure.

5. **Enhanced Error Handling**:
    - Handles `AuthenticationException`, `AuthorizationException`, and other Kafka errors with clear logging.
    - Provides detailed error messages for debugging while maintaining security.

6. **Message Signing/Verification**:
    - Supports HMAC signature for data integrity (optional, enabled via `kafka.security.enable-hmac=true`).
    - Configured with `kafka.security.hmac-secret` for HMAC-SHA256 signing/verification.
    - Producers append signature in the format `Base64(data)|Base64(HMAC)`; consumers verify it.

7. **Message Encryption/Decryption**:
    - Supports AES encryption/decryption for sensitive payloads (optional, enabled via
      `kafka.security.enable-encryption=true`).
    - Configured with `kafka.security.aes-key` for AES-128 encryption/decryption.
    - Producers encrypt before signing; consumers decrypt after verification (if enabled).

8. **Schema Validation**:
    - **JSON**: Optional validation against a JSON schema (enabled via `kafka.security.enable-json-validation=true`,
      schema path set in concrete classes, uses Draft 2020-12).
    - **XML**: Optional validation against an XSD schema (enabled via `kafka.security.enable-xml-validation=true`, XSD
      path set in concrete classes).
    - Prevents sending/processing of malformed or invalid data.

9. **XXE Prevention**:
    - For XML producers and consumers, disables external entities and DTDs to prevent XML External Entity (XXE) attacks.

10. **Retries**:
    - Custom retries for transient failures using Spring Retry (enabled with `spring.retry.enabled=true` in
      `application.yml`).
    - Configured with 3 attempts and exponential backoff (delay=1000ms, multiplier=2).

11. **Metrics**:
    - Micrometer integration for sending/processing metrics (enabled via
      `management.endpoints.web.exposure.include=prometheus`).
    - Records sending time (`kafka.producer.send`) and processing time (`kafka.consumer.process`) tagged with the topic
      name.

## YAML Configuration

Configure the producers and consumers in `application.yml`. Below is an example configuration:

```yaml
spring:
  kafka:
    producer:
      bootstrap-servers: localhost:9093
      ssl:
        trust-store-location: file:/path/to/truststore.jks
        trust-store-password: truststore-password
        key-store-location: file:/path/to/keystore.jks
        key-store-password: keystore-password
        key-password: key-password
      security:
        protocol: SASL_SSL
      sasl:
        mechanism: PLAIN
        jaas:
          config: |
            org.apache.kafka.common.security.plain.PlainLoginModule required \
              username="kafka-user" \
              password="kafka-password";
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.ByteArraySerializer
    consumer:
      bootstrap-servers: localhost:9093
      group-id: my-consumer-group
      auto-offset-reset: earliest
      ssl:
        trust-store-location: file:/path/to/truststore.jks
        trust-store-password: truststore-password
        key-store-location: file:/path/to/keystore.jks
        key-store-password: keystore-password
        key-password: key-password
      security:
        protocol: SASL_SSL
      sasl:
        mechanism: PLAIN
        jaas:
          config: |
            org.apache.kafka.common.security.plain.PlainLoginModule required \
              username="kafka-user" \
              password="kafka-password";
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.ByteArrayDeserializer
  retry:
    enabled: true
management:
  endpoints:
    web:
      exposure:
        include: prometheus
kafka:
  topic:
    my-topic: my-kafka-topic
  schemas:
    email-schema: file:/path/to/email-schema.json
    xml-message-xsd: file:/path/to/xml-message.xsd
  security:
    enable-hmac: false
    hmac-secret: "" # Set for enabling signing/verification
    enable-encryption: false
    aes-key: "" # 16-byte key for AES-128
    enable-json-validation: false
    enable-xml-validation: false
```

### Configuration Details

- **spring.kafka.producer**:
    - `bootstrap-servers`: Kafka broker address (e.g., `localhost:9093`).
    - `ssl`: Configures SSL/TLS with truststore and keystore settings.
    - `security.protocol`: Specifies the security protocol (e.g., `SASL_SSL`).
    - `sasl`: Configures SASL mechanism and JAAS settings.
    - `key-serializer`: Set to `StringSerializer`.
    - `value-serializer`: Set to `ByteArraySerializer`.
- **spring.kafka.consumer**:
    - `bootstrap-servers`: Kafka broker address (e.g., `localhost:9093`).
    - `group-id`: Consumer group ID (e.g., `my-consumer-group`).
    - `auto-offset-reset`: Offset reset policy (e.g., `earliest`).
    - `ssl`: Configures SSL/TLS with truststore and keystore settings.
    - `security.protocol`: Specifies the security protocol (e.g., `SASL_SSL`).
    - `sasl`: Configures SASL mechanism and JAAS settings.
    - `key-deserializer`: Set to `StringDeserializer`.
    - `value-deserializer`: Set to `ByteArrayDeserializer`.
- **spring.retry.enabled**: Enables Spring Retry for transient failure handling.
- **management.endpoints.web.exposure.include**: Exposes Prometheus metrics endpoint.
- **kafka.topic**: Map of topic names to Kafka topic strings (e.g., `my-topic: my-kafka-topic`).
- **kafka.schemas**: Map of schema names to file paths for JSON/XML validation (e.g., `email-schema`,
  `xml-message-xsd`).
- **kafka.security**:
    - `enable-hmac`: Enable HMAC signing/verification.
    - `hmac-secret`: Secret key for HMAC-SHA256.
    - `enable-encryption`: Enable AES encryption/decryption.
    - `aes-key`: 16-byte key for AES-128 encryption/decryption.
    - `enable-json-validation`: Enable JSON schema validation.
    - `enable-xml-validation`: Enable XML schema validation.

### Notes

- Ensure secrets (`hmac-secret`, `aes-key`, `kafka-password`, etc.) are securely managed (e.g., via environment
  variables or a secret manager).
- Schema files (`email-schema`, `xml-message-xsd`) must be accessible to the application (e.g., in the classpath or file
  system).
- Disable security features (`enable-hmac`, `enable-encryption`, etc.) in non-secure environments to simplify testing.
- Use Spring Boot 2.7.x or 3.x and `kafka-clients` 3.0.0 or later for Java 17 compatibility.

## Example Usage

1. **Configure Kafka**: Set up `application.yml` with your Kafka broker, topics, and security settings.
2. **Create Producers/Consumers**: Extend the appropriate abstract classes (e.g., `KafkaJsonProducer<MyData>`,
   `KafkaJsonConsumer<MyData>`).
3. **Implement Consumer Logic**: Define the `process` method for consumers to handle deserialized messages.
4. **Send Messages**: Autowire producers and call `send(myData)` or `send(myData, headers)`.
5. **Run the Application**: Start your Spring Boot application, ensuring the `KafkaTemplate` and
   `ConcurrentKafkaListenerContainerFactory` are configured.
6. **Monitor Metrics**: Expose Prometheus metrics to monitor sending/processing times.

## Dependencies

Add the following to your `pom.xml` or `build.gradle`:

```xml
<!-- Spring Boot Starter Kafka -->
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
    <version>3.0.0</version>
</dependency>
        <!-- Kafka Clients -->
<dependency>
<groupId>org.apache.kafka</groupId>
<artifactId>kafka-clients</artifactId>
<version>3.4.0</version>
</dependency>
        <!-- Jackson for JSON/XML -->
<dependency>
<groupId>com.fasterxml.jackson.core</groupId>
<artifactId>jackson-databind</artifactId>
<version>2.15.2</version>
</dependency>
<dependency>
<groupId>com.fasterxml.jackson.dataformat</groupId>
<artifactId>jackson-dataformat-xml</artifactId>
<version>2.15.2</version>
</dependency>
        <!-- JSON Schema Validation -->
<dependency>
<groupId>com.networknt</groupId>
<artifactId>json-schema-validator</artifactId>
<version>1.0.87</version>
</dependency>
        <!-- Micrometer (optional) -->
<dependency>
<groupId>io.micrometer</groupId>
<artifactId>micrometer-registry-prometheus</artifactId>
<version>1.12.0</version>
</dependency>
        <!-- Lombok (optional) -->
<dependency>
<groupId>org.projectlombok</groupId>
<artifactId>lombok</artifactId>
<version>1.18.34</version>
<scope>provided</scope>
</dependency>
```

## Troubleshooting

- **Null Message Errors**: Ensure messages are not null before sending/processing.
- **Schema Validation Errors**: Verify schema paths and file accessibility.
- **Security Exceptions**: Check HMAC secret, AES key, or Kafka authentication settings.
- **Retry Failures**: Ensure `spring.retry.enabled=true` and review retry policies.
- **Kafka Configuration Issues**: Verify `KafkaTemplate` and `ConcurrentKafkaListenerContainerFactory` configurations
  align with `application.yml`.

This implementation provides a robust foundation for building secure and scalable Kafka producers and consumers tailored
to various data types.