package eu.isygoit.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;

/**
 * The JsonHelper class provides utility methods for handling JSON data,
 * including parsing JSON from/to strings/files, validating against schemas,
 * and converting JSON to/from various other formats such as XML, CSV, YAML, and properties.
 */
public interface JsonHelper {

    Logger logger = LoggerFactory.getLogger(JsonHelper.class);

    public static final ObjectMapper mapper = new ObjectMapper();
    public static final XmlMapper xmlMapper = new XmlMapper();
    public static final YAMLMapper yamlMapper = new YAMLMapper();
    public static final CsvMapper csvMapper = new CsvMapper();

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
     * Assumes the JsonNode is an array of objects (e.g., a list of records).
     *
     * @param jsonNode the JsonNode to convert
     * @return the CSV string representation of the JsonNode
     * @throws JsonProcessingException if an error occurs during the conversion
     */
    public static String jsonToCsv(JsonNode jsonNode) throws JsonProcessingException {
        // Ensure the JSON is an array of objects (records)
        if (!jsonNode.isArray()) {
            throw new JsonProcessingException("The provided JSON must be an array of objects to convert to CSV.") {
            };
        }

        // Use a StringBuilder to construct the CSV output
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
     * Converts a JsonNode to a Properties file format (key=value pairs).
     *
     * @param jsonNode the JsonNode to convert
     * @return the Properties file format string
     * @throws JsonProcessingException if an error occurs during the conversion
     */
    public static String jsonToProperties(JsonNode jsonNode) throws IOException {
        logger.debug("Converting JsonNode to Properties file format");

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
    public static void convertJsonNodeToProperties(JsonNode jsonNode, Properties properties, String prefix) {
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
        logger.debug("Pretty printing JsonNode for better readability");
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
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
     * Converts a CSV string to a JsonNode.
     * Assumes the first line is a header row.
     *
     * @param csv the CSV string to convert
     * @return the JsonNode representation of the CSV
     * @throws JsonProcessingException if an error occurs during the conversion
     */
    public static JsonNode csvToJson(String csv) throws IOException {
        logger.debug("Converting CSV string to JsonNode");

        // Define CSV schema assuming the first row is the header
        CsvSchema schema = csvMapper.schemaFor(Map.class).withHeader().withColumnSeparator(',');
        List<Object> records = csvMapper.readerFor(Map.class).with(schema).readValues(csv).readAll();

        // Return the records as a JsonNode
        return mapper.valueToTree(records);
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
     * The properties are assumed to be in key=value format, where nested properties are represented by dot notation.
     *
     * @param properties the Properties file string to convert
     * @return the JsonNode representation of the properties
     * @throws IOException if an error occurs during reading or conversion
     */
    public static JsonNode propertiesToJson(String properties) throws IOException {
        logger.debug("Converting Properties string to JsonNode");

        // Load the properties from the provided string
        Properties props = new Properties();
        props.load(new StringReader(properties));

        // Convert properties into a nested map structure
        Map<String, Object> map = new HashMap<>();
        for (String key : props.stringPropertyNames()) {
            String[] keys = key.split("\\.");
            Map<String, Object> currentMap = map;
            for (int i = 0; i < keys.length - 1; i++) {
                currentMap = (Map<String, Object>) currentMap.computeIfAbsent(keys[i], k -> new HashMap<>());
            }
            currentMap.put(keys[keys.length - 1], props.getProperty(key));
        }

        // Return the map as a JsonNode
        return mapper.valueToTree(map);
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
        return mapper.readTree(json);
    }
}