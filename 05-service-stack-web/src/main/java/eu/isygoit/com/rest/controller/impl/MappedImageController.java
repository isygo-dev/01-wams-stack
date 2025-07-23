package eu.isygoit.com.rest.controller.impl;

import eu.isygoit.com.rest.api.IMappedImageApi;
import eu.isygoit.com.rest.controller.ResponseFactory;
import eu.isygoit.com.rest.controller.constants.CtrlConstants;
import eu.isygoit.com.rest.service.ICrudServiceMethods;
import eu.isygoit.com.rest.service.ICrudServiceUtils;
import eu.isygoit.com.rest.service.IImageServiceMethods;
import eu.isygoit.dto.IIdAssignableDto;
import eu.isygoit.dto.IImageUploadDto;
import eu.isygoit.dto.ITenantAssignableDto;
import eu.isygoit.dto.common.RequestContextDto;
import eu.isygoit.dto.common.ResourceDto;
import eu.isygoit.model.IIdAssignable;
import eu.isygoit.model.IImageEntity;
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
 * @param <T> the type parameter
 * @param <M> the type parameter
 * @param <F> the type parameter
 * @param <S> the type parameter
 */
@Slf4j
public abstract class MappedImageController<I extends Serializable, T extends IIdAssignable<I> & IImageEntity,
        M extends IIdAssignableDto<I> & IImageUploadDto,
        F extends M,
        S extends IImageServiceMethods<I, T> & ICrudServiceMethods<I, T> & ICrudServiceUtils<I, T>>
        extends CrudControllerUtils<I, T, M, F, S>
        implements IMappedImageApi<I, F> {

    @Override
    public ResponseEntity<F> uploadImage(RequestContextDto requestContext,
                                         I id,
                                         MultipartFile file) {
        log.info("Upload image request received");
        try {
            return ResponseFactory.responseOk(mapper().entityToDto(crudService().uploadImage(id, file)));
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
            ResourceDto resource = crudService().downloadImage(id);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, Files.probeContentType(resource.getResource().getFile().toPath()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getOriginalFileName() + "\"")
                    .body(resource.getResource());
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
            if (dto instanceof ITenantAssignableDto ITenantAssignableDto && StringUtils.isEmpty(ITenantAssignableDto.getTenant())) {
                ITenantAssignableDto.setTenant(requestContext.getSenderTenant());
            }
            dto = this.beforeCreate(dto);
            return ResponseFactory.responseCreated(mapper().entityToDto(
                    this.afterCreate(crudService().createWithImage(mapper().dtoToEntity(dto), file))));
        } catch (Throwable e) {
            log.error("<Error>: create with image : {} ", e);
            return getBackExceptionResponse(e);
        }
    }


    @Override
    public ResponseEntity<F> updateWithImage(RequestContextDto requestContext,
                                             I id,
                                             MultipartFile file,
                                             F dto) {
        log.info("Update with image request received");
        try {
            dto = this.beforeUpdate(dto);
            return ResponseFactory.responseOk(mapper().entityToDto(
                    this.afterUpdate(crudService().updateWithImage(mapper().dtoToEntity(dto), file))));
        } catch (Throwable e) {
            log.error("<Error>: update wth image : {} ", e);
            return getBackExceptionResponse(e);
        }
    }

    /**
     * Before create fulld.
     *
     * @param object the object
     * @return the fulld
     * @throws Exception the exception
     */
    public F beforeCreate(F object) throws Exception {
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
    public F beforeUpdate(F object) throws Exception {
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
