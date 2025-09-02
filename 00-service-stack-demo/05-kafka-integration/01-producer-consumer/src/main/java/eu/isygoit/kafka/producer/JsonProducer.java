package eu.isygoit.kafka.producer;

import eu.isygoit.com.event.KafkaFileProducer;
import eu.isygoit.com.event.KafkaJsonProducer;
import eu.isygoit.kafka.dto.TutorialDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JsonProducer extends KafkaJsonProducer<TutorialDto> {

    @Value("${kafka.topic.json-topic}")
    public void setTopic(String topic) {
        this.topic = topic;
    }
}
