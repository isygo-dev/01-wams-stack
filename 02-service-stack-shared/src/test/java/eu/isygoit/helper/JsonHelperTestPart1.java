package eu.isygoit.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.isygoit.helper.bo.Author;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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

    @Nested
    @DisplayName("JSON Utilities Tests")
    class JsonUtilitiesTests {

        @Test
        @DisplayName("Pretty Print JSON")
        void prettyPrintJson_shouldReturnFormattedJson() throws Exception {
            JsonNode mockJsonNode = objectMapper.readTree("{\"name\":\"John\",\"age\":30}");
            String result = JsonHelper.prettyPrintJson(mockJsonNode);
            assertTrue(result.contains("\n"), "Pretty printed JSON should contain newlines");
            assertTrue(result.contains("  \"name\" : \"John\""), "Pretty printed JSON should be indented");
        }

        @Test
        @DisplayName("Object to Node conversion")
        void objectToNode_shouldConvertObjectToJsonNode() {
            Author author = Author.builder().firstName("John").lastName("Doe").build();
            JsonNode result = JsonHelper.objectToNode(author);
            System.out.println("[DEBUG_LOG] objectToNode result: " + result.toString());
            assertEquals("John", result.get("firstName").asText());
            assertEquals("Doe", result.get("lastName").asText());
        }

        @Test
        @DisplayName("Node to Object conversion")
        void nodeToObject_shouldConvertJsonNodeToObject() throws Exception {
            JsonNode node = objectMapper.readTree("{\"firstName\":\"John\",\"lastName\":\"Doe\"}");
            Author result = JsonHelper.nodeToObject(node, Author.class);
            assertEquals("John", result.getFirstName());
            assertEquals("Doe", result.getLastName());
        }

        @Test
        @DisplayName("Create Empty Node")
        void createEmptyNode_shouldReturnEmptyObjectNode() {
            JsonNode result = JsonHelper.createEmptyNode();
            assertTrue(result.isObject());
            assertEquals(0, result.size());
        }

        @Test
        @DisplayName("JSON to JsonNode conversion")
        void jsonToJsonNode_shouldConvertStringToJsonNode() throws Exception {
            String json = "{\"name\":\"John\"}";
            JsonNode result = JsonHelper.jsonToJsonNode(json);
            assertEquals("John", result.get("name").asText());
        }
    }

    @Nested
    @DisplayName("JSON Diff and Comparison Tests")
    class JsonDiffTests {

        @Test
        @DisplayName("Compute Diff - Basic Changes")
        void computeDiff_shouldReturnChanges() {
            Map<String, Object> prev = new HashMap<>();
            prev.put("firstName", "John");
            prev.put("age", 30);

            Map<String, Object> curr = new HashMap<>();
            curr.put("firstName", "John"); // unchanged
            curr.put("age", 31); // changed
            curr.put("city", "New York"); // added

            ObjectNode diff = JsonHelper.computeDiff(prev, curr);

            assertFalse(diff.has("firstName"));
            assertEquals(31, diff.get("age").asInt());
            assertEquals("New York", diff.get("city").asText());
        }

        @Test
        @DisplayName("Compute Diff - Removed Fields")
        void computeDiff_shouldHandleRemovedFields() {
            Map<String, Object> prev = new HashMap<>();
            prev.put("firstName", "John");
            prev.put("age", 30);

            Map<String, Object> curr = new HashMap<>();
            curr.put("firstName", "John");

            ObjectNode diff = JsonHelper.computeDiff(prev, curr);

            assertTrue(diff.has("age"));
            assertTrue(diff.get("age").isNull());
        }

        @Test
        @DisplayName("Compute Diff - Excluded Keys")
        void computeDiff_shouldExcludeKeys() {
            Map<String, Object> prev = new HashMap<>();
            prev.put("id", 1L);
            prev.put("name", "Old");

            Map<String, Object> curr = new HashMap<>();
            curr.put("id", 2L); // changed but excluded
            curr.put("name", "New");

            ObjectNode diff = JsonHelper.computeDiff(prev, curr);

            assertFalse(diff.has("id"));
            assertEquals("New", diff.get("name").asText());
        }

        @Test
        @DisplayName("Compute Diff - Required Keys")
        void computeDiff_shouldAlwaysIncludeRequiredKeys() {
            Map<String, Object> prev = new HashMap<>();
            prev.put("updatedBy", "user1");
            prev.put("name", "Old");

            Map<String, Object> curr = new HashMap<>();
            curr.put("updatedBy", "user1"); // unchanged but required
            curr.put("name", "New");

            ObjectNode diff = JsonHelper.computeDiff(prev, curr);

            assertEquals("user1", diff.get("updatedBy").asText());
            assertEquals("New", diff.get("name").asText());
        }

        @Test
        @DisplayName("Deep Equals - Complex Objects")
        void computeDiff_deepEquals_shouldHandleComplexObjects() {
            Map<String, Object> prev = new HashMap<>();
            prev.put("tags", Arrays.asList("a", "b"));
            Map<String, String> nestedPrev = new HashMap<>();
            nestedPrev.put("k", "v");
            prev.put("nested", nestedPrev);

            Map<String, Object> curr = new HashMap<>();
            curr.put("tags", Arrays.asList("a", "b")); // unchanged
            Map<String, String> nestedCurr = new HashMap<>();
            nestedCurr.put("k", "v2");
            curr.put("nested", nestedCurr);

            ObjectNode diff = JsonHelper.computeDiff(prev, curr);

            System.out.println("[DEBUG_LOG] computeDiff_deepEquals result: " + diff.toString());
            assertFalse(diff.has("tags"));
            assertTrue(diff.has("nested"));
            
            // Convert POJONode to Tree for verification if needed, or check as Object
            JsonNode nestedNode = objectMapper.valueToTree(diff.get("nested"));
            assertEquals("v2", nestedNode.get("k").asText());
        }

        @Test
        @DisplayName("Create Full Diff")
        void createFullDiff_shouldReturnOldAndNewStates() {
            Author oldAuthor = Author.builder().firstName("John").build();
            Author newAuthor = Author.builder().firstName("Jane").build();

            ObjectNode result = JsonHelper.createFullDiff(oldAuthor, newAuthor);

            System.out.println("[DEBUG_LOG] createFullDiff result: " + result.toString());
            assertTrue(result.has("state"));
            JsonNode state = result.get("state");
            assertTrue(state.has("old"));
            assertTrue(state.has("new"));
            
            JsonNode oldNode = objectMapper.valueToTree(state.get("old"));
            JsonNode newNode = objectMapper.valueToTree(state.get("new"));
            
            assertEquals("John", oldNode.get("firstName").asText());
            assertEquals("Jane", newNode.get("firstName").asText());
        }
    }

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
