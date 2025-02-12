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
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.List;


/**
 * The type Mapped multi file controller.
 *
 * @param <I> the type parameter
 * @param <E> the type parameter
 * @param <L> the type parameter
 * @param <M> the type parameter
 * @param <F> the type parameter
 * @param <S> the type parameter
 */
@Slf4j
public abstract class MappedMultiFileController<I extends Serializable, E extends IIdEntity & IMultiFileEntity,
        L extends LinkedFileMinDto,
        M extends IIdentifiableDto,
        F extends M,
        S extends IMultiFileServiceMethods<I, E> & ICrudServiceMethod<I, E>>
        extends CrudControllerUtils<I, E, M, F, S>
        implements IMappedMultiFileApi<L, I> {

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
