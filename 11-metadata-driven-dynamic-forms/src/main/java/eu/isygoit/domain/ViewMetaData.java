package eu.isygoit.domain;

import java.util.List;
import java.util.Map;

public record ViewMetaData(
        String name,
        String title,
        String description,
        String version,
        List<FieldMetaData> fields,
        Map<String, Object> uiConfig,
        Map<String, Object> actions
) {

    public ViewMetaData(String name, String title, String description, String version,
                        List<FieldMetaData> fields, Map<String, Object> uiConfig,
                        Map<String, Object> actions) {
        this.name = name;
        this.title = title != null ? title : "";
        this.description = description != null ? description : "";
        this.version = version != null ? version : "1.0";
        this.fields = fields != null ? List.copyOf(fields) : List.of();
        this.uiConfig = uiConfig != null ? Map.copyOf(uiConfig) : Map.of();
        this.actions = actions != null ? Map.copyOf(actions) : Map.of();
    }
}