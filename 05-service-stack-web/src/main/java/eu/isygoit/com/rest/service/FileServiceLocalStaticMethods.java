package eu.isygoit.com.rest.service;

import eu.isygoit.constants.TenantConstants;
import eu.isygoit.exception.EmptyPathException;
import eu.isygoit.exception.FileNotFoundException;
import eu.isygoit.exception.ResourceNotFoundException;
import eu.isygoit.model.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Utility class providing static methods for local file operations such as upload, download, and delete.
 * This class is stateless and should not be instantiated.
 */
@Slf4j
public final class FileServiceLocalStaticMethods {

    // Private constructor to prevent instantiation of this utility class
    private FileServiceLocalStaticMethods() {}

    /**
     * Uploads a file to the local file system under a given entity's path.
     *
     * @param <T>    A type that supports file operations and has path, id, and code attributes.
     * @param file   The file to be uploaded (typically from an HTTP request).
     * @param entity The entity providing the storage path and file name.
     * @return The code used as the file name.
     * @throws IOException If writing the file fails.
     */
    public static <T extends IFileEntity & IIdAssignable & ICodeAssignable> String upload(MultipartFile file, T entity) throws IOException {
        Path directory = Path.of(entity.getPath());

        // Ensure the directory exists (creates it and its parents if missing)
        Files.createDirectories(directory);

        // Determine the target path using the entity's code
        Path targetPath = directory.resolve(entity.getCode());

        // Copy the uploaded file to the target location, replacing any existing file
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        return entity.getCode();
    }

    /**
     * Downloads a file as a Spring Resource from the local file system based on an entity's path and file name.
     *
     * @param <T>     A type that supports file operations and has path, id, and code attributes.
     * @param entity  The entity providing the file path and file name.
     * @param version The file version (used for error reporting only).
     * @return The file as a Spring Resource.
     * @throws MalformedURLException If the file path cannot be converted to a URL.
     */
    public static <T extends IFileEntity & IIdAssignable & ICodeAssignable> Resource download(T entity, Long version) throws MalformedURLException {
        String path = entity.getPath();

        // Ensure the path is not null or empty
        if (!StringUtils.hasText(path)) {
            throw new EmptyPathException(buildErrorMessage(entity, version, "Empty path"));
        }

        Path filePath = Path.of(path).resolve(entity.getFileName());
        Resource resource = new UrlResource(filePath.toUri());

        // Check if the file exists, otherwise throw an exception
        if (!resource.exists()) {
            throw new ResourceNotFoundException(buildErrorMessage(entity, version, "Resource not found"));
        }

        return resource;
    }

    /**
     * Deletes a file from the local file system based on the provided entity's path and file name.
     *
     * @param <T>    A type that represents a linked file and provides a code, path, and ID.
     * @param entity The entity containing the file to delete.
     * @return true if the file was successfully deleted.
     * @throws IOException If deletion fails or file does not exist.
     */
    public static <T extends ILinkedFile & ICodeAssignable & IIdAssignable> boolean delete(T entity) throws IOException {
        Path filePath = Path.of(entity.getPath()).resolve(entity.getFileName());
        File file = filePath.toFile();

        // If the file exists, delete it; otherwise, throw a custom not-found exception
        if (!file.exists()) {
            throw new FileNotFoundException(filePath.toString());
        }

        FileUtils.delete(file);
        return true;
    }

    /**
     * Builds a consistent error message for missing resources or paths.
     *
     * @param entity The entity related to the file.
     * @param version The version involved in the request.
     * @param prefix A message prefix such as "Resource not found" or "Empty path".
     * @return A formatted error message with tenant and file info.
     */
    private static String buildErrorMessage(Object entity, Long version, String prefix) {
        String tenant = (entity instanceof ITenantAssignable da)
                ? da.getTenant()
                : TenantConstants.DEFAULT_TENANT_NAME;

        String fileName = (entity instanceof IFileEntity fe)
                ? fe.getFileName()
                : "unknown";

        return String.format("%s: %s/%s/%d", prefix, tenant, fileName, version);
    }
}