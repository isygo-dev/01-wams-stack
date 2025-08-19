package eu.isygoit.com.rest.controller.impl;

import eu.isygoit.com.rest.api.IMappedFileApi;
import eu.isygoit.com.rest.controller.ResponseFactory;
import eu.isygoit.com.rest.service.ICrudServiceMethods;
import eu.isygoit.com.rest.service.ICrudServiceUtils;
import eu.isygoit.com.rest.service.IFileServiceMethods;
import eu.isygoit.dto.IDto;
import eu.isygoit.dto.IFileUploadDto;
import eu.isygoit.dto.IIdAssignableDto;
import eu.isygoit.dto.ITenantAssignableDto;
import eu.isygoit.dto.common.ContextRequestDto;
import eu.isygoit.model.IFileEntity;
import eu.isygoit.model.IIdAssignable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;

/**
 * Abstract controller class for handling file-related operations via REST API.
 * Provides endpoints for uploading, downloading, creating, and updating entities with associated files
 * for entities implementing {@link IFileEntity} and {@link IIdAssignable}.
 *
 * @param <I> the type of the identifier, extending {@link Serializable}
 * @param <T> the entity type, extending {@link IIdAssignable} and {@link IFileEntity}
 * @param <M> the main DTO type, extending {@link IIdAssignableDto} and {@link IFileUploadDto}
 * @param <F> the full DTO type, extending {@link M}
 * @param <S> the service type, implementing {@link IFileServiceMethods}, {@link ICrudServiceMethods}, and {@link ICrudServiceUtils}
 */
@Slf4j
public abstract class MappedFileController<
        I extends Serializable,
        T extends IIdAssignable<I> & IFileEntity,
        M extends IIdAssignableDto<I> & IDto & IFileUploadDto,
        F extends M,
        S extends IFileServiceMethods<I, T> & ICrudServiceMethods<I, T> & ICrudServiceUtils<I, T>
        > extends CrudControllerUtils<I, T, M, F, S> implements IMappedFileApi<I, F> {

    /**
     * Uploads a file for the specified entity.
     *
     * @param requestContext the request context containing metadata
     * @param id             the ID of the entity
     * @param file           the multipart file to upload
     * @return a response entity containing the updated entity DTO
     * @throws IOException if an I/O error occurs during file upload
     */
    @Override
    public ResponseEntity<F> uploadFile(ContextRequestDto requestContext, I id, MultipartFile file) {
        log.debug("Uploading file for entityId: {}", id);
        try {
            var entity = crudService().uploadFile(id, file);
            var dto = mapper().entityToDto(entity);
            log.info("Successfully uploaded file for entityId: {}", id);
            return ResponseFactory.responseOk(dto);
        } catch (IOException e) {
            log.error("Failed to upload file for entityId: {}", id, e);
            return getBackExceptionResponse(e);
        }
    }

    /**
     * Downloads a file associated with the specified entity and version.
     *
     * @param requestContext the request context containing metadata
     * @param id             the ID of the entity
     * @param version        the version of the file to download
     * @return a response entity containing the file resource
     * @throws IOException if an I/O error occurs during file download
     */
    @Override
    public ResponseEntity<Resource> downloadFile(ContextRequestDto requestContext, I id, Long version) {
        log.debug("Downloading file for entityId: {}, version: {}", id, version);
        try {
            var resource = crudService().downloadFile(id, version);
            if (resource != null && resource.getResource() != null) {
                var file = resource.getResource().getFile();
                var contentType = Files.probeContentType(file.toPath());
                log.info("Successfully downloaded file: {} for entityId: {}, version: {}",
                        resource.getOriginalFileName(), id, version);
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, contentType != null ? contentType : "application/octet-stream")
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getOriginalFileName() + "\"")
                        .body(resource.getResource());
            }
            log.warn("File not found for entityId: {}, version: {}", id, version);
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            log.error("Failed to download file for entityId: {}, version: {}", id, version, e);
            return getBackExceptionResponse(e);
        }
    }

    /**
     * Creates an entity with an associated file.
     *
     * @param requestContext the request context containing metadata
     * @param file           the multipart file to upload
     * @param dto            the DTO containing entity data
     * @return a response entity containing the created entity DTO
     * @throws IOException if an I/O error occurs during creation or file upload
     */
    @Override
    public ResponseEntity<F> createWithFile(ContextRequestDto requestContext, MultipartFile file, F dto) {
        log.debug("Creating entity with file for tenant: {}", requestContext.getSenderTenant());
        try {
            if (dto instanceof ITenantAssignableDto tenantAssignableDto && StringUtils.isEmpty(tenantAssignableDto.getTenant())) {
                tenantAssignableDto.setTenant(requestContext.getSenderTenant());
                log.debug("Assigned tenant {} to DTO", requestContext.getSenderTenant());
            }
            var processedDto = beforeCreate(dto);
            var entity = crudService().createWithFile(mapper().dtoToEntity(processedDto), file);
            var result = afterCreate(entity);
            log.info("Successfully created entity with file for tenant: {}", requestContext.getSenderTenant());
            return ResponseFactory.responseCreated(mapper().entityToDto(result));
        } catch (IOException e) {
            log.error("Failed to create entity with file for tenant: {}", requestContext.getSenderTenant(), e);
            return getBackExceptionResponse(e);
        }
    }

    /**
     * Updates an entity with an associated file.
     *
     * @param requestContext the request context containing metadata
     * @param id             the ID of the entity to update
     * @param file           the multipart file to upload
     * @param dto            the DTO containing updated entity data
     * @return a response entity containing the updated entity DTO
     * @throws IOException if an I/O error occurs during update or file upload
     */
    @Override
    public ResponseEntity<F> updateWithFile(ContextRequestDto requestContext, I id, MultipartFile file, F dto) {
        log.debug("Updating entity with file for entityId: {}", id);
        try {
            var processedDto = beforeUpdate(dto);
            var entity = crudService().updateWithFile(id, mapper().dtoToEntity(processedDto), file);
            var result = afterUpdate(entity);
            log.info("Successfully updated entity with file for entityId: {}", id);
            return ResponseFactory.responseOk(mapper().entityToDto(result));
        } catch (IOException e) {
            log.error("Failed to update entity with file for entityId: {}", id, e);
            return getBackExceptionResponse(e);
        }
    }

    /**
     * Hook method called before creating an entity, allowing customization.
     *
     * @param object the DTO to process
     * @return the processed DTO
     * @throws IOException if an I/O error occurs
     */
    protected F beforeCreate(F object) throws IOException {
        log.debug("Executing beforeCreate for DTO with ID: {}", object.getId());
        return object;
    }

    /**
     * Hook method called after creating an entity, allowing customization.
     *
     * @param object the created entity
     * @return the processed entity
     * @throws IOException if an I/O error occurs
     */
    protected T afterCreate(T object) throws IOException {
        log.debug("Executing afterCreate for entity with ID: {}", object.getId());
        return object;
    }

    /**
     * Hook method called before updating an entity, allowing customization.
     *
     * @param object the DTO to process
     * @return the processed DTO
     * @throws IOException if an I/O error occurs
     */
    protected F beforeUpdate(F object) throws IOException {
        log.debug("Executing beforeUpdate for DTO with ID: {}", object.getId());
        return object;
    }

    /**
     * Hook method called after updating an entity, allowing customization.
     *
     * @param object the updated entity
     * @return the processed entity
     * @throws IOException if an I/O error occurs
     */
    protected T afterUpdate(T object) throws IOException {
        log.debug("Executing afterUpdate for entity with ID: {}", object.getId());
        return object;
    }
}