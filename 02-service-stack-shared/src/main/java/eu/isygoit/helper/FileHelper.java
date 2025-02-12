package eu.isygoit.helper;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.FileAttribute;
import java.text.DecimalFormat;
import java.util.*;
import java.util.zip.*;

/**
 * Utility class for handling file operations, including directory management,
 * file storage, multipart file handling, and ZIP compression/extraction.
 */
@Slf4j
public class FileHelper {

    //------------------- Directory Management -------------------

    /**
     * Creates a directory if it does not exist.
     *
     * @param directoryPath the directory path
     */
    public static void createDirectoryIfAbsent(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdirs();
            log.info("Directory created: {}", directoryPath);
        } else {
            log.debug("Directory already exists: {}", directoryPath);
        }
    }

    //------------------- File Storage & Retrieval -------------------

    /**
     * Saves a MultipartFile to the specified directory with a given filename and extension.
     *
     * @param targetDirectory the directory where the file will be saved
     * @param fileName        the desired filename
     * @param multipartFile   the file to save
     * @param fileExtension   the file extension (optional)
     * @return the Path of the saved file
     * @throws IOException if an I/O error occurs
     */
    public static Path saveMultipartFile(String targetDirectory, String fileName, MultipartFile multipartFile, String fileExtension) throws IOException {
        if (multipartFile != null && !multipartFile.isEmpty()) {
            createDirectoryIfAbsent(targetDirectory);

            String finalFileName = StringUtils.hasText(fileName)
                    ? fileName + "." + (StringUtils.hasText(fileExtension) ? fileExtension : FilenameUtils.getExtension(multipartFile.getOriginalFilename()))
                    : multipartFile.getOriginalFilename();

            Path filePath = Paths.get(targetDirectory, finalFileName);
            Files.write(filePath, multipartFile.getBytes());

            log.info("File saved at: {}", filePath);
            return filePath;
        } else {
            log.warn("Attempted to save an empty MultipartFile.");
            throw new IllegalArgumentException("Provided file is empty.");
        }
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
            log.error("Failed to read properties file: {}", propertiesFilePath, e);
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
                log.info("Deleted directory: {}", directoryPath);
            }
            return deleted;
        }
        throw new NotDirectoryException(directoryPath.getName());
    }

    //------------------- ZIP Compression & Extraction -------------------

    /**
     * Compresses a single file into a ZIP archive.
     *
     * @param inputFile    the file to compress
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
            log.info("Compressed file to: {}", outputZipPath);
        }
    }

    /**
     * Compresses multiple files into a ZIP archive.
     *
     * @param inputFiles   the list of files to compress
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
            log.info("Compressed files into ZIP: {}", outputZipPath);
        }
    }

    /**
     * Extracts a ZIP archive to a specified directory.
     *
     * @param zipFilePath the ZIP file path
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
            log.info("Extracted ZIP file to: {}", destinationDir);
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
                basePathInvalid + File.separator + year + "_" + monthFormatted + File.separator + yesterdayFormatted,
                basePathInvalid + File.separator + year + "_" + monthFormatted + File.separator + todayFormatted,
                basePathValid + File.separator + year + "_" + monthFormatted + File.separator + yesterdayFormatted,
                basePathValid + File.separator + year + "_" + monthFormatted + File.separator + todayFormatted
        );

        log.debug("Generated file paths: {}", filePaths);
        return filePaths;
    }
}