package eu.isygoit.encrypt.helper;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * The type Crc 32.
 */
@Slf4j
public class CRC32 {

    /**
     * Calculate int.
     *
     * @param bytes the bytes
     * @return the int
     */
    public static int calculate(byte[] bytes) {
        int crc = 0xffffffff;

        /**************************************************************************
         * Using direct calculation
         **************************************************************************/

        crc = 0x00000000; // initial contents of LFBSR
        final int poly = 0x04C11DB7; // reverse polynomial
        final long TOPBIT = 1L << 31;

        for (final byte b : bytes) {

            int temp = crc ^ b << 24;
            // read 8 bits one at a time
            for (int i = 0; i < 8; i++) {
                if ((temp & TOPBIT) == TOPBIT) {
                    temp = temp << 1 ^ poly;
                } else {
                    temp = temp << 1;
                }
            }
            crc = temp;
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
        final byte[] bytes = FileUtils.readFileToByteArray(inputFile);
        return calculate(bytes);
    }
}
