How to Verify Services: 
    * Zookeeper: http://localhost:2181
    * Kafka: Connect a client to localhost:29092
    * Kafdrop: http://localhost:9000
    * Redpanda: Connect a client to localhost:9093, or check admin API at http://localhost:9644/v1/status/ready
    * Prometheus: http://localhost:9090
    * Grafana: http://localhost:3000 (login with admin/admin or your custom password)


![img.png](img.png)

![img_1.png](img_1.png)

![img_3.png](img_3.png)

![img_2.png](img_2.png)

![img_4.png](img_4.png)

![img_5.png](img_5.png)

![img_6.png](img_6.png)

![img_8.png](img_8.png)

![img_7.png](img_7.png)

Kafka monitoring : 
To configure a Kafka exporter for Prometheus to scrape metrics from the Kafka service in your `docker-compose.yml`, you need to add a Kafka exporter service that exposes Kafka metrics in a Prometheus-compatible format. The `wurstmeister/kafka` image doesn’t natively expose Prometheus metrics, so you’ll use a Kafka exporter (e.g., `danielqsj/kafka-exporter`) to connect to Kafka and translate its JMX metrics or other data into Prometheus metrics.

Below, I’ll provide step-by-step instructions to add and configure a Kafka exporter in your existing `docker-compose.yml`, assuming you want to monitor the Kafka service defined in the file. I’ll also include a sample `prometheus.yml` configuration to scrape metrics from the Kafka exporter and Redpanda.

### Steps to Configure Kafka Exporter

1. **Choose a Kafka Exporter**:
    - The most commonly used Kafka exporter for Prometheus is `danielqsj/kafka-exporter` (available on Docker Hub). It connects to Kafka brokers and exposes metrics like topic/partition lag, consumer group offsets, and broker health.
    - Alternative: If you need JMX metrics (e.g., JVM, broker internals), you can use `prometheus/jmx-exporter`, but it requires configuring Kafka with JMX enabled, which is more complex. For simplicity, I’ll use `danielqsj/kafka-exporter`.

2. **Update `docker-compose.yml`**:
    - Add a `kafka-exporter` service to your `docker-compose.yml` that connects to the `kafka` service and exposes metrics on a port (e.g., `9308`).
    - Ensure the exporter is part of the same network (`monitoring-net`) for communication with Kafka and Prometheus.

3. **Update Prometheus Configuration**:
    - Modify `prometheus.yml` to scrape metrics from the Kafka exporter and Redpanda.

4. **Verify Metrics**:
    - Check that Prometheus and Grafana can access the Kafka exporter metrics.

### Updated `docker-compose.yml`
Below is the updated `docker-compose.yml` with the Kafka exporter service added. I’ve included the previous fixes (e.g., Redpanda image, volumes) and added the Kafka exporter.

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

  kafka:
    image: wurstmeister/kafka:latest
    ports:
      - "9092:9092"   # internal docker-to-docker
      - "29092:29092" # external host access
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
    networks:
      - monitoring-net

  kafdrop:
    image: obsidiandynamics/kafdrop:latest
    container_name: kafdrop
    ports:
      - "9000:9000"
    environment:
      KAFKA_BROKERCONNECT: kafka:9092
      JVM_OPTS: "-Xms32M -Xmx64M"
    depends_on:
      - kafka
    networks:
      - monitoring-net

  redpanda:
    image: redpandadata/redpanda:v24.2.3
    container_name: redpanda
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
      - "9093:9093"       # Kafka client port
      - "9644:9644"       # Redpanda admin API
      - "8080:8080"       # Metrics HTTP endpoint for Prometheus
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:9644/v1/status/ready"]
      interval: 10s
      retries: 5
    volumes:
      - redpanda-data:/var/lib/redpanda/data
    networks:
      - monitoring-net

  kafka-exporter:
    image: danielqsj/kafka-exporter:latest
    container_name: kafka-exporter
    ports:
      - "9308:9308"  # Expose Kafka exporter metrics
    environment:
      - KAFKA_SERVER=kafka:9092
    depends_on:
      - kafka
    networks:
      - monitoring-net

  prometheus:
    image: prom/prometheus:latest
    container_name: prometheus
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml:ro
    ports:
      - "9090:9090"
    depends_on:
      - redpanda
      - kafka-exporter
    networks:
      - monitoring-net

  grafana:
    image: grafana/grafana:latest
    container_name: grafana
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=${GRAFANA_ADMIN_PASSWORD:-admin}
    ports:
      - "3000:3000"
    depends_on:
      - prometheus
    networks:
      - monitoring-net

