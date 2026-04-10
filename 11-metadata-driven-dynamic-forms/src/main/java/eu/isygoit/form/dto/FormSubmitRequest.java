package eu.isygoit.form.dto;

import java.util.Map;

/**
 * Payload sent by the client when submitting a dynamic form.
 * <p>
 * Contains the target view name and a flat map of field key → raw value.
 * The service layer is responsible for coercing values to their declared types
 * and applying annotation-driven validation rules.
 */
public record FormSubmitRequest(

        /**
         * The logical name of the view being submitted, as declared in {@code @FormView#name()}.
         */
        String viewName,

        /**
         * Field values keyed by their {@code @FormField#key()} identifiers.
         * Nested objects use dot-notation (e.g. {@code "address.city"}).
         * Multi-value fields (lists, multi-selects) should be passed as {@code List<?>} values.
         */
        Map<String, Object> fields
) {
}