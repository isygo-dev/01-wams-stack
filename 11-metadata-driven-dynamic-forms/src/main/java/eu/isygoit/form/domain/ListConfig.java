package eu.isygoit.form.domain;

import java.util.List;
import java.util.Map;

public record ListConfig(
        String itemTemplateKey,
        boolean editable,
        boolean sortable,
        int minItems,
        int maxItems,
        String emptyStateMessage,
        String addButtonLabel,
        List<String> actions,
        List<String> bulkActions,
        Map<String, Object> tableConfig
) {

    public ListConfig(String itemTemplateKey, boolean editable, boolean sortable,
                      int minItems, int maxItems, String emptyStateMessage,
                      String addButtonLabel, List<String> actions,
                      List<String> bulkActions, Map<String, Object> tableConfig) {
        this.itemTemplateKey = itemTemplateKey != null ? itemTemplateKey : "";
        this.editable = editable;
        this.sortable = sortable;
        this.minItems = Math.max(0, minItems);
        this.maxItems = maxItems;
        this.emptyStateMessage = emptyStateMessage != null ? emptyStateMessage : "No items yet";
        this.addButtonLabel = addButtonLabel != null ? addButtonLabel : "Add";
        this.actions = actions != null ? List.copyOf(actions) : List.of();
        this.bulkActions = bulkActions != null ? List.copyOf(bulkActions) : List.of();
        this.tableConfig = tableConfig != null ? Map.copyOf(tableConfig) : Map.of();
    }
}