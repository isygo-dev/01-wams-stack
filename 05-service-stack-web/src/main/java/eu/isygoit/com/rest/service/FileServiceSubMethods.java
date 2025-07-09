package eu.isygoit.com.rest.service;

import eu.isygoit.annotation.InjectDmsLinkedFileService;
import eu.isygoit.app.ApplicationContextService;
import eu.isygoit.com.rest.api.ILinkedFileApi;
import eu.isygoit.exception.LinkedFileServiceNotDefinedException;
import eu.isygoit.model.ICodeAssignable;
import eu.isygoit.model.IFileEntity;
import eu.isygoit.model.IIdAssignable;
import eu.isygoit.repository.JpaPagingAndSortingCodeAssingnableRepository;
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
public abstract class FileServiceSubMethods<I extends Serializable,
        T extends IFileEntity & IIdAssignable<I> & ICodeAssignable,
        R extends JpaPagingAndSortingCodeAssingnableRepository<T, I>>
        extends CodeAssignableService<I, T, R> {

    @Autowired
    private ApplicationContextService applicationContextService;

    private ILinkedFileApi linkedFileApi;

    /**
     * Sub upload file string.
     *
     * @param file   the file
     * @param entity the entity
     * @return the string
     */
    final String subUploadFile(MultipartFile file, T entity) {
        return executeWithFallback(
                dms -> FileServiceDmsStaticMethods.upload(file, entity, dms).getCode(),
                () -> FileServiceLocalStaticMethods.upload(file, entity),
                "upload"
        );
    }

    private ILinkedFileApi linkedFileService() throws LinkedFileServiceNotDefinedException {
        if (linkedFileApi == null) {
            InjectDmsLinkedFileService annotation = this.getClass().getAnnotation(InjectDmsLinkedFileService.class);
            if (annotation != null) {
                linkedFileApi = applicationContextService.getBean(annotation.value())
                        .orElseThrow(() -> {
                            String error = "Bean not found: " + annotation.value().getSimpleName();
                            log.error(error);
                            return new LinkedFileServiceNotDefinedException(error);
                        });
            } else {
                log.warn("No @DmsLinkFileService defined for {}. Falling back to local storage.",
                        this.getClass().getSimpleName());
            }
        }
        return linkedFileApi;
    }

    private <R> R executeWithFallback(FileOperation<R> dmsOperation, FallbackSupplier<R> fallback, String operationType) {
        try {
            ILinkedFileApi linkedService = linkedFileService();
            if (linkedService != null) {
                return dmsOperation.execute(linkedService);
            } else {
                return fallback.get();
            }
        } catch (Exception e) {
            log.error("File {} failed: {}", operationType, e.getMessage(), e);
            try {
                return fallback.get();
            } catch (Exception fallbackException) {
                log.error("Fallback {} also failed: {}", operationType, fallbackException.getMessage(), fallbackException);
                return null;
            }
        }
    }

    /**
     * Sub download file resource.
     *
     * @param entity  the entity
     * @param version the version
     * @return the resource
     */
    final Resource subDownloadFile(T entity, Long version) {
        return executeWithFallback(
                dms -> FileServiceDmsStaticMethods.download(entity, version, dms),
                () -> FileServiceLocalStaticMethods.download(entity, version),
                "download"
        );
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
         * @param linkedFileApi the linked file api
         * @return the r
         * @throws Exception the exception
         */
        R execute(ILinkedFileApi linkedFileApi) throws Exception;
    }

    /**
     * The interface Fallback supplier.
     *
     * @param <R> the type parameter
     */
    @FunctionalInterface
    interface FallbackSupplier<R> {
        /**
         * Get r.
         *
         * @return the r
         * @throws Exception the exception
         */
        R get() throws Exception;
    }
}
