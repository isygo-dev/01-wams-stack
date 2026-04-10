package eu.isygoit.form.view;

import eu.isygoit.form.annotation.FormField;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ContactInfo {

    @FormField(label = "Phone", required = true)
    @NotBlank
    private String phone;

    @FormField(label = "Address Line", required = true)
    private String addressLine;
}