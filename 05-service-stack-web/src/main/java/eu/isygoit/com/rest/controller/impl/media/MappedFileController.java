package eu.isygoit.com.rest.controller.impl.media;

import eu.isygoit.com.rest.api.IMappedFileApi;
import eu.isygoit.com.rest.controller.ResponseFactory;
import eu.isygoit.com.rest.controller.impl.CrudControllerOperations;
import eu.isygoit.com.rest.service.ICrudServiceEvents;
import eu.isygoit.com.rest.service.ICrudServiceOperations;
import eu.isygoit.com.rest.service.ICrudServiceUtils;
import eu.isygoit.com.rest.service.media.IFileServiceOperations;
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
 * Concrete controller for entities that have a single associated file.
 * Extends the common operations base → reuses all lifecycle hooks and utilities.
 */
@Slf4j
public abstract class MappedFileController<
        I extends Serializable,
        T extends IIdAssignable<I> & IFileEntity,
        M extends IIdAssignableDto<I> & IDto & IFileUploadDto,
        F extends M,
        S extends IFileServiceOperations<I, T>
                & ICrudServiceOperations<I, T>
                & ICrudServiceEvents<I, T>
                & ICrudServiceUtils<I, T>>
        extends CrudControllerOperations<I, T, M, F, S>
        implements IMappedFileApi<I, F> {

    @Override
    public ResponseEntity<F> uploadFile(ContextRequestDto requestContext, I id, MultipartFile file) {
        log.debug("Uploading file for entityId: {}", id);
        try {
            T entity = crudService().uploadFile(id, file);
            F dto = mapper().entityToDto(entity);
            log.info("Successfully uploaded file for entityId: {}", id);
            return ResponseFactory.responseOk(dto);
        } catch (IOException e) {
            log.error("Failed to upload file for entityId: {}", id, e);
            return getBackExceptionResponse(e);
        }
    }

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

    @Override
    public ResponseEntity<F> createWithFile(ContextRequestDto requestContext, MultipartFile file, F dto) {
        log.debug("Creating entity with file for tenant: {}", requestContext.getSenderTenant());
        try {
            if (dto instanceof ITenantAssignableDto tenantAssignableDto && StringUtils.isEmpty(tenantAssignableDto.getTenant())) {
                tenantAssignableDto.setTenant(requestContext.getSenderTenant());
                log.debug("Assigned tenant {} to DTO", requestContext.getSenderTenant());
            }

            F processed = beforeCreate(dto);
            T entity = crudService().createWithFile(mapper().dtoToEntity(processed), file);
            T result = afterCreate(entity);

            log.info("Successfully created entity with file for tenant: {}", requestContext.getSenderTenant());
            return ResponseFactory.responseCreated(mapper().entityToDto(result));
        } catch (IOException e) {
            log.error("Failed to create entity with file for tenant: {}", requestContext.getSenderTenant(), e);
            return getBackExceptionResponse(e);
        }
    }

    @Override
    public ResponseEntity<F> updateWithFile(ContextRequestDto requestContext, I id, MultipartFile file, F dto) {
        log.debug("Updating entity with file for entityId: {}", id);
        try {
            F processed = beforeUpdate(id, dto);               // ← now uses id
            T entity = crudService().updateWithFile(id, mapper().dtoToEntity(processed), file);
            T result = afterUpdate(entity);

            log.info("Successfully updated entity with file for entityId: {}", id);
            return ResponseFactory.responseOk(mapper().entityToDto(result));
        } catch (IOException e) {
            log.error("Failed to update entity with file for entityId: {}", id, e);
            return getBackExceptionResponse(e);
        }
    }
}