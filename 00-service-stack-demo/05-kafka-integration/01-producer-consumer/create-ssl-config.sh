# 1. client-ssl.properties (à placer dans ./certs/)
cat > ./certs/client-ssl.properties << 'EOF'
security.protocol=SSL
ssl.truststore.location=/etc/kafka/secrets/client.truststore.jks
ssl.truststore.password=changeit
ssl.keystore.location=/etc/kafka/secrets/client.keystore.jks
ssl.keystore.password=changeit
ssl.key.password=changeit
ssl.endpoint.identification.algorithm=
EOF

# 2. kafka_server_jaas.conf (à placer dans ./certs/)
cat > ./certs/kafka_server_jaas.conf << 'EOF'
KafkaServer {
    org.apache.kafka.common.security.plain.PlainLoginModule required
    username="admin"
    password="admin-secret"
    user_admin="admin-secret"
    user_alice="alice-secret";
};

Client {
    org.apache.kafka.common.security.plain.PlainLoginModule required
    username="alice"
    password="alice-secret";
};
EOF

# 3. Script de test SSL
cat > ./test-ssl-connection.sh << 'EOF'
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
EOF

chmod +x ./test-ssl-connection.sh