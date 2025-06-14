package eu.isygoit.helper;

import eu.isygoit.exception.EmptyPathException;
import eu.isygoit.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The type File storage test part 2.
 */
class FileStorageTestPart2 {

    /**
     * The Temp dir.
     */
    @TempDir
    Path tempDir;
    /**
     * The Temp target dir.
     */
    @TempDir
    Path tempTargetDir;
    /**
     * The Temp file.
     */
    Path tempFile;

    /**
     * Sets .
     *
     * @throws IOException the io exception
     */
    @BeforeEach
    void setup() throws IOException {
        // Create and populate a temp file with some content
        tempFile = Files.createTempFile(tempDir, "testfile", ".txt");
        Files.write(tempFile, "Sample content".getBytes());
    }

    /**
     * The type Save multipart file tests.
     */
    @Nested
    @DisplayName("saveMultipartFile() Tests")
    class SaveMultipartFileTests {

        /**
         * Test save multipart file valid file.
         *
         * @throws IOException the io exception
         */
        @Test
        @DisplayName("should save a valid file")
        void testSaveMultipartFile_validFile() throws IOException {
            // Setup mock MultipartFile
            MultipartFile mockFile = mock(MultipartFile.class);
            byte[] content = "test content".getBytes();
            when(mockFile.getBytes()).thenReturn(content);
            when(mockFile.isEmpty()).thenReturn(false);
            when(mockFile.getOriginalFilename()).thenReturn("testfile.txt");

            // Execute the method under test
            Path result = FileHelper.saveMultipartFile(tempTargetDir, "savedFile", mockFile, "txt",
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.SYNC);

            // Verify that the file was saved correctly
            Path expectedPath = tempTargetDir.resolve("savedFile.txt");
            assertAll(
                    () -> assertTrue(Files.exists(result)),
                    () -> assertEquals(expectedPath, result)
            );

            // Cleanup after the test
            Files.delete(result);
        }

        /**
         * Test save multipart file empty file.
         *
         * @throws IOException the io exception
         */
        @Test
        @DisplayName("should throw exception when file is empty")
        void testSaveMultipartFile_emptyFile() throws IOException {
            // Setup mock empty MultipartFile
            MultipartFile mockFile = mock(MultipartFile.class);
            when(mockFile.getBytes()).thenReturn(new byte[0]);
            when(mockFile.isEmpty()).thenReturn(true);
            when(mockFile.getOriginalFilename()).thenReturn("emptyFile.txt");

            // Assert exception thrown
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
                FileHelper.saveMultipartFile(tempTargetDir, "emptyFile", mockFile, "txt",
                        StandardOpenOption.CREATE,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.SYNC);
            });

            assertEquals("Provided file is empty.", thrown.getMessage());
        }
    }

    /**
     * The type Download resource tests.
     */
    @Nested
    @DisplayName("downloadResource() Tests")
    class DownloadResourceTests {

        /**
         * Test download resource valid file.
         *
         * @throws IOException the io exception
         */
        @Test
        @DisplayName("should return valid resource for an existing file")
        void testDownloadResource_validFile() throws IOException {
            // Setup
            Path filePath = tempFile;

            // Download resource
            Resource resource = FileHelper.downloadResource(filePath, 1L);

            // Verify
            assertAll(
                    () -> assertNotNull(resource),
                    () -> assertTrue(Files.exists(filePath)),
                    () -> assertEquals(filePath.toUri(), resource.getURI())
            );
        }

        /**
         * Test download resource file does not exist.
         */
        @Test
        @DisplayName("should throw exception when file does not exist")
        void testDownloadResource_fileDoesNotExist() {
            Path nonExistentFile = tempTargetDir.resolve("nonexistentfile.txt");

            // Assert exception thrown
            ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> {
                FileHelper.downloadResource(nonExistentFile, 1L);
            });

            assertEquals("No resource found for " + nonExistentFile + ":version /1", thrown.getMessage());
        }

        /**
         * Test download resource empty path.
         */
        @Test
        @DisplayName("should throw exception when path is empty")
        void testDownloadResource_emptyPath() {
            // Assert exception thrown
            EmptyPathException thrown = assertThrows(EmptyPathException.class, () -> {
                FileHelper.downloadResource(null, 1L);
            });

            assertEquals("Empty path", thrown.getMessage());
        }
    }
}