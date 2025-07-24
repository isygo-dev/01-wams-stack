package eu.isygoit.helper;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.*;
import java.util.*;

/**
 * The JsonHelper interface provides utility methods for handling JSON data,
 * including parsing, converting between formats (XML, CSV, YAML, Properties),
 * and validating against schemas.
 */
public interface JsonHelper {

    /**
     * The constant EXCLUDED_KEYS.
     */
    static final Set<String> EXCLUDED_KEYS = Set.of("id", "version");

    /**
     * The constant logger.
     */
    Logger logger = LoggerFactory.getLogger(JsonHelper.class);

    /**
     * The constant mapper.
     */
    ObjectMapper objectMapper = new ObjectMapper() {
        {
            registerModule(new JavaTimeModule());
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        }
    };
    /**
     * The constant xmlMapper.
     */
    XmlMapper xmlMapper = new XmlMapper();
    /**
     * The constant yamlMapper.
     */
    YAMLMapper yamlMapper = new YAMLMapper();
    /**
     * The constant csvMapper.
     */
    CsvMapper csvMapper = new CsvMapper();

    // --- Methods for Converting JSON to Other Standard Formats ---

    /**
     * Converts a JsonNode to an XML string.
     *
     * @param jsonNode the JsonNode to convert
     * @return the XML string representation of the JsonNode
     * @throws JsonProcessingException if an error occurs during the conversion
     */
    public static String jsonToXml(JsonNode jsonNode) throws JsonProcessingException {
        logger.debug("Converting JsonNode to XML format");
        return xmlMapper.writeValueAsString(jsonNode);
    }

    /**
     * Converts a JsonNode to a CSV string.
     * Assumes the JsonNode is an array of objects (e.g., list of records).
     *
     * @param jsonNode the JsonNode to convert
     * @return the CSV string representation of the JsonNode
     * @throws JsonProcessingException if an error occurs during the conversion
     */
    public static String jsonToCsv(JsonNode jsonNode) throws JsonProcessingException {
        if (!jsonNode.isArray()) {
            logger.error("Provided JSON is not an array of objects for CSV conversion");
            throw new JsonProcessingException("The provided JSON must be an array of objects to convert to CSV.") {
            };
        }

        logger.debug("Converting JsonNode to CSV format");
        StringBuilder csvBuilder = new StringBuilder();

        // Extract headers from the first record (if available)
        Iterator<String> fieldNames = jsonNode.elements().next().fieldNames();
        List<String> headers = new ArrayList<>();
        fieldNames.forEachRemaining(headers::add);

        // Append headers to CSV
        csvBuilder.append(String.join(",", headers)).append("\n");

        // Append rows to CSV
        for (JsonNode node : jsonNode) {
            List<String> row = new ArrayList<>();
            for (String field : headers) {
                row.add(node.path(field).asText());
            }
            csvBuilder.append(String.join(",", row)).append("\n");
        }

        return csvBuilder.toString();
    }

    /**
     * Converts a JsonNode to a YAML string.
     *
     * @param jsonNode the JsonNode to convert
     * @return the YAML string representation of the JsonNode
     * @throws JsonProcessingException if an error occurs during the conversion
     */
    public static String jsonToYaml(JsonNode jsonNode) throws JsonProcessingException {
        logger.debug("Converting JsonNode to YAML format");
        return yamlMapper.writeValueAsString(jsonNode);
    }

    /**
     * Converts a JsonNode to Properties format (key=value pairs).
     *
     * @param jsonNode the JsonNode to convert
     * @return the Properties format string
     * @throws IOException the io exception
     */
    public static String jsonToProperties(JsonNode jsonNode) throws IOException {
        logger.debug("Converting JsonNode to Properties format");
        Properties properties = new Properties();
        convertJsonNodeToProperties(jsonNode, properties, "");

        StringWriter writer = new StringWriter();
        properties.store(writer, "Generated Properties from JSON");
        return writer.toString();
    }

