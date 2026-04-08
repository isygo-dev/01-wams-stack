package eu.isygoit.form.view;

import eu.isygoit.form.annotation.FormField;
import eu.isygoit.form.annotation.FormView;
import eu.isygoit.form.domain.FieldType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@FormView(
        name = "userCreateForm",
        title = "Create New User",
        description = "Form for creating a new system user",
        version = "1.0"
)
public class UserCreateForm {

    @FormField(
            label = "Username",
            required = true,
            placeholder = "Enter username",
            order = 1
    )
    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    @FormField(
            label = "Email Address",
            type = FieldType.EMAIL,
            required = true,
            placeholder = "user@example.com",
            order = 2
    )
    @NotBlank
    @Email
    private String email;

    @FormField(
            label = "Password",
            type = FieldType.PASSWORD,
            required = true,
            order = 3
    )
    @Size(min = 8)
    private String password;

    @FormField(
            label = "Full Name",
            placeholder = "John Doe",
            order = 4
    )
    private String fullName;

    @FormField(
            label = "Date of Birth",
            type = FieldType.DATE,
            order = 5
    )
    private LocalDate dateOfBirth;

    @FormField(
            label = "Active User",
            type = FieldType.CHECKBOX,
            order = 6
    )
    private boolean active = true;
}