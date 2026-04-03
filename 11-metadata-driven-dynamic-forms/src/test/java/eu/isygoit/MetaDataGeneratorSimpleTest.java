package eu.isygoit;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.isygoit.core.MetaDataGenerator;
import eu.isygoit.domain.ViewMetaData;
import eu.isygoit.exception.MetaDataGenerationException;
import eu.isygoit.view.UserCreateForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("MetaDataGenerator Tests")
class MetaDataGeneratorSimpleTest {

    private MetaDataGenerator generator;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        generator = new MetaDataGenerator();
        objectMapper = new ObjectMapper();

        // Register the test view
        generator.registerView(UserCreateForm.class);
    }

    @Test
    @DisplayName("Should generate metadata for registered view")
    void shouldGenerateMetaDataForRegisteredView() {
        ViewMetaData metadata = generator.generate("userCreateForm");

        assertThat(metadata).isNotNull();
        assertThat(metadata.name()).isEqualTo("userCreateForm");
        assertThat(metadata.title()).isEqualTo("Create New User");
        assertThat(metadata.version()).isEqualTo("1.0");
        assertThat(metadata.fields()).isNotEmpty().hasSizeGreaterThan(0);
    }

    @Test
    @DisplayName("Should correctly map field properties from annotations and Jakarta Validation")
    void shouldCorrectlyMapFieldProperties() {
        ViewMetaData metadata = generator.generate("userCreateForm");

        var usernameField = metadata.fields().stream()
                .filter(f -> "username".equals(f.key()))
                .findFirst()
                .orElseThrow();

        assertThat(usernameField.label()).isEqualTo("Username");
        assertThat(usernameField.type()).isEqualTo(eu.isygoit.domain.FieldType.TEXT);
        assertThat(usernameField.required()).isTrue();
        assertThat(usernameField.validation())
                .containsKey("required")
                .containsEntry("minLength", 3)
                .containsEntry("maxLength", 50);
    }

    @Test
    @DisplayName("Should infer field types correctly")
    void shouldInferFieldTypeCorrectly() {
        ViewMetaData metadata = generator.generate("userCreateForm");

        var emailField = metadata.fields().stream()
                .filter(f -> "email".equals(f.key()))
                .findFirst()
                .orElseThrow();

        var dateField = metadata.fields().stream()
                .filter(f -> "dateOfBirth".equals(f.key()))
                .findFirst()
                .orElseThrow();

        assertThat(emailField.type()).isEqualTo(eu.isygoit.domain.FieldType.EMAIL);
        assertThat(dateField.type()).isEqualTo(eu.isygoit.domain.FieldType.DATE);
    }

    @Test
    @DisplayName("Should serialize metadata to valid pretty JSON")
    void shouldSerializeMetaDataToValidJson() throws Exception {
        ViewMetaData metadata = generator.generate("userCreateForm");

        String json = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(metadata);

        assertThat(json).isNotBlank();
        assertThat(json).contains("userCreateForm");
        assertThat(json).contains("\"username\"");
        assertThat(json).contains("\"email\"");
        assertThat(json).contains("\"password\"");

        // Print the generated JSON for easy verification
        System.out.println("\n=== Generated MetaData JSON ===\n" + json);
    }

    @Test
    @DisplayName("Should use cache on subsequent calls (same instance returned)")
    void shouldUseCacheOnSubsequentCalls() {
        ViewMetaData firstCall = generator.generate("userCreateForm");
        ViewMetaData secondCall = generator.generate("userCreateForm");

        assertThat(firstCall).isSameAs(secondCall); // Same reference = cache hit
    }

    @Test
    @DisplayName("Should throw MetaDataGenerationException for unregistered view")
    void shouldThrowExceptionForUnregisteredView() {
        assertThatThrownBy(() -> generator.generate("nonExistentForm"))
                .isInstanceOf(MetaDataGenerationException.class)
                .hasMessageContaining("nonExistentForm");
    }

    @Test
    @DisplayName("Should allow direct generation from class without registry")
    void shouldAllowDirectGenerationFromClass() {
        ViewMetaData metadata = generator.generateFromClass(UserCreateForm.class);

        assertThat(metadata).isNotNull();
        assertThat(metadata.name()).isEqualTo("userCreateForm");
    }
}