package eu.isygoit.com.camel.repository;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * The type Abstract camel repository.
 */
@Slf4j
public abstract class AbstractCamelRepository implements ICamelRepository {

    /**
     * The constant DIRECT.
     */
    public static final String DIRECT = "direct:";
    private final ProducerTemplate template;


    /**
     * Instantiates a new Abstract camel repository.
     *
     * @param camelContext the camel context
     */
    protected AbstractCamelRepository(CamelContext camelContext) {
        this.template = camelContext.createProducerTemplate();
    }

    @Override
    public CompletableFuture<Object> asyncSendBody(String endpointUri, Object body) {
        return this.template.asyncSendBody(DIRECT + endpointUri, body);
    }

    @Override
    public CompletableFuture<Object> asyncRequestBodyAndHeader(String endpointUri, Object body, String header, Object headerValue) {
        return this.template.asyncRequestBodyAndHeader(DIRECT + endpointUri, body, header, headerValue);
    }

    @Override
    public CompletableFuture<Object> asyncRequestBodyAndHeaders(String endpointUri, Object body, Map<String, Object> headers) {
        return this.template.asyncRequestBodyAndHeaders(DIRECT + endpointUri, body, headers);
    }
}
