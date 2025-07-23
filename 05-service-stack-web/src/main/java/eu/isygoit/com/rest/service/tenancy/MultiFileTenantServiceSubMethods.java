package eu.isygoit.com.rest.service.tenancy;

import eu.isygoit.annotation.InjectDmsLinkedFileService;
import eu.isygoit.annotation.InjectLinkedFileRepository;
import eu.isygoit.app.ApplicationContextService;
import eu.isygoit.com.rest.api.ILinkedFileApi;
import eu.isygoit.com.rest.service.FileServiceDmsStaticMethods;
import eu.isygoit.com.rest.service.FileServiceLocalStaticMethods;
import eu.isygoit.dto.common.ResourceDto;
import eu.isygoit.exception.JpaRepositoryNotDefinedException;
import eu.isygoit.exception.LinkedFileServiceNotDefinedException;
import eu.isygoit.model.*;
import eu.isygoit.repository.JpaPagingAndSortingRepository;
import eu.isygoit.repository.tenancy.JpaPagingAndSortingTenantAndCodeAssignableRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

/**
 * Abstract service class providing methods for handling multiple file operations with tenancy support.
 * This class supports file upload, download, and deletion operations for entities implementing
 * {@link IMultiFileEntity}, {@link IIdAssignable}, {@link ICodeAssignable}, and {@link ITenantAssignable}.
 *
 * @param <I>  the type of the identifier, extending {@link Serializable}
 * @param <T>  the entity type, extending {@link IMultiFileEntity}, {@link IIdAssignable}, {@link ICodeAssignable}, and {@link ITenantAssignable}
 * @param <L>  the linked file type, extending {@link ILinkedFile}, {@link ICodeAssignable}, and {@link IIdAssignable}
 * @param <R>  the repository type for the entity, extending {@link JpaPagingAndSortingTenantAndCodeAssignableRepository}
 * @param <RL> the repository type for linked files, extending {@link JpaPagingAndSortingRepository}
 */
