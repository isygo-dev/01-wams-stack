package eu.isygoit.form.view;

import eu.isygoit.form.annotation.FormField;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
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