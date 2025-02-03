package eu.isygoit.encrypt.helper;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * The type Crc 32.
 */
@Slf4j
public class CRC32 {

    private static final int POLYNOMIAL = 0xEDB88320;
    private static final int INITIAL_CRC = 0xFFFFFFFF; // Standard initial value for CRC-32-ANSI
    private static final int FINAL_CRC = 0xFFFFFFFF; // No final XOR in CRC-16-ANSI

    /**
     * Calculate int.
     *
     * @param bytes the bytes
     * @return the int
     */
    public static int calculate(byte[] bytes) {
        int crc = INITIAL_CRC; // Initial CRC value
        for (byte b : bytes) {
            crc ^= (b & 0xFF) << 24; // Align byte to the left
            for (int i = 0; i < 8; i++) { // Process each bit
                if ((crc & 0x80000000) != 0) { // Check if the leftmost bit is 1
                    crc = (crc << 1) ^ POLYNOMIAL; // Shift left and XOR with polynomial
                } else {
                    crc <<= 1; // Just shift left if no XOR
                }
            }
        }
        return crc ^ FINAL_CRC; // Final XOR
    }

    /**
     * Calculate int.
     *
     * @param inputFile the input file
     * @return the int
     * @throws IOException the io exception
     */
    public static int calculate(File inputFile) throws IOException {
        byte[] fileBytes = Files.readAllBytes(inputFile.toPath()); // Java 17 API
        return calculate(fileBytes);
    }
}
