package eu.isygoit.com.rest.service.impl.utils;

import eu.isygoit.exception.EmptyPathException;
import eu.isygoit.exception.FileNotFoundException;
import eu.isygoit.helper.FileHelper;
import eu.isygoit.model.AssignableCode;
import eu.isygoit.model.AssignableFile;
import eu.isygoit.model.AssignableId;
import eu.isygoit.model.LinkedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;

/**
 * The interface Local file storage utils.
 */
public interface LocalFileStorageUtils {

    /**
     * The constant logger.
     */
    Logger logger = LoggerFactory.getLogger(LocalFileStorageUtils.class);

    /**
     * Upload file string.
     *
     * @param <E>           the type parameter
     * @param multipartFile the multipart file
     * @param fileEntity    the file entity
     * @param options       the options
     * @return the string
     * @throws IOException the io exception
     */
    static <E extends AssignableFile & AssignableId & AssignableCode> String uploadFile(MultipartFile multipartFile, E fileEntity, OpenOption... options) throws IOException {
        Path storagePath = FileHelper.saveMultipartFile(Path.of(fileEntity.getPath()),
                fileEntity.getFileName(),
                multipartFile,
                fileEntity.getExtension(),
                options);

        return fileEntity.getFileName();
    }

    /**
     * Download file resource.
     *
     * @param <E>        the type parameter
     * @param fileEntity the file entity
     * @param version    the version
     * @return the resource
     * @throws MalformedURLException the malformed url exception
     */
    static <E extends AssignableFile & AssignableId & AssignableCode> Resource downloadFile(E fileEntity, Long version) throws MalformedURLException {
        if (!StringUtils.hasText(fileEntity.getPath())) {
            String errorMessage = String.format("No resource found for %s/%s/%d", CrudServiceUtils.getDomainOrDefault(fileEntity), fileEntity.getFileName(), version);
            logger.error("File load failed: {}", errorMessage);
            throw new EmptyPathException(errorMessage);
        }

        Path filePath = Path.of(fileEntity.getPath()).resolve(fileEntity.getFileName());

        logger.info("File '{}' successfully loaded from: {}", fileEntity.getFileName(), filePath);
        return FileHelper.downloadResource(filePath, version);
    }

    /**
     * Delete file boolean.
     *
     * @param <L>        the type parameter
     * @param fileEntity the file entity
     * @return the boolean
     * @throws IOException the io exception
     */
    static <L extends LinkedFile & AssignableCode & AssignableId> boolean deleteFile(L fileEntity) throws IOException {
        Path filePath = Path.of(fileEntity.getPath()).resolve(fileEntity.getFileName());

        if (Files.deleteIfExists(filePath)) {
            logger.info("File '{}' successfully deleted from: {}", fileEntity.getFileName(), filePath);
            return true;
        }

        String errorMessage = "File not found: " + filePath;
        logger.error(errorMessage);
        throw new FileNotFoundException(errorMessage);
    }
}