package eu.isygoit.helper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The type Directory management test.
 */
@DisplayName("Directory Management Tests")
class DirectoryManagementTest {

    /**
     * The Temporary directory.
     */
    @TempDir
    Path temporaryDirectory;

    /**
     * The type Create directory if absent tests.
     */
    @Nested
    @DisplayName("createDirectoryIfAbsent Method")
    class CreateDirectoryIfAbsentTests {

        /**
         * Should create new directory when absent.
         */
        @Test
        @DisplayName("Should create a new directory when it does not exist")
        void shouldCreateNewDirectoryWhenAbsent() {
            // Given
            Path directoryPath = temporaryDirectory.resolve("newDirectory");

            // When
            FileHelper.createDirectoryIfAbsent(directoryPath);

            // Then
            File directory = new File(directoryPath.toUri());
            assertTrue(directory.exists(), "The directory should be created.");
            assertTrue(directory.isDirectory(), "The created path should be a directory.");
        }

        /**
         * Should not alter existing directory.
         */
        @Test
        @DisplayName("Should not change an already existing directory")
        void shouldNotAlterExistingDirectory() {
            // Given
            Path directoryPath = temporaryDirectory.resolve("existingDirectory");
            new File(directoryPath.toUri()).mkdir();

            // When
            FileHelper.createDirectoryIfAbsent(directoryPath);

            // Then
            File directory = new File(directoryPath.toUri());
            assertTrue(directory.exists(), "The existing directory should remain unchanged.");
            assertTrue(directory.isDirectory(), "The existing path should still be a directory.");
        }

        /**
         * Should throw exception when path is a file.
         *
         * @throws IOException the io exception
         */
        @Test
        @DisplayName("Should throw an exception if the path points to a file")
        void shouldThrowExceptionWhenPathIsAFile() throws IOException {
            // Given
            Path filePath = temporaryDirectory.resolve("existingFile.txt");
            new File(filePath.toUri()).createNewFile(); // Create a file at the specified path

            // When & Then
            Exception exception = assertThrows(IllegalArgumentException.class,
                    () -> FileHelper.createDirectoryIfAbsent(filePath),
                    "An exception should be thrown when attempting to create a directory at a file path."
            );

            assertTrue(exception.getMessage().contains("Cannot create directory"),
                    "The error message should indicate directory creation failure.");
        }
    }
}