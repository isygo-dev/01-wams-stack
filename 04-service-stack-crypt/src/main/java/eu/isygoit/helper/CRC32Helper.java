package eu.isygoit.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HexFormat;
import java.util.Optional;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

/**
 * Utility class for calculating CRC-32 checksums.
 * Uses Java's built-in CRC32 implementation.
 */
public interface CRC32Helper {

    Logger logger = LoggerFactory.getLogger(CRC32Helper.class);

    /**
     * Computes the CRC-32 checksum of a byte array.
     *
     * @param bytes The input byte array.
     * @return The computed CRC-32 checksum.
     */
    public static long calculate(byte[] bytes) {
        logger.info("Calculating CRC-32 for {} bytes", bytes.length);

        var crc32 = new CRC32();
        crc32.update(bytes);

        var crc = crc32.getValue();
        logger.info("CRC-32 calculation complete: {}", toHex(crc));

        return crc;
    }

    /**
     * Computes the CRC-32 checksum of a file.
     * Uses a stream to efficiently read large files.
     *
     * @param inputFile The file to process.
     * @return Optional containing the CRC-32 checksum, or empty if an error occurs.
     */
    public static Optional<Long> calculate(File inputFile) {
        if (inputFile == null || !inputFile.exists()) {
            logger.error("File not found: {}", inputFile);
            return Optional.empty();
        }

        logger.info("Reading file: {}", inputFile.getAbsolutePath());

        try (var fis = new FileInputStream(inputFile);
             var cis = new CheckedInputStream(fis, new CRC32())) {

            var buffer = new byte[8192]; // Efficient buffer size
            while (cis.read(buffer) != -1) {
                // Stream automatically updates checksum
            }

            var crc = cis.getChecksum().getValue();
            logger.info("File CRC-32 calculation complete: {}", toHex(crc));
            return Optional.of(crc);

        } catch (IOException e) {
            logger.error("Failed to read file: {}", inputFile.getAbsolutePath(), e);
            return Optional.empty();
        }
    }

    /**
     * Converts the CRC-32 checksum to a hexadecimal string.
     *
     * @param crc The computed CRC-32 checksum.
     * @return The hexadecimal representation of the checksum.
     */
    public static String toHex(long crc) {
        return HexFormat.of().toHexDigits(crc);
    }

    /**
     * Computes CRC-32 checksum and returns an object containing both decimal and hexadecimal values.
     *
     * @param bytes The byte array to compute.
     * @return A CRC32Result object containing both integer and hex representation.
     */
    public static CRC32Result compute(byte[] bytes) {
        var crc = calculate(bytes);
        return new CRC32Result(crc, toHex(crc));
    }

    /**
     * Represents a CRC-32 result containing both decimal and hexadecimal values.
     *
     * @param crc The CRC-32 checksum as a long integer.
     * @param hex The CRC-32 checksum as a hexadecimal string.
     */
    public record CRC32Result(long crc, String hex) {
    }
}