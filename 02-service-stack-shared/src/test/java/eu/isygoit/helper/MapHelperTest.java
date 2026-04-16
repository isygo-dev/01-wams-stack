package eu.isygoit.helper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MapHelper Tests")
class MapHelperTest {

    @Nested
    @DisplayName("convertStringToMap() Tests")
    class ConvertStringToMapTests {

        @Test
        @DisplayName("should convert valid string to map")
        void testConvertStringToMap_valid() {
            String data = "key1:val1,key2:val2";
            Map<String, String> result = MapHelper.convertStringToMap(data, ",");
            assertEquals(2, result.size());
            assertEquals("val1", result.get("key1"));
            assertEquals("val2", result.get("key2"));
        }

        @Test
        @DisplayName("should return empty map for null or empty input")
        void testConvertStringToMap_nullOrEmpty() {
            assertTrue(MapHelper.convertStringToMap(null, ",").isEmpty());
            assertTrue(MapHelper.convertStringToMap("", ",").isEmpty());
        }
    }

    @Nested
    @DisplayName("convertStringArrayToMap() Tests")
    class ConvertStringArrayToMapTests {

        @Test
        @DisplayName("should convert string array to map")
        void testConvertStringArrayToMap_valid() {
            String[] data = {"key1:val1", "key2:val2"};
            Map<String, String> result = MapHelper.convertStringArrayToMap(data);
            assertEquals(2, result.size());
            assertEquals("val1", result.get("key1"));
        }

        @Test
        @DisplayName("should return empty map for null input")
        void testConvertStringArrayToMap_null() {
            assertTrue(MapHelper.convertStringArrayToMap(null).isEmpty());
        }
    }

    @Nested
    @DisplayName("mergeMaps() Tests")
    class MergeMapsTests {

        @Test
        @DisplayName("should merge two maps with custom function")
        void testMergeMaps() {
            Map<String, String> map1 = new HashMap<>(Map.of("k1", "v1", "k2", "v2"));
            Map<String, String> map2 = Map.of("k2", "v2-new", "k3", "v3");

            Map<String, String> result = MapHelper.mergeMaps(map1, map2, (v1, v2) -> v2);

            assertEquals(3, result.size());
            assertEquals("v2-new", result.get("k2"));
            assertEquals("v3", result.get("k3"));
        }
    }

    @Nested
    @DisplayName("filterMapByValue() Tests")
    class FilterMapByValueTests {

        @Test
        @DisplayName("should filter map by value condition")
        void testFilterMapByValue() {
            Map<String, String> map = Map.of("k1", "apple", "k2", "banana", "k3", "apricot");
            Map<String, String> result = MapHelper.filterMapByValue(map, v -> v.startsWith("ap"));

            assertEquals(2, result.size());
            assertTrue(result.containsKey("k1"));
            assertTrue(result.containsKey("k3"));
        }
    }

    @Nested
    @DisplayName("mapToString() Tests")
    class MapToStringTests {

        @Test
        @DisplayName("should convert map to string")
        void testMapToString() {
            Map<String, String> map = Map.of("k1", "v1", "k2", "v2");
            String result = MapHelper.mapToString(map, ",");
            assertTrue(result.contains("k1:v1"));
            assertTrue(result.contains("k2:v2"));
            assertTrue(result.contains(","));
        }
    }

    @Nested
    @DisplayName("invertMap() Tests")
    class InvertMapTests {

        @Test
        @DisplayName("should invert map keys and values")
        void testInvertMap() {
            Map<String, String> map = Map.of("k1", "v1", "k2", "v2");
            Map<String, String> result = MapHelper.invertMap(map);
            assertEquals("k1", result.get("v1"));
            assertEquals("k2", result.get("v2"));
        }
    }

    @Nested
    @DisplayName("safeGet() Tests")
    class SafeGetTests {

        @Test
        @DisplayName("should return value or default if not found")
        void testSafeGet() {
            Map<String, String> map = Map.of("k1", "v1");
            assertEquals("v1", MapHelper.safeGet(map, "k1", "def"));
            assertEquals("def", MapHelper.safeGet(map, "k2", "def"));
            assertEquals("def", MapHelper.safeGet(null, "k1", "def"));
        }
    }

    @Nested
    @DisplayName("getMapDifference() Tests")
    class GetMapDifferenceTests {