volumes:
  redpanda-data:
    name: redpanda-data

networks:
  monitoring-net:
    driver: bridge
```

### Changes Made
1. **Added Kafka Exporter Service**:
    - Image: `danielqsj/kafka-exporter:latest`
    - Port: `9308` (default for Kafka exporter)
    - Environment: `KAFKA_SERVER=kafka:9092` to connect to the Kafka broker
    - Depends on: `kafka` to ensure Kafka is running
    - Network: Added to `monitoring-net` for communication with Kafka and Prometheus

2. **Updated Prometheus Dependencies**:
    - Added `kafka-exporter` to `depends_on` in the `prometheus` service to ensure it starts after the exporter.

### Configure `prometheus.yml`
Create or update the `prometheus.yml` file in the same directory as your `docker-compose.yml` to scrape metrics from both Redpanda and the Kafka exporter. Here’s an example configuration:

```yaml
global:
  scrape_interval: 15s
scrape_configs:
  - job_name: 'redpanda'
    static_configs:
      - targets: ['redpanda:8080']  # Redpanda metrics endpoint
  - job_name: 'kafka-exporter'
    static_configs:
      - targets: ['kafka-exporter:9308']  # Kafka exporter metrics endpoint
```

### Explanation of Kafka Exporter
- **Purpose**: The `danielqsj/kafka-exporter` exposes metrics such as:
    - Consumer group lag (`kafka_consumergroup_lag`)
    - Topic partition offsets
    - Broker status and counts
    - Message rates
- **Port**: By default, it exposes metrics on port `9308` at the `/metrics` endpoint (e.g., `http://localhost:9308/metrics`).
- **Environment Variables**:
    - `KAFKA_SERVER`: Specifies the Kafka broker(s) to connect to (e.g., `kafka:9092`).
    - Optional: You can add flags like `--group.filter` to monitor specific consumer groups or `--topic.filter` for specific topics (e.g., `--group.filter=my-group`).

### Steps to Apply and Verify
1. **Save the Updated Files**:
    - Replace your `docker-compose.yml` with the version above.
    - Save the `prometheus.yml` file in the same directory.

2. **Start the Services**:
   ```bash
   docker-compose up -d
   ```

3. **Verify Kafka Exporter**:
    - Check that the Kafka exporter is running:
      ```bash
      docker ps --filter name=kafka-exporter
      ```
    - Access the metrics endpoint:
      ```bash
      curl http://localhost:9308/metrics
      ```
      You should see Prometheus metrics like `kafka_brokers`, `kafka_topic_partitions`, etc.

4. **Verify Prometheus**:
    - Open Prometheus at `http://localhost:9090`.
    - Go to the “Status” > “Targets” page to confirm that the `kafka-exporter` (`kafka-exporter:9308`) and `redpanda` (`redpanda:8080`) targets are UP.
    - In the Prometheus query interface, try queries like:
        - `kafka_consumergroup_lag` to check consumer group lag
        - `kafka_brokers` to verify the number of brokers

5. **Set Up Grafana**:
    - Open Grafana at `http://localhost:3000` (login with `admin`/`admin` or your custom password).
    - Add Prometheus as a data source:
        - URL: `http://prometheus:9090`
    - Import a Kafka dashboard (e.g., Grafana dashboard ID `7589` for `danielqsj/kafka-exporter`):
        - Go to “Dashboards” > “Import” in Grafana.
        - Enter ID `7589` or search for “Kafka Exporter” dashboards.
        - Select the Prometheus data source and import.
    - Verify that Kafka metrics (e.g., lag, broker status) are displayed.

