package eu.isygoit.com.rest.controller.impl;

import eu.isygoit.com.rest.api.IUploadMultiFileApi;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


/**
 * The type Mapped multi file controller.
 *
 * @param <I>     the type parameter
 * @param <T>     the type parameter
 * @param <L>     the type parameter
 * @param <MIND>  the type parameter
 * @param <FULLD> the type parameter
 * @param <S>     the type parameter
 */
@Slf4j
public abstract class MappedMultiFileController<I, T extends IIdEntity & IMultiFileEntity,
        L extends LinkedFileMinDto,
        MIND extends IIdentifiableDto,
        FULLD extends MIND,
        S extends IMultiFileServiceMethods<I, T> & ICrudServiceMethod<I, T>>
        extends CrudControllerUtils<T, MIND, FULLD, S>
        implements IUploadMultiFileApi<L, I> {

    /**
     * Linked file mapper entity mapper.
     *
     * @return the entity mapper
     */
    public abstract EntityMapper linkedFileMapper();

    @Override
    public ResponseEntity<List<L>> uploadAdditionalFiles(RequestContextDto requestContext,
                                                         I parentId,
                                                         MultipartFile[] files) {
        log.info("update additionl file");
        try {
            return ResponseFactory.ResponseOk(linkedFileMapper().listEntityToDto(crudService().uploadAdditionalFiles(parentId, files)));
        } catch (Throwable e) {
            log.error(CtrlConstants.ERROR_API_EXCEPTION, e);
            return getBackExceptionResponse(e);
        }
    }

    @Override
    public ResponseEntity<List<L>> uploadAdditionalFile(RequestContextDto requestContext,
                                                        I parentId,
                                                        MultipartFile file) {
        log.info("update additionl file");
        try {
            return ResponseFactory.ResponseOk(linkedFileMapper().listEntityToDto(crudService().uploadAdditionalFile(parentId, file)));
        } catch (Throwable e) {
            log.error(CtrlConstants.ERROR_API_EXCEPTION, e);
            return getBackExceptionResponse(e);
        }
    }

    @Override
    public ResponseEntity<Boolean> deleteAdditionalFile(RequestContextDto requestContext,
                                                        I parentId,
                                                        I fileId) {
        log.info("delete additional file");
        try {
            return ResponseFactory.ResponseOk(crudService().deleteAdditionalFile(parentId, fileId));
        } catch (Throwable e) {
            log.error(CtrlConstants.ERROR_API_EXCEPTION, e);
            return getBackExceptionResponse(e);
        }
    }

    @Override
    public ResponseEntity<Resource> download(RequestContextDto requestContext,
                                             I parentId,
                                             I fileId,
                                             Long version
    ) {
        try {
            log.info("download file ");
            try {
                Resource resource = crudService().downloadFile(parentId, fileId, version);
                if (resource != null) {
                    log.info("File downloaded successfully {}", resource.getFilename());
                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_TYPE, "multipart/form-data")
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                            .body(resource);
                }
            } catch (Exception e) {
                log.error("Remote feign call failed : ", e);
            }
            return ResponseEntity.notFound().build();
        } catch (Throwable e) {
            log.error(CtrlConstants.ERROR_API_EXCEPTION, e);
            return getBackExceptionResponse(e);
        }
    }
}
