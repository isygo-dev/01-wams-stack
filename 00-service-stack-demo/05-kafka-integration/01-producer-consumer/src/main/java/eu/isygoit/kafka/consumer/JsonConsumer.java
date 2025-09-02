package eu.isygoit.kafka.consumer;

import eu.isygoit.com.event.KafkaFileConsumer;
import eu.isygoit.com.event.KafkaJsonConsumer;
import eu.isygoit.kafka.dto.TutorialDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Map;

@Slf4j
@Service
public class JsonConsumer extends KafkaJsonConsumer<TutorialDto> {

    @Value("${kafka.topic.json-topic}")
    public void setTopic(String topic) {
        this.topic = topic;
    }

    @Override
    protected void process(TutorialDto message, Map<String, String> headers) throws Exception {
        // Process the deserialized MyData object
        log.info("Received message: {}", message);
    }
}
