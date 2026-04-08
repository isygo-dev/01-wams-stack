package eu.isygoit.form.domain;

import java.util.Map;

public record ConditionalRule(
        String visibleWhen,
        String enabledWhen,
        String requiredWhen,
        Map<String, Object> customConditions
) {

    // Full constructor with defensive copy
    public ConditionalRule(String visibleWhen, String enabledWhen,
                           String requiredWhen, Map<String, Object> customConditions) {
        this.visibleWhen = visibleWhen != null ? visibleWhen : "";
        this.enabledWhen = enabledWhen != null ? enabledWhen : "";
        this.requiredWhen = requiredWhen != null ? requiredWhen : "";
        this.customConditions = customConditions != null ? Map.copyOf(customConditions) : Map.of();
    }
}