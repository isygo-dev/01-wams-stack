package eu.isygoit.com.rest.controller.impl;

import eu.isygoit.com.rest.api.IMappedFileApi;
import eu.isygoit.com.rest.controller.ResponseFactory;
import eu.isygoit.com.rest.controller.constants.CtrlConstants;
import eu.isygoit.com.rest.service.ICrudServiceMethod;
import eu.isygoit.com.rest.service.IFileServiceMethods;
import eu.isygoit.dto.DomainAssignableDto;
import eu.isygoit.dto.IFileUploadDto;
import eu.isygoit.dto.IIdentifiableDto;
import eu.isygoit.dto.common.RequestContextDto;
import eu.isygoit.model.AssignableFile;
import eu.isygoit.model.AssignableId;
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
 * @param <I> the type parameter
 * @param <E> the type parameter
 * @param <M> the type parameter
 * @param <F> the type parameter
 * @param <S> the type parameter
 */
@Slf4j
public abstract class MappedFileController<I extends Serializable, E extends AssignableId & AssignableFile,
        M extends IIdentifiableDto & IFileUploadDto,
        F extends M,
        S extends IFileServiceMethods<I, E> & ICrudServiceMethod<I, E>>
        extends CrudControllerUtils<I, E, M, F, S>
        implements IMappedFileApi<I, F> {

    @Override
    public ResponseEntity<F> uploadFile(RequestContextDto requestContext,
                                        I id, MultipartFile file) {
        log.info("Upload file request received");
        try {
            return ResponseFactory.ResponseOk(getMapper().entityToDto(getCrudService().uploadFile(requestContext.getSenderDomain(), id, file)));
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
            Resource resource = getCrudService().downloadFile(id, version);
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
    public ResponseEntity<F> createWithFile(RequestContextDto requestContext,
                                            F dto) {
        log.info("Create with file request received");
        try {
            if (dto instanceof DomainAssignableDto DomainAssignableDto && StringUtils.isEmpty(DomainAssignableDto.getDomain())) {
                DomainAssignableDto.setDomain(requestContext.getSenderDomain());
            }
            dto = this.beforeCreate(dto);
            F savedResume = getMapper().entityToDto(this.afterCreate(
                    getCrudService().createWithFile(requestContext.getSenderDomain(), getMapper().dtoToEntity(dto), dto.getFile())));
            return ResponseFactory.ResponseOk(savedResume);
        } catch (Exception ex) {
            return getBackExceptionResponse(ex);
        }
    }

    @Override
    public ResponseEntity<F> updateWithFile(RequestContextDto requestContext,
                                            I id,
                                            F dto) {
        log.info("Update with file request received");
        try {
            dto = this.beforeUpdate(dto);
            F savedResume = getMapper().entityToDto(
                    this.afterUpdate(getCrudService().updateWithFile(requestContext.getSenderDomain(), id, getMapper().dtoToEntity(dto), dto.getFile())));
            return ResponseFactory.ResponseOk(savedResume);
        } catch (Exception ex) {
            return getBackExceptionResponse(ex);
        }
    }

    /**
     * Before create f.
     *
     * @param object the object
     * @return the f
     * @throws Exception the exception
     */
    public F beforeCreate(F object) throws Exception {
        return object;
    }

    /**
     * After create e.
     *
     * @param object the object
     * @return the e
     * @throws Exception the exception
     */
    public E afterCreate(E object) throws Exception {
        return object;
    }

    /**
     * Before update f.
     *
     * @param object the object
     * @return the f
     * @throws Exception the exception
     */
    public F beforeUpdate(F object) throws Exception {
        return object;
    }

    /**
     * After update e.
     *
     * @param object the object
     * @return the e
     * @throws Exception the exception
     */
    public E afterUpdate(E object) throws Exception {
        return object;
    }
}
