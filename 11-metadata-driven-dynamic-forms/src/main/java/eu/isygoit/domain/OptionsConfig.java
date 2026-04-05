package eu.isygoit.domain;

import java.util.List;

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
        String optionLayout,
        boolean allowCustomValue,
        int debounceTime,
        List<OptionItem> options
) {
}