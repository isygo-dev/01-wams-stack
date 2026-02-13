package com.isygo.labs.labw1d1;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class KafkaConsumerExample {
    private static final Logger logger =
            LoggerFactory.getLogger(KafkaConsumerExample.class);

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:29092");
        props.put("group.id", "order-consumer-group");
        props.put("key.deserializer",
                "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer",
                "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("auto.offset.reset", "earliest");
        props.put("enable.auto.commit", "true");
        props.put("max.poll.records", 100);
        try (KafkaConsumer<String, String> consumer = new
                KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList("orders"));
            while (true) {
                ConsumerRecords<String, String> records =
                        consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    logger.info("Received: key={}, value={}, partition={}, offset={}",
                            record.key(), record.value(), record.partition(),
                            record.offset());
                }
            }
        } catch (Exception e) {
            logger.error("Consumer error: {}", e.getMessage());
        }
    }
}
