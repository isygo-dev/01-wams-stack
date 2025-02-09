package eu.isygoit.com.rest.controller.impl;

import eu.isygoit.com.rest.api.IMappedMultiFileApi;
import eu.isygoit.com.rest.controller.ResponseFactory;
import eu.isygoit.com.rest.controller.constants.CtrlConstants;
import eu.isygoit.com.rest.service.ICrudServiceMethod;
import eu.isygoit.com.rest.service.IMultiFileServiceMethods;
import eu.isygoit.dto.IIdentifiableDto;
import eu.isygoit.dto.common.LinkedFileMinDto;
import eu.isygoit.dto.common.RequestContextDto;
import eu.isygoit.mapper.EntityMapper;
import eu.isygoit.model.IIdEntity;
import eu.isygoit.model.IMultiFileEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * MappedMultiFileController handles multi-file upload, download, and deletion for entities.
 *
 * @param <I>     the identifier type
 * @param <T>     the entity type that supports multi-file entities
 * @param <L>     the DTO type for linked files
 * @param <MIND>  the DTO type for minimal details
 * @param <FULLD> the DTO type for full details
 * @param <S>     the service type for multi-file operations
 */
@Slf4j
public abstract class MappedMultiFileController<I extends Serializable, T extends IIdEntity & IMultiFileEntity,
        L extends LinkedFileMinDto,
        MIND extends IIdentifiableDto,
        FULLD extends MIND,
        S extends IMultiFileServiceMethods<I, T> & ICrudServiceMethod<I, T>>
        extends CrudControllerUtils<T, MIND, FULLD, S>
        implements IMappedMultiFileApi<L, I> {

    /**
     * Linked file mapper to convert entity objects to DTOs.
     *
     * @return the entity mapper instance
     */
    public abstract EntityMapper linkedFileMapper();

    /**
     * Handles the upload of multiple files for a specific parent entity.
     *
     * @param requestContext the request context
     * @param parentId the ID of the parent entity
     * @param files the files to be uploaded
     * @return the ResponseEntity containing the list of linked file DTOs
     */
    @Override
    public ResponseEntity<List<L>> uploadMultipleFiles(RequestContextDto requestContext, I parentId, MultipartFile[] files) {
        log.info("Starting the upload of {} files for parentId: {}", files.length, parentId);
        try {
            // Upload the files using the service, then map the entities to DTOs
            var uploadedFiles = crudService().upload(parentId, files);
            var response = linkedFileMapper().listEntityToDto(uploadedFiles);
            log.info("Successfully uploaded {} files for parentId: {}", uploadedFiles.size(), parentId);
            return ResponseFactory.ResponseOk(response); // Return a 200 OK with the mapped DTOs
        } catch (Exception e) {
            log.error("Error during file upload for parentId: {}. Details: {}", parentId, e.getMessage(), e);
            return getBackExceptionResponse(e); // Return the error response in case of an exception
        }
    }

    /**
     * Handles the upload of a single file for a specific parent entity.
     *
     * @param requestContext the request context
     * @param parentId the ID of the parent entity
     * @param file the file to be uploaded
     * @return the ResponseEntity containing the list of linked file DTOs
     */
    @Override
    public ResponseEntity<List<L>> uploadSingleFile(RequestContextDto requestContext, I parentId, MultipartFile file) {
        log.info("Starting the upload of a single file for parentId: {}", parentId);
        try {
            // Upload the single file using the service, then map the entity to DTO
            var uploadedFile = crudService().upload(parentId, file);
            var response = linkedFileMapper().listEntityToDto(uploadedFile);
            log.info("Successfully uploaded the file for parentId: {}", parentId);
            return ResponseFactory.ResponseOk(response); // Return a 200 OK with the mapped DTOs
        } catch (Exception e) {
            log.error("Error during file upload for parentId: {}. Details: {}", parentId, e.getMessage(), e);
            return getBackExceptionResponse(e); // Return the error response in case of an exception
        }
    }

    /**
     * Handles the deletion of a file associated with a parent entity.
     *
     * @param requestContext the request context
     * @param parentId the ID of the parent entity
     * @param fileId the ID of the file to be deleted
     * @return the ResponseEntity indicating success or failure of the deletion
     */
    @Override
    public ResponseEntity<Boolean> delete(RequestContextDto requestContext, I parentId, I fileId) {
        log.info("Attempting to delete file with ID: {} for parentId: {}", fileId, parentId);
        try {
            // Attempt to delete the file and return the result
            boolean result = crudService().delete(parentId, fileId);
            if (result) {
                log.info("Successfully deleted file with ID: {} for parentId: {}", fileId, parentId);
            } else {
                log.warn("Failed to delete file with ID: {} for parentId: {}", fileId, parentId);
            }
            return ResponseFactory.ResponseOk(result); // Return a 200 OK with the deletion result (true/false)
        } catch (Exception e) {
            log.error("Error during file deletion for parentId: {} and fileId: {}. Details: {}", parentId, fileId, e.getMessage(), e);
            return getBackExceptionResponse(e); // Return the error response in case of an exception
        }
    }

    /**
     * Handles the download of a specific file for a parent entity.
     *
     * @param requestContext the request context
     * @param parentId the ID of the parent entity
     * @param fileId the ID of the file to be downloaded
     * @param version the version of the file (if applicable)
     * @return the ResponseEntity containing the file or an error status
     */
    @Override
    public ResponseEntity<Resource> download(RequestContextDto requestContext, I parentId, I fileId, Long version) {
        log.info("Attempting to download file with ID: {} for parentId: {}", fileId, parentId);
        try {
            // Download the file using the service
            var resource = crudService().download(parentId, fileId, version);

            if (Objects.nonNull(resource)) {
                log.info("File with ID: {} for parentId: {} downloaded successfully. Filename: {}", fileId, parentId, resource.getFilename());
                // Return the file as an attachment with the proper headers
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, "multipart/form-data")
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource); // Return the file as the body of the response
            } else {
                log.warn("File with ID: {} for parentId: {} not found during download.", fileId, parentId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Return a 404 Not Found if the file does not exist
            }
        } catch (Exception e) {
            log.error("Error during file download for parentId: {} and fileId: {}. Details: {}", parentId, fileId, e.getMessage(), e);
            return getBackExceptionResponse(e); // Return the error response in case of an exception
        }
    }
}