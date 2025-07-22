package eu.isygoit.helper;

import eu.isygoit.exception.EmptyPathException;
import eu.isygoit.exception.ResourceNotFoundException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Utility class for handling file operations, including directory management,
 * file storage, multipart file handling, and ZIP compression/extraction.
 */
public interface FileHelper {

    /**
     * The constant logger.
     */
    Logger logger = LoggerFactory.getLogger(FileHelper.class);
    //------------------- Directory Management -------------------

    /**
     * Creates a directory if it does not exist.
     *
     * @param directoryPath the directory path
     */
    public static void createDirectoryIfAbsent(Path directoryPath) {
        if (directoryPath == null) {
            throw new IllegalArgumentException("Invalid directory path: " + directoryPath);
        }

        File directory = new File(directoryPath.toUri());

        if (directory.exists()) {
            if (directory.isFile()) {
                throw new IllegalArgumentException("Cannot create directory, Path points to a file, not a directory: " + directoryPath);
            } else {
                logger.debug("Directory already exists: {}", directoryPath);
            }
        } else {
            if (directory.mkdirs()) {
                logger.info("Directory created: {}", directoryPath);
            } else {
                logger.error("Failed to create directory: {}", directoryPath);
            }
        }
    }

    //------------------- File Storage & Retrieval -------------------

    /**
     * Saves a MultipartFile to the specified directory with a given filename and extension.
     *
     * @param targetDirectory the directory where the file will be saved
     * @param fileName        the desired filename
     * @param file   the file to save
     * @param fileExtension   the file extension (optional)
     * @param options         the options
     * @return the Path of the saved file
     * @throws IOException if an I/O error occurs
     */
    public static Path saveMultipartFile(Path targetDirectory, String fileName, MultipartFile file, String fileExtension, OpenOption... options) throws IOException {
        if (file != null && !file.isEmpty()) {
            createDirectoryIfAbsent(targetDirectory);

            String finalFileName = StringUtils.hasText(fileName)
                    ? fileName + "." + (StringUtils.hasText(fileExtension) ? fileExtension : FilenameUtils.getExtension(file.getOriginalFilename()))
                    : file.getOriginalFilename();

            Path filePath = targetDirectory.resolve(finalFileName);
            Files.write(filePath, file.getBytes(), options);

            logger.info("File saved at: {}", filePath);
            return filePath;
        } else {
            logger.warn("Attempted to save an empty MultipartFile.");
            throw new IllegalArgumentException("Provided file is empty.");
        }
    }

    /**
     * Download resource resource.
     *
     * @param filePath the file path
     * @param version  the version
     * @return the resource
     * @throws MalformedURLException the malformed url exception
     */
    static Resource downloadResource(Path filePath, Long version) throws MalformedURLException {
        if (filePath == null) {
            logger.error("Empty path");
            throw new EmptyPathException("Empty path");
        }

        // Using URI instead of File.toUri for cleaner code
        URI fileUri = filePath.toUri();
        Resource fileResource = new UrlResource(fileUri);

        // Use Files API to check for file existence and readability
        if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
            String errorMessage = String.format("No resource found for %s:version /%d", filePath, version);
            logger.error("File resource not found: {}", errorMessage);
            throw new ResourceNotFoundException(errorMessage);
        }

