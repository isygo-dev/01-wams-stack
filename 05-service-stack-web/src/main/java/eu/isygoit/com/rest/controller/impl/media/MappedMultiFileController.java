package eu.isygoit.com.rest.controller.impl.media;

import eu.isygoit.com.rest.api.IMappedMultiFileApi;
import eu.isygoit.com.rest.controller.ResponseFactory;
import eu.isygoit.com.rest.controller.impl.CrudControllerOperations;
import eu.isygoit.com.rest.service.ICrudServiceEvents;
import eu.isygoit.com.rest.service.ICrudServiceOperations;
import eu.isygoit.com.rest.service.ICrudServiceUtils;
import eu.isygoit.com.rest.service.media.IMultiFileServiceOperations;
import eu.isygoit.dto.IDto;
import eu.isygoit.dto.IIdAssignableDto;
import eu.isygoit.dto.common.LinkedFileMinDto;
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

/**
 * Concrete controller for entities that support multiple additional files.
 * Extends the common operations base → reuses all lifecycle hooks, monitoring,
 * and utility methods while providing multi-file specific endpoints.
 *
 * @param <I> ID type (must be Serializable)
 * @param <T> Entity type (must implement IIdAssignable + IMultiFileEntity)
 * @param <L> Linked file DTO type (e.g. LinkedFileMinDto or a custom subclass)
 * @param <M> Minimal DTO type for the parent entity
 * @param <F> Full DTO type for the parent entity
 * @param <S> Service type (must support multi-file + CRUD + events + utils)
 */
@Slf4j
public abstract class MappedMultiFileController<
        I extends Serializable,
        T extends IIdAssignable<I> & IMultiFileEntity,
        L extends LinkedFileMinDto,
        M extends IIdAssignableDto<I> & IDto,
        F extends M,
        S extends IMultiFileServiceOperations<I, T>
                & ICrudServiceOperations<I, T>
                & ICrudServiceEvents<I, T>
                & ICrudServiceUtils<I, T>>
        extends CrudControllerOperations<I, T, M, F, S>
        implements IMappedMultiFileApi<L, I> {

    /**
     * Provides the mapper used to convert linked file entities to DTOs.
     * Must be implemented by concrete controllers.
     */
    public abstract EntityMapper linkedFileMapper();

    @Override
    public ResponseEntity<List<L>> uploadAdditionalFiles(I parentId,
                                                         MultipartFile[] files) {
        log.debug("Uploading {} additional files for parentId: {}", files.length, parentId);
        try {
            var uploadedFiles = crudService().uploadAdditionalFiles(parentId, files);
            var fileDtos = linkedFileMapper().listEntityToDto(uploadedFiles);

            log.info("Successfully uploaded {} additional files for parentId: {}", files.length, parentId);
            return ResponseFactory.responseOk(fileDtos);
        } catch (IOException e) {
            log.error("Failed to upload additional files for parentId: {}", parentId, e);
            return getBackExceptionResponse(e);
        }
    }

    @Override
    public ResponseEntity<List<L>> uploadAdditionalFile(I parentId,
                                                        MultipartFile file) {
        log.debug("Uploading one additional file for parentId: {}", parentId);
        try {
            var uploadedFiles = crudService().uploadAdditionalFile(parentId, file);
            var fileDtos = linkedFileMapper().listEntityToDto(uploadedFiles);

            log.info("Successfully uploaded one additional file for parentId: {}", parentId);
            return ResponseFactory.responseOk(fileDtos);
        } catch (IOException e) {
            log.error("Failed to upload additional file for parentId: {}", parentId, e);
            return getBackExceptionResponse(e);
        }
    }

    @Override
    public ResponseEntity<Boolean> deleteAdditionalFile(I parentId,
                                                        I fileId) {
        log.debug("Deleting additional fileId: {} for parentId: {}", fileId, parentId);
        try {
            boolean deleted = crudService().deleteAdditionalFile(parentId, fileId);
            log.info("Successfully deleted additional fileId: {} for parentId: {}", fileId, parentId);
            return ResponseFactory.responseOk(deleted);
        } catch (IOException e) {
            log.error("Failed to delete additional fileId: {} for parentId: {}", fileId, parentId, e);
            return getBackExceptionResponse(e);
        }
    }

    @Override
    public ResponseEntity<Resource> download(I parentId,
                                             I fileId,
                                             Long version) {
        log.debug("Downloading additional fileId: {}, parentId: {}, version: {}", fileId, parentId, version);
        try {
            var resource = crudService().downloadFile(parentId, fileId, version);

            if (resource != null && resource.getResource() != null) {
                var file = resource.getResource().getFile();
                var contentType = Files.probeContentType(file.toPath());

                log.info("Successfully downloaded file: {} for parentId: {}, fileId: {}, version: {}",
                        resource.getOriginalFileName(), parentId, fileId, version);

                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, contentType != null ? contentType : "application/octet-stream")
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"" + resource.getOriginalFileName() + "\"")
                        .body(resource.getResource());
            }

            log.warn("Additional file not found for fileId: {}, parentId: {}, version: {}", fileId, parentId, version);
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            log.error("Failed to download additional fileId: {}, parentId: {}, version: {}", fileId, parentId, version, e);
            return getBackExceptionResponse(e);
        }
    }
}