### Additional Configuration Options
- **Consumer Group Monitoring**:
  If you want to monitor specific consumer groups, add to the `kafka-exporter` service in `docker-compose.yml`:
  ```yaml
  environment:
    - KAFKA_SERVER=kafka:9092
    - GROUP_FILTER=my-consumer-group
  ```
  Replace `my-consumer-group` with your group name or a regex pattern.

- **Topic Monitoring**:
  To monitor specific topics:
  ```yaml
  environment:
    - KAFKA_SERVER=kafka:9092
    - TOPIC_FILTER=my-topic
  ```

- **JMX Exporter (Alternative)**:
  If you need detailed JVM or Kafka internal metrics:
    - Enable JMX in the `kafka` service by adding environment variables:
      ```yaml
      environment:
        - KAFKA_JMX_PORT=9999
        - KAFKA_JMX_HOSTNAME=localhost
      ```
    - Add a `prometheus/jmx-exporter` container, mounting a JMX configuration file.
    - Update `prometheus.yml` to scrape JMX metrics.
      Let me know if you need this setup, and I can provide a detailed configuration.

- **Resource Limits**:
  To prevent the exporter from consuming excessive resources, add limits:
  ```yaml
  kafka-exporter:
    ...
    deploy:
      resources:
        limits:
          cpus: '0.2'
          memory: 256M
  ```

### Troubleshooting
- **Kafka Exporter Fails to Start**:
    - Check logs: `docker logs kafka-exporter`
    - Ensure the Kafka broker (`kafka:9092`) is running and accessible:
      ```bash
      docker exec kafka-exporter kafka-topics --bootstrap-server kafka:9092 --list
      ```

- **No Metrics in Prometheus**:
    - Verify the `prometheus.yml` targets are correct.
    - Ensure the `kafka-exporter` container is running and accessible at `http://localhost:9308/metrics`.
    - Check Prometheus logs: `docker logs prometheus`

- **Grafana Dashboard Empty**:
    - Confirm the Prometheus data source is correctly configured in Grafana.
    - Ensure the dashboard queries match the metrics exposed by `kafka-exporter`.

### Sample Prometheus Queries
- Consumer group lag: `kafka_consumergroup_lag{group="my-group"}`
- Number of brokers: `kafka_brokers`
- Topic partition count: `kafka_topic_partitions{topic="my-topic"}`

### Notes
- **Kafka vs. Redpanda**: The setup assumes you’re monitoring both Kafka and Redpanda. If you only need Kafka, you can remove the `redpanda` service to simplify.
- **Security**: For production, secure the Kafka exporter port (`9308`) and consider enabling authentication for Kafka if not already done.
- **Performance**: The `danielqsj/kafka-exporter` is lightweight but may add overhead if monitoring many topics or groups. Adjust `GROUP_FILTER` and `TOPIC_FILTER` to limit scope.

If you need help with specific Kafka metrics, JMX exporter setup, or Grafana dashboard configuration, let me know!

The provided `docker-compose-monitoring.yml` file is mostly well-configured for monitoring Kafka with Prometheus and Grafana, as it includes a Kafka exporter (`danielqsj/kafka-exporter`), Prometheus, and Grafana, along with Kafka, Zookeeper, Redpanda, and Kafdrop. However, there are a few areas that need verification and potential improvements to ensure robust monitoring of Kafka with Prometheus and Grafana. Below, I’ll review the file, identify any issues, and provide guidance to ensure Kafka metrics are correctly collected and visualized in Grafana.

### Review of the `docker-compose-monitoring.yml`

