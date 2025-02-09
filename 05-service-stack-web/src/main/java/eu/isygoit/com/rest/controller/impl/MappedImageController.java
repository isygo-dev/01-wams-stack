package eu.isygoit.com.rest.controller.impl;

import eu.isygoit.com.rest.api.IMappedImageApi;
import eu.isygoit.com.rest.controller.ResponseFactory;
import eu.isygoit.com.rest.service.ICrudServiceMethod;
import eu.isygoit.com.rest.service.IImageServiceMethods;
import eu.isygoit.dto.IIdentifiableDto;
import eu.isygoit.dto.IImageUploadDto;
import eu.isygoit.dto.ISAASDto;
import eu.isygoit.dto.common.RequestContextDto;
import eu.isygoit.model.IIdEntity;
import eu.isygoit.model.IImageEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.nio.file.Files;

/**
 * The MappedImageController class handles the uploading, downloading, and creation of objects with images.
 *
 * @param <I>     the identifier type
 * @param <T>     the entity type that supports images
 * @param <MIND>  the DTO type for minimal details
 * @param <FULLD> the DTO type for full details
 * @param <S>     the service type for image operations
 */
@Slf4j
public abstract class MappedImageController<I extends Serializable, T extends IIdEntity & IImageEntity,
        MIND extends IIdentifiableDto & IImageUploadDto,
        FULLD extends MIND,
        S extends IImageServiceMethods<I, T> & ICrudServiceMethod<I, T>>
        extends CrudControllerUtils<T, MIND, FULLD, S>
        implements IMappedImageApi<I, FULLD> {

    /**
     * Handles the upload of an image and links it to an entity.
     *
     * @param requestContext the request context, containing metadata like domain
     * @param id             the ID of the entity to which the image will be linked
     * @param file           the image file to be uploaded
     * @return a ResponseEntity containing the full DTO of the updated entity
     */
    @Override
    public ResponseEntity<FULLD> uploadImageAndLinkToObject(RequestContextDto requestContext,
                                                            I id,
                                                            MultipartFile file) {
        log.info("Received request to upload image for entity with ID: {}", id);
        try {
            // Upload the image and link it to the entity
            FULLD resultDto = mapper().entityToDto(crudService().uploadImage(requestContext.getSenderDomain(), id, file));
            log.info("Successfully uploaded image and linked to entity with ID: {}", id);
            return ResponseFactory.ResponseOk(resultDto);
        } catch (Throwable e) {
            log.error("Error occurred while uploading image for entity with ID: {}. Details: {}", id, e.getMessage(), e);
            return getBackExceptionResponse(e); // Return error response
        }
    }

    /**
     * Handles the download of an image for a given entity.
     *
     * @param requestContext the request context
     * @param id             the ID of the entity whose image is being requested
     * @return a ResponseEntity containing the image file resource
     */
    @Override
    public ResponseEntity<Resource> downloadImage(RequestContextDto requestContext,
                                                  I id) {
        log.info("Received request to download image for entity with ID: {}", id);
        try {
            Resource imageResource = crudService().downloadImage(id);
            if (imageResource != null) {
                log.info("Successfully found image for entity with ID: {}", id);
                // Return the image as a downloadable resource with appropriate headers
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, Files.probeContentType(imageResource.getFile().toPath()))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + imageResource.getFilename() + "\"")
                        .body(imageResource);
            } else {
                log.warn("No image found for entity with ID: {}", id);
                return ResponseEntity.notFound().build(); // Return 404 if no image is found
            }
        } catch (Throwable e) {
            log.error("Error occurred while downloading image for entity with ID: {}. Details: {}", id, e.getMessage(), e);
            return getBackExceptionResponse(e); // Return error response
        }
    }

    /**
     * Creates an entity and associates an image with it.
     *
     * @param requestContext the request context
     * @param file           the image file to be uploaded
     * @param dto            the DTO containing the details of the entity to be created
     * @return a ResponseEntity containing the full DTO of the created entity
     */
    @Override
    public ResponseEntity<FULLD> createObjectWithImage(RequestContextDto requestContext,
                                                       MultipartFile file,
                                                       FULLD dto) {
        log.info("Received request to create entity with image.");
        try {
            // Set domain information if it's not provided
            if (dto instanceof ISAASDto isaasDto && StringUtils.isEmpty(isaasDto.getDomain())) {
                isaasDto.setDomain(requestContext.getSenderDomain());
            }
            dto = this.beforeCreate(dto); // Apply any pre-processing steps
            T createdEntity = crudService().createWithImage(requestContext.getSenderDomain(), mapper().dtoToEntity(dto), file);
            FULLD resultDto = mapper().entityToDto(this.afterCreate(createdEntity)); // Apply post-processing and map to DTO
            log.info("Successfully created entity with ID: {}", createdEntity.getId());
            return ResponseFactory.ResponseOk(resultDto);
        } catch (Throwable e) {
            log.error("Error occurred while creating entity with image. Details: {}", e.getMessage(), e);
            return getBackExceptionResponse(e); // Return error response
        }
    }

    /**
     * Updates an entity and associates an image with it.
     *
     * @param requestContext the request context
     * @param file           the image file to be uploaded
     * @param dto            the DTO containing the updated details of the entity
     * @return a ResponseEntity containing the full DTO of the updated entity
     */
    @Override
    public ResponseEntity<FULLD> updateObjectWithImage(RequestContextDto requestContext,
                                                       MultipartFile file,
                                                       FULLD dto) {
        log.info("Received request to update entity with image.");
        try {
            dto = this.beforeUpdate(dto); // Apply any pre-processing steps
            T updatedEntity = crudService().updateWithImage(requestContext.getSenderDomain(), mapper().dtoToEntity(dto), file);
            FULLD resultDto = mapper().entityToDto(this.afterUpdate(updatedEntity)); // Apply post-processing and map to DTO
            log.info("Successfully updated entity with ID: {}", updatedEntity.getId());
            return ResponseFactory.ResponseOk(resultDto);
        } catch (Throwable e) {
            log.error("Error occurred while updating entity with image. Details: {}", e.getMessage(), e);
            return getBackExceptionResponse(e); // Return error response
        }
    }

    /**
     * Before create lifecycle hook for custom processing.
     *
     * @param object the object being created
     * @return the processed object
     * @throws Exception if an error occurs during processing
     */
    public FULLD beforeCreate(FULLD object) throws Exception {
        return object;
    }

    /**
     * After create lifecycle hook for custom processing.
     *
     * @param object the object being created
     * @return the processed object
     * @throws Exception if an error occurs during processing
     */
    public T afterCreate(T object) throws Exception {
        return object;
    }

    /**
     * Before update lifecycle hook for custom processing.
     *
     * @param object the object being updated
     * @return the processed object
     * @throws Exception if an error occurs during processing
     */
    public FULLD beforeUpdate(FULLD object) throws Exception {
        return object;
    }

    /**
     * After update lifecycle hook for custom processing.
     *
     * @param object the object being updated
     * @return the processed object
     * @throws Exception if an error occurs during processing
     */
    public T afterUpdate(T object) throws Exception {
        return object;
    }
}