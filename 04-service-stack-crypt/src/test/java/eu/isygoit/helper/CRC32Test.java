package eu.isygoit.helper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The type Crc 32 test.
 */
class CRC32Test {

    /**
     * Test calculate byte array.
     */
    @Test
    void testCalculateByteArray() {
        // Data arrays
        byte[] data1 = {0x12, 0x34, 0x56, 0x78}; // Example data
        byte[] data2 = {0x00, 0x00, 0x00, 0x00}; // Empty data
        byte[] data3 = {0x12, 0x1F, 0x1B, 0x1C}; // All bytes 0xFFB

        // Expected CRC-32-ANSI values for each test case
        assertEquals(0x4A090E98, CRC32Helper.calculate(data1));  // Expected CRC-32-ANSI for {0x12, 0x34, 0x56, 0x78}
        assertEquals(0x2144DF1C, CRC32Helper.calculate(data2));  // Expected CRC for empty data {0x00, 0x00, 0x00, 0x00}
        assertEquals(0x71193390, CRC32Helper.calculate(data3));  // Expected CRC-32-ANSI for {0xFF, 0xFF, 0xFF, 0xFF}
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

        // Compute CRC32Helper from file and compare with expected value
        int expectedCrc = 0x4A090E98;  // Precomputed CRC-32-ANSI for {0x12, 0x34, 0x56, 0x78}
        assertEquals(expectedCrc, CRC32Helper.calculate(tempFile).get());
    }
}