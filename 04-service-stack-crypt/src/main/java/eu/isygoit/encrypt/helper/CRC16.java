package eu.isygoit.encrypt.helper;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * The type Crc 16.
 */
@Slf4j
public class CRC16 {

    private static final int POLYNOMIAL = 0x1021;
    private static final int INITIAL_VALUE = 0x0000; // The initial crc value

    /**
     * Calculate int.
     *
     * @param bytes the bytes
     * @return the int
     */
    public static int calculate(byte[] bytes) {
        int crc = INITIAL_VALUE;
        for (final byte b : bytes) {
            for (int i = 0; i < 8; i++) {
                final boolean bit = (b >> 7 - i & 1) == 1;
                final boolean c15 = (crc >> 15 & 1) == 1;
                crc <<= 1;
                if (c15 ^ bit) {
                    crc ^= POLYNOMIAL;
                }
            }
        }
        crc &= 0xffff;
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
        final byte[] bytes = FileUtils.readFileToByteArray(inputFile);
        return calculate(bytes);
    }
}
