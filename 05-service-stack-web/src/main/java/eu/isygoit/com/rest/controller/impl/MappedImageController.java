package eu.isygoit.com.rest.controller.impl;

import eu.isygoit.com.rest.api.IMappedImageApi;
import eu.isygoit.com.rest.controller.ResponseFactory;
import eu.isygoit.com.rest.controller.constants.CtrlConstants;
import eu.isygoit.com.rest.service.ICrudServiceMethod;
import eu.isygoit.com.rest.service.IImageServiceMethods;
import eu.isygoit.dto.DomainAssignableDto;
import eu.isygoit.dto.IIdentifiableDto;
import eu.isygoit.dto.IImageUploadDto;
import eu.isygoit.dto.common.RequestContextDto;
import eu.isygoit.model.AssignableId;
import eu.isygoit.model.AssignableImage;
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
 * The type Mapped image controller.
 *
 * @param <I> the type parameter
 * @param <E> the type parameter
 * @param <M> the type parameter
 * @param <F> the type parameter
 * @param <S> the type parameter
 */
@Slf4j
public abstract class MappedImageController<I extends Serializable, E extends AssignableId & AssignableImage,
        M extends IIdentifiableDto & IImageUploadDto,
        F extends M,
        S extends IImageServiceMethods<I, E> & ICrudServiceMethod<I, E>>
        extends CrudControllerUtils<I, E, M, F, S>
        implements IMappedImageApi<I, F> {

    @Override
    public ResponseEntity<F> uploadImage(RequestContextDto requestContext,
                                         I id,
                                         MultipartFile file) {
        log.info("Upload image request received");
        try {
            return ResponseFactory.ResponseOk(getMapper().entityToDto(getCrudService().uploadImage(requestContext.getSenderDomain(), id, file)));
        } catch (Throwable e) {
            log.error(CtrlConstants.ERROR_API_EXCEPTION, e);
            return getBackExceptionResponse(e);
        }
    }

    @Override
    public ResponseEntity<Resource> downloadImage(RequestContextDto requestContext,
                                                  I id) throws IOException {
        log.info("Download image request received");
        try {
            Resource imageResource = getCrudService().downloadImage(id);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, Files.probeContentType(imageResource.getFile().toPath()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + imageResource.getFilename() + "\"")
                    .body(imageResource);
        } catch (Throwable e) {
            log.error(CtrlConstants.ERROR_API_EXCEPTION, e);
            return getBackExceptionResponse(e);
        }
    }

    @Override
    public ResponseEntity<F> createWithImage(RequestContextDto requestContext,
                                             MultipartFile file,
                                             F dto) {
        log.info("Create with image request received");
        try {
            if (dto instanceof DomainAssignableDto domainAssignableDto && StringUtils.isEmpty(domainAssignableDto.getDomain())) {
                domainAssignableDto.setDomain(requestContext.getSenderDomain());
            }
            dto = this.beforeCreate(dto);
            return ResponseFactory.ResponseOk(getMapper().entityToDto(
                    this.afterCreate(getCrudService().createWithImage(requestContext.getSenderDomain(), getMapper().dtoToEntity(dto), file))));
        } catch (Throwable e) {
            log.error("<Error>: create with image : {} ", e);
            return getBackExceptionResponse(e);
        }
    }


    @Override
    public ResponseEntity<F> updateWithImage(RequestContextDto requestContext,
                                             MultipartFile file,
                                             F dto) {
        log.info("Update with image request received");
        try {
            dto = this.beforeUpdate(dto);
            return ResponseFactory.ResponseOk(getMapper().entityToDto(
                    this.afterUpdate(getCrudService().updateWithImage(requestContext.getSenderDomain(), getMapper().dtoToEntity(dto), file))));
        } catch (Throwable e) {
            log.error("<Error>: update wth image : {} ", e);
            return getBackExceptionResponse(e);
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