    /**
     * Recursively converts a JsonNode to Properties.
     * Used internally for deep conversion of nested structures.
     *
     * @param jsonNode   the JsonNode to convert
     * @param properties the Properties object to populate
     * @param prefix     the key prefix to handle nested nodes
     */
    private static void convertJsonNodeToProperties(JsonNode jsonNode, Properties properties, String prefix) {
        if (jsonNode.isObject()) {
            jsonNode.fields().forEachRemaining(entry -> {
                String newPrefix = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
                convertJsonNodeToProperties(entry.getValue(), properties, newPrefix);
            });
        } else if (jsonNode.isValueNode()) {
            properties.setProperty(prefix, jsonNode.asText());
        }
    }

    /**
     * Pretty prints a JsonNode with indentation.
     *
     * @param jsonNode the JsonNode to pretty print
     * @return the pretty-printed JSON string
     * @throws JsonProcessingException if an error occurs during the conversion
     */
    public static String prettyPrintJson(JsonNode jsonNode) throws JsonProcessingException {
        logger.debug("Pretty printing JsonNode");
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
    }

    // --- Reverse Methods for Converting Other Formats Back to JSON ---

    /**
     * Converts an XML string to a JsonNode.
     *
     * @param xml the XML string to convert
     * @return the JsonNode representation of the XML
     * @throws JsonProcessingException if an error occurs during the conversion
     */
    public static JsonNode xmlToJson(String xml) throws JsonProcessingException {
        logger.debug("Converting XML string to JsonNode");
        return xmlMapper.readTree(xml);
    }

    /**
     * Object to node json node.
     *
     * @param object the object
     * @return the json node
     */
    public static JsonNode objectToNode(Object object) {
        logger.debug("Converting Object to JsonNode");
        return objectMapper.valueToTree(object);
    }

    /**
     * Node to object t.
     *
     * @param <T>       the type parameter
     * @param node      the node
     * @param valueType the value type
     * @return the t
     * @throws JsonProcessingException the json processing exception
     */
    public static <T> T nodeToObject(JsonNode node, Class<T> valueType) throws JsonProcessingException {
        logger.debug("Converting Object to JsonNode");
        return objectMapper.treeToValue(node, valueType);
    }

    /**
     * Converts a CSV string to a JsonNode.
     * Assumes the first line is a header row.
     *
     * @param csv the CSV string to convert
     * @return the JsonNode representation of the CSV
     * @throws IOException the io exception
     */
    public static JsonNode csvToJson(String csv) throws IOException {
        logger.debug("Converting CSV string to JsonNode");
        CsvSchema schema = csvMapper.schemaFor(Map.class).withHeader().withColumnSeparator(',');
        List<Object> records = csvMapper.readerFor(Map.class).with(schema).readValues(csv).readAll();

        return objectMapper.valueToTree(records);
    }

    /**
     * Converts a YAML string to a JsonNode.
     *
     * @param yaml the YAML string to convert
     * @return the JsonNode representation of the YAML
     * @throws JsonProcessingException if an error occurs during the conversion
     */
    public static JsonNode yamlToJson(String yaml) throws JsonProcessingException {
        logger.debug("Converting YAML string to JsonNode");
        return yamlMapper.readTree(yaml);
    }

    /**
     * Converts a Properties file string to a JsonNode.
     *
     * @param properties the Properties file string to convert
     * @return the JsonNode representation of the properties
     * @throws IOException if an error occurs during reading or conversion
     */
    public static JsonNode propertiesToJson(String properties) throws IOException {
        logger.debug("Converting Properties string to JsonNode");
        Properties props = new Properties();
        props.load(new StringReader(properties));

        Map<String, Object> map = new HashMap<>();
        for (String key : props.stringPropertyNames()) {
            String[] keys = key.split("\\.");
            Map<String, Object> currentMap = map;
            for (int i = 0; i < keys.length - 1; i++) {
                currentMap = (Map<String, Object>) currentMap.computeIfAbsent(keys[i], k -> new HashMap<>());
            }
            currentMap.put(keys[keys.length - 1], props.getProperty(key));
        }

        return objectMapper.valueToTree(map);
    }

