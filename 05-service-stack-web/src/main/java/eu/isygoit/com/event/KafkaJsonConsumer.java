package eu.isygoit.com.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion.VersionFlag;
import eu.isygoit.helper.JsonHelper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.lang.reflect.ParameterizedType;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Abstract Kafka consumer for processing JSON data.
 * Extends the abstract consumer to handle any type T, deserializing it from JSON.
 * Subclasses must set the topic and JSON schema path (e.g., via @Value or constructor),
 * be annotated with @Service, and implement processMessage.
 *
 * @param <T> the type parameter
 */
@Slf4j
public abstract class KafkaJsonConsumer<T> extends AbstractKafkaConsumer<T> {

    private final Class<T> producerClassType = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * The Json schema path.
     */
    @Getter
    @Setter
    protected String jsonSchemaPath; // Set by concrete classes via @Value

    @Value("${kafka.security.enable-json-validation:false}")
    private boolean enableJsonValidation;

    @Override
    protected T deserialize(byte[] data) throws Exception {
        String json = new String(data, StandardCharsets.UTF_8);
        if (enableJsonValidation && jsonSchemaPath != null && !jsonSchemaPath.isEmpty()) {
            validateJsonSchema(json);
        }
        return JsonHelper.fromJson(json, producerClassType);
    }

    private void validateJsonSchema(String json) throws Exception {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012);
        JsonSchema schema = factory.getSchema(jsonSchemaPath);
        JsonNode node = objectMapper.readTree(json);
        var errors = schema.validate(node);
        if (!errors.isEmpty()) {
            log.error("JSON schema validation failed: {}", errors);
            throw new IllegalArgumentException("JSON schema validation failed: " + errors);
        }
    }

    @Override
    protected final void processMessage(T message, Map<String, String> headers) throws Exception {
        process(message, headers);
    }

    /**
     * Process.
     *
     * @param message the message
     * @param headers the headers
     * @throws Exception the exception
     */
    protected abstract void process(T message, Map<String, String> headers) throws Exception;
}