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

@DisplayName("Comprehensive Form Test - All Properties")
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
    @DisplayName("Should validate every field and all helper methods")
    void shouldValidateAllFieldsAndHelpers() throws Exception {
        ViewMetaData metaData = generator.generate("comprehensiveTestForm");

        // 1. Full Name
        FieldMetaData fullName = findField(metaData, "fullName");
        assertThat(fullName.label()).isEqualTo("Full Name");
        assertThat(fullName.required()).isTrue();
        assertThat(fullName.placeholder()).isEqualTo("Enter your full legal name");
        assertThat(fullName.helpText()).isEqualTo("Must match your official ID");
        assertThat(fullName.tooltip()).isEqualTo("This field is mandatory");
        assertThat(fullName.minLength()).isEqualTo(5);
        assertThat(fullName.maxLength()).isEqualTo(100);

        // 2. Salary
        FieldMetaData salary = findField(metaData, "salary");
        assertThat(salary.prefix()).isEqualTo("$");
        assertThat(salary.suffix()).isEqualTo("USD");
        assertThat(salary.thousandSeparator()).isEqualTo(",");
        assertThat(salary.decimalSeparator()).isEqualTo(".");

        // 3. Phone with mask
        FieldMetaData phone = findField(metaData, "phoneNumber");
        assertThat(phone.mask()).isEqualTo("(###) ###-####");
        assertThat(phone.prefix()).isEqualTo("+216");

        // 4. Experience
        FieldMetaData experience = findField(metaData, "experienceYears");
        assertThat(experience.minValue()).isEqualTo(0.0);
        assertThat(experience.maxValue()).isEqualTo(50.0);

        // 5. Bio
        FieldMetaData bio = findField(metaData, "bio");
        assertThat(bio.rows()).isEqualTo(6);

        // 6. Active
        FieldMetaData active = findField(metaData, "active");
        assertThat(active.defaultValueStr()).isEqualTo("true");

        // 7. Department (Select)
        FieldMetaData department = findField(metaData, "department");
        assertThat(department.clearable()).isTrue();
        assertThat(department.searchable()).isTrue();

        // 8. Skills (Multiselect)
        FieldMetaData skills = findField(metaData, "skills");
        assertThat(skills.multiple()).isTrue();
        assertThat(skills.searchable()).isTrue();
        assertThat(skills.showSelectAll()).isTrue();
        assertThat(skills.maxSelectable()).isEqualTo(8);

        // 9. Resume (File Upload)
        FieldMetaData resume = findField(metaData, "resume");
        assertThat(resume.type()).isEqualTo(FieldType.FILE);

        // 10. Discount with customConditions
        FieldMetaData discount = findField(metaData, "discountAmount");
        assertThat(discount.conditional().visibleWhen()).isEqualTo("membershipLevel == 'PREMIUM'");

        Map<String, Object> customConditions = discount.conditional().customConditions();
        assertThat(customConditions).containsEntry("dependsOn", "membershipLevel");

        // Pretty JSON
        String json = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(metaData);

        System.out.println("\n=== FULL METADATA JSON ===\n" + json);
        System.out.println("\n✅ All fields and helper methods tested successfully!");
    }

    private FieldMetaData findField(ViewMetaData metaData, String key) {
        return metaData.fields().stream()
                .filter(f -> key.equals(f.key()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Field not found: " + key));
    }
}