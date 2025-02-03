package eu.isygoit.encrypt;

import eu.isygoit.encrypt.helper.CRC16;
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
        assertEquals(0x30EC, CRC16.calculate(data1));  // Expected CRC-16-ANSI for {0x12, 0x34, 0x56, 0x78}
        assertEquals(0x84C0, CRC16.calculate(data2));  // Expected CRC for empty data {0x00, 0x00, 0x00, 0x00}
        assertEquals(0x1D0F, CRC16.calculate(data3));  // Expected CRC-16-ANSI for {0xFF, 0xFF, 0xFF, 0xFF}
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
        int expectedCrc = 0x30EC;  // Precomputed CRC-16-ANSI for {0x12, 0x34, 0x56, 0x78}
        assertEquals(expectedCrc, CRC16.calculate(tempFile));
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

            int expectedCrc = 0x30EC;  // Precomputed CRC-16-ANSI for {0x12, 0x34, 0x56, 0x78}
            assertEquals(expectedCrc, CRC16.calculate(mockFile));

            mockedFiles.verify(() -> Files.readAllBytes(mockFile.toPath()), times(1));
        }
    }
}
