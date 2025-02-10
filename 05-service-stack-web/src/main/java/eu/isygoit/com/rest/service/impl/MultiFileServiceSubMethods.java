package eu.isygoit.com.rest.service.impl;

import eu.isygoit.annotation.DmsLinkFileService;
import eu.isygoit.app.ApplicationContextService;
import eu.isygoit.com.rest.api.ILinkedFileApi;
import eu.isygoit.exception.LinkedFileServiceNotDefinedException;
import eu.isygoit.model.ICodifiable;
import eu.isygoit.model.IIdEntity;
import eu.isygoit.model.ILinkedFile;
import eu.isygoit.model.IMultiFileEntity;
import eu.isygoit.repository.JpaPagingAndSortingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContextException;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;
import java.util.Optional;

/**
 * The type Multi file service sub methods.
 *
 * @param <I> the type parameter
 * @param <T> the type parameter
 * @param <L> the type parameter
 * @param <R> the type parameter
 */
@Slf4j
public abstract class MultiFileServiceSubMethods<I, T extends IMultiFileEntity & IIdEntity, L extends ILinkedFile & ICodifiable & IIdEntity, R extends JpaPagingAndSortingRepository>
        extends CodifiableService<I, T, R> {

    private ILinkedFileApi linkedFileApi;

    protected abstract ApplicationContextService getApplicationContextServiceInstance();

    // Returns an Optional containing the ApplicationContextService, if present
    protected Optional<ApplicationContextService> getApplicationContextService() {
        return Optional.ofNullable(getApplicationContextServiceInstance());
    }

    private ILinkedFileApi linkedFileService() throws LinkedFileServiceNotDefinedException {
        if (Objects.isNull(this.linkedFileApi)) {
            var applicationContextService = getApplicationContextService()
                    .orElseThrow(() -> new ApplicationContextException("ApplicationContextService not found"));
            
            var annotation = this.getClass().getAnnotation(DmsLinkFileService.class);
            
            if (Objects.nonNull(annotation)) {
                this.linkedFileApi = applicationContextService.getBean(annotation.value());
                if (Objects.isNull(this.linkedFileApi)) {
                    log.error("<Error>: bean {} not found", annotation.value().getSimpleName());
                    throw new LinkedFileServiceNotDefinedException("Bean not found " + annotation.value().getSimpleName());
                }
            } else {
                log.error("<Error>: Linked file service not defined for {}, local storage will be used!", this.getClass().getSimpleName());
            }
        }
        return this.linkedFileApi;
    }

    private <R> R executeFileOperation(FileOperation<R> operation) {
        try {
            return operation.execute();
        } catch (Exception e) {
            log.error("Remote service operation failed: ", e);
            return null;
        }
    }

    /**
     * Sub upload file l.
     *
     * @param file   the file
     * @param entity the entity
     * @return the l
     */
    final L subUploadFile(MultipartFile file, L entity) {
        return executeFileOperation(() -> {
            var linkedFileService = this.linkedFileService();

            if (Objects.nonNull(linkedFileService)) {
                FileServiceDmsStaticMethods.upload(file, entity, linkedFileService)
                        .ifPresent(linkedFileResponseDto -> entity.setCode(linkedFileResponseDto.getCode()));
            } else {
                entity.setCode(FileServiceLocalStaticMethods.upload(file, entity));
            }
            return entity;
        });
    }

    /**
     * Sub download file resource.
     *
     * @param entity  the entity
     * @param version the version
     * @return the resource
     */
    final Resource subDownloadFile(L entity, Long version) {
        return executeFileOperation(() -> {
            var linkedFileService = this.linkedFileService();

            if (Objects.nonNull(linkedFileService)) {
                return FileServiceDmsStaticMethods.download(entity, version, linkedFileService);
            } else {
                return FileServiceLocalStaticMethods.download(entity, version);
            }
        });
    }

    /**
     * Sub delete file boolean.
     *
     * @param entity the entity
     * @return the boolean
     */
    final boolean subDeleteFile(L entity) {
        return executeFileOperation(() -> {
            repository().delete(entity);

            var linkedFileService = this.linkedFileService();
            if (Objects.nonNull(linkedFileService)) {
                return FileServiceDmsStaticMethods.delete(entity, linkedFileService);
            } else {
                return FileServiceLocalStaticMethods.delete(entity);
            }
        });
    }

    /**
     * The interface File operation.
     *
     * @param <R> the type parameter
     */
    @FunctionalInterface
    interface FileOperation<R> {
        /**
         * Execute r.
         *
         * @return the r
         * @throws Exception the exception
         */
        R execute() throws Exception;
    }
}