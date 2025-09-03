# Kafka Producer-Consumer Integration Demo

This project demonstrates a Spring Boot application integrated with Apache Kafka for producing and consuming messages in
an event-driven architecture. Event-driven systems are designed to react to discrete events—such as user actions or
system updates—asynchronously, enabling decoupled, scalable, and responsive applications. This demo supports multiple
message formats (String, File, JSON, XML) and incorporates security mechanisms like HMAC, SASL, and SSL. Using
Testcontainers and Docker Compose, the project includes comprehensive integration tests to simulate a Kafka environment,
showcasing the production, consumption, and secure transmission of events in a robust and extensible manner suitable for
learning and production-grade applications.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Project Structure](#project-structure)
- [Setup and Configuration](#setup-and-configuration)
    - [General Setup](#general-setup)
    - [SSL Setup](#ssl-setup)
- [Running the Application](#running-the-application)
- [Running Tests](#running-tests)
- [Security Configurations](#security-configurations)
- [Test Scenarios](#test-scenarios)
- [Producer and Consumer Classes](#producer-and-consumer-classes)
- [Dependencies](#dependencies)
- [Troubleshooting](#troubleshooting)
- [License](#license)

## Overview

This demo showcases a Spring Boot application that integrates with Apache Kafka to handle message production and
consumption. It supports multiple message formats (String, File, JSON, XML) and includes security mechanisms such as
HMAC, SASL, and SSL. The integration tests use Testcontainers to create a Kafka instance for reliable testing. The
producer and consumer classes extend abstract classes from the `eu.isygoit.com.event` package, providing a flexible and
extensible framework for Kafka message handling.

## Features

- **Message Formats**: Supports String, File, JSON, and XML message types.
- **Security Configurations**:
    - No Security (`application.yml`)
    - HMAC (`application-hmac.yml`)
    - SASL_PLAINTEXT (`application-sasl.yml`)
    - SSL (`application-ssl.yml`)
- **Integration Tests**: Comprehensive tests for each message type and security configuration using Testcontainers.
- **Dynamic Configuration**: Uses Spring's `DynamicPropertySource` to configure Kafka bootstrap servers dynamically.
- **Header Propagation**: Tests header propagation for all message types.
- **Error Handling**: Tests for null/empty messages and invalid security configurations.
- **Extensible Design**: Producer and consumer classes extend abstract base classes for easy customization.
- **Docker Compose Support**: Includes `docker-compose` files for no-security, SASL, and SSL configurations.
- **SSL Certificate Generation**: Scripts to generate and configure SSL certificates for secure Kafka communication.

## Prerequisites

- **Java**: JDK 17 or higher
- **Maven**: 3.6.0 or higher
- **Docker**: Required for Testcontainers and Docker Compose to run Kafka containers
- **Kafka**: Knowledge of Kafka concepts (topics, producers, consumers)
- **OpenSSL and Keytool**: Required for SSL certificate generation
- **Dependencies**: Ensure all dependencies in `pom.xml` are resolved (e.g., Spring Boot, Kafka, Testcontainers)

## Project Structure

```
src
├── main
│   ├── java
│   │   └── eu.isygoit.kafka
│   │       ├── consumer
│   │       │   ├── FileConsumer.java
│   │       │   ├── JsonConsumer.java
│   │       │   ├── StringConsumer.java
│   │       │   ├── XmlConsumer.java
│   │       ├── producer
│   │       │── FileProducer.java
│   │       │── JsonProducer.java
│   │       │── StringProducer.java
│   │       │── XmlProducer.java
│   │       ├── dto
│   │       │── TutorialDto.java
│   ├── resources
│   │   ├── application.yml
│   │   ├── application-hmac.yml
│   │   ├── application-sasl.yml
│   │   ├── application-ssl.yml
│   │   ├── kafka_jaas.conf
│   │   ├── zookeeper_jaas.conf
├── test
│   ├── java
│   │   └── eu.isygoit.kafka
│   │       ├── nosecu
│   │       │   ├── KafkaFileIntegrationTest.java
│   │       │   ├── KafkaJsonIntegrationTest.java
│   │       │   ├── KafkaStringIntegrationTest.java
│   │       │   ├── KafkaXmlIntegrationTest.java
│   │       ├── hmac
│   │       │   ├── KafkaStringIntegrationTest.java
│   │       ├── sasl
│   │       │   ├── KafkaStringIntegrationTest.java
├── certs
│   ├── client-ssl.properties
│   ├── kafka_server_jaas.conf
│   ├── key-credentials.txt
├── config
├── create-ssl-certs.sh
├── create-ssl-config.sh
├── docker-compose-nosecu.yml
 ├── docker-compose-sasl.yml
├── docker-compose-ssl.yml
├── test-ssl-connection.sh
```

## Setup and Configuration

### General Setup

1. **Clone the Repository**:
   ```bash
   git clone <repository-url>
   cd <repository-directory>
   ```

2. **Configure Kafka**:
    - Update `application.yml`, `application-hmac.yml`, `application-sasl.yml`, and `application-ssl.yml` with
      appropriate Kafka bootstrap server addresses if not using Testcontainers or Docker Compose.
    - For SASL, ensure `kafka_jaas.conf` and `zookeeper_jaas.conf` are correctly configured.

3. **Configure Schemas**:
    - Update `kafka.schemas.email-schema` and `kafka.schemas.xml-message-xsd` in the configuration files with absolute
      paths to valid schema files if JSON/XML validation is enabled.

4. **Install Dependencies**:
   ```bash
   mvn clean install
   ```

### SSL Setup

To enable SSL for Kafka communication, follow these steps to generate and configure SSL certificates:

1. **Generate SSL Certificates**:
    - Run the `create-ssl-certs.sh` script to generate keystores and truststores:
      ```bash
      chmod +x create-ssl-certs.sh
      ./create-ssl-certs.sh
      ```
    - This script creates:
        - `certs/ca-key.pem` and `certs/ca-cert.pem` for the Certificate Authority.
        - Keystores and truststores (`*.jks`) for `zookeeper`, `kafka`, and `client` in the `certs/` directory.
        - The default password for all keystores and truststores is `changeit`.
    - Ensure `openssl` and `keytool` are installed (`sudo apt-get install openjdk-17-jdk openssl` on Ubuntu or
      equivalent).

2. **Create SSL Configuration**:
    - Run the `create-ssl-config.sh` script to generate SSL configuration files:
      ```bash
      chmod +x create-ssl-config.sh
      ./create-ssl-config.sh
      ```
    - This script creates:
        - `certs/key-credentials.txt`: Contains the password (`changeit`) for keystores and truststores.
        - `certs/client-ssl.properties`: SSL properties for Kafka clients.
        - `certs/kafka_server_jaas.conf`: JAAS configuration for SASL (if needed).
        - Updates `test-ssl-connection.sh` for testing SSL connectivity.

3. **Update Application Configuration**:
    - Ensure `application-ssl.yml` points to the correct paths for `ssl.truststore.location` and
      `ssl.keystore.location`. Update the paths to match the location of the generated `client.truststore.jks` and
      `client.keystore.jks` files, e.g.:
      ```yaml
      ssl.truststore.location: ./certs/client.truststore.jks
      ssl.keystore.location: ./certs/client.keystore.jks
      ```
    - Verify that the `ssl.truststore.password`, `ssl.keystore.password`, and `ssl.key.password` are set to `changeit`.

4. **Start Kafka with SSL**:
    - Use the provided `docker-compose-ssl.yml` to start Kafka with SSL support:
      ```bash
      docker-compose -f docker-compose-ssl.yml up -d
      ```
    - This starts Zookeeper, Kafka with SSL on port `9093`, and Kafka UI on port `8080`.

5. **Test SSL Connection**:
    - Run the `test-ssl-connection.sh` script to verify SSL connectivity:
      ```bash
      chmod +x test-ssl-connection.sh
      ./test-ssl-connection.sh
      ```
    - This script sends a test message to `test-ssl-topic` and consumes it using Kafka's console producer and consumer
      with SSL configuration.

6. **Access Kafka UI**:
    - Open `http://localhost:8080` to access the Kafka UI and verify topics, messages, and cluster status with SSL
      configuration.

## Running the Application

1. **Run with Default Profile (No Security)**:
   ```bash
   docker-compose -f docker-compose-nosecu.yml up -d
   mvn spring-boot:run
   ```

2. **Run with HMAC Profile**:
   ```bash
   docker-compose -f docker-compose-nosecu.yml up -d
   mvn spring-boot:run -Dspring.profiles.active=hmac
   ```

3. **Run with SASL Profile**:
   ```bash
   docker-compose -f docker-compose-sasl.yml up -d
   mvn spring-boot:run -Dspring.profiles.active=sasl
   ```

4. **Run with SSL Profile**:
   ```bash
   docker-compose -f docker-compose-ssl.yml up -d
   mvn spring-boot:run -Dspring.profiles.active=ssl
   ```

## Running Tests

The integration tests use Testcontainers to spin up a Kafka instance. Ensure Docker is running before executing tests.

```bash
mvn test
```

To run tests for a specific profile:

- **HMAC**: `mvn test -Dspring.profiles.active=hmac`
- **SASL**: `mvn test -Dspring.profiles.active=sasl`
- **SSL**: Ensure certificates are generated and paths in `application-ssl.yml` are updated, then run:
  ```bash
  mvn test -Dspring.profiles.active=ssl
  ```

## Security Configurations

- **No Security**: Uses `application.yml` with no additional security settings (`docker-compose-nosecu.yml`).
- **HMAC**: Enabled in `application-hmac.yml` with a shared secret for message authentication.
- **SASL_PLAINTEXT**: Configured in `application-sasl.yml` with PLAIN mechanism and admin credentials (
  `docker-compose-sasl.yml`).
- **SSL**: Configured in `application-ssl.yml` with truststore and keystore for secure communication (
  `docker-compose-ssl.yml`).

## Test Scenarios

Each integration test file (`KafkaFileIntegrationTest.java`, `KafkaJsonIntegrationTest.java`,
`KafkaStringIntegrationTest.java`, `KafkaXmlIntegrationTest.java`) includes:

1. **Single Message**: Tests production and consumption of a single message with headers.
2. **Multiple Messages**: Tests sending and receiving multiple messages in sequence.
3. **Null/Empty Message**: Verifies handling of null or empty messages.
4. **Multiple Headers**: Tests propagation of multiple headers.
5. **Security-Specific Tests**:
    - HMAC: Tests invalid HMAC signature handling.
    - SASL: Tests unauthorized access with invalid credentials.

## Producer and Consumer Classes

The project includes concrete producer and consumer classes that extend abstract classes from the `eu.isygoit.com.event`
package:

- **Producers**:
    - `FileProducer`: Extends `KafkaFileProducer` to send `InputStream` data.
    - `JsonProducer`: Extends `KafkaJsonProducer<TutorialDto>` to send JSON-serialized `TutorialDto` objects.
    - `StringProducer`: Extends `KafkaStringProducer` to send string messages.
    - `XmlProducer`: Extends `KafkaXmlProducer<TutorialDto>` to send XML-serialized `TutorialDto` objects.

- **Consumers**:
    - `FileConsumer`: Extends `KafkaFileConsumer` to process `InputStream` data.
    - `JsonConsumer`: Extends `KafkaJsonConsumer<TutorialDto>` to process JSON-deserialized `TutorialDto` objects.
    - `StringConsumer`: Extends `KafkaStringConsumer` to process string messages.
    - `XmlConsumer`: Extends `KafkaXmlConsumer<TutorialDto>` to process XML-deserialized `TutorialDto` objects.

Each class uses `@Value` to inject the Kafka topic from the configuration and logs received messages using SLF4J.

## Dependencies

Add the following to your `pom.xml`:

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
        <!-- Testcontainers -->
<dependency>
<groupId>org.testcontainers</groupId>
<artifactId>kafka</artifactId>
<version>1.19.0</version>
<scope>test</scope>
</dependency>
        <!-- JUnit 5 -->
<dependency>
<groupId>org.junit.jupiter</groupId>
<artifactId>junit-jupiter</artifactId>
<version>5.9.0</version>
<scope>test</scope>
</dependency>
```

## Troubleshooting

- **Docker Issues**: Ensure Docker is running and has sufficient resources. Verify Docker Compose files for correct port
  mappings.
- **Kafka Connection Errors**: Verify bootstrap server addresses and port mappings in configuration files and Docker
  Compose.
- **SSL Errors**: Ensure valid truststore and keystore files are generated and paths are correct in
  `application-ssl.yml`. Run `create-ssl-certs.sh` and `create-ssl-config.sh` if issues persist.
- **Schema Validation Errors**: Verify schema paths and file accessibility for JSON/XML validation.
- **Test Failures**: Check logs in `logs/application.log` for detailed error messages.
- **Null Message Errors**: Ensure messages are not null before sending/processing.
- **Security Exceptions**: Check HMAC secret, AES key, or Kafka authentication settings (SASL credentials or SSL
  certificates).

## License

This project is licensed under the MIT License.