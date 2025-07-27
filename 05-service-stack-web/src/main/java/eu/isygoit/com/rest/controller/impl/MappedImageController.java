package eu.isygoit.com.rest.controller.impl;

import eu.isygoit.com.rest.api.IMappedImageApi;
import eu.isygoit.com.rest.controller.ResponseFactory;
import eu.isygoit.com.rest.service.ICrudServiceMethods;
import eu.isygoit.com.rest.service.ICrudServiceUtils;
import eu.isygoit.com.rest.service.IImageServiceMethods;
import eu.isygoit.dto.IIdAssignableDto;
import eu.isygoit.dto.IImageUploadDto;
import eu.isygoit.dto.ITenantAssignableDto;
import eu.isygoit.dto.common.RequestContextDto;
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
 * Abstract controller class for handling image-related operations via REST API.
 * Provides endpoints for uploading, downloading, creating, and updating entities with associated images
 * for entities implementing {@link IImageEntity} and {@link IIdAssignable}.
 *
 * @param <I> the type of the identifier, extending {@link Serializable}
 * @param <T> the entity type, extending {@link IIdAssignable} and {@link IImageEntity}
 * @param <M> the main DTO type, extending {@link IIdAssignableDto} and {@link IImageUploadDto}
 * @param <F> the full DTO type, extending {@link M}
 * @param <S> the service type, implementing {@link IImageServiceMethods}, {@link ICrudServiceMethods}, and {@link ICrudServiceUtils}
 */
@Slf4j
public abstract class MappedImageController<
        I extends Serializable,
        T extends IIdAssignable<I> & IImageEntity,
        M extends IIdAssignableDto<I> & IImageUploadDto,
        F extends M,
        S extends IImageServiceMethods<I, T> & ICrudServiceMethods<I, T> & ICrudServiceUtils<I, T>
        > extends CrudControllerUtils<I, T, M, F, S> implements IMappedImageApi<I, F> {

    /**
     * Uploads an image for the specified entity.
     *
     * @param requestContext the request context containing metadata
     * @param id             the ID of the entity
     * @param file           the multipart file containing the image
     * @return a response entity containing the updated entity DTO
     * @throws IOException if an I/O error occurs during image upload
     */
    @Override
    public ResponseEntity<F> uploadImage(RequestContextDto requestContext, I id, MultipartFile file) {
        log.debug("Uploading image for entityId: {}", id);
        try {
            var entity = crudService().uploadImage(id, file);
            var dto = mapper().entityToDto(entity);
            log.info("Successfully uploaded image for entityId: {}", id);
            return ResponseFactory.responseOk(dto);
        } catch (IOException e) {
            log.error("Failed to upload image for entityId: {}", id, e);
            return getBackExceptionResponse(e);
        }
    }

    /**
     * Downloads the image associated with the specified entity.
     *
     * @param requestContext the request context containing metadata
     * @param id             the ID of the entity
     * @return a response entity containing the image resource
     * @throws IOException if an I/O error occurs during image download
     */
    @Override
    public ResponseEntity<Resource> downloadImage(RequestContextDto requestContext, I id) {
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

    /**
     * Creates an entity with an associated image.
     *
     * @param requestContext the request context containing metadata
     * @param file           the multipart file containing the image
     * @param dto            the DTO containing entity data
     * @return a response entity containing the created entity DTO
     * @throws IOException if an I/O error occurs during creation or image upload
     */
    @Override
    public ResponseEntity<F> createWithImage(RequestContextDto requestContext, MultipartFile file, F dto) {
        log.debug("Creating entity with image for tenant: {}", requestContext.getSenderTenant());
        try {
            if (dto instanceof ITenantAssignableDto tenantAssignableDto && StringUtils.isEmpty(tenantAssignableDto.getTenant())) {
                tenantAssignableDto.setTenant(requestContext.getSenderTenant());
                log.debug("Assigned tenant {} to DTO", requestContext.getSenderTenant());
            }
            var processedDto = beforeCreate(dto);
            var entity = crudService().createWithImage(mapper().dtoToEntity(processedDto), file);
            var result = afterCreate(entity);
            log.info("Successfully created entity with image for tenant: {}", requestContext.getSenderTenant());
            return ResponseFactory.responseCreated(mapper().entityToDto(result));
        } catch (IOException e) {
            log.error("Failed to create entity with image for tenant: {}", requestContext.getSenderTenant(), e);
            return getBackExceptionResponse(e);
        }
    }

    /**
     * Updates an entity with an associated image.
     *
     * @param requestContext the request context containing metadata
     * @param id             the ID of the entity to update
     * @param file           the multipart file containing the image
     * @param dto            the DTO containing updated entity data
     * @return a response entity containing the updated entity DTO
     * @throws IOException if an I/O error occurs during update or image upload
     */
    @Override
    public ResponseEntity<F> updateWithImage(RequestContextDto requestContext, I id, MultipartFile file, F dto) {
        log.debug("Updating entity with image for entityId: {}", id);
        try {
            var processedDto = beforeUpdate(dto);
            var entity = crudService().updateWithImage(mapper().dtoToEntity(processedDto), file);
            var result = afterUpdate(entity);
            log.info("Successfully updated entity with image for entityId: {}", id);
            return ResponseFactory.responseOk(mapper().entityToDto(result));
        } catch (IOException e) {
            log.error("Failed to update entity with image for entityId: {}", id, e);
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