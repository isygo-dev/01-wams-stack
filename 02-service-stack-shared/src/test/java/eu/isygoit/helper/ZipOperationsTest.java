package eu.isygoit.helper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The type Zip operations test.
 */
class ZipOperationsTest {
    /**
     * The Temp dir.
     */
    @TempDir
    Path tempDir;

    /**
     * Zip single file should create valid zip file.
     *
     * @throws IOException the io exception
     */
    @Test
    void zipSingleFile_ShouldCreateValidZipFile() throws IOException {
        // Setup
        Path sourceFile = tempDir.resolve("source.txt");
        Files.write(sourceFile, "test content" .getBytes());
        String zipPath = tempDir.resolve("output.zip").toString();

        // Test
        FileHelper.zipSingleFile(sourceFile.toFile(), zipPath);

        // Verify
        assertTrue(new File(zipPath).exists());
        assertTrue(new File(zipPath).length() > 0);
    }

    /**
     * Zip multiple files should create valid zip with multiple files.
     *
     * @throws IOException the io exception
     */
    @Test
    void zipMultipleFiles_ShouldCreateValidZipWithMultipleFiles() throws IOException {
        // Setup
        Path file1 = tempDir.resolve("file1.txt");
        Path file2 = tempDir.resolve("file2.txt");
        Files.write(file1, "content1" .getBytes());
        Files.write(file2, "content2" .getBytes());

        List<File> files = Arrays.asList(file1.toFile(), file2.toFile());
        String zipPath = tempDir.resolve("multiple.zip").toString();

        // Test
        FileHelper.zipMultipleFiles(files, zipPath);

        // Verify
        assertTrue(new File(zipPath).exists());
        assertTrue(new File(zipPath).length() > 0);
    }

    /**
     * Unzip file should extract files correctly.
     *
     * @throws IOException the io exception
     */
    @Test
    void unzipFile_ShouldExtractFilesCorrectly() throws IOException {
        // Setup
        Path sourceFile = tempDir.resolve("source.txt");
        Files.write(sourceFile, "test content" .getBytes());
        Path zipFile = tempDir.resolve("test.zip");
        FileHelper.zipSingleFile(sourceFile.toFile(), zipFile.toString());

        Path extractDir = tempDir.resolve("extract");
        Files.createDirectory(extractDir);

        // Test
        FileHelper.unzipFile(zipFile.toString(), extractDir.toString());

        // Verify
        assertTrue(Files.exists(extractDir.resolve("source.txt")));
        assertEquals(
                "test content",
                Files.readString(extractDir.resolve("source.txt"))
        );
    }
}