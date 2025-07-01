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
 * The type File service local static methods.
 */
@Slf4j
public final class FileServiceLocalStaticMethods {

    // Private constructor to prevent instantiation of this utility class
    private FileServiceLocalStaticMethods() {
    }

    /**
     * Upload string.
     *
     * @param <T>    the type parameter
     * @param file   the file
     * @param entity the entity
     * @return the string
     * @throws IOException the io exception
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
     * Download resource.
     *
     * @param <T>     the type parameter
     * @param entity  the entity
     * @param version the version
     * @return the resource
     * @throws MalformedURLException the malformed url exception
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
     * Delete boolean.
     *
     * @param <T>    the type parameter
     * @param entity the entity
     * @return the boolean
     * @throws IOException the io exception
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