package eu.isygoit.helper;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The interface Map helper.
 */
public interface MapHelper {

    /**
     * Convert string to map map.
     *
     * @param data  the data
     * @param delim the delim
     * @return the map
     */
    static Map<String, String> convertStringToMap(String data, String delim) {
        return Arrays.stream(data.split(delim))  // Split the string by the delimiter
                .map(token -> token.split(":"))       // Split each token by ":"
                .collect(Collectors.toMap(parts -> parts[0], parts -> parts[1]));  // Collect into a map
    }

    /**
     * Convert string array to map map.
     *
     * @param data the data
     * @return the map
     */
    static Map<String, String> convertStringArrayToMap(String[] data) {
        return Arrays.stream(data)
                .map(keyValue -> keyValue.split(":"))  // Split each string by ":"
                .collect(Collectors.toMap(parts -> parts[0], parts -> parts[1]));  // Create a map from the split parts
    }
}
