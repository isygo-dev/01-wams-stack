package eu.isygoit.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class StringToMapConverter
        implements Converter<String, Map<String, String>> {

    private final ObjectMapper objectMapper;

    public StringToMapConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Map<String, String> convert(String source) {
        try {
            return objectMapper.readValue(
                    source,
                    new TypeReference<>() {
                    }
            );
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(
                    "Invalid JSON format for tags", e
            );
        }
    }
}