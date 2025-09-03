package eu.isygoit.kafka.consumer;

import eu.isygoit.com.event.KafkaFileConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Map;

@Slf4j
@Service
public class FileConsumer extends KafkaFileConsumer {

    @Value("${kafka.topic.file-topic}")
    public void setTopic(String topic) {
        this.topic = topic;
    }

    @Override
    protected void process(InputStream message, Map<String, String> headers) throws Exception {
        // Process the deserialized MyData object
        log.info("Received message: {}", message);
    }
}
