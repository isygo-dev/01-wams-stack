package eu.isygoit.helper;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

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
        Map<String, String> map = new HashMap<>();
        StringTokenizer tokenizer = new StringTokenizer(data, delim);

        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            String[] keyValue = token.split(":");
            map.put(keyValue[0], keyValue[1]);
        }

        return map;
    }

    /**
     * Convert string array to map map.
     *
     * @param data the data
     * @return the map
     */
    static Map<String, String> convertStringArrayToMap(String[] data) {
        Map<String, String> map = new HashMap<>();

        for (String keyValue : data) {
            String[] parts = keyValue.split(":");
            map.put(parts[0], parts[1]);
        }

        return map;
    }
}