@Slf4j
public abstract class MultiFileTenantServiceSubMethods<
        I extends Serializable,
        T extends IMultiFileEntity<L> & IIdAssignable<I> & ICodeAssignable & ITenantAssignable,
        L extends ILinkedFile & ICodeAssignable & IIdAssignable<I>,
        R extends JpaPagingAndSortingTenantAndCodeAssignableRepository<T, I>,
        RL extends JpaPagingAndSortingRepository<L, I>
        > extends CodeAssignableTenantService<I, T, R> {

    @Autowired
    private ApplicationContextService applicationContextService;

    private volatile RL linkFileRepository;
    private volatile ILinkedFileApi linkedFileApi;

    /**
     * Retrieves the linked file repository, initializing it if not already set.
     * Uses double-checked locking for thread-safe initialization and performance.
     *
     * @return the linked file repository
     * @throws JpaRepositoryNotDefinedException if the repository cannot be found or initialized
     */
    protected final RL getLinkFileRepository() {
        if (linkFileRepository == null) {
            synchronized (this) {
                if (linkFileRepository == null) {
                    var annotation = this.getClass().getAnnotation(InjectLinkedFileRepository.class);
                    if (annotation == null) {
                        log.error("Link file repository annotation missing for class: {}", this.getClass().getSimpleName());
                        throw new JpaRepositoryNotDefinedException("Link file repository annotation not defined");
                    }
                    linkFileRepository = applicationContextService.getBean(annotation.value())
                            .map(bean -> (RL) bean)
                            .orElseThrow(() -> {
                                log.error("Link file repository bean not found: {}", annotation.value().getSimpleName());
                                return new JpaRepositoryNotDefinedException("Link file repository: " + annotation.value().getSimpleName());
                            });
                    log.debug("Initialized link file repository: {}", annotation.value().getSimpleName());
                }
            }
        }
        return linkFileRepository;
    }

    /**
     * Retrieves the linked file API service, initializing it if not already set.
     * Uses double-checked locking for thread-safe initialization and performance.
     *
     * @return the linked file API service, or null if local storage is used
     * @throws LinkedFileServiceNotDefinedException if the service cannot be initialized
     */
    private ILinkedFileApi getLinkedFileApi() {
        if (linkedFileApi == null) {
            synchronized (this) {
                if (linkedFileApi == null) {
                    var annotation = this.getClass().getAnnotation(InjectDmsLinkedFileService.class);
                    if (annotation == null) {
                        log.warn("Linked file API annotation missing for class: {}. Using local storage.", this.getClass().getSimpleName());
                        return null;
                    }
                    linkedFileApi = applicationContextService.getBean((Class<ILinkedFileApi>) annotation.value())
                            .orElseThrow(() -> {
                                log.error("Linked file API bean not found: {}", annotation.value().getSimpleName());
                                return new LinkedFileServiceNotDefinedException("Linked file API: " + annotation.value().getSimpleName());
                            });
                    log.debug("Initialized linked file API: {}", annotation.value().getSimpleName());
                }
            }
        }
        return linkedFileApi;
    }

    /**
     * Uploads a file for the given entity, using either a remote service or local storage.
     *
     * @param file   the multipart file to upload
     * @param entity the linked file entity to associate with the uploaded file
     * @return the updated entity with the file name set
     * @throws IllegalArgumentException if the file or entity is null
     */
    protected final L subUploadFile(MultipartFile file, L entity) {
        if (file == null || entity == null) {
            log.error("Invalid input: file or entity is null");
            throw new IllegalArgumentException("File and entity must not be null");
        }
        log.debug("Uploading file for entity with code: {}", entity.getCode());
        return executeSafely(() -> {
            var service = getLinkedFileApi();
            String fileName = service != null
                    ? FileServiceDmsStaticMethods.upload(file, entity, service).getCode()
                    : FileServiceLocalStaticMethods.upload(file, entity);
            entity.setFileName(fileName);
            log.info("File uploaded successfully for entity: {}, fileName: {}", entity.getCode(), fileName);
            return entity;
        }, entity, "upload file");
    }

    /**
     * Downloads a file for the given entity and version.
     *
     * @param entity  the linked file entity
     * @param version the version of the file to download
     * @return the resource DTO containing the file data
     * @throws IllegalArgumentException if the entity is null
     */
    protected final ResourceDto subDownloadFile(L entity, Long version) {
        if (entity == null) {
            log.error("Invalid input: entity is null");
            throw new IllegalArgumentException("Entity must not be null");
        }
        log.debug("Downloading file for entity: {}, version: {}", entity.getCode(), version);
        return executeSafely(() -> {
            var service = getLinkedFileApi();
            ResourceDto resource = service != null
                    ? FileServiceDmsStaticMethods.download(entity, version, service)
                    : FileServiceLocalStaticMethods.download(entity, version);
            log.info("File downloaded successfully for entity: {}, version: {}", entity.getCode(), version);
            return resource;
        }, null, "download file");
    }

    /**
     * Deletes a file associated with the given entity.
     *
     * @param entity the linked file entity
     * @return true if the file was deleted successfully, false otherwise
     * @throws IllegalArgumentException if the entity is null
     */
    protected final boolean subDeleteFile(L entity) {
        if (entity == null) {
            log.error("Invalid input: entity is null");
            throw new IllegalArgumentException("Entity must not be null");
        }
        log.debug("Deleting file for entity: {}", entity.getCode());
        return executeSafely(() -> {
            getLinkFileRepository().delete(entity);
            var service = getLinkedFileApi();
            boolean deleted = service != null
                    ? FileServiceDmsStaticMethods.delete(entity, service)
                    : FileServiceLocalStaticMethods.delete(entity);
            log.info("File deleted successfully for entity: {}", entity.getCode());
            return deleted;
        }, false, "delete file");
    }

    /**
     * Executes a supplier operation safely, handling exceptions and logging errors.
     *
     * @param operation     the operation to execute
     * @param defaultValue  the default value to return if the operation fails
     * @param operationName the name of the operation for logging
     * @param <T>           the type of the result
     * @return the result of the operation or the default value if an exception occurs
     */
    private <T> T executeSafely(CheckedSupplier<T> operation, T defaultValue, String operationName) {
        try {
            return operation.get();
        } catch (Exception e) {
            log.error("Failed to {}: {}", operationName, e.getMessage(), e);
            return defaultValue;
        }
    }

    /**
     * Functional interface for operations that may throw exceptions.
     *
     * @param <T> the type of the result
     */
    @FunctionalInterface
    private interface CheckedSupplier<T> {
        T get() throws Exception;
    }
}