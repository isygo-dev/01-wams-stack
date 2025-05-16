package eu.isygoit.com.rest.service;

import eu.isygoit.annotation.DmsLinkFileService;
import eu.isygoit.app.ApplicationContextService;
import eu.isygoit.com.rest.api.ILinkedFileApi;
import eu.isygoit.exception.LinkedFileServiceNotDefinedException;
import eu.isygoit.model.ICodeAssignable;
import eu.isygoit.model.IFileEntity;
import eu.isygoit.model.IIdAssignable;
import eu.isygoit.repository.JpaPagingAndSortingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

/**
 * The type File service sub methods.
 *
 * @param <I> the type parameter
 * @param <T> the type parameter
 * @param <R> the type parameter
 */
@Slf4j
public abstract class FileServiceSubMethods<I extends Serializable, T extends IFileEntity & IIdAssignable & ICodeAssignable, R extends JpaPagingAndSortingRepository>
        extends CodeAssignableService<I, T, R> {

    @Autowired
    private ApplicationContextService applicationContextService;

    private ILinkedFileApi linkedFileApi;

    private ILinkedFileApi linkedFileService() throws LinkedFileServiceNotDefinedException {
        if (this.linkedFileApi == null) {
            DmsLinkFileService annotation = this.getClass().getAnnotation(DmsLinkFileService.class);
            if (annotation != null) {
                this.linkedFileApi = applicationContextService.getBean(annotation.value())
                        .orElseThrow(() -> new LinkedFileServiceNotDefinedException("Bean not found " + annotation.value().getSimpleName() + " not found"));
            } else {
                log.error("<Error>: Linked file service not defined for {}, local storage will be used!", this.getClass().getSimpleName());
            }
        }

        return this.linkedFileApi;
    }

    /**
     * Sub upload file string.
     *
     * @param file   the file
     * @param entity the entity
     * @return the string
     */
    final String subUploadFile(MultipartFile file, T entity) {
        try {
            ILinkedFileApi linkedFileService = this.linkedFileService();
            if (linkedFileService != null) {
                return FileServiceDmsStaticMethods.upload(file, entity, linkedFileService).getCode();
            } else {
                return FileServiceLocalStaticMethods.upload(file, entity);
            }
        } catch (Exception e) {
            log.error("Remote feign call failed : ", e);
        }

        return null;
    }

    /**
     * Sub download file resource.
     *
     * @param entity  the entity
     * @param version the version
     * @return the resource
     */
    final Resource subDownloadFile(T entity, Long version) {
        try {
            ILinkedFileApi linkedFileService = this.linkedFileService();
            if (linkedFileService != null) {
                return FileServiceDmsStaticMethods.download(entity, version, linkedFileService);
            } else {
                return FileServiceLocalStaticMethods.download(entity, version);
            }
        } catch (Exception e) {
            log.error("Remote feign call failed : ", e);
        }
        return null;
    }
}
