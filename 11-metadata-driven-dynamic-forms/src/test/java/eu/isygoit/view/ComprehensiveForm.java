package eu.isygoit.view;

import eu.isygoit.annotation.*;
import eu.isygoit.domain.FieldType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@FormView(
        name = "comprehensiveTestForm",
        title = "Comprehensive Test Form",
        description = "Full test of all supported properties",
        version = "1.3"
)
public class ComprehensiveForm {

    @FormField(
            label = "Full Name",
            placeholder = "Enter your full legal name",
            helpText = "Must match your official ID",
            tooltip = "This field is mandatory",
            required = true,
            order = 10
    )
    @NotBlank
    @Size(min = 5, max = 100)
    private String fullName;

    @FormField(
            label = "Email Address",
            type = FieldType.EMAIL,
            placeholder = "user@company.com",
            required = true,
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
            label = "Monthly Salary",
            type = FieldType.DECIMAL,
            prefix = "$",
            suffix = "USD",
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
            placeholder = "(123) 456-7890",
            order = 45
    )
    private String phoneNumber;

    @FormField(
            label = "Years of Experience",
            type = FieldType.INTEGER,
            minValue = 0,
            maxValue = 50,
            order = 50
    )
    private Integer experienceYears;

    @FormField(
            label = "Bio / About You",
            type = FieldType.TEXTAREA,
            maxLength = 500,
            rows = 6,
            placeholder = "Tell us about yourself...",
            order = 60
    )
    private String bio;

    @FormField(
            label = "Is Active Employee",
            type = FieldType.CHECKBOX,
            defaultValue = "true",
            order = 70
    )
    private boolean active = true;

    // ==================== RICH OPTIONS ====================
    @FormField(
            label = "Department",
            type = FieldType.SELECT,
            required = true,
            searchable = true,
            clearable = true,
            order = 80
    )
    private String department;

    @FormField(
            label = "Skills",
            type = FieldType.MULTISELECT,
            multiple = true,
            searchable = true,
            showSelectAll = true,
            maxSelectable = 8,
            order = 85
    )
    private List<String> skills;

    // ==================== FILE UPLOAD ====================
    @FormField(
            label = "Resume / CV",
            type = FieldType.FILE,
            multipleFiles = false,
            acceptedTypes = {".pdf", ".doc", ".docx"},
            maxFileSize = 5242880L, // 5MB
            order = 90
    )
    private Object resume;

    // ==================== ADVANCED CONDITIONAL ====================
    @FormField(
            label = "Discount Amount",
            type = FieldType.DECIMAL,
            order = 95
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

    // List with full config
    @FormField(
            key = "professionalSkills",
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
    private List<Skill> professionalSkills;
}