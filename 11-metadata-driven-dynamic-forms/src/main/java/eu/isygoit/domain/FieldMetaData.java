package eu.isygoit.domain;

import java.util.List;
import java.util.Map;

public record FieldMetaData(
        String key,
        String label,
        FieldType type,
        boolean required,
        Object defaultValue,
        Map<String, Object> validation,
        Map<String, Object> ui,
        List<FieldMetaData> children,
        ListConfig listConfig,
        OptionsConfig options,
        FileUploadConfig fileUploadConfig,
        ConditionalRule conditional
) {

    // ==================== UI HELPERS ====================
    public String placeholder()     { return getUi("placeholder"); }
    public String helpText()        { return getUi("helpText"); }
    public String tooltip()         { return getUi("tooltip"); }
    public String mask()            { return getUi("mask"); }
    public String prefix()          { return getUi("prefix"); }
    public String suffix()          { return getUi("suffix"); }
    public String thousandSeparator() { return getUi("thousandSeparator"); }
    public String decimalSeparator()  { return getUi("decimalSeparator"); }

    // Options Helpers
    public boolean multiple()       { return getBoolean(ui, "multiple"); }
    public boolean searchable()     { return getBoolean(ui, "searchable"); }
    public boolean clearable()      { return getBoolean(ui, "clearable"); }
    public boolean showSelectAll()  { return getBoolean(ui, "showSelectAll"); }
    public Integer maxSelectable()  { return getInt(ui, "maxSelectable"); }

    // Validation Helpers
    public Integer minLength() { return getInt(validation, "minLength"); }
    public Integer maxLength() { return getInt(validation, "maxLength"); }
    public Double minValue()   { return getDouble(validation, "minValue"); }
    public Double maxValue()   { return getDouble(validation, "maxValue"); }

    public Integer rows()      { return getInt(ui, "rows"); }

    public String defaultValueStr() {
        return defaultValue != null ? defaultValue.toString() : null;
    }

    // ==================== PRIVATE HELPERS ====================
    private String getUi(String key) {
        return ui != null ? (String) ui.get(key) : null;
    }

    private boolean getBoolean(Map<String, Object> map, String key) {
        Object v = map != null ? map.get(key) : null;
        return Boolean.TRUE.equals(v);
    }

    private Integer getInt(Map<String, Object> map, String key) {
        Object v = map != null ? map.get(key) : null;
        return v instanceof Integer ? (Integer) v : null;
    }

    private Double getDouble(Map<String, Object> map, String key) {
        Object v = map != null ? map.get(key) : null;
        return v instanceof Number ? ((Number) v).doubleValue() : null;
    }
}