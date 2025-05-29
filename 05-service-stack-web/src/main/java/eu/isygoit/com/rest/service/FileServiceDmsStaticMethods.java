package eu.isygoit.com.rest.service;

import eu.isygoit.com.rest.api.ILinkedFileApi;
import eu.isygoit.constants.DomainConstants;
import eu.isygoit.dto.common.LinkedFileRequestDto;
import eu.isygoit.dto.common.LinkedFileResponseDto;
import eu.isygoit.dto.common.RequestContextDto;
import eu.isygoit.exception.EntityNullException;
import eu.isygoit.exception.LinkedFileServiceNullException;
import eu.isygoit.exception.MultiPartFileNullException;
import eu.isygoit.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Utility class for managing file operations via DMS (Document Management System) linked file service.
 */
@Slf4j
public final class FileServiceDmsStaticMethods {

    /**
     * Uploads a file and associates it with the given entity through the linked file service.
     *
     * @param <T>               the type parameter constrained to file entities with id and code
     * @param file              the multipart file to upload
     * @param entity            the entity associated with the file
     * @param linkedFileService the linked file service interface
     * @return LinkedFileResponseDto containing upload response data, or null if upload fails
     * @throws IOException if an I/O error occurs during file upload
     */
    static <T extends IFileEntity & IIdAssignable & ICodeAssignable> LinkedFileResponseDto upload(
            MultipartFile file,
            T entity,
            ILinkedFileApi linkedFileService) throws IOException {

        if (linkedFileService == null) {
            log.error("LinkedFileApi service is null in upload");
            throw new LinkedFileServiceNullException("LinkedFileApi service is null");
        }
        if (file == null) {
            log.error("MultipartFile is null in upload");
            throw new MultiPartFileNullException("MultipartFile must not be null");
        }
        if (entity == null) {
            log.error("Entity is null in upload");
            throw new EntityNullException("Entity must not be null");
        }

        // Determine domain, fallback to default if entity doesn't implement IDomainAssignable
        var domain = (entity instanceof IDomainAssignable domainAssignable)
                ? domainAssignable.getDomain()
                : DomainConstants.DEFAULT_DOMAIN_NAME;

        // Prepare the request DTO for uploading the linked file
        var requestDto = LinkedFileRequestDto.builder()
                .domain(domain)
                .code(entity.getCode())
                .path(entity.getClass().getSimpleName().toLowerCase())
                .tags(entity.getTags())
                .categoryNames(List.of(entity.getClass().getSimpleName()))
                .file(file)
                .build();

        // Call linked file service to upload
        ResponseEntity<LinkedFileResponseDto> response = linkedFileService.upload(requestDto);

        // Return body if successful, else null
        if (response.getStatusCode().is2xxSuccessful() && response.hasBody()) {
            log.info("File uploaded successfully: {} with code {}", file.getOriginalFilename(), response.getBody().getCode());
            return response.getBody();
        }

        log.warn("Failed to upload file: {} for entity code {}", file.getOriginalFilename(), entity.getCode());
        return null;
    }

    /**
     * Downloads the resource associated with the entity and version from the linked file service.
     *
     * @param <T>               the type parameter constrained to file entities with id and code
     * @param entity            the entity whose file to download
     * @param version           the version of the file (currently unused but kept for interface compatibility)
     * @param linkedFileService the linked file service interface
     * @return the downloaded Resource, or null if download fails
     * @throws IOException if an I/O error occurs during download
     */
    static <T extends IFileEntity & IIdAssignable & ICodeAssignable> Resource download(
            T entity,
            Long version,
            ILinkedFileApi linkedFileService) throws IOException {

        if (linkedFileService == null) {
            log.error("LinkedFileApi service is null in upload");
            throw new LinkedFileServiceNullException("LinkedFileApi service is null");
        }

        if (entity == null) {
            log.error("Entity is null in upload");
            throw new EntityNullException("Entity must not be null");
        }

        var domain = (entity instanceof IDomainAssignable domainAssignable)
                ? domainAssignable.getDomain()
                : DomainConstants.DEFAULT_DOMAIN_NAME;

        // Perform the download request
        ResponseEntity<Resource> response = linkedFileService.download(RequestContextDto.builder().build(), domain, entity.getCode());

        if (response.getStatusCode().is2xxSuccessful() && response.hasBody()) {
            log.info("File downloaded successfully with domain {} and code {}", domain, entity.getCode());
            return response.getBody();
        }

        log.warn("Failed to download file for domain {} and code {}", domain, entity.getCode());
        return null;
    }

    /**
     * Deletes the linked file associated with the given entity via the linked file service.
     *
     * @param <L>               the type parameter constrained to linked files with code and id
     * @param entity            the linked file entity to delete
     * @param linkedFileService the linked file service interface
     * @return true if deletion was successful, false otherwise
     */
    public static <L extends ILinkedFile & ICodeAssignable & IIdAssignable> boolean delete(
            L entity,
            ILinkedFileApi linkedFileService) {

        if (linkedFileService == null) {
            log.error("LinkedFileApi service is null in upload");
            throw new LinkedFileServiceNullException("LinkedFileApi service is null");
        }

        if (entity == null) {
            log.error("Entity is null in upload");
            throw new EntityNullException("Entity must not be null");
        }

        var domain = (entity instanceof IDomainAssignable domainAssignable)
                ? domainAssignable.getDomain()
                : DomainConstants.DEFAULT_DOMAIN_NAME;

        var response = linkedFileService.deleteFile(RequestContextDto.builder().build(), domain, entity.getCode());

        if (response.getStatusCode().is2xxSuccessful() && Boolean.TRUE.equals(response.getBody())) {
            log.info("File deleted successfully with domain {} and code {}", domain, entity.getCode());
            return true;
        }

        log.warn("Failed to delete file with domain {} and code {}", domain, entity.getCode());
        return false;
    }
}