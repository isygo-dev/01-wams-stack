package eu.isygoit.form.domain;

import java.util.Map;

public record UiConfig(
        String theme,
        String size,
        boolean showHelpIcons,
        boolean enableAutoSave,
        Map<String, Object> customStyles
) {
    public UiConfig(String theme, String size, boolean showHelpIcons,
                    boolean enableAutoSave, Map<String, Object> customStyles) {
        this.theme = theme != null ? theme : "default";
        this.size = size != null ? size : "medium";
        this.showHelpIcons = showHelpIcons;
        this.enableAutoSave = enableAutoSave;
        this.customStyles = customStyles != null ? Map.copyOf(customStyles) : Map.of();
    }
}