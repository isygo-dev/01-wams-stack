package eu.isygoit.com.rest.service;

import eu.isygoit.exception.EmptyPathException;
import eu.isygoit.exception.FileNotFoundException;
import eu.isygoit.exception.ResourceNotFoundException;
import eu.isygoit.model.Resume;
import eu.isygoit.model.ResumeLinkedFile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FileServiceLocalStaticMethods using the Resume entity.
 * Covers upload, download, delete with success and failure scenarios.
 */
class FileServiceLocalStaticMethodsTest {

    // Temporary directory for testing file operations
    private static final Path TEMP_DIR = Path.of(System.getProperty("java.io.tmpdir"), "file-service-test");

    /**
     * Sets .
     *
     * @throws IOException the io exception
     */
    @BeforeEach
    void setup() throws IOException {
        // Ensure the temporary directory is clean and exists before each test
        //if (Files.exists(TEMP_DIR)) {
        //    FileUtils.deleteDirectory(TEMP_DIR.toFile());
        //}
        //Files.createDirectories(TEMP_DIR);
    }

    /**
     * Cleanup.
     *
     * @throws IOException the io exception
     */
    @AfterEach
    void cleanup() throws IOException {
        // Clean up after each test to avoid residue files
        //if (Files.exists(TEMP_DIR)) {
        //    FileUtils.deleteDirectory(TEMP_DIR.toFile());
        //}
    }

    /**
     * Helper to create a Resume entity with given path, code, and filename.
     */
    private Resume createResume(String path, String code, String fileName) {
        return Resume.builder()
                .path(path)
                .code(code)
                .fileName(fileName)
                .domain("testDomain")
                .build();
    }

    /**
     * Helper to create ResumeLinkedFile entity with path, code, and filename.
     */
    private ResumeLinkedFile createLinkedFile(String path, String code, String fileName) {
        return ResumeLinkedFile.builder()
                .domain("testDomain")
                .path(path)
                .code(code)
                .fileName(fileName)
                .build();
    }

    /**
     * Test uploading a file creates directory and stores the file correctly.
     *
     * @throws IOException the io exception
     */
    @Test
    void upload_shouldCreateDirectoryAndStoreFile() throws IOException {
        var fileContent = "Upload test content";
        var file = new MockMultipartFile("file", "upload.txt", "text/plain", fileContent.getBytes());
        var resume = createResume(TEMP_DIR.resolve("nested").toString(), "upload.txt", "upload.txt");

        String returnedCode = FileServiceLocalStaticMethods.upload(file, resume);

        // Directory should be created
        assertTrue(Files.exists(Path.of(resume.getPath())));

        // File should exist and content should match
        Path uploadedFilePath = Path.of(resume.getPath()).resolve(resume.getCode());
        assertTrue(Files.exists(uploadedFilePath));
        assertEquals(fileContent, Files.readString(uploadedFilePath));

        // Returned code should be the file code
        assertEquals(resume.getCode(), returnedCode);

        Files.deleteIfExists(uploadedFilePath);
    }

    /**
     * Test downloading a file resource when file exists.
     *
     * @throws IOException the io exception
     */
    @Test
    void download_shouldReturnResource_whenFileExists() throws IOException {
        Path filePath = TEMP_DIR.resolve("download.txt");
        String content = "Download test content";
        Files.writeString(filePath, content);

        var resume = createResume(TEMP_DIR.toString(), "download.txt", "download.txt");

        Resource resource = FileServiceLocalStaticMethods.download(resume, 1L);

        assertNotNull(resource);
        assertTrue(resource.exists());
        assertEquals(filePath.toUri(), resource.getURI());
        assertEquals(content, new String(resource.getInputStream().readAllBytes()));
    }

    /**
     * Test downloading throws ResourceNotFoundException when file is missing.
     */
    @Test
    void download_shouldThrowResourceNotFoundException_whenFileMissing() {
        var resume = createResume(TEMP_DIR.toString(), "missing.txt", "missing.txt");

        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () ->
                FileServiceLocalStaticMethods.download(resume, 123L)
        );

        assertTrue(thrown.getMessage().contains("Resource not found: testDomain/missing.txt"));
    }

    /**
     * Test downloading throws EmptyPathException when entity path is empty or null.
     */
    @Test
    void download_shouldThrowEmptyPathException_whenPathIsEmpty() {
        var resume = createResume("", "file.txt", "file.txt");

        EmptyPathException thrown = assertThrows(EmptyPathException.class, () ->
                FileServiceLocalStaticMethods.download(resume, 456L)
        );

        assertTrue(thrown.getMessage().contains("Empty path: testDomain/file.txt"));
    }

    /**
     * Test deleting a file successfully deletes it.
     *
     * @throws IOException the io exception
     */
    @Test
    void delete_shouldDeleteFile_whenFileExists() throws IOException {
        Path filePath = TEMP_DIR.resolve("deleteLinkedFile.txt");
        Files.writeString(filePath, "To be deleted by linked file");

        ResumeLinkedFile linkedFile = createLinkedFile(TEMP_DIR.toString(), "deleteLinkedFile.txt", "deleteLinkedFile.txt");

        boolean deleted = FileServiceLocalStaticMethods.delete(linkedFile);

        assertTrue(deleted);
        assertFalse(Files.exists(filePath));
    }

    /**
     * Test deleting throws FileNotFoundException when file does not exist.
     */
    @Test
    void delete_shouldThrowFileNotFoundException_whenFileMissing() {
        ResumeLinkedFile linkedFile = createLinkedFile(TEMP_DIR.toString(), "nonexistentLinkedFile.txt", "nonexistentLinkedFile.txt");

        FileNotFoundException thrown = assertThrows(FileNotFoundException.class, () ->
                FileServiceLocalStaticMethods.delete(linkedFile)
        );

        assertTrue(thrown.getMessage().contains(linkedFile.getCode()));
    }

    /**
     * Test upload overwrites existing file.
     *
     * @throws IOException the io exception
     */
    @Test
    void upload_shouldOverwriteExistingFile() throws IOException {
        Path filePath = TEMP_DIR.resolve("overwrite.txt");
        Files.writeString(filePath, "Old content");

        var oldFile = createResume(TEMP_DIR.toString(), "overwrite.txt", "overwrite.txt");
        var newFileContent = "New content";
        var newFile = new MockMultipartFile("file", "overwrite.txt", "text/plain", newFileContent.getBytes());

        String code = FileServiceLocalStaticMethods.upload(newFile, oldFile);

        assertEquals("overwrite.txt", code);
        assertEquals(newFileContent, Files.readString(filePath));
    }

    /**
     * Test download with version in path or message (just checking no errors thrown for version param).
     */
    @Test
    void download_shouldConsiderVersionInExceptionMessage() {
        var resume = createResume(TEMP_DIR.toString(), "missingVersion.txt", "missingVersion.txt");
        long version = 99L;

        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () ->
                FileServiceLocalStaticMethods.download(resume, version)
        );

        assertTrue(thrown.getMessage().contains(Long.toString(version)));
    }
}