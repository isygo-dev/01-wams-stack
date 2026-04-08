package eu.isygoit.form.domain;

import java.util.Map;

public record ValidationRule(
        String type,
        Object value,
        String message,
        Map<String, Object> parameters
) {

    public ValidationRule(String type, Object value, String message, Map<String, Object> parameters) {
        this.type = type != null ? type : "";
        this.value = value;
        this.message = message != null ? message : "";
        this.parameters = parameters != null ? Map.copyOf(parameters) : Map.of();
    }
}