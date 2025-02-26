package eu.isygoit.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The interface MapHelper provides utility methods for converting strings to Maps and various map operations.
 * It includes methods for common map manipulations such as merging, filtering, and retrieving subsets.
 */
public interface MapHelper {

    /**
     * The constant logger.
     */
    Logger logger = LoggerFactory.getLogger(MapHelper.class);

    /**
     * Converts a string into a Map using a specified delimiter. The input string should consist of key-value pairs
     * separated by the delimiter, and the key-value pairs should be separated by a colon.
     *
     * @param data  the string containing key-value pairs separated by the delimiter
     * @param delim the delimiter to split the data (e.g., "," or ";")
     * @return a Map containing the parsed key-value pairs
     */
    public static Map<String, String> convertStringToMap(String data, String delim) {
        if (data == null || data.isEmpty()) {
            logger.warn("Input data is null or empty.");
            return new HashMap<>();
        }

        logger.info("Converting string data to map using delimiter '{}'.", delim);

        return Stream.of(data.split(delim))
                .map(token -> token.split(":"))
                .filter(parts -> parts.length == 2)  // Ensure that each token contains exactly 2 parts (key and value)
                .collect(Collectors.toMap(parts -> parts[0], parts -> parts[1]));
    }

    /**
     * Converts an array of strings into a Map. Each string must follow the format "key:value".
     *
     * @param data the array containing key-value pairs as strings
     * @return a Map containing the parsed key-value pairs
     */
    public static Map<String, String> convertStringArrayToMap(String[] data) {
        logger.info("Converting an array of size {} to a Map.", data.length);

        return Optional.ofNullable(data)
                .map(arr -> Stream.of(arr)
                        .map(keyValue -> keyValue.split(":"))
                        .filter(parts -> parts.length == 2)
                        .collect(Collectors.toMap(parts -> parts[0], parts -> parts[1])))
                .orElseGet(() -> {
                    logger.warn("Input array is null or empty.");
                    return new HashMap<>();
                });
    }

    /**
     * Merges two maps, with the option to provide a custom merge strategy for conflicting keys.
     *
     * @param map1          the first map
     * @param map2          the second map
     * @param mergeFunction the function to merge conflicting values
     * @return a new map containing merged key-value pairs
     */
    public static Map<String, String> mergeMaps(Map<String, String> map1, Map<String, String> map2,
                                                BiFunction<String, String, String> mergeFunction) {
        // Creating a new map to store the result
        Map<String, String> mergedMap = new HashMap<>(map1);

        // For each entry in map2, merge with the corresponding entry in the mergedMap
        map2.forEach((key, value) -> mergedMap.merge(key, value, mergeFunction));

        logger.info("Maps merged successfully with custom merge function.");
        return mergedMap;
    }

