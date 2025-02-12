package eu.isygoit.helper;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Set;

/**
 * The type Json helper.
 */
@Slf4j
@Component
public final class JsonHelper {

    private static final ObjectMapper mapper = new ObjectMapper();

    private JsonHelper() {
        super();
    }

    /**
     * From json t.
     *
     * @param <E>       the type parameter
     * @param json      the json
     * @param valueType the value type
     * @return the t
     * @throws JsonParseException   the json parse exception
     * @throws JsonMappingException the json mapping exception
     * @throws IOException          the io exception
     */
    public static <E> E fromJson(String json, Class<E> valueType) throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, valueType);
    }

    /**
     * To json string.
     *
     * @param obj the obj
     * @return the string
     * @throws JsonGenerationException the json generation exception
     * @throws JsonMappingException    the json mapping exception
     * @throws IOException             the io exception
     */
    public static String toJson(Object obj) throws JsonGenerationException, JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(obj);
    }

    /**
     * From json file t.
     *
     * @param <E>        the type parameter
     * @param jsonReader the json reader
     * @param valueType  the value type
     * @return the t
     * @throws JsonParseException   the json parse exception
     * @throws JsonMappingException the json mapping exception
     * @throws IOException          the io exception
     */
    public static <E> E fromJsonFile(FileReader jsonReader, Class<E> valueType) throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(jsonReader, valueType);
    }

    /**
     * To json file.
     *
     * @param jsonWriter the json writer
     * @param obj        the obj
     * @throws JsonParseException   the json parse exception
     * @throws JsonMappingException the json mapping exception
     * @throws IOException          the io exception
     */
    public static void toJsonFile(FileWriter jsonWriter, Object obj) throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(jsonWriter, obj);
    }

    /**
     * Validate json set.
     *
     * @param jsonFilePath   the json file path
     * @param schemaFilePath the schema file path
     * @param schemaLanguage the schema language
     * @return the set
     * @throws IOException the io exception
     */
    public static Set<ValidationMessage> validateJson(String jsonFilePath, String schemaFilePath, String schemaLanguage) throws IOException {
        File schemaFile = new File(schemaFilePath);
        if (!schemaFile.exists() || !schemaFile.isFile()) {
            throw new FileNotFoundException("Schema file not found: " + schemaFilePath);
        }

        File jsonFile = new File(jsonFilePath);
        if (!jsonFile.exists() || !jsonFile.isFile()) {
            throw new FileNotFoundException("JSON file not found: " + jsonFilePath);
        }

        JsonSchema jsonSchema = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4).getSchema(new FileInputStream(schemaFile));
        JsonNode jsonNode = mapper.readTree(new FileInputStream(jsonFile));

        return jsonSchema.validate(jsonNode);
    }

    /**
     * Validate json set.
     *
     * @param jsonFile       the json file
     * @param schemaFile     the schema file
     * @param schemaLanguage the schema language
     * @return the set
     * @throws IOException the io exception
     */
    public static Set<ValidationMessage> validateJson(File jsonFile, File schemaFile, String schemaLanguage) throws IOException {
        if (!schemaFile.exists() || !schemaFile.isFile()) {
            throw new FileNotFoundException("Schema file not found: " + schemaFile);
        }

        if (!jsonFile.exists() || !jsonFile.isFile()) {
            throw new FileNotFoundException("JSON file not found: " + jsonFile);
        }

        JsonSchema jsonSchema = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4).getSchema(new FileInputStream(schemaFile));
        JsonNode jsonNode = mapper.readTree(new FileInputStream(jsonFile));

        return jsonSchema.validate(jsonNode);
    }
}
