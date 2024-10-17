package eu.isygoit.com.camel.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.transaction.annotation.Transactional;

/**
 * The type Abstract string processor.
 */
@Slf4j
public abstract class AbstractStringProcessor implements Processor {

    /**
     * The constant ERROR_HEADER.
     */
    public static final String ERROR_HEADER = "error";
    /**
     * The constant RETURN_HEADER.
     */
    public static final String RETURN_HEADER = "return";

    /**
     * Perform processor.
     *
     * @param exchange the exchange
     * @param object   the object
     * @throws Exception the exception
     */
    public abstract void performProcessor(Exchange exchange, String object) throws Exception;

    @Transactional
    @Override
    public void process(Exchange exchange) throws Exception {
        log.info("START EXECUTING PROCESSOR: {}", this.getClass().getSimpleName());
        try {
            exchange.getIn().setHeader(RETURN_HEADER, false);
            String object = (String) exchange.getIn().getBody();
            this.performProcessor(exchange, object);
        } catch (Throwable e) {
            log.error("<Error>: {} ", e);
            exchange.getIn().setHeader(ERROR_HEADER, e.getMessage());
        }
        log.info("COMPLETE EXECUTING PROCESSOR: {}", this.getClass().getSimpleName());
    }
}
