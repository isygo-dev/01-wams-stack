package eu.isygoit.com.rest.controller.impl.tenancy;

import eu.isygoit.com.rest.api.IMappedMultiFileApi;
import eu.isygoit.com.rest.controller.ResponseFactory;
import eu.isygoit.com.rest.controller.constants.CtrlConstants;
import eu.isygoit.com.rest.controller.impl.CrudControllerUtils;
import eu.isygoit.com.rest.service.ICrudServiceUtils;
import eu.isygoit.com.rest.service.tenancy.ICrudTenantServiceOperations;
import eu.isygoit.com.rest.service.tenancy.IMultiFileTenantServiceOperations;
import eu.isygoit.dto.IDto;
import eu.isygoit.dto.IIdAssignableDto;
import eu.isygoit.dto.common.ContextRequestDto;
import eu.isygoit.dto.common.LinkedFileMinDto;
import eu.isygoit.dto.common.ResourceDto;
import eu.isygoit.mapper.EntityMapper;
import eu.isygoit.model.IIdAssignable;
import eu.isygoit.model.IMultiFileEntity;
import eu.isygoit.model.ITenantAssignable;
import eu.isygoit.service.RequestContextService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.nio.file.Files;
import java.util.List;


/**
 * The type Mapped multi file controller.
 *
 * @param <I> the type parameter
 * @param <T> the type parameter
 * @param <L> the type parameter
 * @param <M> the type parameter
 * @param <F> the type parameter
 * @param <S> the type parameter
 */
@Slf4j
public abstract class MappedMultiFileTenatController<I extends Serializable,
        T extends IIdAssignable<I> & IMultiFileEntity & ITenantAssignable,
        L extends LinkedFileMinDto,
        M extends IIdAssignableDto<I> & IDto,
        F extends M,
        S extends IMultiFileTenantServiceOperations<I, T> & ICrudTenantServiceOperations<I, T> & ICrudServiceUtils<I, T>>
        extends CrudControllerUtils<I, T, M, F, S>
        implements IMappedMultiFileApi<L, I> {

    @Getter
    @Autowired
    private RequestContextService requestContextService;
    
    /**
     * Linked file mapper entity mapper.
     *
     * @return the entity mapper
     */
    public abstract EntityMapper linkedFileMapper();

    @Override
    public ResponseEntity<List<L>> uploadAdditionalFiles(
                                                         I parentId,
                                                         MultipartFile[] files) {
        log.info("update additionl file");
        try {
            return ResponseFactory.responseOk(linkedFileMapper().listEntityToDto(crudService().uploadAdditionalFiles(requestContextService.getCurrentContext().getSenderTenant(), parentId, files)));
        } catch (Throwable e) {
            log.error(CtrlConstants.ERROR_API_EXCEPTION, e);
            return getBackExceptionResponse(e);
        }
    }

    @Override
    public ResponseEntity<List<L>> uploadAdditionalFile(
                                                        I parentId,
                                                        MultipartFile file) {
        log.info("update additionl file");
        try {
            return ResponseFactory.responseOk(linkedFileMapper().listEntityToDto(crudService().uploadAdditionalFile(requestContextService.getCurrentContext().getSenderTenant(), parentId, file)));
        } catch (Throwable e) {
            log.error(CtrlConstants.ERROR_API_EXCEPTION, e);
            return getBackExceptionResponse(e);
        }
    }

    @Override
    public ResponseEntity<Boolean> deleteAdditionalFile(
                                                        I parentId,
                                                        I fileId) {
        log.info("delete additional file");
        try {
            return ResponseFactory.responseOk(crudService().deleteAdditionalFile(requestContextService.getCurrentContext().getSenderTenant(), parentId, fileId));
        } catch (Throwable e) {
            log.error(CtrlConstants.ERROR_API_EXCEPTION, e);
            return getBackExceptionResponse(e);
        }
    }

    @Override
    public ResponseEntity<Resource> download(
                                             I parentId,
                                             I fileId,
                                             Long version
    ) {
        try {
            log.info("download file ");
            try {
                ResourceDto resource = crudService().downloadFile(requestContextService.getCurrentContext().getSenderTenant(), parentId, fileId, version);
                if (resource != null) {
                    log.info("File downloaded successfully {}", resource.getResource().getFilename());
                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_TYPE, Files.probeContentType(resource.getResource().getFile().toPath()))
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getOriginalFileName() + "\"")
                            .body(resource.getResource());
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
