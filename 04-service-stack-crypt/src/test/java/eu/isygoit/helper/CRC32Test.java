package eu.isygoit.helper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

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
        byte[] data3 = {(byte) 255, (byte) 255, (byte) 255, (byte) 255}; // All bytes 0xFF

        // Expected CRC-32-ANSI values for each test case
        assertEquals(0xFEAAE43F, CRC32Helper.calculate(data1));  // Expected CRC-32-ANSI for {0x12, 0x34, 0x56, 0x78}
        assertEquals(0x810E88FF, CRC32Helper.calculate(data2));  // Expected CRC for empty data {0x00, 0x00, 0x00, 0x00}
        assertEquals(0xFFFFFFFF, CRC32Helper.calculate(data3));  // Expected CRC-32-ANSI for {0xFF, 0xFF, 0xFF, 0xFF}
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
        int expectedCrc = 0xFEAAE43F;  // Precomputed CRC-32-ANSI for {0x12, 0x34, 0x56, 0x78}
        assertEquals(expectedCrc, CRC32Helper.calculate(tempFile));
    }

    /**
     * Test calculate file with mock.
     *
     * @throws IOException the io exception
     */
    @Test
    void testCalculateFileWithMock() throws IOException {
        File mockFile = mock(File.class);
        byte[] mockData = {0x12, 0x34, 0x56, 0x78}; // Mock data to simulate file content

        try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.readAllBytes(mockFile.toPath())).thenReturn(mockData);

            int expectedCrc = 0xFEAAE43F;  // Precomputed CRC-32-ANSI for {0x12, 0x34, 0x56, 0x78}
            assertEquals(expectedCrc, CRC32Helper.calculate(mockFile));

            mockedFiles.verify(() -> Files.readAllBytes(mockFile.toPath()), times(1));
        }
    }
}