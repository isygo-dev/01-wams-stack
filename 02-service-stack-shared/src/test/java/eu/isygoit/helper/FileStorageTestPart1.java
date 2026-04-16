package eu.isygoit.helper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * The type File storage test.
 */
class FileStorageTestPart1 {

    /**
     * The Temp dir.
     */
    @TempDir
    Path tempDir;

    /**
     * The type Save multipart file tests.
     */
    @Nested
    @DisplayName("saveMultipartFile() Tests")
    class SaveMultipartFileTests {

        /**
         * Save multipart file should save valid file.
         *
         * @throws IOException the io exception
         */
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
                    "txt",
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.SYNC);

            // Verify
            assertTrue(savedPath.toFile().exists());
            assertEquals("testFile.txt", savedPath.getFileName().toString());
        }

        /**
         * Save multipart file should throw exception when file is empty.
         */
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
                            ,
                            StandardOpenOption.CREATE,
                            StandardOpenOption.WRITE,
                            StandardOpenOption.TRUNCATE_EXISTING,
                            StandardOpenOption.SYNC)
            );
        }
    }

    @Nested
    @DisplayName("deleteDirectoryRecursively() Tests")
    class DeleteDirectoryRecursivelyTests {

        @Test
        @DisplayName("should delete directory recursively")
        void deleteDirectoryRecursively_shouldDeleteDirectory() throws IOException {
            Path subDir = tempDir.resolve("subDir");
            Files.createDirectory(subDir);
            Path fileInSubDir = subDir.resolve("file.txt");
            Files.write(fileInSubDir, "content".getBytes());

            boolean result = FileHelper.deleteDirectoryRecursively(subDir.toFile(), false);

            assertTrue(result);
            assertFalse(Files.exists(subDir));
        }
    }

    @Nested
    @DisplayName("isImage() Tests")
    class IsImageTests {

        @Test
        @DisplayName("should return true for image content type")
        void isImage_shouldReturnTrueForImage() {
            MultipartFile mockFile = Mockito.mock(MultipartFile.class);
            when(mockFile.getContentType()).thenReturn("image/jpeg");
            assertTrue(FileHelper.isImage(mockFile));

            when(mockFile.getContentType()).thenReturn("image/png");
            assertTrue(FileHelper.isImage(mockFile));
        }

        @Test
        @DisplayName("should return false for non-image content type")
        void isImage_shouldReturnFalseForNonImage() {
            MultipartFile mockFile = Mockito.mock(MultipartFile.class);
            when(mockFile.getContentType()).thenReturn("text/plain");
            assertFalse(FileHelper.isImage(mockFile));

            when(mockFile.getContentType()).thenReturn(null);
            assertFalse(FileHelper.isImage(mockFile));
        }
    }

    /**
     * The type Read properties file tests.
     */
    @Nested
    @DisplayName("readPropertiesFile() Tests")
    class ReadPropertiesFileTests {

        /**
         * Read properties file should read file contents.
         *
         * @throws IOException the io exception
         */
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