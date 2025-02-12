package eu.isygoit.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 * Utility class providing helper methods for byte array operations, such as
 * converting byte arrays to hexadecimal strings, calculating checksums, serializing
 * objects, and more. The class utilizes modern Java 17 features such as lambda expressions,
 * streams, and Optional for enhanced readability and functionality.
 * <p>
 * This class is meant to be used as a utility class and cannot be instantiated.
 */
public interface ByteArrayHelper {

    static final Logger logger = LoggerFactory.getLogger(ByteArrayHelper.class);
    static final int HEX_MASK = 0xf;

    /**
     * Converts a byte array to a hexadecimal string representation.
     *
     * @param bytes the byte array to convert.
     * @return the hexadecimal string representation of the byte array.
     */
    public static String convertBytesToHex(byte[] bytes) {
        if (bytes == null) {
            logger.warn("Input byte array is null. Returning empty string.");
            return "";
        }

        return IntStream.range(0, bytes.length)
                .mapToObj(i -> String.format("%02X", bytes[i]))
                .reduce(String::concat)
                .orElse("");
    }

    /**
     * Calculates the checksum of a byte array using CRC32.
     *
     * @param bytes the byte array to calculate the checksum for.
     * @return the checksum value.
     * @throws IOException if an I/O error occurs.
     */
    public static long calculateChecksum(byte[] bytes) throws IOException {
        if (bytes == null || bytes.length == 0) {
            logger.warn("Input byte array is empty. Returning checksum 0.");
            return 0;
        }

        Checksum checksum = new CRC32();
        checksum.update(bytes, 0, bytes.length);
        long checksumValue = checksum.getValue();

        logger.debug("Calculated CRC32 checksum for byte array: {}", checksumValue);
        return checksumValue;
    }

    /**
     * Converts a hexadecimal string to a byte array.
     *
     * @param hexString the hexadecimal string to convert.
     * @return the byte array representation of the hexadecimal string.
     */
    public static byte[] convertHexToBytes(String hexString) {
        if (hexString == null || hexString.isEmpty()) {
            logger.warn("Input hex string is null or empty. Returning empty byte array.");
            return new byte[0];
        }

        int length = hexString.length();
        byte[] byteArray = new byte[length / 2];

        for (int i = 0; i < length; i += 2) {
            byteArray[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }

        logger.debug("Converted hex string to byte array: {}", convertBytesToHex(byteArray));
        return byteArray;
    }

    /**
     * Serializes an object into a byte array.
     *
     * @param object the object to serialize.
     * @return the byte array representing the object.
     * @throws IOException if an I/O error occurs during serialization.
     */
    public static byte[] serializeObject(Object object) throws IOException {
        if (object == null) {
            logger.warn("Attempted to serialize a null object.");
            return new byte[0];
        }

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(object);
            objectOutputStream.flush();
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            logger.debug("Serialized object into byte array: {}", convertBytesToHex(byteArray));
            return byteArray;
        }
    }

