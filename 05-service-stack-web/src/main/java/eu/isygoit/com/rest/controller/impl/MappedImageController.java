package eu.isygoit.com.rest.controller.impl;

import eu.isygoit.com.rest.api.IUploadImageApi;
import eu.isygoit.com.rest.controller.ResponseFactory;
import eu.isygoit.com.rest.controller.constants.CtrlConstants;
import eu.isygoit.com.rest.service.ICrudServiceMethod;
import eu.isygoit.com.rest.service.IImageServiceMethods;
import eu.isygoit.dto.IIdentifiableDto;
import eu.isygoit.dto.IImageUploadDto;
import eu.isygoit.dto.common.RequestContextDto;
import eu.isygoit.model.IIdEntity;
import eu.isygoit.model.IImageEntity;
import eu.isygoit.model.ISAASEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;


/**
 * The type Mapped image controller.
 *
 * @param <I>     the type parameter
 * @param <T>     the type parameter
 * @param <MIND>  the type parameter
 * @param <FULLD> the type parameter
 * @param <S>     the type parameter
 */
@Slf4j
public abstract class MappedImageController<I, T extends IIdEntity & IImageEntity,
        MIND extends IIdentifiableDto & IImageUploadDto,
        FULLD extends MIND,
        S extends IImageServiceMethods<I, T> & ICrudServiceMethod<I, T>>
        extends CrudControllerUtils<T, MIND, FULLD, S>
        implements IUploadImageApi<I, FULLD> {

    @Override
    public ResponseEntity<FULLD> uploadImage(RequestContextDto requestContext,
                                             I id,
                                             MultipartFile file) {
        log.info("Upload image request received");
        try {
            return ResponseFactory.ResponseOk(mapper().entityToDto(crudService().uploadImage(requestContext.getSenderDomain(), id, file)));
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
            Resource imageResource = crudService().downloadImage(id);
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
    public ResponseEntity<FULLD> createWithImage(RequestContextDto requestContext,
                                                 MultipartFile file,
                                                 FULLD dto) {
        log.info("Create with image request received");
        try {
            if (ISAASEntity.class.isAssignableFrom(dto.getClass()) && StringUtils.isEmpty(((ISAASEntity) dto).getDomain())) {
                ((ISAASEntity) dto).setDomain(requestContext.getSenderDomain());
            }
            dto = this.beforeCreate(dto);
            return ResponseFactory.ResponseOk(mapper().entityToDto(
                    this.afterCreate(crudService().createWithImage(requestContext.getSenderDomain(), mapper().dtoToEntity(dto), file))));
        } catch (Throwable e) {
            log.error("<Error>: create with image : {} ", e);
            return getBackExceptionResponse(e);
        }
    }


    @Override
    public ResponseEntity<FULLD> updateWithImage(RequestContextDto requestContext,
                                                 MultipartFile file,
                                                 FULLD dto) {
        log.info("Update with image request received");
        try {
            dto = this.beforeUpdate(dto);
            return ResponseFactory.ResponseOk(mapper().entityToDto(
                    this.afterUpdate(crudService().updateWithImage(requestContext.getSenderDomain(), mapper().dtoToEntity(dto), file))));
        } catch (Throwable e) {
            log.error("<Error>: update wth image : {} ", e);
            return getBackExceptionResponse(e);
        }
    }

    /**
     * Before create full d.
     *
     * @param object the object
     * @return the full d
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
     * Before update full d.
     *
     * @param object the object
     * @return the full d
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