    /**
     * Filters the map based on the provided value condition.
     *
     * @param map       the map to filter
     * @param condition the condition to filter the values
     * @return a new map containing only the entries that satisfy the condition
     */
    public static Map<String, String> filterMapByValue(Map<String, String> map, Predicate<String> condition) {
        logger.info("Filtering map by value...");

        return map.entrySet().stream()
                .filter(entry -> condition.test(entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Converts a Map to a string representation using the specified delimiter.
     *
     * @param map   the map to convert
     * @param delim the delimiter to separate key-value pairs
     * @return a string representation of the map
     */
    public static String mapToString(Map<String, String> map, String delim) {
        logger.info("Converting map to string with delimiter '{}'.", delim);

        return map.entrySet().stream()
                .map(entry -> entry.getKey() + ":" + entry.getValue())
                .collect(Collectors.joining(delim));
    }

    /**
     * Inverts the key-value pairs of a Map. Keys become values, and values become keys.
     *
     * @param map the map to invert
     * @return a new map with keys and values swapped
     */
    public static Map<String, String> invertMap(Map<String, String> map) {
        logger.info("Inverting map...");

        return map.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    }

    /**
     * Converts the map to a string representation of key-value pairs (e.g., "key:value").
     *
     * @param map the map to convert
     * @return a string containing all key-value pairs in the map
     */
    public static String mapToKeyValueString(Map<String, String> map) {
        logger.info("Converting map entries to key-value string representation.");

        return map.entrySet().stream()
                .map(entry -> entry.getKey() + ":" + entry.getValue())
                .collect(Collectors.joining(", "));
    }

    /**
     * Retrieves the value associated with the given key, returning a default value if the key doesn't exist.
     *
     * @param map          the map to search
     * @param key          the key to look for
     * @param defaultValue the value to return if the key is not found
     * @return the value associated with the key or the default value
     */
    public static String safeGet(Map<String, String> map, String key, String defaultValue) {
        logger.info("Retrieving value for key '{}' from map.", key);

        return Optional.ofNullable(map.get(key)).orElse(defaultValue);
    }

    /**
     * Retrieves the difference between two maps, i.e., entries that exist in the first map but not in the second.
     *
     * @param map1 the first map
     * @param map2 the second map
     * @return a new map containing entries that exist in map1 but not in map2
     */
    public static Map<String, String> getMapDifference(Map<String, String> map1, Map<String, String> map2) {
        logger.info("Calculating map difference...");

        return map1.entrySet().stream()
                .filter(entry -> !map2.containsKey(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Combines multiple maps into one. Non-null values from the maps will be used. If a key is duplicated,
     * the last non-null value will be used.
     *
     * @param maps an array of maps to combine
     * @return a new combined map
     */
    public static Map<String, String> combineMaps(Map<String, String>... maps) {
        logger.info("Combining multiple maps...");

        return Arrays.stream(maps)
                .flatMap(map -> map.entrySet().stream())
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v2));  // Resolve duplicates by taking the last value
    }

    /**
     * Checks if a key exists in the map.
     *
     * @param map the map to check
     * @param key the key to check for existence
     * @return true if the key exists, otherwise false
     */
    public static boolean keyExists(Map<String, String> map, String key) {
        return map.containsKey(key);
    }

    /**
     * Checks if a value exists in the map.
     *
     * @param map   the map to check
     * @param value the value to check for existence
     * @return true if the value exists, otherwise false
     */
    public static boolean valueExists(Map<String, String> map, String value) {
        return map.containsValue(value);
    }

    /**
     * Removes entries from the map by keys provided in a list.
     *
     * @param map  the map to remove entries from
     * @param keys the list of keys whose entries should be removed
     * @return a new map with the specified entries removed
     */
    public static Map<String, String> removeEntriesByKey(Map<String, String> map, List<String> keys) {
        logger.info("Removing entries by key...");

        return map.entrySet().stream()
                .filter(entry -> !keys.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Retrieves the intersection between two maps, i.e., entries that are present in both maps.
     *
     * @param map1 the first map
     * @param map2 the second map
     * @return a new map containing only the entries present in both maps
     */
    public static Map<String, String> getMapIntersection(Map<String, String> map1, Map<String, String> map2) {
        logger.info("Calculating map intersection...");

        return map1.entrySet().stream()
                .filter(entry -> map2.containsKey(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Replaces the value associated with a given key if it exists.
     *
     * @param map      the map to update
     * @param key      the key whose value needs to be replaced
     * @param newValue the new value to assign to the key
     * @return the previous value associated with the key or null if the key did not exist
     */
    public static String replaceValue(Map<String, String> map, String key, String newValue) {
        logger.info("Replacing value for key: {}", key);

        return map.replace(key, newValue);
    }

    /**
     * Retrieves a subset of the map based on a list of keys.
     *
     * @param map  the map to filter
     * @param keys the list of keys to retrieve from the map
     * @return a new map containing only the specified entries
     */
    public static Map<String, String> getMapSubSet(Map<String, String> map, List<String> keys) {
        logger.info("Retrieving map subset...");

        return map.entrySet().stream()
                .filter(entry -> keys.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Retrieves a map with keys that start with a specified prefix.
     *
     * @param map    the map to filter
     * @param prefix the prefix to match for keys
     * @return a new map containing entries whose keys start with the prefix
     */
    public static Map<String, String> getKeysStartingWith(Map<String, String> map, String prefix) {
        logger.info("Retrieving keys starting with prefix: {}", prefix);

        return map.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(prefix))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Merges two maps and includes entries based on a custom predicate.
     *
     * @param map1      the first map
     * @param map2      the second map
     * @param predicate the condition to include entries
     * @return a new map with merged entries satisfying the predicate
     */
    public static Map<String, String> mergeMapsWithPredicate(Map<String, String> map1, Map<String, String> map2,
                                                             Predicate<Map.Entry<String, String>> predicate) {
        logger.info("Merging maps with predicate...");

        return Stream.concat(map1.entrySet().stream(), map2.entrySet().stream())
                .filter(predicate)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}