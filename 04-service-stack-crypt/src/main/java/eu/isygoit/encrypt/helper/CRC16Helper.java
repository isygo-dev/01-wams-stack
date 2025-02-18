package eu.isygoit.encrypt.helper;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HexFormat;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * Utility class for calculating CRC-16 checksums.
 * Uses the standard CRC-16-CCITT algorithm (polynomial: 0x1021).
 */
@Slf4j
public final class CRC16Helper {

    // CRC-16 standard polynomial (CCITT)
    public static final int POLYNOMIAL = 0x1021;

    // Initial CRC value (commonly set to 0x0000)
    public static final int INITIAL_VALUE = 0x0000;

    /**
     * Private constructor to prevent instantiation.
     */
    private CRC16Helper() {
        throw new UnsupportedOperationException("Utility class should not be instantiated.");
    }

    /**
     * Computes the CRC-16 checksum of a byte array.
     *
     * @param bytes The input byte array.
     * @return The computed CRC-16 checksum.
     */
    public static long calculate(byte[] bytes) {
        log.info("Calculating CRC-16 for {} bytes", bytes.length);

        // Initialize CRC value
        var crc = INITIAL_VALUE;

        // Process each byte in the input array
        for (var b : bytes) {
            // Process each bit (8 bits per byte)
            crc = IntStream.range(0, 8).reduce(crc, (c, i) -> {
                var bit = ((b >> (7 - i)) & 1) == 1;  // Extract the current bit
                var c15 = ((c >> 15) & 1) == 1;      // Extract the MSB of CRC
                return ((c << 1) ^ (c15 ^ bit ? POLYNOMIAL : 0)) & 0xFFFF;
            });
        }

        log.info("CRC-16 calculation complete: {}", toHex(crc));
        return crc;
    }

    /**
     * Computes the CRC-16 checksum of a file.
     * Logs an error if the file cannot be read.
     *
     * @param inputFile The file to process.
     * @return Optional containing the CRC-16 checksum, or empty if an error occurs.
     */
    public static Optional<Long> calculate(File inputFile) {
        if (inputFile == null || !inputFile.exists()) {
            log.error("File not found: {}", inputFile);
            return Optional.empty();
        }

        log.info("Reading file: {}", inputFile.getAbsolutePath());

        return Optional.ofNullable(inputFile)
                .map(file -> {
                    try {
                        var bytes = Files.readAllBytes(file.toPath());
                        log.info("File read successfully ({} bytes)", bytes.length);
                        return calculate(bytes);
                    } catch (IOException e) {
                        log.error("Failed to read file: {}", file.getAbsolutePath(), e);
                        return null;
                    }
                });
    }

    /**
     * Converts the CRC-16 checksum to a hexadecimal string.
     *
     * @param crc The computed CRC-16 checksum.
     * @return The hexadecimal representation of the checksum.
     */
    public static String toHex(long crc) {
        return HexFormat.of().toHexDigits(crc);
    }

    /**
     * Computes CRC-16 checksum and returns an object containing both decimal and hexadecimal values.
     *
     * @param bytes The byte array to compute.
     * @return A CRC16Result object containing both integer and hex representation.
     */
    public static CRC16Result compute(byte[] bytes) {
        var crc = calculate(bytes);
        return new CRC16Result(crc, toHex(crc));
    }

    /**
     * Represents a CRC-16 result containing both decimal and hexadecimal values.
     *
     * @param crc The CRC-16 checksum as an integer.
     * @param hex The CRC-16 checksum as a hexadecimal string.
     */
    public record CRC16Result(long crc, String hex) {
    }
}