package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;

/**
 * The type Kafka exception.
 */
@MsgLocale(value = "kafka.exception")
public class KafkaException extends ManagedException {

    public KafkaException(String message) {
        super(message);
    }

    public KafkaException(Throwable throwable) {
        super(throwable);
    }

    public KafkaException(String message, Throwable cause) {
        super(message, cause);
    }
}
