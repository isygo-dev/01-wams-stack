package eu.isygoit.domain;

import java.util.List;
import java.util.Map;

public record OptionsConfig(
        boolean multiple,
        boolean searchable,
        boolean clearable,
        boolean showSelectAll,
        int maxSelectable,
        String optionsSource,
        String dependsOn,
        String valueKey,
        String labelKey,
        List<OptionItem> options,
        String optionLayout,
        Map<String, Object> autocompleteConfig
) {

    public OptionsConfig(boolean multiple, boolean searchable, boolean clearable,
                         boolean showSelectAll, int maxSelectable, String optionsSource,
                         String dependsOn, String valueKey, String labelKey,
                         List<OptionItem> options, String optionLayout,
                         Map<String, Object> autocompleteConfig) {
        this.multiple = multiple;
        this.searchable = searchable;
        this.clearable = clearable;
        this.showSelectAll = showSelectAll;
        this.maxSelectable = Math.max(0, maxSelectable);
        this.optionsSource = optionsSource != null ? optionsSource : "static";
        this.dependsOn = dependsOn != null ? dependsOn : "";
        this.valueKey = valueKey != null ? valueKey : "value";
        this.labelKey = labelKey != null ? labelKey : "label";
        this.options = options != null ? List.copyOf(options) : List.of();
        this.optionLayout = optionLayout != null ? optionLayout : "dropdown";
        this.autocompleteConfig = autocompleteConfig != null ? Map.copyOf(autocompleteConfig) : Map.of();
    }
}