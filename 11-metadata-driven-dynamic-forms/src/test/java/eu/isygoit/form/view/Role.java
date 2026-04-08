package eu.isygoit.form.view;

import eu.isygoit.form.annotation.FormField;
import eu.isygoit.form.domain.FieldType;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
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