    /**
     * Converts a pretty-printed JSON string back to a JsonNode.
     *
     * @param json the pretty-printed JSON string
     * @return the JsonNode representation of the JSON string
     * @throws JsonProcessingException if an error occurs during the conversion
     */
    public static JsonNode prettyPrintJsonToJsonNode(String json) throws JsonProcessingException {
        logger.debug("Converting Pretty Printed JSON string back to JsonNode");
        return objectMapper.readTree(json);
    }

    // --- JSON (De)Serialization Methods ---

    /**
     * Converts a JSON string to an object of type T.
     *
     * @param <T>       the target type
     * @param json      the JSON string
     * @param valueType the target type class
     * @return the object representation of the JSON
     * @throws JsonParseException   if an error occurs during parsing
     * @throws JsonMappingException if an error occurs during mapping
     * @throws IOException          if an IO error occurs
     */
    public static <T> T fromJson(String json, Class<T> valueType) throws JsonParseException, JsonMappingException, IOException {
        logger.debug("Converting JSON string to object of type {}", valueType.getName());
        return objectMapper.readValue(json, valueType);
    }

    /**
     * Converts an object to a JSON string.
     *
     * @param obj the object to convert
     * @return the JSON string representation of the object
     * @throws JsonGenerationException if an error occurs during generation
     * @throws JsonMappingException    if an error occurs during mapping
     * @throws IOException             if an IO error occurs
     */
    public static String toJson(Object obj) throws JsonGenerationException, JsonMappingException, IOException {
        logger.debug("Converting object to JSON string");
        return objectMapper.writeValueAsString(obj);
    }

    // --- JSON File Methods ---

    /**
     * Converts a JSON file to an object of type T.
     *
     * @param <T>        the target type
     * @param jsonReader the file reader for the JSON file
     * @param valueType  the target type class
     * @return the object representation of the JSON file
     * @throws JsonParseException   if an error occurs during parsing
     * @throws JsonMappingException if an error occurs during mapping
     * @throws IOException          if an IO error occurs
     */
    public static <T> T fromJsonFile(FileReader jsonReader, Class<T> valueType) throws JsonParseException, JsonMappingException, IOException {
        logger.debug("Converting JSON file to object of type {}", valueType.getName());
        return objectMapper.readValue(jsonReader, valueType);
    }

    /**
     * Converts an object to a JSON file.
     *
     * @param jsonWriter the file writer for the JSON file
     * @param obj        the object to convert
     * @throws JsonParseException   if an error occurs during parsing
     * @throws JsonMappingException if an error occurs during mapping
     * @throws IOException          if an IO error occurs
     */
    public static void toJsonFile(FileWriter jsonWriter, Object obj) throws JsonParseException, JsonMappingException, IOException {
        logger.debug("Writing object to JSON file");
        objectMapper.writeValue(jsonWriter, obj);
    }

    // --- JSON Schema Validation Methods ---

    /**
     * Validates a JSON file against a JSON schema.
     *
     * @param jsonFilePath   the path to the JSON file
     * @param schemaFilePath the path to the schema file
     * @param schemaLanguage the schema language version
     * @return a set of validation messages
     * @throws IOException if an error occurs during the validation process
     */
    static Set<ValidationMessage> validateJson(String jsonFilePath, String schemaFilePath, String schemaLanguage) throws IOException {
        File schemaFile = new File(schemaFilePath);
        File jsonFile = new File(jsonFilePath);

        if (!schemaFile.exists() || !schemaFile.isFile()) {
            logger.error("Schema file not found: {}", schemaFilePath);
            throw new FileNotFoundException("Schema file not found: " + schemaFilePath);
        }

        if (!jsonFile.exists() || !jsonFile.isFile()) {
            logger.error("JSON file not found: {}", jsonFilePath);
            throw new FileNotFoundException("JSON file not found: " + jsonFilePath);
        }

        JsonSchema jsonSchema = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4).getSchema(new FileInputStream(schemaFile));
        JsonNode jsonNode = objectMapper.readTree(new FileInputStream(jsonFile));

