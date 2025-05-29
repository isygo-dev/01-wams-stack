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
 * Provides shared sub-methods for file service operations, including DMS and local upload/download logic.
 *
 * @param <I> ID type
 * @param <T> File entity type
 * @param <R> Repository type
 */
@Slf4j
public abstract class FileServiceSubMethods<I extends Serializable,
        T extends IFileEntity & IIdAssignable<I> & ICodeAssignable,
        R extends JpaPagingAndSortingRepository<T, I>>
        extends CodeAssignableService<I, T, R> {

    @Autowired
    private ApplicationContextService applicationContextService;

    private ILinkedFileApi linkedFileApi;

    /**
     * Functional interface to encapsulate DMS operations that may throw exceptions.
     */
    @FunctionalInterface
    interface FileOperation<R> {
        R execute(ILinkedFileApi linkedFileApi) throws Exception;
    }

    /**
     * Get the DMS file service bean from annotation, or return null to fallback to local.
     */
    private ILinkedFileApi linkedFileService() throws LinkedFileServiceNotDefinedException {
        if (linkedFileApi == null) {
            DmsLinkFileService annotation = this.getClass().getAnnotation(DmsLinkFileService.class);
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

    /**
     * Generic wrapper that tries the DMS operation and falls back to local in case of errors or null service.
     */
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
     * Uploads a file either to the DMS service if available, or to the local storage.
     */
    final String subUploadFile(MultipartFile file, T entity) {
        return executeWithFallback(
                dms -> FileServiceDmsStaticMethods.upload(file, entity, dms).getCode(),
                () -> FileServiceLocalStaticMethods.upload(file, entity),
                "upload"
        );
    }

    /**
     * Downloads a file from either the DMS service or local storage.
     */
    final Resource subDownloadFile(T entity, Long version) {
        return executeWithFallback(
                dms -> FileServiceDmsStaticMethods.download(entity, version, dms),
                () -> FileServiceLocalStaticMethods.download(entity, version),
                "download"
        );
    }

    /**
     * Fallback functional interface to supply results without arguments.
     */
    @FunctionalInterface
    interface FallbackSupplier<R> {
        R get() throws Exception;
    }
}
