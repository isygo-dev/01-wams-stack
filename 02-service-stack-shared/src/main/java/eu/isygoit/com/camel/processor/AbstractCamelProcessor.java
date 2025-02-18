package eu.isygoit.com.camel.processor;

import eu.isygoit.dto.IIdentifiableDto;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.Optional;

/**
 * Abstract base class for Camel processors that handle processing of {@link IIdentifiableDto} objects.
 *
 * @param <E> the type of the identifiable DTO
 */
@Slf4j
public abstract class AbstractCamelProcessor<I extends Serializable, E extends IIdentifiableDto> implements Processor {

    /**
     * The constant ERROR_HEADER is used to store error messages in the Camel exchange headers.
     */
    public static final String ERROR_HEADER = "error";

    /**
     * The constant RETURN_HEADER is used to store the result status in the Camel exchange headers.
     */
    public static final String RETURN_HEADER = "return";

    /**
     * The constant ORIGIN is used to store the origin information in the Camel exchange headers.
     */
    public static final String ORIGIN = "origin";

    //Attention !!! should get the class type of th persist entity
    @Getter
    private final Class<E> persistentClass = Optional.ofNullable(getClass().getGenericSuperclass())
            .filter(type -> type instanceof ParameterizedType)
            .map(type -> (ParameterizedType) type)
            .map(paramType -> paramType.getActualTypeArguments())
            .filter(args -> args.length > 1)
            .map(args -> args[1])
            .filter(Class.class::isInstance)
            .map(clazz -> (Class<E>) clazz)
            .orElseThrow(() -> new IllegalStateException("Could not determine persistent class"));

    /**
     * Perform the actual processing of the object. Must be implemented by subclasses.
     *
     * @param exchange the Camel exchange
     * @param object   the object to process
     * @throws Exception if an error occurs during processing
     */
    public abstract void performProcessor(Exchange exchange, E object) throws Exception;

    /**
     * Process the exchange, handle any exceptions, and set appropriate headers.
     *
     * @param exchange the Camel exchange
     * @throws Exception if an error occurs during processing
     */
    @Transactional
    @Override
    public void process(Exchange exchange) throws Exception {
        // Log the start of the processing
        log.info("===== PROCESSING STARTED =====");
        log.info("Processor: {} is starting to process object of type: {}", getClass().getSimpleName(), getPersistentClass().getSimpleName());

        try {
            // Set default return header to false
            exchange.getIn().setHeader(RETURN_HEADER, false);

            // Safely retrieve the object from the exchange body using Optional to avoid nulls
            var object = Optional.ofNullable(exchange.getIn().getBody())
                    .filter(getPersistentClass()::isInstance)
                    .map(getPersistentClass()::cast)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid object type in the exchange body. Expected: " + getPersistentClass().getSimpleName()));

            log.info("Processing object: {}", object);

            // Perform the actual processing logic
            performProcessor(exchange, object);

            // Log success
            log.info("Successfully processed object: {}", object);

        } catch (IllegalArgumentException e) {
            // Log invalid object type error
            log.error("Invalid object type in the exchange body. Expected: {}, but found a different type.", getPersistentClass().getSimpleName(), e);
            exchange.getIn().setHeader(ERROR_HEADER, "Invalid object type: " + e.getMessage());
        } catch (Throwable e) {
            // Log generic error and store the exception message
            log.error("Processing failed due to unexpected error: {}", e.getMessage(), e);
            exchange.getIn().setHeader(ERROR_HEADER, e.getMessage());
        }

        // Log the completion of the processing
        log.info("===== PROCESSING COMPLETED =====");
        log.info("Processor: {} finished processing object of type: {}", getClass().getSimpleName(), getPersistentClass().getSimpleName());
    }
}