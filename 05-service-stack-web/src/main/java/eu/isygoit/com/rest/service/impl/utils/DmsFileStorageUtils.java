package eu.isygoit.com.rest.service.impl.utils;

import eu.isygoit.com.rest.api.IDmsLinkedFileService;
import eu.isygoit.dto.common.LinkedFileRequestDto;
import eu.isygoit.dto.common.LinkedFileResponseDto;
import eu.isygoit.dto.common.RequestContextDto;
import eu.isygoit.model.AssignableCode;
import eu.isygoit.model.AssignableFile;
import eu.isygoit.model.AssignableId;
import eu.isygoit.model.LinkedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * The interface Dms file storage utils.
 */
public interface DmsFileStorageUtils {

    /**
     * The constant logger.
     */
    Logger logger = LoggerFactory.getLogger(DmsFileStorageUtils.class);

    /**
     * Upload file linked file response dto.
     *
     * @param <E>         the type parameter
     * @param file        the file
     * @param fileEntity  the file entity
     * @param fileService the file service
     * @return the linked file response dto
     * @throws IOException the io exception
     */
    static <E extends AssignableId & AssignableFile & AssignableCode> LinkedFileResponseDto uploadFile(MultipartFile file, E fileEntity, IDmsLinkedFileService fileService) throws IOException {

        logger.info("Starting file upload: {}", file.getOriginalFilename());

        // Determine the domain for the file entity
        final var domain = CrudServiceUtils.getDomainOrDefault(fileEntity);

        // Construct file request payload
        var fileRequest = LinkedFileRequestDto.builder()
                .domain(domain)
                .code(fileEntity.getCode())
                .path(File.separator + fileEntity.getClass().getSimpleName().toLowerCase())
                .tags(fileEntity.getTags())
                .categoryNames(List.of(fileEntity.getClass().getSimpleName()))
                .file(file)
                .build();

        logger.debug("Uploading file with request payload: {}", fileRequest);

        // Call the file service to upload
        ResponseEntity<LinkedFileResponseDto> responseEntity = fileService.upload(fileRequest);

        return Optional.ofNullable(responseEntity)
                .filter(response -> response.getStatusCode().is2xxSuccessful())
                .map(ResponseEntity::getBody)
                .map(responseBody -> {
                    logger.info("File '{}' uploaded successfully with code '{}'",
                            file.getOriginalFilename(), responseBody.getCode());
                    return responseBody;
                })
                .orElseThrow(() -> {
                    logger.error("File upload failed for: {}", file.getOriginalFilename());
                    return new IOException("File upload failed");
                });
    }

    /**
     * Download file resource.
     *
     * @param <E>         the type parameter
     * @param fileEntity  the file entity
     * @param version     the version
     * @param fileService the file service
     * @return the resource
     * @throws IOException the io exception
     */
    static <E extends AssignableId & AssignableFile & AssignableCode> Resource downloadFile(
            E fileEntity, Long version, IDmsLinkedFileService fileService) throws IOException {

        logger.info("Initiating file download for entity: {}, version: {}", fileEntity.getCode(), version);

        // Determine the domain for the file entity
        final var domain = CrudServiceUtils.getDomainOrDefault(fileEntity);

        logger.debug("Downloading file from domain: {}", domain);

        // Call the file service to download
        ResponseEntity<Resource> responseEntity = fileService.download(RequestContextDto.builder().build(),
                domain,
                fileEntity.getCode());

        return Optional.ofNullable(responseEntity)
                .filter(response -> response.getStatusCode().is2xxSuccessful())
                .map(ResponseEntity::getBody)
                .map(resource -> {
                    logger.info("File downloaded successfully from domain '{}' with code '{}'",
                            domain, fileEntity.getCode());
                    return resource;
                })
                .orElseThrow(() -> {
                    logger.error("File download failed for entity: {}", fileEntity.getCode());
                    return new IOException("File download failed");
                });
    }

    /**
     * Delete file boolean.
     *
     * @param <L>         the type parameter
     * @param fileEntity  the file entity
     * @param fileService the file service
     * @return the boolean
     */
    static <L extends AssignableId & LinkedFile & AssignableCode> boolean deleteFile(
            L fileEntity, IDmsLinkedFileService fileService) {

        logger.info("Request to delete file for entity: {}", fileEntity.getCode());

        // Determine the domain for the file entity
        final var domain = CrudServiceUtils.getDomainOrDefault(fileEntity);

        logger.debug("Deleting file from domain: {}", domain);

        // Call the file service to delete the file
        ResponseEntity<Boolean> responseEntity = fileService.deleteFile(RequestContextDto.builder().build(),
                domain,
                fileEntity.getCode());

        return Optional.ofNullable(responseEntity)
                .filter(response -> response.getStatusCode().is2xxSuccessful())
                .map(ResponseEntity::getBody)
                .map(deletionSuccess -> {
                    if (deletionSuccess) {
                        logger.info("File deleted successfully from domain '{}' with code '{}'", domain, fileEntity.getCode());
                    } else {
                        logger.warn("File deletion was unsuccessful for domain '{}' and code '{}'", domain, fileEntity.getCode());
                    }
                    return deletionSuccess;
                })
                .orElseGet(() -> {
                    logger.error("File deletion failed for entity: {}", fileEntity.getCode());
                    return false;
                });
    }
}