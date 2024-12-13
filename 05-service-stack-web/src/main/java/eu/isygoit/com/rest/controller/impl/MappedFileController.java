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


/**
 * The type Mapped file controller.
 *
 * @param <I>     the type parameter
 * @param <T>     the type parameter
 * @param <MIND>  the type parameter
 * @param <FULLD> the type parameter
 * @param <S>     the type parameter
 */
@Slf4j
public abstract class MappedFileController<I, T extends IIdEntity & IFileEntity,
        MIND extends IIdentifiableDto & IFileUploadDto,
        FULLD extends MIND,
        S extends IFileServiceMethods<I, T> & ICrudServiceMethod<I, T>>
        extends CrudControllerUtils<T, MIND, FULLD, S>
        implements IMappedFileApi<I, FULLD> {

    @Override
    public ResponseEntity<FULLD> uploadFile(RequestContextDto requestContext,
                                            I id, MultipartFile file) {
        log.info("Upload file request received");
        try {
            return ResponseFactory.ResponseOk(mapper().entityToDto(crudService().uploadFile(requestContext.getSenderDomain(), id, file)));
        } catch (Throwable e) {
            log.error(CtrlConstants.ERROR_API_EXCEPTION, e);
            return getBackExceptionResponse(e);
        }
    }

    @Override
    public ResponseEntity<Resource> downloadFile(RequestContextDto requestContext,
                                                 I id,
                                                 Long version) {
        log.info("Download file request received");
        try {
            Resource resource = crudService().downloadFile(id, version);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "multipart/form-data")
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Throwable e) {
            log.error(CtrlConstants.ERROR_API_EXCEPTION, e);
            return getBackExceptionResponse(e);
        }
    }

    @Override
    public ResponseEntity<FULLD> createWithFile(RequestContextDto requestContext,
                                                FULLD dto) {
        log.info("Create with file request received");
        try {
            if (dto instanceof ISAASDto ISAASDto && StringUtils.isEmpty(ISAASDto.getDomain())) {
                ISAASDto.setDomain(requestContext.getSenderDomain());
            }
            dto = this.beforeCreate(dto);
            FULLD savedResume = mapper().entityToDto(this.afterCreate(
                    crudService().createWithFile(requestContext.getSenderDomain(), mapper().dtoToEntity(dto), dto.getFile())));
            return ResponseFactory.ResponseOk(savedResume);
        } catch (Exception ex) {
            return getBackExceptionResponse(ex);
        }
    }

    @Override
    public ResponseEntity<FULLD> updateWithFile(RequestContextDto requestContext,
                                                I id,
                                                FULLD dto) {
        log.info("Update with file request received");
        try {
            dto = this.beforeUpdate(dto);
            FULLD savedResume = mapper().entityToDto(
                    this.afterUpdate(crudService().updateWithFile(requestContext.getSenderDomain(), id, mapper().dtoToEntity(dto), dto.getFile())));
            return ResponseFactory.ResponseOk(savedResume);
        } catch (Exception ex) {
            return getBackExceptionResponse(ex);
        }
    }

    /**
     * Before create fulld.
     *
     * @param object the object
     * @return the fulld
     * @throws Exception the exception
     */
    public FULLD beforeCreate(FULLD object) throws Exception {
        return object;
    }

    /**
     * After create t.
     *
     * @param object the object
     * @return the t
     * @throws Exception the exception
     */
    public T afterCreate(T object) throws Exception {
        return object;
    }

    /**
     * Before update fulld.
     *
     * @param object the object
     * @return the fulld
     * @throws Exception the exception
     */
    public FULLD beforeUpdate(FULLD object) throws Exception {
        return object;
    }

    /**
     * After update t.
     *
     * @param object the object
     * @return the t
     * @throws Exception the exception
     */
    public T afterUpdate(T object) throws Exception {
        return object;
    }
}
