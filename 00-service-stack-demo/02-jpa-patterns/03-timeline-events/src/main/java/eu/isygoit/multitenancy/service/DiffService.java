package eu.isygoit.multitenancy.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

@Service
public class DiffService {
    private final ObjectMapper objectMapper;

    @Autowired
    public DiffService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ObjectNode computeDiff(Object previousState, Object currentState) {
        try {
            Map<String, Object> prevMap = objectMapper.convertValue(previousState, Map.class);
            Map<String, Object> currMap = objectMapper.convertValue(currentState, Map.class);

            ObjectNode diff = objectMapper.createObjectNode();
            currMap.forEach((key, value) -> {
                Object prevValue = prevMap.get(key);
                if (!Objects.equals(value, prevValue) && !key.equals("id") && !key.equals("tenant") && !key.equals("version")) {
                    ObjectNode change = objectMapper.createObjectNode();
                    change.putPOJO("old", prevValue);
                    change.putPOJO("new", value);
                    diff.set(key, change);
                }
            });

            return diff;
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute diff", e);
        }
    }
}