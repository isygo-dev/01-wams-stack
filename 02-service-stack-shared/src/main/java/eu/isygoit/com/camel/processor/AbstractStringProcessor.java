package eu.isygoit.com.camel.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.transaction.annotation.Transactional;

/**
 * Abstract base processor for handling String-based exchanges in Apache Camel.
 *
 * This class provides a standardized structure for processing String-based payloads
 * within a Camel route. It includes transaction management, structured logging,
 * and error handling.
 */
@Slf4j
public abstract class AbstractStringProcessor implements Processor {

    /**
     * Header key for storing error messages in the Camel exchange.
     */
    public static final String ERROR_HEADER = "error";

    /**
     * Header key to indicate whether processing was successful.
     */
    public static final String RETURN_HEADER = "return";

    /**
     * Abstract method that must be implemented by subclasses.
     * This defines the actual processing logic for the given exchange and object.
     *
     * @param exchange the current Camel exchange containing headers and body.
     * @param object   the string extracted from the exchange body.
     * @throws Exception if any error occurs during processing.
     */
    public abstract void performProcessor(Exchange exchange, String object) throws Exception;

    /**
     * Processes the incoming exchange.
     *
     * - Extracts the String object from the exchange body.
     * - Calls {@link #performProcessor(Exchange, String)} to execute custom logic.
     * - Handles exceptions and logs errors appropriately.
     * - Ensures that transaction management is applied.
     *
     * @param exchange the current Camel exchange.
     */
    @Transactional
    @Override
    public void process(Exchange exchange) {
        log.info("START EXECUTING PROCESSOR: {}", this.getClass().getSimpleName());
        try {
            // Set return header to false by default (can be overridden by performProcessor)
            exchange.getIn().setHeader(RETURN_HEADER, false);

            // Extract the string payload from the exchange body
            String object = exchange.getIn().getBody(String.class);

            // Process the object using the abstract method implemented by subclasses
            performProcessor(exchange, object);
        } catch (Exception e) {
            // Log and store error details in the exchange headers
            log.error("Error processing in {}: {}", this.getClass().getSimpleName(), e.getMessage(), e);
            exchange.getIn().setHeader(ERROR_HEADER, e.getMessage());
        }
        log.info("COMPLETE EXECUTING PROCESSOR: {}", this.getClass().getSimpleName());
    }
}