package eu.isygoit.view;

import eu.isygoit.annotation.*;
import eu.isygoit.domain.FieldType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@FormView(
        name = "comprehensiveTestForm",
        title = "Comprehensive Test Form",
        description = "Tests all available form, field and list properties",
        version = "1.2"
)
public class ComprehensiveForm {

    @FormField(
            label = "Full Name",
            placeholder = "Enter your full name",
            required = true,
            order = 10,
            tooltip = "This field is mandatory",
            helpText = "Please use your legal name"
    )
    @NotBlank
    @Size(min = 5, max = 100)
    private String fullName;

    @FormField(
            label = "Email Address",
            type = FieldType.EMAIL,
            required = true,
            placeholder = "user@company.com",
            order = 20
    )
    @Email
    private String email;

    @FormField(
            label = "Password",
            type = FieldType.PASSWORD,
            required = true,
            order = 30
    )
    @Size(min = 12)
    private String password;

    @FormField(
            label = "Salary",
            type = FieldType.DECIMAL,
            prefix = "$",
            suffix = "/month",
            thousandSeparator = ",",
            decimalSeparator = ".",
            order = 40
    )
    private Double salary;

    @FormField(
            label = "Phone Number",
            type = FieldType.TEXT,
            useMask = true,
            mask = "(###) ###-####",
            prefix = "+216",
            order = 45
    )
    private String phoneNumber;

    @FormField(
            label = "Experience Years",
            type = FieldType.INTEGER,
            minValue = 0,
            maxValue = 50,
            order = 50
    )
    private Integer experienceYears;

    @FormField(
            label = "Bio",
            type = FieldType.TEXTAREA,
            maxLength = 500,
            rows = 5,
            order = 60
    )
    private String bio;

    @FormField(
            label = "Is Active",
            type = FieldType.CHECKBOX,
            defaultValue = "true",
            order = 70
    )
    private boolean active;

    // ==================== FIELD WITH NON-EMPTY customConditions ====================
    @FormField(
            label = "Discount Amount",
            type = FieldType.DECIMAL,
            order = 55
    )
    @FormConditional(
            visibleWhen = "membershipLevel == 'PREMIUM'",
            value = {
                    @Condition(key = "dependsOn", value = "membershipLevel"),
                    @Condition(key = "operator", value = "EQUALS"),
                    @Condition(key = "logic", value = "AND"),
                    @Condition(key = "minOrderAmount", value = "100"),
                    @Condition(key = "action", value = "applyDiscount")
            }
    )
    private Double discountAmount;

    // Nested Object
    @FormField(
            key = "contactInfo",
            label = "Contact Information",
            type = FieldType.OBJECT,
            order = 100
    )
    private ContactInfo contactInfo;

    // List
    @FormField(
            key = "skills",
            label = "Professional Skills",
            type = FieldType.LIST,
            order = 110
    )
    @FormList(
            minItems = 1,
            maxItems = 10,
            emptyStateMessage = "No skills added yet. Click below to add your first skill.",
            addButtonLabel = "Add New Skill",
            sortable = true,
            editable = true,
            actions = {"add", "edit", "delete", "duplicate", "archive"},
            bulkActions = {"delete", "archive"}
    )
    private List<Skill> skills;
}