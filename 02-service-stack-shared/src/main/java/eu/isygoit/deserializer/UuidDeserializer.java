package eu.isygoit.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * The type Uuid deserializer.
 */
@Component
public class UuidDeserializer extends JsonDeserializer<UUID> {

    @Override
    public UUID deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        String uuid = node.asText();
        return UUID.fromString(uuid);
    }
}
