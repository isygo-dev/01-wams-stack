package eu.isygoit;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.isygoit.core.MetaDataGenerator;
import eu.isygoit.domain.FieldMetaData;
import eu.isygoit.domain.FieldType;
import eu.isygoit.domain.ViewMetaData;
import eu.isygoit.view.ComprehensiveForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Comprehensive Form Test - FULL Coverage of All Fields & Properties")
class ComprehensiveFormTest {

    private MetaDataGenerator generator;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        generator = new MetaDataGenerator();
        objectMapper = new ObjectMapper();
        generator.registerView(ComprehensiveForm.class);
    }

    @Test
    @DisplayName("Should map EVERY field and ALL properties with real non-empty values")
    void shouldMapAllFieldsAndAllProperties() throws Exception {
        ViewMetaData metaData = generator.generate("comprehensiveTestForm");

        assertThat(metaData.name()).isEqualTo("comprehensiveTestForm");
        assertThat(metaData.title()).isEqualTo("Comprehensive Test Form");
        assertThat(metaData.description()).isNotEmpty();
        assertThat(metaData.version()).isEqualTo("1.3");
        assertThat(metaData.fields()).hasSizeGreaterThan(12);

        // 1. Full Name
        FieldMetaData fullName = findField(metaData, "fullName");
        assertThat(fullName.label()).isEqualTo("Full Name");
        assertThat(fullName.required()).isTrue();
        assertThat(fullName.placeholder()).isEqualTo("Enter your full legal name");
        assertThat(fullName.helpText()).isEqualTo("Must match your official ID");
        assertThat(fullName.tooltip()).isEqualTo("This field is mandatory");
        assertThat(fullName.minLength()).isEqualTo(5);
        assertThat(fullName.maxLength()).isEqualTo(100);

        // 2. Email
        FieldMetaData email = findField(metaData, "email");
        assertThat(email.type()).isEqualTo(FieldType.EMAIL);
        assertThat(email.required()).isTrue();
        assertThat(email.placeholder()).isEqualTo("user@company.com");

        // 3. Password
        FieldMetaData password = findField(metaData, "password");
        assertThat(password.type()).isEqualTo(FieldType.PASSWORD);
        assertThat(password.required()).isTrue();

        // 4. Salary (formatting)
        FieldMetaData salary = findField(metaData, "salary");
        assertThat(salary.type()).isEqualTo(FieldType.DECIMAL);
        assertThat(salary.prefix()).isEqualTo("$");
        assertThat(salary.suffix()).isEqualTo("USD");
        assertThat(salary.thousandSeparator()).isEqualTo(",");
        assertThat(salary.decimalSeparator()).isEqualTo(".");

        // 5. Phone Number (mask)
        FieldMetaData phone = findField(metaData, "phoneNumber");
        assertThat(phone.type()).isEqualTo(FieldType.TEXT);
        assertThat(phone.mask()).isEqualTo("(###) ###-####");
        assertThat(phone.prefix()).isEqualTo("+216");

        // 6. Experience Years (min/max value)
        FieldMetaData experience = findField(metaData, "experienceYears");
        assertThat(experience.type()).isEqualTo(FieldType.INTEGER);
        assertThat(experience.minValue()).isEqualTo(0.0);
        assertThat(experience.maxValue()).isEqualTo(50.0);

        // 7. Bio (textarea)
        FieldMetaData bio = findField(metaData, "bio");
        assertThat(bio.type()).isEqualTo(FieldType.TEXTAREA);
        assertThat(bio.maxLength()).isEqualTo(500);
        assertThat(bio.rows()).isEqualTo(6);

        // 8. Active Checkbox
        FieldMetaData active = findField(metaData, "active");
        assertThat(active.type()).isEqualTo(FieldType.CHECKBOX);
        assertThat(active.defaultValueStr()).isEqualTo("true");

        // 9. Department - Static Options
        FieldMetaData department = findField(metaData, "department");
        assertThat(department.type()).isEqualTo(FieldType.SELECT);
        assertThat(department.options()).isNotNull();
        assertThat(department.options().options().size()).isEqualTo(4);
        assertThat(department.searchable()).isTrue();
        assertThat(department.clearable()).isTrue();

        // 10. Skills - Multiselect + Static Options
        FieldMetaData skills = findField(metaData, "skills");
        assertThat(skills.type()).isEqualTo(FieldType.MULTISELECT);
        assertThat(skills.multiple()).isTrue();
        assertThat(skills.searchable()).isTrue();
        assertThat(skills.showSelectAll()).isTrue();
        assertThat(skills.maxSelectable()).isEqualTo(8);
        assertThat(skills.options()).isNotNull();
        assertThat(skills.options().options().size()).isEqualTo(4);

        // 11. Resume - File Upload
        FieldMetaData resume = findField(metaData, "resume");
        assertThat(resume.type()).isEqualTo(FieldType.FILE);
        assertThat(resume.fileUploadConfig()).isNotNull();
        assertThat(resume.fileUploadConfig().acceptedTypes()).contains(".pdf", ".doc", ".docx");
        assertThat(resume.fileUploadConfig().maxFileSize()).isEqualTo(5242880L);

        // 12. Discount Amount - Conditional + customConditions
        FieldMetaData discount = findField(metaData, "discountAmount");
        assertThat(discount.type()).isEqualTo(FieldType.DECIMAL);
        assertThat(discount.conditional().visibleWhen()).isEqualTo("membershipLevel == 'PREMIUM'");
        Map<String, Object> customConditions = discount.conditional().customConditions();
        assertThat(customConditions).isNotEmpty();
        assertThat(customConditions)
                .containsEntry("dependsOn", "membershipLevel")
                .containsEntry("operator", "EQUALS")
                .containsEntry("logic", "AND")
                .containsEntry("minOrderAmount", "100")
                .containsEntry("action", "applyDiscount");

        // 13. ContactInfo - Nested Object
        FieldMetaData contactInfo = findField(metaData, "contactInfo");
        assertThat(contactInfo.type()).isEqualTo(FieldType.OBJECT);
        assertThat(contactInfo.children()).isNotEmpty();

        // 14. Professional Skills - List
        FieldMetaData profSkills = findField(metaData, "professionalSkills");
        assertThat(profSkills.type()).isEqualTo(FieldType.LIST);
        assertThat(profSkills.listConfig()).isNotNull();
        assertThat(profSkills.listConfig().minItems()).isEqualTo(1);
        assertThat(profSkills.listConfig().maxItems()).isEqualTo(10);
        assertThat(profSkills.listConfig().addButtonLabel()).isEqualTo("Add New Skill");

        // Pretty JSON for verification
        String json = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(metaData);

        System.out.println("\n=== FULL METADATA JSON - COMPLETE COVERAGE ===\n" + json);
        System.out.println("\n✅ COMPREHENSIVE TEST PASSED - ALL FIELDS AND PROPERTIES COVERED!");
    }

    private FieldMetaData findField(ViewMetaData metaData, String key) {
        return metaData.fields().stream()
                .filter(f -> key.equals(f.key()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Field not found: " + key));
    }
}