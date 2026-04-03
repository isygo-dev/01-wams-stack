package eu.isygoit.domain;

public enum FieldType {
    AUTO,
    TEXT, EMAIL, PASSWORD, NUMBER, DECIMAL, INTEGER,
    DATE, DATETIME, TEXTAREA,
    SELECT, RADIO, CHECKBOX, MULTISELECT, AUTOCOMPLETE,
    OBJECT, LIST, SET, TABLE,
    FILE          // NEW - File upload support
}