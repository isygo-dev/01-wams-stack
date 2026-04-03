package eu.isygoit;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.isygoit.core.MetaDataGenerator;
import eu.isygoit.domain.ViewMetaData;
import eu.isygoit.view.EmployeeCreateForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Complex MetaDataGenerator Tests - Nested + List")
class MetaDataGeneratorComplexTest {

    private MetaDataGenerator generator;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        generator = new MetaDataGenerator();
        objectMapper = new ObjectMapper();
        generator.registerView(EmployeeCreateForm.class);
    }

    @Test
    @DisplayName("Should generate metadata with nested object and list")
    void shouldGenerateMetaDataWithNestedAndList() {
        ViewMetaData metadata = generator.generate("employeeCreateForm");

        assertThat(metadata).isNotNull();
        assertThat(metadata.name()).isEqualTo("employeeCreateForm");
        assertThat(metadata.fields()).hasSizeGreaterThan(5);

        // Check nested address object
        var addressField = metadata.fields().stream()
                .filter(f -> "address".equals(f.key()))
                .findFirst()
                .orElseThrow();

        assertThat(addressField.type()).isEqualTo(eu.isygoit.domain.FieldType.OBJECT);
        assertThat(addressField.children()).isNotEmpty();

        // Check roles list
        var rolesField = metadata.fields().stream()
                .filter(f -> "roles".equals(f.key()))
                .findFirst()
                .orElseThrow();

        assertThat(rolesField.type()).isEqualTo(eu.isygoit.domain.FieldType.LIST);
        assertThat(rolesField.listConfig()).isNotNull();
    }

    @Test
    @DisplayName("Should serialize complex metadata to JSON correctly")
    void shouldSerializeComplexMetaDataToJson() throws Exception {
        ViewMetaData metadata = generator.generate("employeeCreateForm");

        String json = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(metadata);

        assertThat(json).isNotBlank();
        assertThat(json).contains("address");
        assertThat(json).contains("roles");
        assertThat(json).contains("street");
        assertThat(json).contains("city");
        assertThat(json).contains("name"); // from Role

        System.out.println("\n=== Complex Employee Form MetaData JSON ===\n" + json);
    }

    @Test
    @DisplayName("Should map mask, prefix and list constraints correctly")
    void shouldMapAdvancedFieldProperties() {
        ViewMetaData metadata = generator.generate("employeeCreateForm");

        var phoneField = metadata.fields().stream()
                .filter(f -> "phone".equals(f.key()))
                .findFirst()
                .orElseThrow();

        assertThat(phoneField.ui()).containsEntry("mask", "(###) ###-####");
        assertThat(phoneField.ui()).containsEntry("prefix", "+216");
    }
}