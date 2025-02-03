package eu.isygoit.com.rest.service.impl;

import eu.isygoit.exception.EmptyPathException;
import eu.isygoit.exception.FileNotFoundException;
import eu.isygoit.exception.ResourceNotFoundException;
import eu.isygoit.model.ICodifiable;
import eu.isygoit.model.IFileEntity;
import eu.isygoit.model.IIdEntity;
import eu.isygoit.model.ILinkedFile;
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

    private static void createDirectoryIfNeeded(Path filePath) throws IOException {
        if (!Files.exists(filePath)) {
            Files.createDirectories(filePath);
            log.info("Directory created at {}", filePath);
        }
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
    static <T extends IFileEntity & IIdEntity & ICodifiable> String upload(MultipartFile file, T entity) throws IOException {
        Path filePath = Path.of(entity.getPath());
        createDirectoryIfNeeded(filePath);

        Files.copy(file.getInputStream(), filePath.resolve(entity.getCode()), StandardCopyOption.REPLACE_EXISTING);
        log.info("File uploaded successfully: {} at {}", file.getOriginalFilename(), filePath.resolve(entity.getCode()));
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
    static <T extends IFileEntity & IIdEntity & ICodifiable> Resource download(T entity, Long version) throws MalformedURLException {
        if (StringUtils.hasText(entity.getPath())) {
            Path filePath = Path.of(entity.getPath(), entity.getFileName());
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) {
                throw new ResourceNotFoundException("No resource found for " + filePath);
            }
            log.info("File downloaded successfully: {} from path {}", entity.getFileName(), filePath);
            return resource;
        } else {
            throw new EmptyPathException("Path is empty for entity " + entity.getClass().getSimpleName());
        }
    }

    /**
     * Delete boolean.
     *
     * @param <L>    the type parameter
     * @param entity the entity
     * @return the boolean
     * @throws IOException the io exception
     */
    public static <L extends ILinkedFile & ICodifiable & IIdEntity> boolean delete(L entity) throws IOException {
        File file = new File(entity.getPath() + File.separator + entity.getFileName());
        if (file.exists()) {
            FileUtils.delete(file);
            log.info("File deleted successfully: {} at {}", entity.getFileName(), file.getAbsolutePath());
            return true;
        }

        throw new FileNotFoundException("File not found: " + file.getAbsolutePath());
    }
}