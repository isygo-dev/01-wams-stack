package eu.isygoit.view;

import eu.isygoit.annotation.FormField;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ContactInfo {

    @FormField(label = "Phone", required = true)
    @NotBlank
    private String phone;

    @FormField(label = "Address Line", required = true)
    private String addressLine;
}