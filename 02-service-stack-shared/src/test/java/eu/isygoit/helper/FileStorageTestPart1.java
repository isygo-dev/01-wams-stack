package eu.isygoit.helper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class FileStorageTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("saveMultipartFile() Tests")
    class SaveMultipartFileTests {

        @Test
        @DisplayName("should save a valid file")
        void saveMultipartFile_ShouldSaveValidFile() throws IOException {
            // Setup
            MultipartFile mockFile = Mockito.mock(MultipartFile.class);
            byte[] content = "test content".getBytes();
            when(mockFile.getBytes()).thenReturn(content);
            when(mockFile.isEmpty()).thenReturn(false);
            when(mockFile.getOriginalFilename()).thenReturn("test.txt");

            // Test
            Path savedPath = FileHelper.saveMultipartFile(
                    tempDir,
                    "testFile",
                    mockFile,
                    "txt"
            );

            // Verify
            assertTrue(savedPath.toFile().exists());
            assertEquals("testFile.txt", savedPath.getFileName().toString());
        }

        @Test
        @DisplayName("should throw exception when file is empty")
        void saveMultipartFile_ShouldThrowException_WhenFileIsEmpty() {
            MultipartFile mockFile = Mockito.mock(MultipartFile.class);
            when(mockFile.isEmpty()).thenReturn(true);

            assertThrows(IllegalArgumentException.class, () ->
                    FileHelper.saveMultipartFile(
                            tempDir,
                            "testFile",
                            mockFile,
                            "txt"
                    )
            );
        }
    }

    @Nested
    @DisplayName("readPropertiesFile() Tests")
    class ReadPropertiesFileTests {

        @Test
        @DisplayName("should read file contents")
        void readPropertiesFile_ShouldReadFileContents() throws IOException {
            // Setup
            Path propertiesFile = tempDir.resolve("test.properties");
            String content = "key=value";
            java.nio.file.Files.write(propertiesFile, content.getBytes());

            // Test
            String result = FileHelper.readPropertiesFile(propertiesFile.toString());

            // Verify
            assertEquals(content, result);
        }
    }
}