package eu.isygoit.com.rest.service;

import eu.isygoit.com.rest.api.ILinkedFileApi;
import eu.isygoit.constants.TenantConstants;
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
 * The type File service dms static methods.
 */
@Slf4j
public final class FileServiceDmsStaticMethods {

    /**
     * Upload linked file response dto.
     *
     * @param <T>               the type parameter
     * @param file              the file
     * @param entity            the entity
     * @param linkedFileService the linked file service
     * @return the linked file response dto
     * @throws IOException the io exception
     */
    public static <T extends IFileEntity & IIdAssignable & ICodeAssignable> LinkedFileResponseDto upload(
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

        // Determine tenant, fallback to default if entity doesn't implement ITenantAssignable
        var tenant = (entity instanceof ITenantAssignable tenantAssignable)
                ? tenantAssignable.getTenant()
                : TenantConstants.DEFAULT_TENANT_NAME;

        // Prepare the request DTO for uploading the linked file
        var requestDto = LinkedFileRequestDto.builder()
                .tenant(tenant)
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
     * Download resource.
     *
     * @param <T>               the type parameter
     * @param entity            the entity
     * @param version           the version
     * @param linkedFileService the linked file service
     * @return the resource
     * @throws IOException the io exception
     */
    public static <T extends IFileEntity & IIdAssignable & ICodeAssignable> Resource download(
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

        var tenant = (entity instanceof ITenantAssignable tenantAssignable)
                ? tenantAssignable.getTenant()
                : TenantConstants.DEFAULT_TENANT_NAME;

        // Perform the download request
        ResponseEntity<Resource> response = linkedFileService.download(RequestContextDto.builder().build(), tenant, entity.getCode());

        if (response.getStatusCode().is2xxSuccessful() && response.hasBody()) {
            log.info("File downloaded successfully with tenant {} and code {}", tenant, entity.getCode());
            return response.getBody();
        }

        log.warn("Failed to download file for tenant {} and code {}", tenant, entity.getCode());
        return null;
    }

    /**
     * Delete boolean.
     *
     * @param <L>               the type parameter
     * @param entity            the entity
     * @param linkedFileService the linked file service
     * @return the boolean
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

        var tenant = (entity instanceof ITenantAssignable tenantAssignable)
                ? tenantAssignable.getTenant()
                : TenantConstants.DEFAULT_TENANT_NAME;

        var response = linkedFileService.deleteFile(RequestContextDto.builder().build(), tenant, entity.getCode());

        if (response.getStatusCode().is2xxSuccessful() && Boolean.TRUE.equals(response.getBody())) {
            log.info("File deleted successfully with tenant {} and code {}", tenant, entity.getCode());
            return true;
        }

        log.warn("Failed to delete file with tenant {} and code {}", tenant, entity.getCode());
        return false;
    }
}