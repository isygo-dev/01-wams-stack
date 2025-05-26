package eu.isygoit.helper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The type Crc 16 test.
 */
class CRC16Test {

    /**
     * Test calculate byte array.
     */
    @Test
    void testCalculateByteArray() {
        // Data arrays
        byte[] data1 = {0x12, 0x34, 0x56, 0x78}; // Example data
        byte[] data2 = {0x00, 0x00, 0x00, 0x00}; // Empty data
        byte[] data3 = {(byte) 255, (byte) 255, (byte) 255, (byte) 255}; // All bytes 0xFF

        // Expected CRC-16-ANSI values for each test case
        assertEquals(0xB42C, CRC16Helper.calculate(data1));  // Expected CRC-16-ANSI for {0x12, 0x34, 0x56, 0x78}
        assertEquals(0x0000, CRC16Helper.calculate(data2));  // Expected CRC for empty data {0x00, 0x00, 0x00, 0x00}
        assertEquals(0x99CF, CRC16Helper.calculate(data3));  // Expected CRC-16-ANSI for {0xFF, 0xFF, 0xFF, 0xFF}
    }

    /**
     * Test calculate file.
     *
     * @param tempDir the temp dir
     * @throws IOException the io exception
     */
    @Test
    void testCalculateFile(@TempDir File tempDir) throws IOException {
        // Create a temporary file with known content
        File tempFile = new File(tempDir, "test.txt");
        Files.write(tempFile.toPath(), new byte[]{0x12, 0x34, 0x56, 0x78});

        // Compute CRC16 from file and compare with expected value
        int expectedCrc = 0xB42C;  // Precomputed CRC-16-ANSI for {0x12, 0x34, 0x56, 0x78}
        assertEquals(expectedCrc, CRC16Helper.calculate(tempFile).get());
    }
}