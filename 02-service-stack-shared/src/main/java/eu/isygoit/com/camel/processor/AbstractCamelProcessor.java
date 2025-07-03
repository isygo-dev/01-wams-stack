package eu.isygoit.com.camel.processor;

import eu.isygoit.dto.IIdAssignableDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.ParameterizedType;

/**
 * The type Abstract camel processor.
 *
 * @param <T> the type parameter
 */
@Slf4j
public abstract class AbstractCamelProcessor<T extends IIdAssignableDto> implements Processor {

    /**
     * The constant ERROR_HEADER.
     */
    public static final String ERROR_HEADER = "error";
    /**
     * The constant RETURN_HEADER.
     */
    public static final String RETURN_HEADER = "return";
    /**
     * The constant ORIGIN.
     */
    public static final String ORIGIN = "origin";

    private final Class<T> persistentClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];

    /**
     * Perform processor.
     *
     * @param exchange the exchange
     * @param object   the object
     * @throws Exception the exception
     */
    public abstract void performProcessor(Exchange exchange, T object) throws Exception;

    @Transactional
    @Override
    public void process(Exchange exchange) throws Exception {
        log.info("START EXECUTING PROCESSOR: {} on object {}", this.getClass().getSimpleName(), persistentClass.getSimpleName());
        try {
            exchange.getIn().setHeader(RETURN_HEADER, false);
            T object = (T) exchange.getIn().getBody();
            log.info("PROCESSING... {}", object.toString());
            this.performProcessor(exchange, object);
        } catch (Throwable e) {
            log.error("<Error>: failed processing", e);
            exchange.getIn().setHeader(ERROR_HEADER, e.getMessage());
        }
        log.info("COMPLETE EXECUTING PROCESSOR: {}", this.getClass().getSimpleName());
    }
}
