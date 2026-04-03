package eu.isygoit.view;

import eu.isygoit.annotation.FormField;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class Address {

    @FormField(
            label = "Street",
            required = true
    )
    @NotBlank
    private String street;

    @FormField(
            label = "City",
            required = true
    )
    @NotBlank
    private String city;

    @FormField(
            label = "Postal Code",
            required = true
    )
    @NotBlank
    private String postalCode;

    @FormField(
            label = "Country",
            required = true
    )
    @NotBlank
    private String country = "Tunisia";
}