package eu.isygoit.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The type Json helper test part 1.
 */
class JsonHelperTestPart1 {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final XmlMapper xmlMapper = new XmlMapper();
    private final YAMLMapper yamlMapper = new YAMLMapper();
    private final CsvMapper csvMapper = new CsvMapper();

    /**
     * The type Json to format tests.
     */
    @Nested
    @DisplayName("JSON to Other Format Conversion Tests")
    class JsonToFormatTests {

        /**
         * Json to xml should convert json node to xml.
         *
         * @throws Exception the exception
         */
        @Test
        @DisplayName("Convert JSON to XML")
        void jsonToXml_shouldConvertJsonNodeToXml() throws Exception {
            JsonNode mockJsonNode = objectMapper.readTree("{\"name\":\"John\",\"age\":30}");
            String expectedXml = "<ObjectNode><name>John</name><age>30</age></ObjectNode>";

            String result = JsonHelper.jsonToXml(mockJsonNode);

            assertEquals(expectedXml, result, "The XML conversion should return the expected XML string");
        }

        /**
         * Json to csv should convert json node to csv.
         *
         * @throws Exception the exception
         */
        @Test
        @DisplayName("Convert JSON to CSV")
        void jsonToCsv_shouldConvertJsonNodeToCsv() throws Exception {
            JsonNode mockJsonNode = objectMapper.readTree("[{\"name\":\"John\",\"age\":30}, {\"name\":\"Jane\",\"age\":25}]");
            String expectedCsv = "name,age\nJohn,30\nJane,25\n";

            String result = JsonHelper.jsonToCsv(mockJsonNode);

            assertEquals(expectedCsv, result, "The CSV conversion should return the expected CSV string");
        }

        /**
         * Json to yaml should convert json node to yaml.
         *
         * @throws Exception the exception
         */
        @Test
        @DisplayName("Convert JSON to YAML")
        void jsonToYaml_shouldConvertJsonNodeToYaml() throws Exception {
            JsonNode mockJsonNode = objectMapper.readTree("{\"name\":\"John\",\"age\":30}");
            String expectedYaml = "---\n" +
                    "name: \"John\"\n" +
                    "age: 30\n";

            String result = JsonHelper.jsonToYaml(mockJsonNode);

            assertEquals(expectedYaml, result, "The YAML conversion should return the expected YAML string");
        }

        /**
         * Json to properties should convert json node to properties.
         *
         * @throws Exception the exception
         */
        @Test
        @DisplayName("Convert JSON to Properties")
        void jsonToProperties_shouldConvertJsonNodeToProperties() throws Exception {
            JsonNode mockJsonNode = objectMapper.readTree("{\"name\":\"John\",\"age\":30}");
            String expectedProperties = "name=John\nage=30\n";

            String result = JsonHelper.jsonToProperties(mockJsonNode);

            assertTrue(result.contains("name=John"), "The Properties conversion should contain the correct properties");
            assertTrue(result.contains("age=30"), "The Properties conversion should contain the correct properties");
        }
    }

    /**
     * The type Format to json tests.
     */
    @Nested
    @DisplayName("Other Format to JSON Conversion Tests")
    class FormatToJsonTests {

        /**
         * Xml to json should convert xml to json.
         *
         * @throws Exception the exception
         */
        @Test
        @DisplayName("Convert XML to JSON")
        void xmlToJson_shouldConvertXmlToJson() throws Exception {
            String xml = "<root><name>John</name><age>30</age></root>";
            JsonNode expectedJsonNode = objectMapper.readTree("{\"name\":\"John\",\"age\":\"30\"}");

            JsonNode result = JsonHelper.xmlToJson(xml);

            assertEquals(expectedJsonNode, result, "The XML to JSON conversion should return the correct JsonNode");
        }

        /**
         * Csv to json should convert csv to json.
         *
         * @throws Exception the exception
         */
        @Test
        @DisplayName("Convert CSV to JSON")
        void csvToJson_shouldConvertCsvToJson() throws Exception {
            String csv = "name,age\nJohn,30\nJane,25\n";
            JsonNode expectedJsonNode = objectMapper.readTree("[{\"name\":\"John\",\"age\":\"30\"}, {\"name\":\"Jane\",\"age\":\"25\"}]");

            JsonNode result = JsonHelper.csvToJson(csv);

            assertEquals(expectedJsonNode, result, "The CSV to JSON conversion should return the correct JsonNode");
        }

        /**
         * Yaml to json should convert yaml to json.
         *
         * @throws Exception the exception
         */
        @Test
        @DisplayName("Convert YAML to JSON")
        void yamlToJson_shouldConvertYamlToJson() throws Exception {
            String yaml = "name: John\nage: 30\n";
            JsonNode expectedJsonNode = objectMapper.readTree("{\"name\":\"John\",\"age\":30}");

            JsonNode result = JsonHelper.yamlToJson(yaml);

            assertEquals(expectedJsonNode, result, "The YAML to JSON conversion should return the correct JsonNode");
        }

        /**
         * Properties to json should convert properties to json.
         *
         * @throws Exception the exception
         */
        @Test
        @DisplayName("Convert Properties to JSON")
        void propertiesToJson_shouldConvertPropertiesToJson() throws Exception {
            String properties = "name=John\nage=30\n";
            JsonNode expectedJsonNode = objectMapper.readTree("{\"name\":\"John\",\"age\":\"30\"}");

            JsonNode result = JsonHelper.propertiesToJson(properties);

            assertEquals(expectedJsonNode, result, "The Properties to JSON conversion should return the correct JsonNode");
        }
    }

    /**
     * The type Error handling tests.
     */
    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        /**
         * Json to csv should throw exception if json is not array.
         *
         * @throws Exception the exception
         */
        @Test
        @DisplayName("Handle Invalid JSON to CSV Conversion")
        void jsonToCsv_shouldThrowExceptionIfJsonIsNotArray() throws Exception {
            JsonNode mockJsonNode = objectMapper.readTree("{\"name\":\"John\",\"age\":30}");

            JsonProcessingException exception = assertThrows(JsonProcessingException.class, () -> {
                JsonHelper.jsonToCsv(mockJsonNode);
            });

            assertEquals("The provided JSON must be an array of objects to convert to CSV.", exception.getMessage());
        }
    }


}
