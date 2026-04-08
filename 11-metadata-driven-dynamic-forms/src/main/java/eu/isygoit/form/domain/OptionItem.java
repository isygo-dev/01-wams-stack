package eu.isygoit.form.domain;

import java.util.Map;

public record OptionItem(
        Object value,
        String label,
        String description,
        boolean disabled,
        String group,
        String icon,
        String color,
        int order,
        Map<String, Object> extraData
) {

    public OptionItem(Object value, String label, String description, boolean disabled,
                      String group, String icon, String color, int order,
                      Map<String, Object> extraData) {
        this.value = value;
        this.label = label != null ? label : "";
        this.description = description != null ? description : "";
        this.disabled = disabled;
        this.group = group != null ? group : "";
        this.icon = icon != null ? icon : "";
        this.color = color != null ? color : "";
        this.order = order;
        this.extraData = extraData != null ? Map.copyOf(extraData) : Map.of();
    }
}