        @Test
        @DisplayName("should return entries in map1 not in map2")
        void testGetMapDifference() {
            Map<String, String> map1 = Map.of("k1", "v1", "k2", "v2");
            Map<String, String> map2 = Map.of("k1", "v1", "k3", "v3");
            Map<String, String> result = MapHelper.getMapDifference(map1, map2);

            assertEquals(1, result.size());
            assertTrue(result.containsKey("k2"));
        }
    }

    @Nested
    @DisplayName("combineMaps() Tests")
    class CombineMapsTests {

        @Test
        @DisplayName("should combine multiple maps")
        void testCombineMaps() {
            Map<String, String> map1 = Map.of("k1", "v1");
            Map<String, String> map2 = Map.of("k2", "v2");
            Map<String, String> map3 = Map.of("k3", "v3");

            Map<String, String> result = MapHelper.combineMaps(map1, map2, map3);
            assertEquals(3, result.size());
        }
    }

    @Nested
    @DisplayName("removeEntriesByKey() Tests")
    class RemoveEntriesByKeyTests {

        @Test
        @DisplayName("should remove specified keys")
        void testRemoveEntriesByKey() {
            Map<String, String> map = new HashMap<>(Map.of("k1", "v1", "k2", "v2", "k3", "v3"));
            Map<String, String> result = MapHelper.removeEntriesByKey(map, List.of("k1", "k3"));

            assertEquals(1, result.size());
            assertTrue(result.containsKey("k2"));
        }
    }

    @Nested
    @DisplayName("getMapIntersection() Tests")
    class GetMapIntersectionTests {

        @Test
        @DisplayName("should return common entries based on key")
        void testGetMapIntersection() {
            Map<String, String> map1 = Map.of("k1", "v1", "k2", "v2");
            Map<String, String> map2 = Map.of("k1", "v1", "k2", "diff", "k3", "v3");
            Map<String, String> result = MapHelper.getMapIntersection(map1, map2);

            assertEquals(2, result.size());
            assertEquals("v1", result.get("k1"));
            assertEquals("v2", result.get("k2"));
        }
    }

    @Nested
    @DisplayName("replaceValue() Tests")
    class ReplaceValueTests {

        @Test
        @DisplayName("should replace value for key")
        void testReplaceValue() {
            Map<String, String> map = new HashMap<>(Map.of("k1", "v1"));
            String oldVal = MapHelper.replaceValue(map, "k1", "v2");

            assertEquals("v1", oldVal);
            assertEquals("v2", map.get("k1"));
        }
    }

    @Nested
    @DisplayName("getMapSubSet() Tests")
    class GetMapSubSetTests {

        @Test
        @DisplayName("should return subset of map")
        void testGetMapSubSet() {
            Map<String, String> map = Map.of("k1", "v1", "k2", "v2", "k3", "v3");
            Map<String, String> result = MapHelper.getMapSubSet(map, List.of("k1", "k3"));

            assertEquals(2, result.size());
            assertTrue(result.containsKey("k1"));
            assertTrue(result.containsKey("k3"));
        }
    }

    @Nested
    @DisplayName("getKeysStartingWith() Tests")
    class GetKeysStartingWithTests {

        @Test
        @DisplayName("should return entries where key starts with prefix")
        void testGetKeysStartingWith() {
            Map<String, String> map = Map.of("pre_k1", "v1", "other_k2", "v2", "pre_k3", "v3");
            Map<String, String> result = MapHelper.getKeysStartingWith(map, "pre_");

            assertEquals(2, result.size());
            assertTrue(result.containsKey("pre_k1"));
            assertTrue(result.containsKey("pre_k3"));
        }
    }

    @Nested
    @DisplayName("mergeMapsWithPredicate() Tests")
    class MergeMapsWithPredicateTests {

        @Test
        @DisplayName("should merge maps using predicate")
        void testMergeMapsWithPredicate() {
            Map<String, String> map1 = Map.of("k1", "v1");
            Map<String, String> map2 = Map.of("k2", "v2", "k3", "v3");

            Map<String, String> result = MapHelper.mergeMapsWithPredicate(map1, map2, entry -> !entry.getKey().equals("k3"));

            assertEquals(2, result.size());
            assertTrue(result.containsKey("k1"));
            assertTrue(result.containsKey("k2"));
            assertFalse(result.containsKey("k3"));
        }
    }
}
