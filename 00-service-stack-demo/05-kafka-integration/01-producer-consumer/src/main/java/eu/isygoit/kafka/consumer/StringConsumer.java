package eu.isygoit.kafka.consumer;

import eu.isygoit.com.event.KafkaStringConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class StringConsumer extends KafkaStringConsumer {

    @Value("${kafka.topic.string-topic}")
    public void setTopic(String topic) {
        this.topic = topic;
    }

    @Override
    protected void process(String message, Map<String, String> headers) throws Exception {
        // Process the deserialized MyData object
        log.info("Received message: {}", message);
    }
}