        logger.info("File '{}' successfully loaded", filePath);
        return fileResource;
    }

    /**
     * Reads the contents of a properties file.
     *
     * @param propertiesFilePath the properties file path
     * @return file contents as a string
     */
    public static String readPropertiesFile(String propertiesFilePath) {
        try (InputStream inputStream = new FileInputStream(propertiesFilePath)) {
            return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("Failed to read properties file: {}", propertiesFilePath, e);
            return null;
        }
    }

    /**
     * Deletes a directory and its contents recursively.
     *
     * @param directoryPath the directory path
     * @param forceDelete   whether to force deletion
     * @return true if deletion was successful, false otherwise
     * @throws NotDirectoryException if the given path is not a directory
     */
    public static boolean deleteDirectoryRecursively(File directoryPath, boolean forceDelete) throws NotDirectoryException {
        if (directoryPath.isDirectory()) {
            for (File file : Objects.requireNonNull(directoryPath.listFiles())) {
                boolean success = deleteDirectoryRecursively(file, forceDelete);
                if (!success) return false;
            }
            boolean deleted = directoryPath.delete();
            if (deleted) {
                logger.info("Deleted directory: {}", directoryPath);
            }
            return deleted;
        }
        throw new NotDirectoryException(directoryPath.getName());
    }

    //------------------- ZIP Compression & Extraction -------------------

    /**
     * Compresses a single file into a ZIP archive.
     *
     * @param inputFile     the file to compress
     * @param outputZipPath the destination ZIP file path
     * @throws IOException if an I/O error occurs
     */
    public static void zipSingleFile(File inputFile, String outputZipPath) throws IOException {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(outputZipPath))) {
            zipOutputStream.putNextEntry(new ZipEntry(inputFile.getName()));
            try (FileInputStream fileInputStream = new FileInputStream(inputFile)) {
                IOUtils.copy(fileInputStream, zipOutputStream);
            }
            zipOutputStream.closeEntry();
            logger.info("Compressed file to: {}", outputZipPath);
        }
    }

    /**
     * Compresses multiple files into a ZIP archive.
     *
     * @param inputFiles    the list of files to compress
     * @param outputZipPath the destination ZIP file path
     * @throws IOException if an I/O error occurs
     */
    public static void zipMultipleFiles(List<File> inputFiles, String outputZipPath) throws IOException {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(outputZipPath))) {
            for (File file : inputFiles) {
                try (FileInputStream fileInputStream = new FileInputStream(file)) {
                    zipOutputStream.putNextEntry(new ZipEntry(file.getName()));
                    IOUtils.copy(fileInputStream, zipOutputStream);
                    zipOutputStream.closeEntry();
                }
            }
            logger.info("Compressed files into ZIP: {}", outputZipPath);
        }
    }

    /**
     * Extracts a ZIP archive to a specified directory.
     *
     * @param zipFilePath    the ZIP file path
     * @param destinationDir the directory where extracted files will be saved
     * @throws IOException if an I/O error occurs
     */
    public static void unzipFile(String zipFilePath, String destinationDir) throws IOException {
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                File outputFile = new File(destinationDir, entry.getName());
                if (entry.isDirectory()) {
                    outputFile.mkdirs();
                } else {
                    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile))) {
                        IOUtils.copy(zipInputStream, bos);
                    }
                }
                zipInputStream.closeEntry();
            }
            logger.info("Extracted ZIP file to: {}", destinationDir);
        }
    }

    //------------------- Utility Methods -------------------

    /**
     * Builds a list of daily-generated file paths based on given base directories.
     *
     * @param basePathValid   the base path for valid files
     * @param basePathInvalid the base path for invalid files
     * @return the list of generated file paths
     */
    public static List<String> generateDailyFilePaths(String basePathValid, String basePathInvalid) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int today = calendar.get(Calendar.DAY_OF_MONTH);
        int yesterday = today - 1;

        String monthFormatted = new DecimalFormat("#00").format(month);
        String todayFormatted = new DecimalFormat("#00").format(today);
        String yesterdayFormatted = new DecimalFormat("#00").format(yesterday);

        List<String> filePaths = List.of(
                Path.of(basePathInvalid).resolve(year + "_" + monthFormatted).resolve(yesterdayFormatted).toString(),
                Path.of(basePathInvalid).resolve(year + "_" + monthFormatted).resolve(todayFormatted).toString(),
                Path.of(basePathValid).resolve(year + "_" + monthFormatted).resolve(yesterdayFormatted).toString(),
                Path.of(basePathValid).resolve(year + "_" + monthFormatted).resolve(todayFormatted).toString()
        );

        logger.debug("Generated file paths: {}", filePaths);
        return filePaths;
    }

    static boolean isImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        String contentType = file.getContentType();
        return contentType != null && contentType.toLowerCase().startsWith("image/");
    }
}