#### Strengths
1. **Kafka Exporter**: The `kafka-exporter` service is correctly set up to connect to the Kafka broker (`kafka:9092`) and expose metrics on port `9308`, which is ideal for Prometheus scraping.
2. **Prometheus Configuration**: The `prometheus` service mounts a `prometheus.yml` file, which presumably includes scrape configurations for the Kafka exporter and Redpanda.
3. **Grafana Integration**: Grafana is included and depends on Prometheus, making it ready to visualize metrics.
4. **Networking**: All services are on the `monitoring-net` network, ensuring they can communicate.
5. **Kafka Configuration**: The Kafka service is properly configured with listeners for internal and external access.
6. **Redpanda Data Persistence**: The `redpanda` service includes a persistent volume to avoid data loss.
7. **Dependencies**: The `depends_on` directives ensure services start in the correct order (e.g., `kafka` before `kafka-exporter`, `prometheus` before `grafana`).

#### Potential Issues and Improvements
1. **Prometheus Configuration File**:
    - The `prometheus.yml` file is referenced but not provided in the document. If it’s missing or incorrectly configured, Prometheus won’t scrape Kafka metrics.
    - **Solution**: Verify or provide a `prometheus.yml` file with scrape configurations for `kafka-exporter` and optionally Redpanda.

2. **Kafka Exporter Scope**:
    - The `kafka-exporter` service doesn’t specify filters for consumer groups or topics. This could lead to excessive metrics if you have many topics or groups, potentially impacting performance.
    - **Solution**: Optionally add `GROUP_FILTER` or `TOPIC_FILTER` environment variables to limit the scope.

3. **Kafka JMX Metrics**:
    - The `wurstmeister/kafka` image doesn’t enable JMX by default, so metrics like JVM memory, thread counts, or detailed broker internals won’t be available unless explicitly configured.
    - **Solution**: Add JMX configuration to Kafka and a JMX exporter if needed (optional, as `danielqsj/kafka-exporter` covers most use cases).

4. **Grafana Setup**:
    - The `grafana` service uses a default admin password (`admin`), which is insecure for production. Additionally, there’s no mention of pre-configured dashboards or data sources.
    - **Solution**: Provide guidance to set up a Prometheus data source and import a Kafka dashboard in Grafana.

5. **Resource Limits**:
    - No resource limits are defined, which could lead to resource contention in a production environment.
    - **Solution**: Add CPU and memory limits for critical services.

6. **Redpanda Monitoring**:
    - The setup includes Redpanda, which may or may not be intended for monitoring alongside Kafka. If you only want to monitor Kafka, Redpanda could be removed to simplify the setup.
    - **Solution**: Clarify whether Redpanda is needed; if not, remove it.

7. **Healthchecks**:
    - The `kafka` service lacks a healthcheck, which could help ensure it’s running correctly before dependent services (e.g., `kafka-exporter`, `kafdrop`) start.
    - **Solution**: Add a healthcheck for Kafka.

### Assumptions
- You want to monitor Kafka (not Redpanda) with Prometheus and Grafana, but I’ll keep Redpanda in the configuration unless you specify otherwise.
- You have a `prometheus.yml` file, but I’ll provide a sample to ensure Kafka metrics are scraped.
- This is a development or testing setup, so I’ll keep configurations simple but include recommendations for production.

### Updated `docker-compose-monitoring.yml`
Below is the updated `docker-compose.yml` with improvements for monitoring Kafka, including a healthcheck for Kafka and resource limits for key services. I’ve kept Redpanda and other services as they are, assuming they’re part of your setup.

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
      - "9092:9092"   # internal docker-to-docker
      - "29092:29092" # external host access
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
    container_name: kafdrop
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
    container_name: redpanda
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
      - "9093:9093"       # Kafka client port
      - "9644:9644"       # Redpanda admin API
      - "8080:8080"       # Metrics HTTP endpoint for Prometheus
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
    container_name: kafka-exporter
    ports:
      - "9308:9308"  # Expose Kafka exporter metrics
    environment:
      - KAFKA_SERVER=kafka:9092
      # Optional: Uncomment to filter specific consumer groups or topics
      # - GROUP_FILTER=my-consumer-group
      # - TOPIC_FILTER=my-topic
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
    container_name: prometheus
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml:ro
    ports:
      - "9090:9090"
    depends_on:
      - redpanda
      - kafka-exporter
    networks:
      - monitoring-net
    deploy:
      resources:
        limits:
          cpus: '0.5'
          memory: 512M

  grafana:
    image: grafana/grafana:latest
    container_name: grafana
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=${GRAFANA_ADMIN_PASSWORD:-admin}
      - GF_SECURITY_ADMIN_USER=admin
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
    name: redpanda-data

