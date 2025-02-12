package eu.isygoit.com.rest.service.impl;

import eu.isygoit.annotation.DmsLinkFileService;
import eu.isygoit.app.ApplicationContextService;
import eu.isygoit.com.rest.api.ILinkedFileApi;
import eu.isygoit.exception.LinkedFileServiceNotDefinedException;
import eu.isygoit.model.IAssignableCode;
import eu.isygoit.model.IFileEntity;
import eu.isygoit.model.IIdEntity;
import eu.isygoit.repository.JpaPagingAndSortingRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

/**
 * The type File service sub methods.
 *
 * @param <I> the type parameter
 * @param <E> the type parameter
 * @param <R> the type parameter
 */
@Slf4j
public abstract class FileServiceSubMethods<I extends Serializable, E extends IFileEntity & IIdEntity & IAssignableCode, R extends JpaPagingAndSortingRepository>
        extends AssignableCodeService<I, E, R> {

    @Autowired
    private ApplicationContextService applicationContextService;

    private ILinkedFileApi linkedFileApi;

    private ILinkedFileApi linkedFileService() throws LinkedFileServiceNotDefinedException {
        if (this.linkedFileApi == null) {
            DmsLinkFileService annotation = this.getClass().getAnnotation(DmsLinkFileService.class);
            if (annotation != null) {
                this.linkedFileApi = applicationContextService.getBean(annotation.value());
                if (this.linkedFileApi == null) {
                    log.error("<Error>: bean {} not found", annotation.value().getSimpleName());
                    throw new LinkedFileServiceNotDefinedException("Bean not found " + annotation.value().getSimpleName() + " not found");
                }
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
    final String subUploadFile(MultipartFile file, E entity) {
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
    final Resource subDownloadFile(E entity, Long version) {
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
