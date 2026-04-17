package eu.isygoit.helper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ByteArrayHelper Tests")
class ByteArrayHelperTest {

    @Test
    @DisplayName("printByteArray() should not throw exception")
    void testPrintByteArray() {
        assertDoesNotThrow(() -> ByteArrayHelper.printByteArray(new byte[]{0x01}));
        assertDoesNotThrow(() -> ByteArrayHelper.printByteArray(null));
    }

    @Nested
    @DisplayName("convertBytesToHex() Tests")
    class ConvertBytesToHexTests {

        @Test
        @DisplayName("should convert byte array to hex string")
        void testConvertBytesToHex_valid() {
            byte[] bytes = {0x01, 0x0A, (byte) 0xFF};
            assertEquals("010AFF", ByteArrayHelper.convertBytesToHex(bytes));
        }

        @Test
        @DisplayName("should return empty string for null input")
        void testConvertBytesToHex_null() {
            assertEquals("", ByteArrayHelper.convertBytesToHex(null));
        }

        @Test
        @DisplayName("should return empty string for empty array")
        void testConvertBytesToHex_empty() {
            assertEquals("", ByteArrayHelper.convertBytesToHex(new byte[0]));
        }
    }

    @Nested
    @DisplayName("calculateChecksum() Tests")
    class CalculateChecksumTests {

        @Test
        @DisplayName("should calculate CRC32 checksum")
        void testCalculateChecksum_valid() throws IOException {
            byte[] bytes = "Hello World".getBytes();
            long checksum = ByteArrayHelper.calculateChecksum(bytes);
            assertTrue(checksum != 0);
            assertEquals(checksum, ByteArrayHelper.calculateChecksum(bytes));
        }

        @Test
        @DisplayName("should return 0 for null input")
        void testCalculateChecksum_null() throws IOException {
            assertEquals(0, ByteArrayHelper.calculateChecksum(null));
        }

        @Test
        @DisplayName("should return 0 for empty array")
        void testCalculateChecksum_empty() throws IOException {
            assertEquals(0, ByteArrayHelper.calculateChecksum(new byte[0]));
        }
    }

    @Nested
    @DisplayName("convertHexToBytes() Tests")
    class ConvertHexToBytesTests {

        @Test
        @DisplayName("should convert hex string to byte array")
        void testConvertHexToBytes_valid() {
            String hex = "010AFF";
            byte[] expected = {0x01, 0x0A, (byte) 0xFF};
            assertArrayEquals(expected, ByteArrayHelper.convertHexToBytes(hex));
        }

        @Test
        @DisplayName("should return empty array for null or empty input")
        void testConvertHexToBytes_nullOrEmpty() {
            assertArrayEquals(new byte[0], ByteArrayHelper.convertHexToBytes(null));
            assertArrayEquals(new byte[0], ByteArrayHelper.convertHexToBytes(""));
        }
    }

    @Nested
    @DisplayName("Serialization/Deserialization Tests")
    class SerializationTests {

        @Test
        @DisplayName("should serialize and deserialize an object")
        void testSerializeDeserialize() throws IOException, ClassNotFoundException {
            TestObject original = new TestObject("test", 123);
            byte[] serialized = ByteArrayHelper.serializeObject(original);
            assertNotNull(serialized);
            assertTrue(serialized.length > 0);

            TestObject deserialized = (TestObject) ByteArrayHelper.deserializeObject(serialized);
            assertEquals(original, deserialized);
        }

        @Test
        @DisplayName("serializeObject should return empty array for null")
        void testSerializeObject_null() throws IOException {
            assertArrayEquals(new byte[0], ByteArrayHelper.serializeObject(null));
        }

        @Test
        @DisplayName("deserializeObject should return null for null or empty array")
        void testDeserializeObject_nullOrEmpty() throws IOException, ClassNotFoundException {
            assertNull(ByteArrayHelper.deserializeObject(null));
            assertNull(ByteArrayHelper.deserializeObject(new byte[0]));
        }

        static class TestObject implements Serializable {
            String name;
            int value;

            TestObject(String name, int value) {
                this.name = name;
                this.value = value;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                TestObject that = (TestObject) o;
                return value == that.value && java.util.Objects.equals(name, that.name);
            }

            @Override
            public int hashCode() {
                return java.util.Objects.hash(name, value);
            }
        }
    }

    @Nested
    @DisplayName("isNullOrEmpty() Tests")
    class IsNullOrEmptyTests {

        @Test
        @DisplayName("should return true for null or empty array")
        void testIsNullOrEmpty() {
            assertTrue(ByteArrayHelper.isNullOrEmpty(null));
            assertTrue(ByteArrayHelper.isNullOrEmpty(new byte[0]));
            assertFalse(ByteArrayHelper.isNullOrEmpty(new byte[]{0x01}));
        }
    }