        logger.debug("Validating JSON against schema: {}", schemaFilePath);
        return jsonSchema.validate(jsonNode);
    }

    /**
     * Computes the difference between two object states, returning an ObjectNode
     * containing only the changed fields with their old and new values.
     *
     * @param previousState The previous state object
     * @param currentState  The current state object
     * @return ObjectNode containing the differences
     * @throws IllegalArgumentException if conversion to Map fails
     */
    public static ObjectNode computeDiff(Object previousState, Object currentState) {
        // Handle null cases efficiently
        if (previousState == null && currentState == null) {
            return objectMapper.createObjectNode();
        }
        if (previousState == null || currentState == null) {
            return createFullDiff(previousState, currentState);
        }

        try {
            // Convert objects to maps with type safety
            Map<String, Object> prevMap = convertToMap(previousState);
            Map<String, Object> currMap = convertToMap(currentState);

            ObjectNode diff = objectMapper.createObjectNode();

            // Process all keys from both maps to handle additions and removals
            Set<String> allKeys = new HashSet<>(prevMap.keySet());
            allKeys.addAll(currMap.keySet());

            for (String key : allKeys) {
                if (EXCLUDED_KEYS.contains(key)) {
                    continue;
                }

                Object prevValue = prevMap.get(key);
                Object currValue = currMap.get(key);

                if (!deepEquals(prevValue, currValue)) {
                    ObjectNode change = objectMapper.createObjectNode();
                    change.putPOJO("old", prevValue);
                    change.putPOJO("new", currValue);
                    diff.set(key, change);
                }
            }

            return diff;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to compute diff: " + e.getMessage(), e);
        }
    }

    /**
     * Converts an object to a Map with proper error handling.
     */
    private static Map<String, Object> convertToMap(Object obj) {
        if (obj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) obj;
            return map;
        }
        try {
            return objectMapper.convertValue(obj, Map.class);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Failed to convert object to Map: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a diff when one of the states is null.
     */
    private static ObjectNode createFullDiff(Object previousState, Object currentState) {
        ObjectNode diff = objectMapper.createObjectNode();
        ObjectNode change = objectMapper.createObjectNode();
        change.putPOJO("old", previousState);
        change.putPOJO("new", currentState);
        diff.set("state", change);
        return diff;
    }

    /**
     * Performs a deep comparison of two objects, handling collections and nested objects.
     */
    private static boolean deepEquals(Object a, Object b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        if (a.getClass() != b.getClass()) {
            return false;
        }

        if (a instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> mapA = (Map<String, Object>) a;
            @SuppressWarnings("unchecked")
            Map<String, Object> mapB = (Map<String, Object>) b;
            if (!mapA.keySet().equals(mapB.keySet())) {
                return false;
            }
            for (String key : mapA.keySet()) {
                if (!deepEquals(mapA.get(key), mapB.get(key))) {
                    return false;
                }
            }
            return true;
        }

        if (a instanceof Iterable) {
            Iterable<?> iterA = (Iterable<?>) a;
            Iterable<?> iterB = (Iterable<?>) b;
            java.util.Iterator<?> itA = iterA.iterator();
            java.util.Iterator<?> itB = iterB.iterator();
            while (itA.hasNext() && itB.hasNext()) {
                if (!deepEquals(itA.next(), itB.next())) {
                    return false;
                }
            }
            return !itA.hasNext() && !itB.hasNext();
        }

        return Objects.equals(a, b);
    }
}
