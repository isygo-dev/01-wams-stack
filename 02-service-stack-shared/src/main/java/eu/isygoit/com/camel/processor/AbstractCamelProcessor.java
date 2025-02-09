package eu.isygoit.com.camel.processor;

import eu.isygoit.dto.IIdentifiableDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.ParameterizedType;
import java.util.Optional;

/**
 * Abstract base processor for Apache Camel routes.
 * <p>
 * This class provides common processing logic for handling objects of a specific type (`T`)
 * within a Camel exchange. It automatically determines the generic type at runtime
 * and provides structured logging, error handling, and transaction management.
 *
 * @param <T> the type of DTO that this processor will handle, must extend {@link IIdentifiableDto}.
 */
@Slf4j
public abstract class AbstractCamelProcessor<T extends IIdentifiableDto> implements Processor {

    public static final String ERROR_HEADER = "error";
    public static final String RETURN_HEADER = "return";
    public static final String ORIGIN = "origin";

    private final Class<T> persistentClass;

    @SuppressWarnings("unchecked")
    protected AbstractCamelProcessor() {
        this.persistentClass = (Class<T>) Optional.ofNullable(getClass().getGenericSuperclass())
                .filter(ParameterizedType.class::isInstance)
                .map(ParameterizedType.class::cast)
                .map(type -> type.getActualTypeArguments()[0])
                .filter(Class.class::isInstance)
                .map(Class.class::cast)
                .orElseThrow(() -> new IllegalStateException("Could not determine generic type"));
    }

    /**
     * Abstract method that must be implemented by subclasses.
     * This defines the actual processing logic for the given exchange and object.
     *
     * @param exchange the current Camel exchange containing headers and body.
     * @param object   the deserialized DTO object extracted from the exchange body.
     * @throws Exception if any error occurs during processing.
     */
    public abstract void performProcessor(Exchange exchange, T object) throws Exception;

    /**
     * Processes the incoming exchange.
     * <p>
     * - Extracts the DTO object from the exchange body.
     * - Calls {@link #performProcessor(Exchange, IIdentifiableDto)} to execute custom logic.
     * - Handles exceptions and logs errors appropriately.
     * - Ensures that transaction management is applied.
     *
     * @param exchange the current Camel exchange.
     */
    @Transactional
    @Override
    public void process(Exchange exchange) {
        log.info("START EXECUTING PROCESSOR: {} on object type {}",
                this.getClass().getSimpleName(), persistentClass.getSimpleName());
        try {
            exchange.getIn().setHeader(RETURN_HEADER, false);
            T object = exchange.getIn().getBody(persistentClass);
            log.info("PROCESSING... {}", object);
            performProcessor(exchange, object);
        } catch (Exception e) {
            log.error("Error processing in {}: {}", this.getClass().getSimpleName(), e.getMessage(), e);
            exchange.getIn().setHeader(ERROR_HEADER, e.getMessage());
        }
        log.info("COMPLETE EXECUTING PROCESSOR: {}", this.getClass().getSimpleName());
    }
}