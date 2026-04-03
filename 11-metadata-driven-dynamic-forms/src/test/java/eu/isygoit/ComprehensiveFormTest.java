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

@DisplayName("Comprehensive Form Test - All Field Properties")
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
    @DisplayName("Should correctly map ALL field properties including customConditions")
    void shouldMapAllFieldProperties() throws Exception {
        ViewMetaData metaData = generator.generate("comprehensiveTestForm");

        assertThat(metaData.name()).isEqualTo("comprehensiveTestForm");

        // 1. Full Name
        FieldMetaData fullName = findField(metaData, "fullName");
        assertThat(fullName.label()).isEqualTo("Full Name");
        assertThat(fullName.required()).isTrue();
        assertThat(fullName.placeholder()).isEqualTo("Enter your full name");
        assertThat(fullName.helpText()).isEqualTo("Please use your legal name");
        assertThat(fullName.tooltip()).isEqualTo("This field is mandatory");
        assertThat(fullName.minLength()).isEqualTo(5);
        assertThat(fullName.maxLength()).isEqualTo(100);

        // 2. Salary
        FieldMetaData salary = findField(metaData, "salary");
        assertThat(salary.type()).isEqualTo(FieldType.DECIMAL);
        assertThat(salary.prefix()).isEqualTo("$");
        assertThat(salary.suffix()).isEqualTo("/month");
        assertThat(salary.thousandSeparator()).isEqualTo(",");
        assertThat(salary.decimalSeparator()).isEqualTo(".");

        // 3. Phone with mask
        FieldMetaData phone = findField(metaData, "phoneNumber");
        assertThat(phone.mask()).isEqualTo("(###) ###-####");
        assertThat(phone.prefix()).isEqualTo("+216");

        // 4. Experience Years
        FieldMetaData experience = findField(metaData, "experienceYears");
        assertThat(experience.minValue()).isEqualTo(0.0);
        assertThat(experience.maxValue()).isEqualTo(50.0);

        // 5. Bio
        FieldMetaData bio = findField(metaData, "bio");
        assertThat(bio.type()).isEqualTo(FieldType.TEXTAREA);
        assertThat(bio.maxLength()).isEqualTo(500);
        assertThat(bio.rows()).isEqualTo(5);

        // 6. Active with defaultValue
        FieldMetaData active = findField(metaData, "active");
        assertThat(active.defaultValue()).isEqualTo("true");

        // 7. Discount Amount with customConditions (NON-EMPTY)
        FieldMetaData discount = findField(metaData, "discountAmount");
        assertThat(discount.label()).isEqualTo("Discount Amount");

        var conditional = discount.conditional();
        assertThat(conditional.visibleWhen()).isEqualTo("membershipLevel == 'PREMIUM'");

        Map<String, Object> customConditions = conditional.customConditions();
        assertThat(customConditions).isNotEmpty();
        assertThat(customConditions)
                .containsEntry("dependsOn", "membershipLevel")
                .containsEntry("operator", "EQUALS")
                .containsEntry("logic", "AND")
                .containsEntry("minOrderAmount", "100")
                .containsEntry("action", "applyDiscount");

        // 8. List
        FieldMetaData skills = findField(metaData, "skills");
        assertThat(skills.type()).isEqualTo(FieldType.LIST);
        assertThat(skills.listConfig().minItems()).isEqualTo(1);
        assertThat(skills.listConfig().maxItems()).isEqualTo(10);

        // 9. Nested Object
        FieldMetaData contactInfo = findField(metaData, "contactInfo");
        assertThat(contactInfo.type()).isEqualTo(FieldType.OBJECT);
        assertThat(contactInfo.children()).isNotEmpty();

        // Pretty JSON
        String json = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(metaData);

        System.out.println("\n=== FULL COMPREHENSIVE METADATA JSON ===\n" + json);
        System.out.println("\n✅ ALL FIELD PROPERTIES TESTED SUCCESSFULLY!");
    }

    private FieldMetaData findField(ViewMetaData metaData, String key) {
        return metaData.fields().stream()
                .filter(f -> key.equals(f.key()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Field not found: " + key));
    }
}