    @Nested
    @DisplayName("reverseByteArray() Tests")
    class ReverseByteArrayTests {

        @Test
        @DisplayName("should reverse byte array")
        void testReverseByteArray() {
            byte[] original = {0x01, 0x02, 0x03};
            byte[] expected = {0x03, 0x02, 0x01};
            assertArrayEquals(expected, ByteArrayHelper.reverseByteArray(original));
        }

        @Test
        @DisplayName("should return null/empty for null/empty input")
        void testReverseByteArray_nullOrEmpty() {
            assertNull(ByteArrayHelper.reverseByteArray(null));
            assertArrayEquals(new byte[0], ByteArrayHelper.reverseByteArray(new byte[0]));
        }
    }

    @Nested
    @DisplayName("areArraysEqual() Tests")
    class AreArraysEqualTests {

        @Test
        @DisplayName("should compare arrays for equality")
        void testAreArraysEqual() {
            byte[] a = {0x01, 0x02};
            byte[] b = {0x01, 0x02};
            byte[] c = {0x01, 0x03};
            assertTrue(ByteArrayHelper.areArraysEqual(a, b));
            assertFalse(ByteArrayHelper.areArraysEqual(a, c));
            assertFalse(ByteArrayHelper.areArraysEqual(a, null));
        }
    }

    @Nested
    @DisplayName("getSubarray() Tests")
    class GetSubarrayTests {

        @Test
        @DisplayName("should return correct subarray")
        void testGetSubarray() {
            byte[] original = {0x01, 0x02, 0x03, 0x04};
            byte[] expected = {0x02, 0x03};
            assertArrayEquals(expected, ByteArrayHelper.getSubarray(original, 1, 3));
        }
    }

    @Nested
    @DisplayName("getByteAtIndex() Tests")
    class GetByteAtIndexTests {

        @Test
        @DisplayName("should return byte at index")
        void testGetByteAtIndex() {
            byte[] bytes = {0x01, 0x02, 0x03};
            assertEquals((byte) 0x02, ByteArrayHelper.getByteAtIndex(bytes, 1));
        }

        @Test
        @DisplayName("should throw IndexOutOfBoundsException for invalid index")
        void testGetByteAtIndex_invalidIndex() {
            byte[] bytes = {0x01};
            assertThrows(IndexOutOfBoundsException.class, () -> ByteArrayHelper.getByteAtIndex(bytes, 2));
            assertThrows(IndexOutOfBoundsException.class, () -> ByteArrayHelper.getByteAtIndex(bytes, -1));
            assertThrows(IndexOutOfBoundsException.class, () -> ByteArrayHelper.getByteAtIndex(null, 0));
        }
    }

    @Nested
    @DisplayName("Base64 Conversion Tests")
    class Base64Tests {

        @Test
        @DisplayName("should convert bytes to base64 and back")
        void testBase64Conversion() {
            byte[] bytes = "Hello".getBytes();
            String base64 = ByteArrayHelper.convertBytesToBase64(bytes);
            assertEquals("SGVsbG8=", base64);
            assertArrayEquals(bytes, ByteArrayHelper.convertBase64ToBytes(base64));
        }

        @Test
        @DisplayName("should handle null or empty base64")
        void testBase64_nullOrEmpty() {
            assertEquals("", ByteArrayHelper.convertBytesToBase64(null));
            assertEquals("", ByteArrayHelper.convertBytesToBase64(new byte[0]));
            assertArrayEquals(new byte[0], ByteArrayHelper.convertBase64ToBytes(null));
            assertArrayEquals(new byte[0], ByteArrayHelper.convertBase64ToBytes(""));
        }
    }

    @Nested
    @DisplayName("splitArrayIntoChunks() Tests")
    class SplitArrayIntoChunksTests {

        @Test
        @DisplayName("should split array into chunks")
        void testSplitArrayIntoChunks() {
            byte[] array = {0x01, 0x02, 0x03, 0x04, 0x05};
            byte[][] chunks = ByteArrayHelper.splitArrayIntoChunks(array, 2);
            assertEquals(3, chunks.length);
            assertArrayEquals(new byte[]{0x01, 0x02}, chunks[0]);
            assertArrayEquals(new byte[]{0x03, 0x04}, chunks[1]);
            assertArrayEquals(new byte[]{0x05}, chunks[2]);
        }

        @Test
        @DisplayName("should return empty for null array or invalid chunk size")
        void testSplitArrayIntoChunks_invalid() {
            assertArrayEquals(new byte[0][], ByteArrayHelper.splitArrayIntoChunks(null, 2));
            assertArrayEquals(new byte[0][], ByteArrayHelper.splitArrayIntoChunks(new byte[]{0x01}, 0));
        }
    }
}
