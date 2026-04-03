package eu.isygoit.view;

import eu.isygoit.annotation.FormField;
import eu.isygoit.domain.FieldType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class Role {

    @FormField(
            label = "Role Name",
            required = true
    )
    @NotBlank
    private String name;

    @FormField(
            label = "Description",
            type = FieldType.TEXTAREA
    )
    private String description;

    @FormField(
            label = "Is Primary",
            type = FieldType.CHECKBOX
    )
    private boolean primary = false;
}