package com.isygo.labs.labw1d1;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class KafkaProducerExample {

    private static final Logger logger =
            LoggerFactory.getLogger(KafkaProducerExample.class);

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:29092");
        props.put("key.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");
        props.put("acks", "all");
        props.put("retries", 3);
        props.put("compression.type", "snappy");
        props.put("linger.ms", 5);
        try (KafkaProducer<String, String> producer = new
                KafkaProducer<>(props)) {
            for (int i = 1; i <= 10; i++) {
                ProducerRecord<String, String> record = new
                        ProducerRecord<>("orders", "order" + i, "Order data " + i);
                int finalI = i;
                producer.send(record, (metadata, exception) -> {
                    if (exception != null) {
                        logger.error("Error sending message: {}",
                                exception.getMessage());
                    } else {
                        logger.info("Sent: key=order{}, partition={}, offset={}",
                                finalI, metadata.partition(), metadata.offset());
                    }
                });
            }
            producer.flush();
        } catch (Exception e) {
            logger.error("Producer error: {}", e.getMessage());
        }
    }
}