networks:
  monitoring-net:
    driver: bridge
```

### Changes Made
1. **Added Healthcheck for Kafka**:
    - Added a healthcheck to the `kafka` service using `kafka-topics.sh` to verify the broker is responsive.

2. **Added Resource Limits**:
    - Added `deploy.resources.limits` for all services to prevent resource contention (e.g., CPU and memory limits for `kafka`, `redpanda`, `kafka-exporter`, etc.).

3. **Kafka Exporter Healthcheck**:
    - Added a healthcheck for `kafka-exporter` to ensure the metrics endpoint is accessible.

4. **Grafana Admin User**:
    - Explicitly set `GF_SECURITY_ADMIN_USER=admin` for clarity, though it’s the default.

5. **Commented Optional Kafka Exporter Filters**:
    - Added commented `GROUP_FILTER` and `TOPIC_FILTER` environment variables for `kafka-exporter` to allow easy customization.

### Prometheus Configuration (`prometheus.yml`)
To ensure Prometheus scrapes Kafka metrics, create or verify a `prometheus.yml` file in the same directory as your `docker-compose.yml`. Here’s a sample configuration that includes both Kafka and Redpanda (if you’re using both):

```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s
scrape_configs:
  - job_name: 'kafka-exporter'
    static_configs:
      - targets: ['kafka-exporter:9308']  # Kafka exporter metrics
  - job_name: 'redpanda'
    static_configs:
      - targets: ['redpanda:8080']  # Redpanda metrics (optional)
  - job_name: 'prometheus'
    static_configs:
      - targets: ['prometheus:9090']  # Prometheus self-monitoring
```

### Steps to Set Up Monitoring in Grafana
1. **Start the Services**:
   ```bash
   docker-compose -f docker-compose-monitoring.yml up -d
   ```

2. **Verify Kafka Exporter**:
    - Check that the Kafka exporter is running:
      ```bash
      docker ps --filter name=kafka-exporter
      ```
    - Access the metrics endpoint:
      ```bash
      curl http://localhost:9308/metrics
      ```
      Look for metrics like `kafka_consumergroup_lag`, `kafka_brokers`, or `kafka_topic_partitions`.

3. **Verify Prometheus**:
    - Open Prometheus at `http://localhost:9090`.
    - Go to “Status” > “Targets” to confirm that `kafka-exporter:9308` (and `redpanda:8080` if applicable) are UP.
    - Run queries in the Prometheus UI, e.g.:
        - `kafka_consumergroup_lag` (consumer group lag)
        - `kafka_brokers` (number of brokers)
        - `kafka_topic_partition_leader` (partition leader status)

4. **Configure Grafana**:
    - Open Grafana at `http://localhost:3000` (login with `admin`/`${GRAFANA_ADMIN_PASSWORD:-admin}`).
    - Add Prometheus as a data source:
        - Go to “Configuration” > “Data Sources” > “Add data source”.
        - Select “Prometheus”.
        - Set URL to `http://prometheus:9090`.
        - Save and test.
    - Import a Kafka dashboard:
        - Go to “Dashboards” > “Import”.
        - Use Grafana dashboard ID `7589` (for `danielqsj/kafka-exporter`) or search for “Kafka Exporter” dashboards.
        - Select the Prometheus data source and import.
        - Verify metrics like consumer group lag, topic offsets, and broker status are displayed.
    - Optionally, import a Redpanda dashboard (e.g., ID `14239`) if monitoring Redpanda.

