package eu.isygoit.helper;


import com.networknt.schema.ValidationMessage;
import eu.isygoit.helper.bo.Author;
import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.file.Files;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The type Json helper test part 2.
 */
@DisplayName("JsonHelper Methods Tests with Real Files")
public class JsonHelperTestPart2 {

    private File jsonFile;
    private File schemaFile;
    private File tempJsonFile;
    private File tempSchemaFile;

    /**
     * Sets up.
     *
     * @throws IOException the io exception
     */
    @BeforeEach
    void setUp() throws IOException {
        // Create temporary files for the tests
        jsonFile = createTempFile("test", ".json");
        schemaFile = createTempFile("schema", ".json");

        // Create temporary JSON and schema files with sample content
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(jsonFile))) {
            writer.write("{\"name\": \"John\", \"age\": 30}");
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(schemaFile))) {
            writer.write("{\"type\": \"object\", \"properties\": {\"name\": {\"type\": \"string\"}, \"age\": {\"type\": \"integer\"}}}");
        }

        // Creating temp files for testing
        tempJsonFile = Files.createTempFile("tempJson", ".json").toFile();
        tempSchemaFile = Files.createTempFile("tempSchema", ".json").toFile();
    }

    /**
     * Tear down.
     *
     * @throws IOException the io exception
     */
    @AfterEach
    void tearDown() throws IOException {
        // Clean up temporary files after each test
        if (jsonFile.exists()) jsonFile.delete();
        if (schemaFile.exists()) schemaFile.delete();
        if (tempJsonFile.exists()) tempJsonFile.delete();
        if (tempSchemaFile.exists()) tempSchemaFile.delete();
    }

    private File createTempFile(String prefix, String suffix) throws IOException {
        File tempFile = Files.createTempFile(prefix, suffix).toFile();
        tempFile.deleteOnExit();
        return tempFile;
    }

    /**
     * The type Validate json tests.
     */
    @Nested
    @DisplayName("Tests for validateJson method")
    class ValidateJsonTests {

        /**
         * Validate json success.
         *
         * @throws Exception the exception
         */
        @Test
        @DisplayName("Should validate JSON against schema successfully")
        void validateJson_Success() throws Exception {
            // Assuming validateJson returns an empty set (valid JSON)
            Set<ValidationMessage> validationMessages = JsonHelper.validateJson(jsonFile.getPath(), schemaFile.getPath(), "V4");

            // Validating that no errors are found (i.e., it's a valid schema)
            assertTrue(validationMessages.isEmpty(), "Validation should pass with no messages");
        }

        /**
         * Validate json file not found exception.
         */
        @Test
        @DisplayName("Should throw exception if schema file is missing")
        void validateJson_FileNotFoundException() {
            assertThrows(FileNotFoundException.class, () -> {
                JsonHelper.validateJson(tempJsonFile.getPath() + "not", tempSchemaFile.getPath() + "not", "V4");
            });
        }
    }

    /**
     * The type Json file conversion tests.
     */
    @Nested
    @DisplayName("Tests for toJsonFile and fromJsonFile methods")
    class JsonFileConversionTests {

        /**
         * To json file success.
         *
         * @throws IOException the io exception
         */
        @Test
        @DisplayName("Should write object to JSON file")
        void toJsonFile_Success() throws IOException {
            // Create an object to convert to JSON
            Author obj = Author.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .build();

            // Write the object to the temp file
            try (FileWriter fileWriter = new FileWriter(tempJsonFile)) {
                JsonHelper.toJsonFile(fileWriter, obj);
            }

            // Assert that the file was created and contains some content
            assertTrue(tempJsonFile.exists(), "JSON file should be created");
            assertTrue(tempJsonFile.length() > 0, "JSON file should not be empty");
        }

        /**
         * From json file success.
         *
         * @throws IOException the io exception
         */
        @Test
        @DisplayName("Should read object from JSON file")
        void fromJsonFile_Success() throws IOException {
            // Writing a sample JSON object into the temp file
            String jsonString = "{\"name\":\"John\", \"age\":30}";
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempJsonFile))) {
                writer.write(jsonString);
            }

            // Read the object from the file
            try (FileReader fileReader = new FileReader(tempJsonFile)) {
                Object result = JsonHelper.fromJsonFile(fileReader, Object.class);

                // Assert that the result is deserialized
                assertNotNull(result, "Object should be deserialized from JSON string");
            }
        }
    }

    /**
     * The type Json serialization tests.
     */
    @Nested
    @DisplayName("Tests for toJson and fromJson methods")
    class JsonSerializationTests {

        /**
         * To json success.
         *
         * @throws IOException the io exception
         */
        @Test
        @DisplayName("Should convert object to JSON string")
        void toJson_Success() throws IOException {
            Author obj = Author.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .build();
            String json = JsonHelper.toJson(obj);

            assertNotNull(json, "JSON string should not be null");
            assertFalse(json.isEmpty(), "JSON string should not be empty");
        }

        /**
         * From json success.
         *
         * @throws IOException the io exception
         */
        @Test
        @DisplayName("Should convert JSON string to object")
        void fromJson_Success() throws IOException {
            String json = "{\"name\":\"John\", \"age\":30}";
            Object obj = JsonHelper.fromJson(json, Object.class);

            assertNotNull(obj, "Object should be deserialized from JSON string");
        }
    }
}