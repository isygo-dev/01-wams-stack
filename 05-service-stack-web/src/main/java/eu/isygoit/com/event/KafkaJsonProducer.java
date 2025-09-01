package eu.isygoit.com.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import eu.isygoit.helper.JsonHelper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.nio.charset.StandardCharsets;

/**
 * Abstract Kafka producer for sending JSON data.
 * Extends the abstract producer to handle any type T, serializing it to JSON.
 * Subclasses must set the topic and JSON schema path (e.g., via @Value or constructor) and be annotated with @Service.
 *
 * @param <T> the type parameter
 */
@Slf4j
public abstract class KafkaJsonProducer<T> extends AbstractKafkaProducer<T> {

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
    protected byte[] serialize(T message) throws Exception {
        if (message == null) {
            throw new IllegalArgumentException("Cannot serialize null message");
        }
        if (enableJsonValidation && jsonSchemaPath != null && !jsonSchemaPath.isEmpty()) {
            validateJsonSchema(message);
        }
        String json = JsonHelper.toJson(message); // Use ObjectMapper for consistency
        return json.getBytes(StandardCharsets.UTF_8);
    }

    private void validateJsonSchema(T message) throws Exception {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012); // Specify Draft 2020-12
        JsonSchema schema = factory.getSchema(jsonSchemaPath); // Path or JSON string
        JsonNode node = objectMapper.valueToTree(message);
        var errors = schema.validate(node);
        if (!errors.isEmpty()) {
            log.error("JSON schema validation failed for message: {}", errors);
            throw new IllegalArgumentException("JSON schema validation failed: " + errors);
        }
    }
}