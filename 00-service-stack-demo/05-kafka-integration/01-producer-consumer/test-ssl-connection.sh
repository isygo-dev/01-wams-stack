#!/bin/bash

echo "Testing SSL connection to Kafka..."

# Test avec kafka-console-producer
echo "test message" | docker exec -i kafka kafka-console-producer \
    --broker-list localhost:9093 \
    --topic test-ssl-topic \
    --producer.config /etc/kafka/secrets/client-ssl.properties

# Test avec kafka-console-consumer
echo "Reading messages..."
docker exec -it kafka kafka-console-consumer \
    --bootstrap-server localhost:9093 \
    --topic test-ssl-topic \
    --from-beginning \
    --consumer.config /etc/kafka/secrets/client-ssl.properties \
    --max-messages 1