### Additional Recommendations
1. **Secure Grafana**:
    - For production, set a strong `GF_SECURITY_ADMIN_PASSWORD` in a `.env` file:
      ```env
      GRAFANA_ADMIN_PASSWORD=your-secure-password
      ```
    - Avoid using the default `admin` password.

2. **Filter Kafka Exporter Metrics**:
    - If you have specific consumer groups or topics to monitor, uncomment and configure `GROUP_FILTER` and `TOPIC_FILTER` in the `kafka-exporter` service:
      ```yaml
      environment:
        - KAFKA_SERVER=kafka:9092
        - GROUP_FILTER=my-consumer-group
        - TOPIC_FILTER=my-topic
      ```

3. **JMX Metrics (Optional)**:
    - If you need detailed Kafka internals (e.g., JVM memory, request latencies):
        - Enable JMX in the `kafka` service:
          ```yaml
          environment:
            ...
            KAFKA_JMX_PORT: 9999
            KAFKA_JMX_HOSTNAME: kafka
          ```
        - Add a `prometheus/jmx-exporter` service:
          ```yaml
          kafka-jmx-exporter:
            image: prom/jmx-exporter:latest
            container_name: kafka-jmx-exporter
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
        - Create a `jmx_config.yml` file (example available from Prometheus JMX Exporter documentation).
        - Update `prometheus.yml` to scrape `kafka-jmx-exporter:9101`.
          Let me know if you need this setup.

4. **Remove Redpanda (if not needed)**:
    - If you only want to monitor Kafka, remove the `redpanda` service and its volume definition:
      ```yaml
      services:
        # Remove redpanda service
      volumes:
        # Remove redpanda-data
      ```
    - Update `prometheus.yml` to remove the `redpanda` job.

5. **Production Considerations**:
    - Enable persistent storage for Prometheus and Grafana to retain metrics and dashboards:
      ```yaml
      prometheus:
        ...
        volumes:
          - ./prometheus.yml:/etc/prometheus/prometheus.yml:ro
          - prometheus-data:/prometheus
      grafana:
        ...
        volumes:
          - grafana-data:/var/lib/grafana
      volumes:
        ...
        prometheus-data:
        grafana-data:
      ```
    - Secure Kafka with SSL/TLS and authentication.
    - Restrict exposed ports (e.g., limit `9090`, `3000`, `9308` to specific hosts).

### Troubleshooting
- **Kafka Exporter Metrics Missing**:
    - Check logs: `docker logs kafka-exporter`
    - Ensure Kafka is running: `docker exec kafka kafka-topics.sh --bootstrap-server localhost:9092 --list`
    - Verify the metrics endpoint: `curl http://localhost:9308/metrics`

- **Prometheus Targets Down**:
    - Check `http://localhost:9090/targets` to see if `kafka-exporter:9308` is reachable.
    - Verify network connectivity: `docker exec prometheus ping kafka-exporter`

- **Grafana Dashboard Empty**:
    - Ensure the Prometheus data source is correctly configured.
    - Verify the dashboard queries match metrics from `kafka-exporter` (e.g., `kafka_consumergroup_lag`).

### Sample Grafana Dashboard
For `danielqsj/kafka-exporter`, import Grafana dashboard ID `7589`. It includes panels for:
- Consumer group lag
- Topic partition offsets
- Broker status
- Message rates

If you need a custom dashboard or specific metrics (e.g., for a particular topic or consumer group), let me know, and I can provide a tailored configuration.

### Verification
After starting the services:
1. Check Kafka metrics: `http://localhost:9308/metrics`
2. Check Prometheus targets: `http://localhost:9090/targets`
3. Verify Grafana dashboard: `http://localhost:3000` with imported dashboard ID `7589`.

If you confirm whether Redpanda is needed, need a JMX exporter setup, or have specific topics/consumer groups to monitor, I can further refine the configuration. Let me know if you encounter any issues or need additional guidance!

![img_9.png](img_9.png)

![img_10.png](img_10.png)

![img_11.png](img_11.png)