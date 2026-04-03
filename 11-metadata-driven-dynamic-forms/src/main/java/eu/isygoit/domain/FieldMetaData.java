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
        ConditionalRule conditional
) {

    // === Common UI helpers ===
    public String placeholder()     { return getUi("placeholder"); }
    public String helpText()        { return getUi("helpText"); }
    public String tooltip()         { return getUi("tooltip"); }
    public String mask()            { return getUi("mask"); }
    public String prefix()          { return getUi("prefix"); }
    public String suffix()          { return getUi("suffix"); }
    public String thousandSeparator() { return getUi("thousandSeparator"); }
    public String decimalSeparator()  { return getUi("decimalSeparator"); }

    // === Validation helpers ===
    public Integer minLength() {
        Object val = validation.get("minLength");
        return val instanceof Integer ? (Integer) val : null;
    }

    public Integer maxLength() {
        Object val = validation.get("maxLength");
        return val instanceof Integer ? (Integer) val : null;
    }

    public Double minValue() {
        Object val = validation.get("minValue");
        return val instanceof Double ? (Double) val : null;
    }

    public Double maxValue() {
        Object val = validation.get("maxValue");
        return val instanceof Double ? (Double) val : null;
    }

    public Integer rows() {
        Object val = ui.get("rows");
        return val instanceof Integer ? (Integer) val : null;
    }

    public Object defaultValue() {
        return defaultValue != null ? defaultValue : null;
    }

    private String getUi(String key) {
        return ui != null ? (String) ui.get(key) : null;
    }
}