package eu.isygoit.annotation;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FormList {

    int minItems() default 0;

    int maxItems() default Integer.MAX_VALUE;

    String emptyStateMessage() default "No items added yet";

    String addButtonLabel() default "Add Item";

    // Per-item actions
    String[] actions() default {"add", "edit", "delete", "duplicate"};

    // Bulk actions
    String[] bulkActions() default {};

    // For table-specific UI hints
    boolean sortable() default true;

    boolean editable() default true;
}