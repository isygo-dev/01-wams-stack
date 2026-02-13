# Kafka Monitoring with Docker Compose

This guide provides comprehensive instructions for setting up and monitoring a Kafka ecosystem using Docker Compose,
including Kafka, Zookeeper, Kafdrop, Redpanda (optional), Prometheus, and Grafana. It details how to configure a Kafka
exporter to scrape metrics for Prometheus and visualize them in Grafana, with step-by-step verification,
troubleshooting, and production-ready recommendations. Screenshots illustrate the setup and monitoring interfaces.

## Table of Contents

- [Overview](#overview)
- [Service Explanations](#service-explanations)
    - [Zookeeper](#zookeeper)
    - [Kafka](#kafka)
    - [Kafdrop](#kafdrop)
    - [Redpanda](#redpanda)
    - [Kafka Exporter](#kafka-exporter)
    - [Prometheus](#prometheus)
    - [Grafana](#grafana)
- [Prerequisites](#prerequisites)
- [Setup Instructions](#setup-instructions)
    - [1. Configure Docker Compose](#1-configure-docker-compose)
    - [2. Configure Prometheus](#2-configure-prometheus)
    - [3. Start Services](#3-start-services)
    - [4. Verify Services](#4-verify-services)
    - [5. Configure Grafana](#5-configure-grafana)
- [Key Metrics](#key-metrics)
- [Production Recommendations](#production-recommendations)
- [Troubleshooting](#troubleshooting)
- [Screenshots](#screenshots)
- [Next Steps](#next-steps)

## Overview

This setup deploys a robust Kafka ecosystem with monitoring tools to track performance and health. All services are
connected via a custom Docker network (`monitoring-net`) for seamless communication. The setup is designed for
development and testing but includes recommendations for production environments.

## Service Explanations

### Zookeeper

- **Purpose**: Zookeeper is a distributed coordination service that manages Kafka's cluster metadata, such as broker
  information, topic configurations, and partition leader elections. It ensures Kafka brokers operate as a cohesive
  cluster.
- **How It Works**:
    - Zookeeper runs as a centralized service that Kafka brokers connect to for coordination.
    - It stores metadata in a hierarchical namespace (similar to a file system) and handles tasks like leader election
      for partitions and tracking consumer offsets.
    - In this setup, Zookeeper listens on port `2181` and is configured with `ZOOKEEPER_CLIENT_PORT` and
      `ZOOKEEPER_TICK_TIME` for reliable operation.
    - The service is lightweight, with CPU and memory limits (`0.5` CPU, `512M` memory) to prevent resource overuse.
    - Kafka depends on Zookeeper, so it must be running before Kafka starts.

### Kafka

- **Purpose**: Kafka is a distributed streaming platform for publishing, subscribing, and processing large volumes of
  messages in real-time, with high throughput and fault tolerance.
- **How It Works**:
    - Kafka operates as a cluster of brokers (in this setup, a single broker with `KAFKA_BROKER_ID: 1`) that store and
      serve messages organized into topics and partitions.
    - It uses Zookeeper to manage cluster state and metadata.
    - Configured with dual listeners: `PLAINTEXT://kafka:9092` for internal Docker communication and
      `PLAINTEXT_HOST://localhost:29092` for external access.
    - Key configurations include `KAFKA_NUM_PARTITIONS: 3` for topic partitioning, `KAFKA_MESSAGE_MAX_BYTES: 1000000`
      for message size limits, and `KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1` for single-broker setups.
    - A healthcheck (`kafka-topics.sh --list`) ensures the broker is responsive.
    - Resource limits (`1.0` CPU, `1G` memory) balance performance and resource usage.

### Kafdrop

- **Purpose**: Kafdrop is a web-based user interface for browsing Kafka topics, messages, consumer groups, and offsets,
  providing a visual tool for inspecting Kafka's state.
- **How It Works**:
    - Kafdrop connects to the Kafka broker (`kafka:9092`) to retrieve metadata and message data.
    - It runs on port `9000` and provides a browser-based UI at `http://localhost:9000`.
    - Configured with minimal JVM resources (`-Xms32M -Xmx64M`) for lightweight operation.
    - Depends on Kafka to ensure the broker is available before starting.
    - Resource limits (`0.2` CPU, `256M` memory) ensure it doesn't consume excessive resources.
    - Users can view topic messages, consumer group lags, and partition details, as shown in screenshots (`img.png`,
      `img_1.png`).

### Redpanda

- **Purpose**: Redpanda is a Kafka-compatible streaming platform designed for low-latency, high-throughput data
  processing. It can be used as an alternative to Kafka or for specific use cases requiring high performance.
- **Is It Mandatory?**: **Redpanda is not mandatory** in this setup. It is included as an optional component to allow
  comparison with Kafka or to support applications that specifically require Redpanda's features. If you are solely
  focused on Kafka, Redpanda can be safely removed.
- **When to Use Redpanda**:
    - **Use Redpanda** if:
        - You need a Kafka-compatible system with lower latency and higher throughput for specific workloads (e.g.,
          real-time analytics or event streaming with large volumes).
        - You are evaluating Redpanda as an alternative to Kafka for its simpler deployment or performance
          optimizations.
        - Your application already uses Redpanda, and you want to monitor it alongside or instead of Kafka.
        - You want to leverage Redpanda's built-in metrics endpoint (`8080`) for additional monitoring insights.
    - **Do Not Use Redpanda** if:
        - Your application relies exclusively on Kafka, and you have no need for a Kafka-compatible alternative.
        - You want to simplify the setup to reduce resource usage and configuration complexity.
        - You are not evaluating or testing Redpanda, as it adds overhead (e.g., additional container, volume, and
          metrics scraping).
- **How It Works**:
    - Redpanda mimics Kafka's API, allowing Kafka clients to connect to it (`localhost:9093`).
    - It runs as a broker and controller (`KAFKA_PROCESS_ROLES: broker,controller`) with a unique `CLUSTER_ID`.
    - Exposes an admin API (`9644`) for cluster management and a metrics endpoint (`8080`) for Prometheus scraping.
    - A healthcheck (`curl http://localhost:9644/v1/status/ready`) ensures readiness.
    - Uses a persistent volume (`redpanda-data`) to store data across container restarts.
    - Resource limits (`1.0` CPU, `1G` memory) match Kafka's for consistency.
    - To remove Redpanda, delete its service and volume from `docker-compose.yml` and the `redpanda` job from
      `prometheus.yml`.

### Kafka Exporter

- **Purpose**: Kafka Exporter (`danielqsj/kafka-exporter`) translates Kafka metrics into a Prometheus-compatible format,
  enabling monitoring of consumer group lag, broker status, and topic metrics.
- **How It Works**:
    - Connects to the Kafka broker (`kafka:9092`) to collect metrics like consumer group lag (
      `kafka_consumergroup_lag`), broker counts (`kafka_brokers`), and partition details.
    - Exposes metrics on port `9308` at the `/metrics` endpoint (`http://localhost:9308/metrics`).
    - Supports optional filters (`GROUP_FILTER`, `TOPIC_FILTER`) to limit metrics scope for performance.
    - A healthcheck (`curl http://localhost:9308/metrics`) ensures the metrics endpoint is accessible.
    - Depends on Kafka to ensure the broker is running.
    - Resource limits (`0.2` CPU, `256M` memory) keep it lightweight.

### Prometheus

- **Purpose**: Prometheus is a time-series database that collects, stores, and queries metrics from services like Kafka
  Exporter and Redpanda for monitoring and alerting.
- **How It Works**:
    - Scrapes metrics from configured endpoints (`kafka-exporter:9308`, `redpanda:8080`, `prometheus:9090`) as defined
      in `prometheus.yml`.
    - Runs on port `9090`, providing a web UI (`http://localhost:9090`) for querying metrics and viewing target status.
    - Uses a persistent volume (`prometheus-data`) to store metrics data.
    - Depends on Kafka Exporter and Redpanda to ensure metrics sources are available.
    - Resource limits (`0.5` CPU, `512M` memory) balance performance and resource usage.
    - Metrics are queried using PromQL (e.g., `kafka_consumergroup_lag`) and visualized in Grafana.

### Grafana

- **Purpose**: Grafana is a visualization platform that creates dashboards from Prometheus metrics, providing insights
  into Kafka's performance and health.
- **How It Works**:
    - Connects to Prometheus (`http://prometheus:9090`) as a data source to retrieve metrics.
    - Runs on port `3000`, accessible at `http://localhost:3000` with default credentials (`admin`/
      `${GRAFANA_ADMIN_PASSWORD}`).
    - Supports importing pre-built dashboards (e.g., ID `7589` for Kafka Exporter, `14239` for Redpanda) to visualize
      metrics like consumer lag and broker status.
    - Uses a persistent volume (`grafana-data`) to retain dashboards and settings.
    - Configured with security settings (`GF_USERS_ALLOW_SIGN_UP=false`, `GF_AUTH_ANONYMOUS_ENABLED=false`) to restrict
      access.
    - Resource limits (`0.2` CPU, `256M` memory) ensure lightweight operation.
    - Dashboards display metrics as shown in screenshots (`img_7.png`, `img_8.png`, `img_9.png`, `img_10.png`,
      `img_11.png`).

## Prerequisites

Before starting, ensure the following:

- **Docker and Docker Compose**: Installed and configured (version 3.8 or later).
- **Basic Knowledge**: Familiarity with Kafka, Prometheus, Grafana, and Docker networking.
- **Environment File**: A `.env` file for sensitive configurations (e.g., `GRAFANA_ADMIN_PASSWORD`).
- **Screenshots Directory**: Access to `Lab-w1d1/readme-image/` containing screenshots (`img.png`, `img_1.png`, etc.).
- **Network Access**: Ports `2181`, `29092`, `9000`, `9093`, `9644`, `8080`, `9308`, `9090`, and `3000` available on
  localhost.

## Setup Instructions

### 1. Configure Docker Compose

Create a `docker-compose.yml` file with the configuration below. It includes resource limits, healthchecks, and the
Kafka exporter for monitoring.

```yaml
version: "3.8"
services:
  zookeeper:
    image: wurstmeister/zookeeper:latest
    ports:
      - "2181:2181"
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    networks:
      - monitoring-net
    deploy:
      resources:
        limits:
          cpus: '0.5'
          memory: 512M

  kafka:
    image: wurstmeister/kafka:latest
    ports:
      - "9092:9092"   # Internal docker-to-docker
      - "29092:29092" # External host access
    environment:
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_BROKER_ID: 1
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_MESSAGE_MAX_BYTES: 1000000
      KAFKA_NUM_PARTITIONS: 3
      KAFKA_MIN_INSYNC_REPLICAS: 1
      KAFKA_DEFAULT_REPLICATION_FACTOR: 1
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9092,PLAINTEXT_HOST://0.0.0.0:29092
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092,PLAINTEXT_HOST://localhost:29092
    depends_on:
      - zookeeper
    healthcheck:
      test: ["CMD", "kafka-topics.sh", "--bootstrap-server", "localhost:9092", "--list"]
      interval: 10s
      retries: 5
    networks:
      - monitoring-net
    deploy:
      resources:
        limits:
          cpus: '1.0'
          memory: 1G

  kafdrop:
    image: obsidiandynamics/kafdrop:latest
    ports:
      - "9000:9000"
    environment:
      KAFKA_BROKERCONNECT: kafka:9092
      JVM_OPTS: "-Xms32M -Xmx64M"
    depends_on:
      - kafka
    networks:
      - monitoring-net
    deploy:
      resources:
        limits:
          cpus: '0.2'
          memory: 256M

  redpanda:
    image: redpandadata/redpanda:v24.2.3
    command:
      - redpanda
      - start
      - --overprovisioned
      - --smp 1
      - --memory 1G
      - --reserve-memory 0M
      - --node-id 0
      - --check=false
    environment:
      KAFKA_PROCESS_ROLES: broker,controller
      CLUSTER_ID: ${CLUSTER_ID:-3fa85f64-5717-4562-b3fc-2c963f66afa6}
    ports:
      - "9093:9093" # Kafka client
      - "9644:9644" # Admin API
      - "8080:8080" # Metrics
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:9644/v1/status/ready"]
      interval: 10s
      retries: 5
    volumes:
      - redpanda-data:/var/lib/redpanda/data
    networks:
      - monitoring-net
    deploy:
      resources:
        limits:
          cpus: '1.0'
          memory: 1G

  kafka-exporter:
    image: danielqsj/kafka-exporter:latest
    ports:
      - "9308:9308"
    environment:
      KAFKA_SERVER: kafka:9092
      # Optional: GROUP_FILTER=my-consumer-group
      # Optional: TOPIC_FILTER=my-topic
    depends_on:
      - kafka
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:9308/metrics"]
      interval: 10s
      retries: 5
    networks:
      - monitoring-net
    deploy:
      resources:
        limits:
          cpus: '0.2'
          memory: 256M

  prometheus:
    image: prom/prometheus:latest
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml:ro
      - prometheus-data:/prometheus
    ports:
      - "9090:9090"
    depends_on:
      - kafka-exporter
      - redpanda
    networks:
      - monitoring-net
    deploy:
      resources:
        limits:
          cpus: '0.5'
          memory: 512M

  grafana:
    image: grafana/grafana:latest
    environment:
      GF_SECURITY_ADMIN_USER: admin
      GF_SECURITY_ADMIN_PASSWORD: ${GRAFANA_ADMIN_PASSWORD:-admin}
      GF_USERS_ALLOW_SIGN_UP: false
      GF_AUTH_ANONYMOUS_ENABLED: false
    volumes:
      - grafana-data:/var/lib/grafana
    ports:
      - "3000:3000"
    depends_on:
      - prometheus
    networks:
      - monitoring-net
    deploy:
      resources:
        limits:
          cpus: '0.2'
          memory: 256M

volumes:
  redpanda-data:
  prometheus-data:
  grafana-data:

networks:
  monitoring-net:
    driver: bridge
```

### 2. Configure Prometheus

Create a `prometheus.yml` file in the same directory as `docker-compose.yml` to scrape metrics from Kafka Exporter,
Redpanda (if used), and Prometheus itself.

```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s
scrape_configs:
  - job_name: 'kafka-exporter'
    static_configs:
      - targets: ['kafka-exporter:9308']
    metrics_path: /metrics
  - job_name: 'redpanda'
    static_configs:
      - targets: ['redpanda:8080']
    metrics_path: /metrics
  - job_name: 'prometheus'
    static_configs:
      - targets: ['prometheus:9090']
    metrics_path: /metrics
```

**Notes**:

- The `scrape_interval` is set to 15 seconds for frequent updates.
- Remove the `redpanda` job if not using Redpanda.

### 3. Start Services

Run the following command to start all services in detached mode:

```bash
docker-compose up -d
```

Check the status with:

```bash
docker-compose ps
```

### 4. Verify Services

Verify each service using the following URLs and commands:

- **Zookeeper**: `docker exec zookeeper zkCli.sh -server localhost:2181 ls /`
- **Kafka**: `docker exec kafka kafka-topics.sh --bootstrap-server localhost:29092 --list`
- **Kafdrop**: Open `http://localhost:9000` to view topics and messages
- **Redpanda**: `curl http://localhost:9644/v1/status/ready` or connect to `localhost:9093`
- **Kafka Exporter**: `curl http://localhost:9308/metrics`
- **Prometheus**: Access `http://localhost:9090` and check "Status" > "Targets"
- **Grafana**: Log in at `http://localhost:3000` (default: `admin`/`${GRAFANA_ADMIN_PASSWORD}`)

### 5. Configure Grafana

1. **Add Prometheus Data Source**:
    - Log in to Grafana (`http://localhost:3000`).
    - Go to "Configuration" > "Data Sources" > "Add data source".
    - Select "Prometheus", set URL to `http://prometheus:9090`, and save.

2. **Import Kafka Dashboard**:
    - Go to "Dashboards" > "Import".
    - Enter ID `7589` (for `danielqsj/kafka-exporter`).
    - Select the Prometheus data source and import.

3. **Optional Redpanda Dashboard**:
    - Import ID `14239` for Redpanda metrics if needed.

## Key Metrics

The `danielqsj/kafka-exporter` provides:

- `kafka_consumergroup_lag`: Consumer group lag per topic/partition.
- `kafka_brokers`: Number of active brokers.
- `kafka_topic_partitions`: Partition counts per topic.
- `kafka_topic_partition_leader`: Leader status for partitions.

**Sample Queries**:

- `kafka_consumergroup_lag{group="my-group"}`
- `kafka_brokers`
- `kafka_topic_partitions{topic="my-topic"}`

## Production Recommendations

1. **Security**:
    - Set a strong `GF_SECURITY_ADMIN_PASSWORD` in `.env`:
      ```env
      GRAFANA_ADMIN_PASSWORD=your-secure-password
      ```
    - Enable SSL/TLS for Kafka and restrict ports (`9090`, `3000`, `9308`).

2. **Persistence**:
    - Use persistent volumes (`prometheus-data`, `grafana-data`, `redpanda-data`).
    - Back up `redpanda-data` if using Redpanda.

3. **Performance**:
    - Limit metrics scope in `kafka-exporter`:
      ```yaml
      environment:
        KAFKA_SERVER: kafka:9092
        GROUP_FILTER: my-consumer-group
        TOPIC_FILTER: my-topic
      ```
    - Adjust resource limits based on hardware.

4. **JMX Metrics (Optional)**:
    - Enable JMX in `kafka`:
      ```yaml
      environment:
        KAFKA_JMX_PORT: 9999
        KAFKA_JMX_HOSTNAME: kafka
      ```
    - Add `prometheus/jmx-exporter` service:
      ```yaml
      kafka-jmx-exporter:
        image: prom/jmx-exporter:latest
        ports:
          - "9101:9101"
        volumes:
          - ./jmx_config.yml:/etc/jmx_exporter/config.yml:ro
        depends_on:
          - kafka
        networks:
          - monitoring-net
        deploy:
          resources:
            limits:
              cpus: '0.2'
              memory: 256M
      ```
    - Create `jmx_config.yml` (see [Prometheus JMX Exporter](https://github.com/prometheus/jmx_exporter)).
    - Update `prometheus.yml`:
      ```yaml
      - job_name: 'kafka-jmx'
        static_configs:
          - targets: ['kafka-jmx-exporter:9101']
        metrics_path: /metrics
      ```

5. **Remove Redpanda** (if not needed):
    - Delete `redpanda` service and volume from `docker-compose.yml`:
      ```yaml
      services:
        # Remove redpanda service
      volumes:
        # Remove redpanda-data
      ```
    - Remove `redpanda` job from `prometheus.yml`:
      ```yaml
      scrape_configs:
        # Remove redpanda job
      ```

6. **High Availability**:
    - Set `KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 3` and `KAFKA_DEFAULT_REPLICATION_FACTOR: 3`.
    - Add multiple Kafka brokers with unique `KAFKA_BROKER_ID`.

## Troubleshooting

- **Kafka Exporter Issues**:
    - Logs: `docker logs kafka-exporter`
    - Verify Kafka: `docker exec kafka-exporter kafka-topics --bootstrap-server kafka:9092 --list`
    - Metrics: `curl http://localhost:9308/metrics`

- **Prometheus Targets Down**:
    - Check `http://localhost:9090/targets`.
    - Network: `docker exec prometheus ping kafka-exporter`

- **Grafana Dashboard Empty**:
    - Verify Prometheus data source (`http://prometheus:9090`).
    - Ensure queries match `kafka-exporter` metrics.

## Screenshots

Below are screenshots demonstrating the setup, including Kafdrop interfaces, terminal outputs for consumer groups,
application logs from producers and consumers, and Prometheus targets. Each image includes a description for clarity.

![img.png](Lab-w1d1%2Freadme-image%2Fimg.png)

![img_1.png](Lab-w1d1%2Freadme-image%2Fimg_1.png)

![img_3.png](Lab-w1d1%2Freadme-image%2Fimg_3.png)

![img_2.png](Lab-w1d1%2Freadme-image%2Fimg_2.png)

![img_4.png](Lab-w1d1%2Freadme-image%2Fimg_4.png)

![img_5.png](Lab-w1d1%2Freadme-image%2Fimg_5.png)

![img_6.png](Lab-w1d1%2Freadme-image%2Fimg_6.png)

![img_8.png](Lab-w1d1%2Freadme-image%2Fimg_8.png)

![img_7.png](Lab-w1d1%2Freadme-image%2Fimg_7.png)

![img_9.png](Lab-w1d1%2Freadme-image%2Fimg_9.png)

![img_10.png](Lab-w1d1%2Freadme-image%2Fimg_10.png)

![img_11.png](Lab-w1d1%2Freadme-image%2Fimg_11.png)

## Next Steps

- Configure `GROUP_FILTER` and `TOPIC_FILTER` for specific monitoring.
- Set up JMX for detailed Kafka metrics.
- Create custom Grafana dashboards.
- Implement SSL/TLS and authentication for production.
- Contact support for advanced configurations.