    /**
     * Deserializes a byte array into an object.
     *
     * @param bytes the byte array to deserialize.
     * @return the deserialized object.
     * @throws IOException            if an I/O error occurs during deserialization.
     * @throws ClassNotFoundException if the class of the object cannot be found.
     */
    public static Object deserializeObject(byte[] bytes) throws IOException, ClassNotFoundException {
        if (bytes == null || bytes.length == 0) {
            logger.warn("Input byte array is null or empty. Returning null object.");
            return null;
        }

        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
            Object object = objectInputStream.readObject();

            logger.debug("Deserialized byte array into object: {}", object);
            return object;
        }
    }

    /**
     * Checks if a byte array is null or empty.
     *
     * @param bytes the byte array to check.
     * @return true if the byte array is null or empty, otherwise false.
     */
    public static boolean isNullOrEmpty(byte[] bytes) {
        boolean isEmpty = bytes == null || bytes.length == 0;
        if (isEmpty) {
            logger.warn("Byte array is empty or null.");
        }
        return isEmpty;
    }

    /**
     * Reverses the given byte array.
     *
     * @param bytes the byte array to reverse.
     * @return a new byte array with the elements reversed.
     */
    public static byte[] reverseByteArray(byte[] bytes) {
        if (isNullOrEmpty(bytes)) {
            return bytes;
        }

        byte[] reversedArray = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            reversedArray[i] = bytes[bytes.length - i - 1];
        }

        logger.debug("Reversed byte array: {}", convertBytesToHex(reversedArray));
        return reversedArray;
    }

    /**
     * Compares two byte arrays for equality.
     *
     * @param array1 the first byte array.
     * @param array2 the second byte array.
     * @return true if the arrays are equal, otherwise false.
     */
    public static boolean areArraysEqual(byte[] array1, byte[] array2) {
        boolean areEqual = Arrays.equals(array1, array2);
        logger.debug("Comparison result for byte arrays: {}", areEqual);
        return areEqual;
    }

    /**
     * Returns a subarray from the given byte array from the specified range.
     *
     * @param original   the original byte array.
     * @param startIndex the starting index (inclusive).
     * @param endIndex   the ending index (exclusive).
     * @return a byte array containing the specified range of the original array.
     */
    public static byte[] getSubarray(byte[] original, int startIndex, int endIndex) {
        byte[] subarray = Arrays.copyOfRange(original, startIndex, endIndex);
        logger.debug("Subarray from {} to {}: {}", startIndex, endIndex, convertBytesToHex(subarray));
        return subarray;
    }

    /**
     * Retrieves the byte at the specified index, with bounds checking.
     *
     * @param bytes the byte array.
     * @param index the index to retrieve the byte from.
     * @return the byte at the specified index.
     * @throws IndexOutOfBoundsException if the index is invalid.
     */
    public static byte getByteAtIndex(byte[] bytes, int index) {
        if (bytes == null || index < 0 || index >= bytes.length) {
            throw new IndexOutOfBoundsException("Invalid index or null byte array");
        }

        byte byteValue = bytes[index];
        logger.debug("Byte at index {}: {}", index, String.format("%02X", byteValue));
        return byteValue;
    }

    /**
     * Converts a byte array to a Base64 encoded string.
     *
     * @param bytes the byte array to convert.
     * @return the Base64 encoded string.
     */
    public static String convertBytesToBase64(byte[] bytes) {
        if (isNullOrEmpty(bytes)) {
            return "";
        }

        String base64String = Base64.getEncoder().encodeToString(bytes);
        logger.debug("Converted byte array to Base64: {}", base64String);
        return base64String;
    }

    /**
     * Converts a Base64 encoded string to a byte array.
     *
     * @param base64String the Base64 encoded string.
     * @return the decoded byte array.
     */
    public static byte[] convertBase64ToBytes(String base64String) {
        if (base64String == null || base64String.isEmpty()) {
            logger.warn("Input Base64 string is null or empty. Returning empty byte array.");
            return new byte[0];
        }

        byte[] decodedBytes = Base64.getDecoder().decode(base64String);
        logger.debug("Converted Base64 string to byte array: {}", convertBytesToHex(decodedBytes));
        return decodedBytes;
    }

    /**
     * Splits a byte array into smaller chunks of the specified size.
     *
     * @param array     the byte array to split.
     * @param chunkSize the size of each chunk.
     * @return an array of byte arrays representing the chunks.
     */
    public static byte[][] splitArrayIntoChunks(byte[] array, int chunkSize) {
        if (isNullOrEmpty(array) || chunkSize <= 0) {
            return new byte[0][];
        }

        int numChunks = (int) Math.ceil((double) array.length / chunkSize);
        byte[][] chunks = new byte[numChunks][];
        for (int i = 0; i < numChunks; i++) {
            int from = i * chunkSize;
            int to = Math.min(from + chunkSize, array.length);
            chunks[i] = Arrays.copyOfRange(array, from, to);
        }

        logger.debug("Split byte array into {} chunks.", numChunks);
        return chunks;
    }

    /**
     * Utility method to print byte array for debugging.
     *
     * @param bytes the byte array to print.
     */
    public static void printByteArray(byte[] bytes) {
        Optional.ofNullable(bytes)
                .ifPresentOrElse(
                        byteArray -> logger.debug("Byte array content: {}", convertBytesToHex(byteArray)),
                        () -> logger.warn("Received null byte array")
                );
    }
}