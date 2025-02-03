package eu.isygoit.encrypt.helper;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * The type Crc 16.
 */
@Slf4j
public class CRC16 {

    private static final int POLYNOMIAL = 0x1021; // Standard CRC16-CCITT Polynomial
    private static final int INITIAL_CRC = 0xFFFF; // Standard Initial Value
    private static final int FINAL_CRC = 0xFFFF; // Standard Initial Value

    /**
     * Calculate int.
     *
     * @param bytes the bytes
     * @return the int
     */
    public static int calculate(byte[] bytes) {
        int crc = INITIAL_CRC;
        for (byte b : bytes) {
            crc ^= (b & 0xFF) << 8;
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x8000) != 0) {
                    crc = (crc << 1) ^ POLYNOMIAL;
                } else {
                    crc <<= 1;
                }
            }
            crc &= FINAL_CRC; // Ensure 16-bit output
        }
        return crc;
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
