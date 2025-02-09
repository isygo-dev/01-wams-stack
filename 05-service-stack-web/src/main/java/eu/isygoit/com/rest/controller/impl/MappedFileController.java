package eu.isygoit.com.rest.controller.impl;

import eu.isygoit.com.rest.api.IMappedFileApi;
import eu.isygoit.com.rest.controller.ResponseFactory;
import eu.isygoit.com.rest.controller.constants.CtrlConstants;
import eu.isygoit.com.rest.service.ICrudServiceMethod;
import eu.isygoit.com.rest.service.IFileServiceMethods;
import eu.isygoit.dto.IFileUploadDto;
import eu.isygoit.dto.IIdentifiableDto;
import eu.isygoit.dto.ISAASDto;
import eu.isygoit.dto.common.RequestContextDto;
import eu.isygoit.model.IFileEntity;
import eu.isygoit.model.IIdEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

/**
 * The type Mapped file controller.
 *
 * @param <I>     the type parameter (ID)
 * @param <T>     the type parameter (Entity)
 * @param <MIND>  the type parameter (Minimal DTO)
 * @param <FULLD> the type parameter (Full DTO)
 * @param <S>     the type parameter (Service)
 */
@Slf4j
public abstract class MappedFileController<I extends Serializable, T extends IIdEntity & IFileEntity,
        MIND extends IIdentifiableDto & IFileUploadDto,
        FULLD extends MIND,
        S extends IFileServiceMethods<I, T> & ICrudServiceMethod<I, T>>
        extends CrudControllerUtils<T, MIND, FULLD, S>
        implements IMappedFileApi<I, FULLD> {

    /**
     * Uploads a file and returns a response containing the updated DTO.
     *
     * @param requestContext the request context
     * @param id             the entity ID
     * @param file           the file to be uploaded
     * @return ResponseEntity containing the updated DTO
     */
    @Override
    public ResponseEntity<FULLD> uploadFile(RequestContextDto requestContext, I id, MultipartFile file) {
        log.info("Upload file request received");
        try {
            // Upload the file and map the entity to a DTO
            var uploadedFile = crudService().uploadFile(requestContext.getSenderDomain(), id, file);
            return ResponseFactory.ResponseOk(mapper().entityToDto(uploadedFile));
        } catch (Throwable e) {
            log.error(CtrlConstants.ERROR_API_EXCEPTION, e);
            return getBackExceptionResponse(e);
        }
    }

    /**
     * Downloads a file, returning the file as a Resource.
     *
     * @param requestContext the request context
     * @param id             the entity ID
     * @param version        the version of the file to be downloaded
     * @return ResponseEntity containing the file as a resource
     */
    @Override
    public ResponseEntity<Resource> download(RequestContextDto requestContext, I id, Long version) {
        log.info("Download file request received");
        try {
            // Fetch the file resource
            var resource = crudService().download(id, version);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "multipart/form-data")
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Throwable e) {
            log.error(CtrlConstants.ERROR_API_EXCEPTION, e);
            return getBackExceptionResponse(e);
        }
    }

    /**
     * Creates a new entity with an associated file.
     *
     * @param requestContext the request context
     * @param dto            the DTO containing the entity data and file
     * @return ResponseEntity containing the created entity as a DTO
     */
    @Override
    public ResponseEntity<FULLD> createWithFile(RequestContextDto requestContext, FULLD dto) {
        log.info("Create with file request received");
        try {
            // Set the domain if not provided (using pattern matching)
            if (dto instanceof ISAASDto isaasDto && StringUtils.isEmpty(isaasDto.getDomain())) {
                isaasDto.setDomain(requestContext.getSenderDomain());
            }
            dto = beforeCreate(dto);
            var createdEntity = crudService().createWithFile(requestContext.getSenderDomain(), mapper().dtoToEntity(dto), dto.getFile());
            FULLD savedResume = mapper().entityToDto(afterCreate(createdEntity));
            return ResponseFactory.ResponseOk(savedResume);
        } catch (Exception ex) {
            return getBackExceptionResponse(ex);
        }
    }

    /**
     * Updates an existing entity with an associated file.
     *
     * @param requestContext the request context
     * @param id             the entity ID
     * @param dto            the DTO containing the updated data and file
     * @return ResponseEntity containing the updated entity as a DTO
     */
    @Override
    public ResponseEntity<FULLD> updateWithFile(RequestContextDto requestContext, I id, FULLD dto) {
        log.info("Update with file request received");
        try {
            dto = beforeUpdate(dto);
            var updatedEntity = crudService().updateWithFile(requestContext.getSenderDomain(), id, mapper().dtoToEntity(dto), dto.getFile());
            FULLD savedResume = mapper().entityToDto(afterUpdate(updatedEntity));
            return ResponseFactory.ResponseOk(savedResume);
        } catch (Exception ex) {
            return getBackExceptionResponse(ex);
        }
    }

    /**
     * Method called before creating an entity, can be overridden for custom logic.
     *
     * @param object the object to be created
     * @return the updated object
     * @throws Exception if an error occurs
     */
    public FULLD beforeCreate(FULLD object) throws Exception {
        return object;  // Default implementation does nothing
    }

    /**
     * Method called after creating an entity, can be overridden for custom logic.
     *
     * @param object the object to be created
     * @return the created object
     * @throws Exception if an error occurs
     */
    public T afterCreate(T object) throws Exception {
        return object;  // Default implementation does nothing
    }

    /**
     * Method called before updating an entity, can be overridden for custom logic.
     *
     * @param object the object to be updated
     * @return the updated object
     * @throws Exception if an error occurs
     */
    public FULLD beforeUpdate(FULLD object) throws Exception {
        return object;  // Default implementation does nothing
    }

    /**
     * Method called after updating an entity, can be overridden for custom logic.
     *
     * @param object the object to be updated
     * @return the updated object
     * @throws Exception if an error occurs
     */
    public T afterUpdate(T object) throws Exception {
        return object;  // Default implementation does nothing
    }
}