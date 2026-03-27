package eu.isygoit.com.rest.controller.impl.media;

import eu.isygoit.com.rest.api.IMappedImageApi;
import eu.isygoit.com.rest.controller.ResponseFactory;
import eu.isygoit.com.rest.controller.impl.CrudControllerOperations;
import eu.isygoit.com.rest.service.ICrudServiceEvents;
import eu.isygoit.com.rest.service.ICrudServiceOperations;
import eu.isygoit.com.rest.service.ICrudServiceUtils;
import eu.isygoit.com.rest.service.media.IImageServiceOperations;
import eu.isygoit.dto.IDto;
import eu.isygoit.dto.IIdAssignableDto;
import eu.isygoit.dto.IImageUploadDto;
import eu.isygoit.dto.ITenantAssignableDto;
import eu.isygoit.dto.common.ContextRequestDto;
import eu.isygoit.model.IIdAssignable;
import eu.isygoit.model.IImageEntity;
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
 * Concrete controller for entities that have a single associated image.
 * Extends the common operations base → reuses all lifecycle hooks and utilities.
 */
@Slf4j
public abstract class MappedImageController<
        I extends Serializable,
        T extends IIdAssignable<I> & IImageEntity,
        M extends IIdAssignableDto<I> & IDto & IImageUploadDto,
        F extends M,
        S extends IImageServiceOperations<I, T>
                & ICrudServiceOperations<I, T>
                & ICrudServiceEvents<I, T>
                & ICrudServiceUtils<I, T>>
        extends CrudControllerOperations<I, T, M, F, S>
        implements IMappedImageApi<I, F> {

    @Override
    public ResponseEntity<F> uploadImage(ContextRequestDto requestContext, I id, MultipartFile file) {
        log.debug("Uploading image for entityId: {}", id);
        try {
            T entity = crudService().uploadImage(id, file);
            F dto = mapper().entityToDto(entity);
            log.info("Successfully uploaded image for entityId: {}", id);
            return ResponseFactory.responseOk(dto);
        } catch (IOException e) {
            log.error("Failed to upload image for entityId: {}", id, e);
            return getBackExceptionResponse(e);
        }
    }

    @Override
    public ResponseEntity<Resource> downloadImage(ContextRequestDto requestContext, I id) {
        log.debug("Downloading image for entityId: {}", id);
        try {
            var resource = crudService().downloadImage(id);
            if (resource != null && resource.getResource() != null) {
                var file = resource.getResource().getFile();
                var contentType = Files.probeContentType(file.toPath());
                log.info("Successfully downloaded image: {} for entityId: {}", resource.getOriginalFileName(), id);
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, contentType != null ? contentType : "application/octet-stream")
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getOriginalFileName() + "\"")
                        .body(resource.getResource());
            }
            log.warn("Image not found for entityId: {}", id);
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            log.error("Failed to download image for entityId: {}", id, e);
            return getBackExceptionResponse(e);
        }
    }

    @Override
    public ResponseEntity<F> createWithImage(ContextRequestDto requestContext, MultipartFile file, F dto) {
        log.debug("Creating entity with image for tenant: {}", requestContext.getSenderTenant());
        try {
            if (dto instanceof ITenantAssignableDto tenantAssignableDto && StringUtils.isEmpty(tenantAssignableDto.getTenant())) {
                tenantAssignableDto.setTenant(requestContext.getSenderTenant());
                log.debug("Assigned tenant {} to DTO", requestContext.getSenderTenant());
            }

            F processed = beforeCreate(dto);                    // ← reused from base
            T entity = crudService().createWithImage(mapper().dtoToEntity(processed), file);
            T result = afterCreate(entity);                     // ← reused from base

            log.info("Successfully created entity with image for tenant: {}", requestContext.getSenderTenant());
            return ResponseFactory.responseCreated(mapper().entityToDto(result));
        } catch (IOException e) {
            log.error("Failed to create entity with image for tenant: {}", requestContext.getSenderTenant(), e);
            return getBackExceptionResponse(e);
        }
    }

    @Override
    public ResponseEntity<F> updateWithImage(ContextRequestDto requestContext, I id, MultipartFile file, F dto) {
        log.debug("Updating entity with image for entityId: {}", id);
        try {
            F processed = beforeUpdate(id, dto);               // ← now uses id (better)
            T entity = crudService().updateWithImage(mapper().dtoToEntity(processed), file);
            T result = afterUpdate(entity);                    // ← reused from base

            log.info("Successfully updated entity with image for entityId: {}", id);
            return ResponseFactory.responseOk(mapper().entityToDto(result));
        } catch (IOException e) {
            log.error("Failed to update entity with image for entityId: {}", id, e);
            return getBackExceptionResponse(e);
        }
    }
}