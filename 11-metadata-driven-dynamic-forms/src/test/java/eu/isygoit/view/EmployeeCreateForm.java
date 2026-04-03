package eu.isygoit.view;

import eu.isygoit.annotation.FormField;
import eu.isygoit.annotation.FormList;
import eu.isygoit.annotation.FormView;
import eu.isygoit.domain.FieldType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.List;

@FormView(
        name = "employeeCreateForm",
        title = "Create New Employee",
        description = "Complex form with nested address and role list",
        version = "1.1"
)
public class EmployeeCreateForm {

    @FormField(
            label = "Employee ID",
            required = true,
            order = 1
    )
    @NotBlank
    private String employeeId;

    @FormField(
            label = "Full Name",
            required = true,
            order = 2
    )
    @NotBlank
    private String fullName;

    @FormField(
            label = "Email",
            type = FieldType.EMAIL,
            required = true,
            order = 3
    )
    @Email
    private String email;

    @FormField(
            label = "Phone Number",
            type = FieldType.TEXT,
            useMask = true,
            mask = "(###) ###-####",
            prefix = "+216",
            order = 4
    )
    private String phone;

    @FormField(
            label = "Date of Birth",
            type = FieldType.DATE,
            order = 5
    )
    private LocalDate dateOfBirth;

    // ==================== NESTED OBJECT ====================
    @FormField(
            key = "address",
            label = "Address",
            type = FieldType.OBJECT,
            order = 10
    )
    private Address address;

    // ==================== LIST OF OBJECTS ====================
    @FormField(
            key = "roles",
            label = "Roles",
            type = FieldType.LIST,
            order = 20
    )
    @FormList(
            minItems = 1,
            maxItems = 5,
            emptyStateMessage = "No roles assigned yet",
            addButtonLabel = "Add Role",
            sortable = true,
            editable = true
    )
    private List<Role> roles;
}