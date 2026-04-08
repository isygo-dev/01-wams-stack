package eu.isygoit.form.dto;

import java.util.List;
import java.util.Map;

/**
 * Result returned after processing a dynamic form submission.
 * <p>
 * On success, {@link #success()} is {@code true} and {@link #fieldErrors()} is empty.
 * On validation failure, {@link #success()} is {@code false} and each entry in
 * {@link #fieldErrors()} maps a field key to one or more human-readable error messages.
 */
public record FormSubmitResponse(

        /**
         * Whether the submission passed all validation rules and was accepted.
         */
        boolean success,

        /**
         * Field-level validation errors.
         * Key: the field key as declared in {@code @FormField#key()}.
         * Value: list of error messages for that field.
         */
        Map<String, List<String>> fieldErrors,

        /**
         * Optional top-level message (e.g. success confirmation or a global error summary).
         */
        String message
) {

    /**
     * Convenience factory for a successful submission with no errors.
     *
     * @param message optional confirmation message
     * @return a success response
     */
    public static FormSubmitResponse ok(String message) {
        return new FormSubmitResponse(true, Map.of(), message);
    }

    /**
     * Convenience factory for a failed submission with field-level errors.
     *
     * @param fieldErrors map of field key to error message list
     * @return a failure response
     */
    public static FormSubmitResponse invalid(Map<String, List<String>> fieldErrors) {
        return new FormSubmitResponse(false, fieldErrors, "Validation failed");
    }
}