package eu.isygoit.com.rest.controller.impl;

import eu.isygoit.com.rest.api.IMappedMultiFileApi;
import eu.isygoit.com.rest.controller.ResponseFactory;
import eu.isygoit.com.rest.service.ICrudServiceMethods;
import eu.isygoit.com.rest.service.ICrudServiceUtils;
import eu.isygoit.com.rest.service.IMultiFileServiceMethods;
import eu.isygoit.dto.IIdAssignableDto;
import eu.isygoit.dto.common.LinkedFileMinDto;
import eu.isygoit.dto.common.RequestContextDto;
import eu.isygoit.mapper.EntityMapper;
import eu.isygoit.model.IIdAssignable;
import eu.isygoit.model.IMultiFileEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

/**
 * Abstract controller class for handling multiple file operations via REST API.
 * Provides endpoints for uploading, downloading, and deleting additional files associated with entities
 * implementing {@link IMultiFileEntity} and {@link IIdAssignable}.
 *
 * @param <I> the type of the identifier, extending {@link Serializable}
 * @param <T> the entity type, extending {@link IIdAssignable} and {@link IMultiFileEntity}
 * @param <L> the linked file DTO type, extending {@link LinkedFileMinDto}
 * @param <M> the main DTO type, extending {@link IIdAssignableDto}
 * @param <F> the full DTO type, extending {@link M}
 * @param <S> the service type, implementing {@link IMultiFileServiceMethods}, {@link ICrudServiceMethods}, and {@link ICrudServiceUtils}
 */
@Slf4j
public abstract class MappedMultiFileController<
        I extends Serializable,
        T extends IIdAssignable<I> & IMultiFileEntity,
        L extends LinkedFileMinDto,
        M extends IIdAssignableDto<I>,
        F extends M,
        S extends IMultiFileServiceMethods<I, T> & ICrudServiceMethods<I, T> & ICrudServiceUtils<I, T>
        > extends CrudControllerUtils<I, T, M, F, S> implements IMappedMultiFileApi<L, I> {

    /**
     * Provides the entity mapper for linked file DTOs.
     *
     * @return the entity mapper for converting between linked file entities and DTOs
     */
    public abstract EntityMapper linkedFileMapper();

    /**
     * Uploads multiple additional files for the specified parent entity.
     *
     * @param requestContext the request context containing metadata
     * @param parentId       the ID of the parent entity
     * @param files          the array of multipart files to upload
     * @return a response entity containing the list of uploaded file DTOs
     * @throws IOException if an I/O error occurs during file upload
     */
    @Override
    public ResponseEntity<List<L>> uploadAdditionalFiles(RequestContextDto requestContext, I parentId, MultipartFile[] files) {
        log.debug("Uploading {} files for parentId: {}", files.length, parentId);
        try {
            var uploadedFiles = crudService().uploadAdditionalFiles(parentId, files);
            var fileDtos = linkedFileMapper().listEntityToDto(uploadedFiles);
            log.info("Successfully uploaded {} files for parentId: {}", files.length, parentId);
            return ResponseFactory.responseOk(fileDtos);
        } catch (IOException e) {
            log.error("Failed to upload files for parentId: {}", parentId, e);
            return getBackExceptionResponse(e);
        }
    }

    /**
     * Uploads a single additional file for the specified parent entity.
     *
     * @param requestContext the request context containing metadata
     * @param parentId       the ID of the parent entity
     * @param file           the multipart file to upload
     * @return a response entity containing the list of uploaded file DTOs
     * @throws IOException if an I/O error occurs during file upload
     */
    @Override
    public ResponseEntity<List<L>> uploadAdditionalFile(RequestContextDto requestContext, I parentId, MultipartFile file) {
        log.debug("Uploading file for parentId: {}", parentId);
        try {
            var uploadedFiles = crudService().uploadAdditionalFile(parentId, file);
            var fileDtos = linkedFileMapper().listEntityToDto(uploadedFiles);
            log.info("Successfully uploaded file for parentId: {}", parentId);
            return ResponseFactory.responseOk(fileDtos);
        } catch (IOException e) {
            log.error("Failed to upload file for parentId: {}", parentId, e);
            return getBackExceptionResponse(e);
        }
    }

    /**
     * Deletes an additional file associated with the specified parent and file IDs.
     *
     * @param requestContext the request context containing metadata
     * @param parentId       the ID of the parent entity
     * @param fileId         the ID of the file to delete
     * @return a response entity indicating whether the deletion was successful
     * @throws IOException if an I/O error occurs during file deletion
     */
    @Override
    public ResponseEntity<Boolean> deleteAdditionalFile(RequestContextDto requestContext, I parentId, I fileId) {
        log.debug("Deleting file with fileId: {} for parentId: {}", fileId, parentId);
        try {
            boolean deleted = crudService().deleteAdditionalFile(parentId, fileId);
            log.info("Successfully deleted file with fileId: {} for parentId: {}", fileId, parentId);
            return ResponseFactory.responseOk(deleted);
        } catch (IOException e) {
            log.error("Failed to delete file with fileId: {} for parentId: {}", fileId, parentId, e);
            return getBackExceptionResponse(e);
        }
    }

    /**
     * Downloads a file associated with the specified parent and file IDs.
     *
     * @param requestContext the request context containing metadata
     * @param parentId       the ID of the parent entity
     * @param fileId         the ID of the file to download
     * @param version        the version of the file to download
     * @return a response entity containing the file resource
     * @throws IOException if an I/O error occurs during file download
     */
    @Override
    public ResponseEntity<Resource> download(RequestContextDto requestContext, I parentId, I fileId, Long version) {
        log.debug("Downloading file with fileId: {}, parentId: {}, version: {}", fileId, parentId, version);
        try {
            var resource = crudService().downloadFile(parentId, fileId, version);
            if (resource != null && resource.getResource() != null) {
                var file = resource.getResource().getFile();
                var contentType = Files.probeContentType(file.toPath());
                log.info("Successfully downloaded file: {} for parentId: {}, fileId: {}, version: {}",
                        resource.getOriginalFileName(), parentId, fileId, version);
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, contentType != null ? contentType : "application/octet-stream")
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getOriginalFileName() + "\"")
                        .body(resource.getResource());
            }
            log.warn("File not found for fileId: {}, parentId: {}, version: {}", fileId, parentId, version);
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            log.error("Failed to download file with fileId: {}, parentId: {}, version: {}", fileId, parentId, version, e);
            return getBackExceptionResponse(e);
        }
    }
}