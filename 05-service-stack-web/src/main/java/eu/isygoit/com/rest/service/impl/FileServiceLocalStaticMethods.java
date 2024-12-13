package eu.isygoit.com.rest.service.impl;

import eu.isygoit.constants.DomainConstants;
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
        if (!Files.exists(filePath)) {
            Files.createDirectories(filePath);
        }

        Files.copy(file.getInputStream(), filePath.resolve(entity.getCode()), StandardCopyOption.REPLACE_EXISTING);
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
            Resource resource = new UrlResource(Path.of(entity.getPath() +
                    File.separator + entity.getFileName()).toUri());
            if (!resource.exists()) {
                throw new ResourceNotFoundException("No resource found for "
                        + (entity instanceof ISAASEntity isaasEntity
                        ? isaasEntity.getDomain()
                        : DomainConstants.DEFAULT_DOMAIN_NAME
                        + '/' + entity.getFileName()
                        + "/" + version));
            }
            return resource;
        } else {
            throw new EmptyPathException("For entity "
                    + (entity instanceof ISAASEntity isaasEntity
                    ? isaasEntity.getDomain()
                    : DomainConstants.DEFAULT_DOMAIN_NAME
                    + '/' + entity.getFileName()
                    + "/" + version));
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
        File file = new File(entity.getPath()
                + File.separator + entity.getFileName());
        if (file.exists()) {
            FileUtils.delete(file);
            return true;
        }

        throw new FileNotFoundException(entity.getPath()
                + File.separator + entity.getCode());
    }
}
